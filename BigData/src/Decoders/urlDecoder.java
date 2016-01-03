package Decoders;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

public class urlDecoder {
	InputStream in;
	PrintWriter writer;
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	public urlDecoder() {
		super();
	}

	public synchronized void Read(String link,String path) throws IOException {
		  writer.append(NEW_LINE_SEPARATOR);
		  BufferedInputStream inputStream = null;
		  URL fileURL = new URL(link);
		  ArrayList<String> allWords = new ArrayList<String>();
		  URLConnection connection = fileURL.openConnection();
		  connection.connect();
		  try {
			  inputStream = new java.io.BufferedInputStream(connection.getInputStream());
			  
		  } catch (FileNotFoundException e) {
		  }
		  if (inputStream!=null){
			  BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		        StringBuilder out = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		            String[] splitedString= line.split(",");
		            for (int i = 0; i < splitedString.length; i++) {
						allWords.add(splitedString[i].toString());
					}
		        }
		  }
		  for (int i = 0; i < allWords.size(); i++) {
				//System.out.println(allWords.get(i));
				writer.write(allWords.get(i));
				writer.append(COMMA_DELIMITER);
			}
			writer.close();
		  
	}
	public void generateCsvFile(String ToCsvFile) throws IOException{
		writer = new PrintWriter(ToCsvFile, "UTF-8");
        writer.append(NEW_LINE_SEPARATOR);


	}
	public void readFromcsv(String ToCsvFile) throws IOException{
		System.out.println("using Scanner to scan the csv file");
		Scanner scanner = new Scanner(new File(ToCsvFile));
        
        //Set the delimiter used in file
        scanner.useDelimiter(",");
         
        //Get all tokens and store them in some data structure
        //I am just printing them
        while (scanner.hasNext())
        {
            System.out.print(scanner.next() + "|");
        }
         
        //Do not forget to close the scanner 
        scanner.close();
	  }

}
