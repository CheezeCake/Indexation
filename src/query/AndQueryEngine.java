package query;

import indexation.content.IndexEntry;
import indexation.content.Posting;
import indexation.content.processing.Normalizer;
import indexation.content.processing.Tokenizer;
import indexation.Index;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class AndQueryEngine
{
	private Index index;

	private static Comparator<List<Posting>> COMPARATOR = new Comparator<List<Posting>>() {
		public int compare(List<Posting> l1, List<Posting> l2)
		{
			return (l1.size() - l2.size());
		}
	};
	
	public AndQueryEngine(Index index)
	{
		this.index = index;
	}

	public void splitQuery(String query, List<List<Posting>> result)
	{
		Tokenizer tokenizer = Index.getTokenizer();
		Normalizer normalizer = Index.getNormalizer();

		List<String> tokenizedQuery = tokenizer.tokenizeString(query);

		for (String token : tokenizedQuery) {
			String normalizedToken = normalizer.normalizeType(token);
			if (normalizedToken != null) {
				IndexEntry entry = index.getEntry(normalizedToken);
				if (entry != null)
					result.add(entry.getPostings());
			}
		}
	}

	private List<Posting> processConjunction(List<Posting> list1,
			List<Posting> list2)
	{
		List<Posting> ret = new LinkedList<>();
		Iterator<Posting> it1 = list1.iterator();
		Iterator<Posting> it2 = list2.iterator();
		
		Posting p1 = (it1.hasNext()) ? it1.next() : null;
		Posting p2 = (it2.hasNext()) ? it2.next() : null;
		
		while (p1 != null && p2 != null) {
			int cmp = p1.compareTo(p2);
			
			if (cmp == 0) {
				ret.add(p1);
				
				p1 = (it1.hasNext()) ? it1.next() : null;
				p2 = (it2.hasNext()) ? it2.next() : null;
			}
			else if (cmp > 0) {
				p2 = (it2.hasNext()) ? it2.next() : null;
			}
			else {
				p1 = (it1.hasNext()) ? it1.next() : null;
			}
		}
		
		return ret;

	}

	private List<Posting> processConjunctions(List<List<Posting>> postings)
	{
		List<Posting> ret;

		if (postings.isEmpty())
			return new LinkedList<Posting>();

		Collections.sort(postings, COMPARATOR);

		Iterator<List<Posting>> it = postings.iterator();
		ret = it.next();
		
		while (it.hasNext())
			ret = processConjunction(ret, it.next());

		return ret;
	}

	public List<Posting> processQuery(String query)
	{
		long start, end;
		System.out.println("Processing request \"" + query + "\"");
		start = System.currentTimeMillis();

		List<List<Posting>> postings = new LinkedList<>();
		splitQuery(query, postings);

		end = System.currentTimeMillis();
		System.out.println("Query processed, duration=" + (end - start) + " ms");

		return processConjunctions(postings);
	}

	public static List<String> getFileNames(List<Posting> postings)
	{
		List<String> ret = new LinkedList<String>();

		File dir = new File(".." + File.separator + "Common" + File.separator + "corpus");
		String[] files = dir.list();
		Arrays.sort(files);

		for (Posting p : postings)
			ret.add(files[p.getDocId()]);

		return ret;
	}
}
