package argssearch.indexing.index;

import java.util.ArrayList;
import java.util.List;

public class Weighter {
	
	private int argTokCount;
	private int docTokCount;
	private int docCount;

	
	public Weighter () {
		//TODO - aus DB holen
		//SELECT argumentCounter FROM token (?)
		//number of token occurrences in argument
		this.argTokCount = 0;
		//SELECT totalCounter FROM token (?)
		//number of documents containing token
		this.docTokCount = 0;
		//SELECT occurences FROM arg_Index (?)
		//total number of arguments
		this.docCount = 0;
	}
	
	public double tfIDF (String token) {
		double result = (Math.log(argTokCount)) * (Math.log(docCount/docTokCount));
		return result;
		//Insert into DB
	}
	
	public double BM25 () {
		//TODO
		double result = 0;
		return result;
	}
}
