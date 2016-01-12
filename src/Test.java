import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import indexation.Index;
import indexation.content.Posting;
import indexation.content.Token;
import indexation.content.processing.Tokenizer;
import query.AndOrQueryEngine;
import query.AndQueryEngine;
import query.DocScore;
import query.PositionalQueryEngine;
import query.RankingQueryEngine;

public class Test
{
	public static final String CORPUS_FOLDER = ".." + File.separator + "Common" + File.separator + "corpus";
	
	public static void testIndexation()
	{
		Index index = Index.indexCorpus(CORPUS_FOLDER);
		
		/*
		try {
			index.write("data" + File.separator + "index");
			index = Index.read("data" + File.separator + "index");
		
			//if (index != null)
				//index.print();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	public static void testQuery()
	{
		final String[] queries = new String[] {
				//"project",
				//"project SOFTWARE",
				//"project SOFTWARE web",
				//"project,SOFTWARE,Web,pattern,computer",
				//"project SOFTWARE Web pattern computer",
				//"project SOFTWARE Web,pattern computer",
				//"Project which was created for the web"
				"Windows /0 Phone",
				"Digital /3 Education",
				" Social /1 Media /4 Colleges "
				//"bible",
				//"solar panel electricity"
		};
		
		try {
			System.out.println("Loading the index");
			long start = System.currentTimeMillis();
			Index index = Index.read("data" + File.separator + "index");
			long end = System.currentTimeMillis();
			System.out.println("Index loaded, duration=" + (end - start) + " ms");
			
			//AndQueryEngine engine = new AndQueryEngine(index);
			//AndOrQueryEngine engine = new AndOrQueryEngine(index);
			PositionalQueryEngine engine = new PositionalQueryEngine(index);
			//RankingQueryEngine engine = new RankingQueryEngine(index);

			for (String query : queries) {
				List<Posting> postings = engine.processQuery(query);
				System.out.println("Result: " + postings.size() + " document (s)");
				System.out.println(postings);
				System.out.println("Files:\n" + AndQueryEngine.getFileNames(postings) + "\n");
				printPostings(postings, CORPUS_FOLDER, index);
				/*
				List<DocScore> docScores = new LinkedList<>();
				engine.processQuery(query, 5, docScores);
				System.out.println("Result: " + docScores.size() + " document (s)");
				System.out.println(docScores);
				System.out.println("Files:\n" + RankingQueryEngine.getFileNamesFromDocScores(docScores) + "\n");
				*/
			}

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void printPosting(Posting posting, File document, Tokenizer tokenizer)
	{
		System.out.println(posting.getDocId() + ". " + document.getName());
		
		List<Token> tokens = new LinkedList<>();
		tokenizer.tokenizeDocument(document, posting.getDocId(), tokens);
		
		Iterator<Token> it = tokens.iterator();
		Token token = null;
		
		List<Integer> positions = posting.getPositions();

		if (!positions.isEmpty())
			System.out.print("[...] ");

		for (int i = 0; i < positions.size() && i < 5; i++) {
			int p = positions.get(i);

			while (it.hasNext()) {
				token = it.next();
				if (token.getPosition() == p - 10)
					break;
			}
			

			int j = token.getPosition();
			while (it.hasNext() && j < p + 10) {
				System.out.print(token.getType() + " ");
				token = it.next();
				++j;
			}

			System.out.print("[...] ");
		}
		
		System.out.println();
	}
	
	private static void printPostings(List<Posting> postings, String corpus, Index index)
	{
		File dir = new File(corpus);
		String[] files = dir.list();
		Arrays.sort(files);

		for (Posting posting : postings)
			printPosting(posting, new File(corpus + File.separator + files[posting.getDocId()]), index.getTokenizer());
	}

	public static void main(String[] args)
	{
		//testIndexation();
		testQuery();
	}
}
