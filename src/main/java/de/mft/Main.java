package de.mft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;

import weka.core.Instance;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String header = "id,person_name,person_name_lc,person_name_ngram,person_name_length,person_name_counts\n";
		boolean a = rewrite("solr/musicbrainz_person_names.csv", header);
		if (a) System.out.println("Rewriting file succeed");
		else System.out.println("Rewriting file failed");
	}

	public static boolean rewrite(String model, String header) {
		BufferedReader br;
		Writer out;
		String line;
		StringBuffer sb = new StringBuffer();
		sb.append(header);
		try {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(
				model), "UTF8"));
		String[] arr = null;
			while ((line = br.readLine()) != null) {
				arr = line.split(",");
				String target = arr[0].trim();
				sb.append(UUID.randomUUID() + "," + WordUtils.capitalize(target.toLowerCase()) + "," + target.toLowerCase() + "," + target.split("\\s+").length + "," + target.length() +"," + randInt(0, 10) + "\n");
			}
			br.close();
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(model), "UTF8"));
			out.append(sb.toString());
			out.flush();
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
}
	
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

	
}
