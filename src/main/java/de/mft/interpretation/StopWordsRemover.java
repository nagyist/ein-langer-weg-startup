package de.mft.interpretation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class StopWordsRemover {

	private static String removeStopWords(String query,String lang) throws IOException {
		
		String cleaned_query = query;
		File stopWords_file = new File("nlp/stopWords_"+lang+".txt");
		BufferedReader br = new BufferedReader(new FileReader(stopWords_file));
	    String line;
	    while((line = br.readLine()) != null) {
	        if ((line = " " + line.trim() + " ") != null && cleaned_query.indexOf(line) != -1) {
	        	cleaned_query = cleaned_query.substring(0, cleaned_query.indexOf(line)) 
	        			+ " " +  cleaned_query.substring(cleaned_query.indexOf(line) + line.length(), cleaned_query.length());            	
	        } 
	        if ((line = line.trim() + " ") != null && cleaned_query.indexOf(line) != -1 && cleaned_query.indexOf(line) == 0) {
	            	cleaned_query = cleaned_query.substring(cleaned_query.indexOf(line) + line.length(), cleaned_query.length());            	
	        }
	        if ((line = " " + line.trim()) != null && cleaned_query.indexOf(line) != -1 && cleaned_query.indexOf(line) + line.length() == cleaned_query.length()) {       	                    	
	        		cleaned_query = cleaned_query.substring(0, cleaned_query.indexOf(line));            
	        }
	    }
	    return cleaned_query;
	}

	private static String removeDeStopWords(String query) throws IOException  {
		return removeStopWords(query, "de");
	}

	private static String removeEnStopWords(String query) throws IOException  {
		return removeStopWords(query, "en");
	}
	
	public static String removeStopWords(String query) throws IOException {
		return removeEnStopWords(removeDeStopWords(query));
	}

	

}
