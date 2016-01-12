package indexation.content;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Posting implements Comparable<Posting>, Serializable
{
	private int docId;
	private List<Integer> positions;
	private int frequency;
	
	public Posting(int docId)
	{
		this.docId = docId;
		positions = new LinkedList<Integer>();
		frequency = 0;
	}
	
	public void addPosition(int position)
	{
		positions.add(position);
	}

	@Override
	public int compareTo(Posting p)
	{
		return (docId - p.docId);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (compareTo((Posting)o) == 0);
	}
	
	@Override
	public String toString()
	{
		String ret = "<" + docId + " [" + frequency + "] (";
		
		for (Integer position : positions)
			ret += " " + position;
		
		ret += " )>";
		
		return ret;
	}

	public int getDocId()
	{
		return docId;
	}
	
	public List<Integer> getPositions()
	{
		return positions;
	}
	
	public int getFrequency()
	{
		return frequency;
	}
	
	public void setFrequency(int frequency)
	{
		this.frequency = frequency;
	}
}
