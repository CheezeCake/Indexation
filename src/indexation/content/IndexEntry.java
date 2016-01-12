package indexation.content;

import java.io.Serializable;
import java.util.List;

public class IndexEntry implements Comparable<IndexEntry>, Serializable
{
	private String term;
	private List<Posting> postings;
	private int frequency;

	public IndexEntry(String term, List<Posting> postings, int frequency)
	{
		this.term = term;
		this.postings = postings;
		this.frequency = frequency;
	}

	@Override
	public int compareTo(IndexEntry ie)
	{
		return term.compareTo(ie.term);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (compareTo((IndexEntry)o) == 0);
	}
	
	@Override
	public String toString()
	{
		String ret =  "<" + term + " ["+ frequency + "] (";

		for (Posting posting : postings)
			ret += " " + posting.toString();
		
		return ret + " )>";
	}
	
	public String getTerm()
	{
		return term;
	}

	public List<Posting> getPostings()
	{
		return postings;
	}
	
	public int getFrequency()
	{
		return frequency;
	}
}
