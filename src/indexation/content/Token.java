package indexation.content;

public class Token implements Comparable<Token>
{
	private String type;
	private int docId;
	private int position;
	
	public Token(String type, int docId, int position)
	{
		this.type = type;
		this.docId = docId;
		this.position = position;
	}

	@Override
	public int compareTo(Token tok)
	{
		int typeCmp = type.compareTo(tok.type);
		if (typeCmp != 0)
			return typeCmp;

		int docIdCmp = (docId - tok.docId);
		if (docIdCmp != 0)
			return docIdCmp;
		
		return (position - tok.position);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (compareTo((Token)o) == 0);
	}
	
	@Override
	public String toString()
	{
		return "(" + type + ", " + docId + ", " + position + ")";
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public int getDocId()
	{
		return docId;
	}
	
	public void setDocId(int docId)
	{
		this.docId = docId;
	}
	
	public int getPosition()
	{
		return position;
	}
}
