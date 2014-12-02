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
		boolean a = rewrite("solr/music/feedback.arff", null);
		if (a) System.out.println("Rewriting file succeed");
		else System.out.println("Rewriting file failed");
	}

	public static boolean rewrite(String model, String header) {
		BufferedReader br;
		Writer out;
		String line;
		StringBuffer sb = new StringBuffer();
		try {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(
				model), "UTF8"));
		String[] arr = null;
			while ((line = br.readLine()) != null) {
				arr = line.split(",");
				sb.append(arr[2] + "," + arr[3] + "," + arr[4] + "," + arr[5] + "," + arr[6] + "," + arr[7] + "," + arr[8] + "\n");
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
