package Decoders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class excelReader {
	
	
	String ToCsvFile;
	File file;
	PrintWriter writer;
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "Date,Volume,low,high,open,close";
	
	public excelReader(String to) {
		super();
		this.ToCsvFile = to;
	}

	public void generateCsvFile() throws IOException{
		writer = new PrintWriter("C:/Users/nir/Desktop/Output.csv", "UTF-8");
        writer.append(FILE_HEADER.toString());
        writer.append(NEW_LINE_SEPARATOR);


	}
	
	
	public void read(String fromCsvFile){

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		try {

			br = new BufferedReader(new FileReader(fromCsvFile));
			while ((line = br.readLine()) != null) {

				String[] country = line.split(cvsSplitBy);
				Menaya main = new Menaya(country[0], country[1], country[2], country[3], country[4], country[5]);
				String a = "Menaya [Adj Date= " + country[0] 
                        + " , Volume=" + country[1] +", close= "+ country[2]+ 
                        ", Low= "+ country[3]+ " , High= "+ country[4]+
                        ", Open= "+ country[5]+ ", Close= "+ country[6]+"]";
				System.out.println(main.getDate());
				//System.out.println("Menaya [Adj Date= " + country[0] 
	            //                     + " , Volume=" + country[1] +", close= "+ country[2]+ 
	             //                    ", Low= "+ country[3]+ " , High= "+ country[4]+
	              //                   ", Open= "+ country[5]+ ", Close= "+ country[6]+"]");
				for(int i=0;i<5;i++){
					writer.write(main.getVolume());
					writer.append(COMMA_DELIMITER);
					writer.write(main.getDate());
					writer.append(COMMA_DELIMITER);

					writer.write(main.getHigh());
					writer.append(COMMA_DELIMITER);

					writer.write(main.getLow());
					writer.append(COMMA_DELIMITER);

					writer.write(main.getOpen());



					
					
					writer.append(NEW_LINE_SEPARATOR);

				}
				
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done");
	  }
	}

