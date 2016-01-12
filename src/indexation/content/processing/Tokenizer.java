package indexation.content.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import indexation.content.Token;

public class Tokenizer implements Serializable
{
	public List<String> tokenizeString(String string)
	{
		String[] split = string.split("[^\\pL\\pN]");
		List<String> tokens = new LinkedList<String>();
		
		for (String token : split) {
			if (!token.isEmpty())
				tokens.add(token);
		}

		return tokens;
	}

	/*private*/ public void tokenizeDocument(File document, int docId, List<Token> tokens)
	{
		FileInputStream fis;

		try {
			fis = new FileInputStream(document);
			InputStreamReader isr = new InputStreamReader(fis);
			Scanner scanner = new Scanner(isr);
		
			int position = 0;
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				List<String> tokenizedString = tokenizeString(line);
				Iterator<String> it = tokenizedString.iterator();

				while (it.hasNext())
					tokens.add(new Token(it.next(), docId, position++));
			}
		
			scanner.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public int tokenizeCorpus(String folder, List<Token> tokens)
	{
		File dir = new File(folder);

		String[] files = dir.list();
		Arrays.sort(files);
		
		for (int docId = 0; docId < files.length; docId++)
			tokenizeDocument(new File(folder + File.separator + files[docId]), docId, tokens);
		
		return files.length;
	}
}
