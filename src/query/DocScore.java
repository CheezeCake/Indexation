package query;

public class DocScore implements Comparable<DocScore>
{
	private int docId;
	private float score;
	
	public DocScore(int docId, float score)
	{
		this.docId = docId;
		this.score = score;
	}

	@Override
	public int compareTo(DocScore o)
	{
		int scoreCmp = (int)Math.signum(score - o.score);
		return (scoreCmp == 0) ? (docId - o.docId) : scoreCmp;
	}

	@Override
	public boolean equals(Object o)
	{
		return (compareTo((DocScore)o) == 0);
	}
	
	@Override
	public String toString()
	{
		return String.format("%d (%.4f)", docId, score);
	}
	
	public int getDocId()
	{
		return docId;
	}
	
	public float getScore()
	{
		return score;
	}
}
