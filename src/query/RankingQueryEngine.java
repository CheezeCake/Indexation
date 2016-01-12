package query;

import indexation.content.IndexEntry;
import indexation.content.Posting;
import indexation.content.processing.Normalizer;
import indexation.content.processing.Tokenizer;
import indexation.Index;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class RankingQueryEngine
{
	private Index index;

	public RankingQueryEngine(Index index)
	{
		this.index = index;
	}
	
	private float processWf(Posting posting)
	{
		return (posting.getFrequency() > 0)
				? (float)(1.0f + Math.log10(posting.getFrequency()))
				: 0.0f;
	}
	
	private float processIdf(IndexEntry entry)
	{
		return (float)Math.log10((float)index.getDocNbr() / entry.getFrequency());
	}
	
	private void sortDocuments(List<IndexEntry> queryEntries, int k, List<DocScore> docScores)
	{
		TreeSet<DocScore> docScoresSet = new TreeSet<>();
		Map<Integer, Float> scores = new HashMap<>();
		Map<Integer, Float> normes = new HashMap<>();
		
		int nbDocsInQueryEntries = 0;
		/*
		for (IndexEntry entry : queryEntries)
			nbDocsInQueryEntries += entry.getFrequency();
			*/

		float normeQ = 0;

		for (IndexEntry entry : queryEntries) {
			List<Posting> postings = entry.getPostings();

			/*
			float stq = entry.getFrequency() / nbDocsInQueryEntries;
			normeQ += stq * stq;
			*/

			float idf = processIdf(entry);
			
			for (Posting posting : postings) {
				float std = processWf(posting) * idf;
				
				int docId = posting.getDocId();
				Float scoreo = scores.get(docId);
				float score = (scoreo == null) ? 0.0f : scoreo;
				//score += (std * stq);
				scores.put(docId, score);
				
				Float normeo = normes.get(docId);
				float norme = (normeo == null) ? 0.0f : normeo;
				norme += (std * std);
				normes.put(docId, norme);
			}
		}
		
		normeQ = (float)Math.sqrt(normeQ);
		
		for (Integer docId : scores.keySet()) {
			float score = (float)(scores.get(docId) / (Math.sqrt(normes.get(docId)) * normeQ));
			DocScore docScore = new DocScore(docId, score);
			
			docScoresSet.add(docScore);
		}
		
		for (int i = 0; i < k; i++) {
			DocScore dc = docScoresSet.last();
			docScores.add(dc);
			docScoresSet.remove(dc);
		}
	}

	public void splitQuery(String query, List<IndexEntry> result)
	{
		Tokenizer tokenizer = Index.getTokenizer();
		Normalizer normalizer = Index.getNormalizer();

		List<String> tokenizedQuery = tokenizer.tokenizeString(query);

		for (String token : tokenizedQuery) {
			String normalizedToken = normalizer.normalizeType(token);
			if (normalizedToken != null) {
				IndexEntry entry = index.getEntry(normalizedToken);
				if (entry != null)
					result.add(entry);
			}
		}
	}

	public void processQuery(String query, int k, List<DocScore> docScores)
	{
		long start, end;
		System.out.println("Processing request \"" + query + "\"");
		start = System.currentTimeMillis();

		List<IndexEntry> indexEntries = new LinkedList<>();
		splitQuery(query, indexEntries);
		
		sortDocuments(indexEntries, k, docScores);

		end = System.currentTimeMillis();
		System.out.println("Query processed, duration=" + (end - start) + " ms");
	}

	//public static List<String> getFileNamesFromPostings(List<Posting> postings)
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
	
	public static List<String> getFileNamesFromDocScores(List<DocScore> docScores)
	{
		List<String> ret = new LinkedList<String>();

		File dir = new File(".." + File.separator + "Common" + File.separator + "corpus");
		String[] files = dir.list();
		Arrays.sort(files);

		for (DocScore ds : docScores)
			ret.add(files[ds.getDocId()]);

		return ret;
	}
}
