package query;

import indexation.content.IndexEntry;
import indexation.content.Posting;
import indexation.content.processing.Normalizer;
import indexation.content.processing.Tokenizer;
import indexation.Index;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class PositionalQueryEngine
{
	private Index index;

	/*
	private static Comparator<List<Posting>> COMPARATOR = new Comparator<List<Posting>>() {
		public int compare(List<Posting> l1, List<Posting> l2)
		{
			return (l1.size() - l2.size());
		}
	};
	*/

	public PositionalQueryEngine(Index index)
	{
		this.index = index;
	}

	public void normalizeType(String type, List<Posting> result)
	{
		Tokenizer tokenizer = Index.getTokenizer();
		Normalizer normalizer = Index.getNormalizer();

		List<String> tokenizedType = tokenizer.tokenizeString(type);
		if (tokenizedType.size() > 1)
			throw new RuntimeException("type is not a single type");

		String normalizedType =
				normalizer.normalizeType(tokenizedType.get(0));
		if (normalizedType != null) {
			IndexEntry entry = index.getEntry(normalizedType);
			if (entry != null)
				result.addAll(entry.getPostings());
		}
	}
	
	private void splitQuery(String query, List<List<Posting>> result,
			List<Integer> thresholds)
	{
		List<Posting> postings;

		System.out.println(query);
		Pattern pattern = Pattern.compile(" \\/([0-9]+) ");
		Matcher matcher = pattern.matcher(query);
		
		int start = 0;
		
		while (matcher.find()) {
			int threshold = Integer.parseInt(matcher.group(1));
			thresholds.add(threshold);

			String op1 = query.substring(start, matcher.start());

			if (start == 0) {
				postings = new LinkedList<>();
				normalizeType(op1, postings);
				result.add(postings);
			}
			
			start = matcher.end();
			int end = query.indexOf(' ', start);
			if (end == -1)
				end = query.length();

			String op2 = query.substring(matcher.end(), end);
			
			postings = new LinkedList<>();
			normalizeType(op2, postings);
			result.add(postings);
		}
	}
	
	private Posting processPositionalConjunction(Posting posting1,
			Posting posting2, int threshold)
	{
		Posting ret = new Posting(posting1.getDocId());
		Iterator<Integer> it1 = posting1.getPositions().iterator();
		Iterator<Integer> it2 = posting2.getPositions().iterator();

		Integer p1 = (it1.hasNext()) ? it1.next() : null;
		Integer p2 = (it2.hasNext()) ? it2.next() : null;

		while (p1 != null && p2 != null) {
			if (Math.abs(p1 - p2) <= threshold + 1) {
				ret.addPosition(p2);

				p1 = (it1.hasNext()) ? it1.next() : null;
				p2 = (it2.hasNext()) ? it2.next() : null;
			}
			else {
				if (p1 < p2)
					p1 = (it1.hasNext()) ? it1.next() : null;
				else
					p2 = (it2.hasNext()) ? it2.next() : null;
			}
		}

		ret.setFrequency(ret.getPositions().size());

		return ret;
	}

	private List<Posting> processPositionalConjunction(List<Posting> list1,
			List<Posting> list2, int threshold)
	{
		List<Posting> ret = new LinkedList<>();
		Iterator<Posting> it1 = list1.iterator();
		Iterator<Posting> it2 = list2.iterator();

		Posting p1 = (it1.hasNext()) ? it1.next() : null;
		Posting p2 = (it2.hasNext()) ? it2.next() : null;

		while (p1 != null && p2 != null) {
			int cmp = p1.compareTo(p2);

			if (cmp == 0) {
				Posting conjunction = processPositionalConjunction(p1, p2, threshold);
				if (conjunction.getFrequency() > 0)
					ret.add(conjunction);

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

	private List<Posting> processPositionalConjunctions(List<List<Posting>> postings,
			List<Integer> thresholds)
	{
		List<Posting> ret;

		if (postings.isEmpty())
			return new LinkedList<Posting>();

		//Collections.sort(postings, COMPARATOR);

		Iterator<List<Posting>> it = postings.iterator();
		ret = it.next();
		Iterator<Integer> thresholdIt = thresholds.iterator();

		while (it.hasNext())
			ret = processPositionalConjunction(ret, it.next(), thresholdIt.next());

		return ret;
	}

	public List<Posting> processQuery(String query)
	{
		long start, end;
		System.out.println("Processing request \"" + query + "\"");
		start = System.currentTimeMillis();

		List<List<Posting>> postings = new LinkedList<>();
		List<Integer> thresholds = new LinkedList<Integer>();
		splitQuery(query, postings, thresholds);

		end = System.currentTimeMillis();
		System.out.println("Query processed, duration=" + (end - start) + " ms");

		return processPositionalConjunctions(postings, thresholds);
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
