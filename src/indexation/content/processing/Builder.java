package indexation.content.processing;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import indexation.Index;
import indexation.content.IndexEntry;
import indexation.content.Posting;
import indexation.content.Token;

public class Builder
{
	private int countTerms(List<Token> tokens)
	{
		Iterator<Token> iterator = tokens.iterator();
		int size = 0;
		
		Token tmp = null;

		while (iterator.hasNext()) {
			Token token = iterator.next();

			if (tmp == null || !token.getType().equals(tmp.getType()))
				++size;
			
			tmp = token;
		}

		return size;
	}
	
	private int buildPostings(List<Token> tokens, Index index)
	{
		Iterator<Token> iterator = tokens.iterator();
		int size = 0;
		int dataIndex = 0;
		
		Token tmp = null;
		List<Posting> postings = new LinkedList<>();
		Posting posting = null;

		while (iterator.hasNext()) {
			Token token = iterator.next();
			if (tmp != null) {
				if (token.getType().equals(tmp.getType())) {
					if (token.getDocId() == tmp.getDocId()) {
						posting.addPosition(token.getPosition());
					}
					else {
						posting.setFrequency(posting.getPositions().size());
						postings.add(posting);
						posting = new Posting(token.getDocId());
						posting.addPosition(token.getPosition());
						++size;
					}
				}
				else {
					postings.add(posting);
					index.data[dataIndex++] = new IndexEntry(tmp.getType(), postings, postings.size());
					postings = new LinkedList<Posting>();
					posting = new Posting(token.getDocId());
					posting.addPosition(token.getPosition());
					++size;
				}
			}
			else {
				posting = new Posting(token.getDocId());
				posting.addPosition(token.getPosition());
				++size;
			}
			
			tmp = token;
		}
		
		if (posting.getPositions().size() > 0) {
			posting.setFrequency(posting.getPositions().size());
			postings.add(posting);
		}
		
		if (postings.size() > 0)
			index.data[dataIndex] = new IndexEntry(tmp.getType(), postings, postings.size());

		return size;
	}
	
	public Index buildIndex(List<Token> tokens)
	{
		long start, end;

		System.out.println("\tSorting tokens...");
		start = System.currentTimeMillis();
		Collections.sort(tokens);
		end = System.currentTimeMillis();
		System.out.println("\t" + tokens.size() + " tokens sorted, duration=" + (end - start) + " ms\n");

		System.out.println("\tCounting terms...");
		start = System.currentTimeMillis();
		int nbTerms = countTerms(tokens);
		end = System.currentTimeMillis();
		System.out.println("\t" + nbTerms + " terms identified, duration=" + (end - start) + " ms\n");

		Index index = new Index(nbTerms);
		System.out.println("\tBuilding posting list...");
		start = System.currentTimeMillis();
		int nbPostings = buildPostings(tokens, index);
		end = System.currentTimeMillis();
		System.out.println("\t" + nbPostings + " postings listed, duration=" + (end - start) + " ms");

		return index;
	}
}
