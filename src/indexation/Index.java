package indexation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import indexation.content.IndexEntry;
import indexation.content.Token;
import indexation.content.processing.Builder;
import indexation.content.processing.Normalizer;
import indexation.content.processing.Tokenizer;

public class Index implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IndexEntry[] data;
	
	private int docNbr;

	private static Normalizer normalizer = new Normalizer("data" + File.separator + "stop-words.txt");
	private static Tokenizer tokenizer = new Tokenizer();
	
	public Index(int size)
	{
		data = new IndexEntry[size];
	}
	
	public void print()
	{
		for (IndexEntry ie : data)
			System.out.println(ie);
	}
	
	public IndexEntry getEntry(String term)
	{
		int index = Arrays.binarySearch(data, new IndexEntry(term, null, 0));
		return (index < 0) ? null : data[index];
	}
	
	public static Index indexCorpus(String folder)
	{
		long startTotal;
		long start, end;

		System.out.println("Tokenizing corpus...");
		start = startTotal = System.currentTimeMillis();
		List<Token> tokens = new LinkedList<Token>();
		int docNbr = tokenizer.tokenizeCorpus(folder, tokens);
		end = System.currentTimeMillis();
		System.out.println(tokens.size() + " tokens were found, duration=" + (end - start) + " ms\n");

		System.out.println("Normalizing tokens...");
		start = System.currentTimeMillis();
		normalizer.normalizeToken(tokens);
		end = System.currentTimeMillis();
		System.out.println(tokens.size() + " tokens remaining after normalization, duration=" + (end - start) + " ms\n");
		
		Builder builder = new Builder();
		System.out.println("Building index...");
		start = System.currentTimeMillis();
		Index index = builder.buildIndex(tokens);
		index.setDocNbr(docNbr);
		end = System.currentTimeMillis();
		System.out.println("There are " + index.data.length + " entries in the index, duration=" + (end - start) + " ms");
		
		System.out.println("Total duration=" + (end - startTotal) + " ms");
		
		return index;
	}
	
	public void write(String filename) throws IOException
	{
		File file = new File(filename);
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
	}
	
	public static Index read(String filename) throws IOException
	{
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Index index = null;

		try {
			index = (Index)ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		ois.close();
		
		return index;
	}

	public static Tokenizer getTokenizer()
	{
		return tokenizer;
	}

	public static Normalizer getNormalizer()
	{
		return normalizer;
	}
	
	public void setDocNbr(int docNbr)
	{
		this.docNbr = docNbr;
	}
	
	public int getDocNbr()
	{
		return docNbr;
	}
}
