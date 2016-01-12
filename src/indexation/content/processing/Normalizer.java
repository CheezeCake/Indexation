package indexation.content.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.Normalizer.Form;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import indexation.content.Token;

public class Normalizer implements Serializable
{
	private TreeSet<String> stopWords;
	
	public Normalizer(String fileName)
	{
		stopWords = loadStopWords(fileName);
	}
	
	public String normalizeType(String type)
	{
		String normalizedType = java.text.Normalizer.normalize(type.toLowerCase(), Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		return (normalizedType.isEmpty() || stopWords.contains(normalizedType)) ? null : normalizedType;
	}
	
	public void normalizeToken(List<Token> tokens)
	{
		Iterator<Token> iterator = tokens.iterator();

		while(iterator.hasNext()) {
			Token token = iterator.next();
			String normalizedType = normalizeType(token.getType());

			if (normalizedType == null)
				iterator.remove();
			else
				token.setType(normalizedType);
		}
	}
	
	private TreeSet<String> loadStopWords(String fileName)
	{
		try {
			TreeSet<String> stopWords = new TreeSet<>();

			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			Scanner scanner = new Scanner(isr);
			
			while (scanner.hasNext())
				stopWords.add(scanner.nextLine());
			
			scanner.close();
			
			return stopWords;
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}