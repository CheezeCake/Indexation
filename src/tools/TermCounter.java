package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import indexation.Index;
import indexation.content.Token;
import indexation.content.processing.Normalizer;
import indexation.content.processing.Tokenizer;

public class TermCounter
{
	private static Map<String,Integer> countTerms(List<Token> tokens)
	{
		Map<String, Integer> counts = new HashMap<>();
		for (Token token : tokens) {
			String type = token.getType();
			Integer count = counts.get(type);
			if (count == null)
				counts.put(type,  1);
			else
				counts.put(type, count + 1);
		}
		
		return counts;
	}
	
	private static void writeCounts(Map<String,Integer> counts, String fileName)
	{
		try {
			File file = new File(fileName);
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			PrintWriter writer = new PrintWriter(osw);
			
			Set<String> countsKeys = counts.keySet();
			for (String key : countsKeys)
				writer.println(key + "\t" + counts.get(key));
			
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void processCorpus(String folder, String outFile)
	{
		Tokenizer tokenizer = Index.getTokenizer();
		Normalizer normalizer = Index.getNormalizer();
		
		List<Token> tokens = null;
		tokenizer.tokenizeCorpus(folder, tokens);
		normalizer.normalizeToken(tokens);
		
		writeCounts(countTerms(tokens), outFile);
	}
	
	public static void main(String[] args)
	{
		processCorpus("../Common/corpus", "data/term-count.txt");
	}
}
