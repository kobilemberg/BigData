package Decoders;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;

import ac.lemberg.kobi.properties.userProperties;

import java.io.*;

public class urlEncoder {
		String endOfCsv = ".csv";
        ArrayList<String> menayot = new ArrayList<String>();
        BufferedReader in;
        int kamotMenayot;
        ArrayList<String> menayotcsv = new ArrayList<String>();
        
		public urlEncoder(int kamotMenayot) {
			super();
			this.kamotMenayot = kamotMenayot;
		}
        
		public void connectAndRead(String url) throws IOException
		{
			URL urlToConnect = new URL(url);
	        URLConnection urlConnect = urlToConnect.openConnection();
	        in = new BufferedReader(new InputStreamReader(urlConnect.getInputStream()));
	        String inputLine;
			int counter = 0;
			while ((inputLine = in.readLine()) != null) 
			{
				
				String[] splitedArg = inputLine.split("\\|");
				System.out.println(splitedArg[0].toString());
				if (menayot.contains(splitedArg[0]))
				{
					continue;
				}
				menayot.add(splitedArg[0]);
				counter++;
				if(counter == kamotMenayot){
					break;
				}
			}
			in.close();
			System.out.println(menayot.size());
			for (int i = 0; i < menayot.size(); i++)
			{
				String fileName = menayot.get(i)+".csv";
				menayotcsv.add(fileName);
				urlDecoder urldec = new urlDecoder(fileName);
				urldec.Read("http://ichart.yahoo.com/table.csv?s="+menayot.get(i), "C:/Users/nir/Desktop/csvFiles/");
			}
		}

		public ArrayList<String> getMenayotcsv() {
			return menayotcsv;
		}


	}
