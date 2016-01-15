package ac.lemberg.kobi.stocks;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import java.io.*;

public class URLStockHandler {
	

        private final Map<String, Integer> FEATURE_INDEX_MAP = ImmutableMap.of("OPEN",1 ,"HIGH",2, "LOW", 3, "CLOSE", 4);
        private HashMap<String, Stock> stocksMap;
        private ArrayList<String> stocksNameArr;
        private int stocksAmount;
        private int daysToDownload;
        private Integer[] featuresIndexArr;

        
		public URLStockHandler(int stocksAmount,int daysToDownload,String[] features) {
			super();
			this.stocksAmount = stocksAmount;
			this.stocksNameArr = new ArrayList<String>();
			this.stocksMap = new HashMap<String,Stock>();
			this.daysToDownload = daysToDownload;
			
			ArrayList<Integer> featuresIndexArr = new ArrayList<Integer>();
			for (int i = 0; i < features.length; i++) {
				if (FEATURE_INDEX_MAP.containsKey(features[i]))
				{
					featuresIndexArr.add(FEATURE_INDEX_MAP.get(features[i]));
				}
			}
			this.featuresIndexArr = featuresIndexArr.toArray(new Integer[featuresIndexArr.size()]);			
		}
		
		
		public void getAllstockSymbols(String nasdaqListedPath)throws IOException
		{
			BufferedReader in = new BufferedReader(new FileReader(nasdaqListedPath));
			String fileInputLine;
			int counter=0;
			while (((fileInputLine = in.readLine()) != null)&&counter<stocksAmount) 
			{
				
				String[] splitedfileInputLine = fileInputLine.split("\\|");
				System.out.println("Stock Symbol:"+splitedfileInputLine[0].toString()+ ", Amount:"+stocksAmount);
				if (!stocksNameArr.contains(splitedfileInputLine[0].trim())&&!splitedfileInputLine[0].equals("Symbol"))
				{
					//Adding his symbol name
					stocksNameArr.add(splitedfileInputLine[0].trim());
					counter++;
				}
				
			}
			in.close();
			if(stocksNameArr.size()!= stocksAmount)
				System.out.println("Some thing happned with stocksAmount, stocksAmount="+stocksAmount+" stocksArr.size()="+stocksNameArr.size());
			System.out.println("Final Stock names:");
			for(String stockName:stocksNameArr )
			{
				System.out.println(stockName);
			}
			System.out.println("***********************************************");
		}
		
        
		public HashMap<String, Stock> connectAndReadStocks(String nasdaqListedPath) throws IOException
		{
			/*Insert only needed stock symbols from URL into stocksNameArr*/
			getAllstockSymbols(nasdaqListedPath);
			
			/*For each stock symbol*/
			for (int stockNameIndex = 0; stockNameIndex < stocksNameArr.size(); stockNameIndex++)
			{
				
				/*Start streaming of the stock*/
				System.out.println("Downloading stock:"+stocksNameArr.get(stockNameIndex));
				URLConnection urlOfStockValues = new URL("http://ichart.yahoo.com/table.csv?s="+stocksNameArr.get(stockNameIndex)).openConnection();
				BufferedReader in;
				try{
					in = new BufferedReader(new InputStreamReader(urlOfStockValues.getInputStream()));
				}
				catch(Exception e){
					System.out.println("Downloading stock:"+stocksNameArr.get(stockNameIndex));
					urlOfStockValues = new URL("http://ichart.yahoo.com/table.csv?s="+stocksNameArr.get(stockNameIndex)).openConnection();
					in = new BufferedReader(new InputStreamReader(urlOfStockValues.getInputStream()));
				}
		        int day = 1;  								//Represent the day backwards
		        String stockInputLine = in.readLine(); 		//Read the first (CSV headers)
		        
		        /*While we can read and we still need (for each day)*/
				while ((stockInputLine = in.readLine()) != null &&day<=daysToDownload) 
				{
					/*Filtering the values from the line:
					 * if we are looking on field at index that we have in featuresArrIndex-> 
					 * 		add this field to filtered array of values
					 * */
					String[] stockInputLineSplitedArr = stockInputLine.split(",");
					System.out.print("Stock "+stocksNameArr.get(stockNameIndex)+" in day number "+day+" is splited:\n[");
					for(String arg :stockInputLineSplitedArr)
						System.out.print(arg+",");
					System.out.print("]\n");
					
					List<Double> filteredStockPerDay = new  ArrayList<Double>();
					for (int feature = 0; feature < stockInputLineSplitedArr.length; feature++) {
						if((new ArrayList<Integer>(Arrays.asList(featuresIndexArr)).contains(feature)))
						{
							filteredStockPerDay.add(new Double(stockInputLineSplitedArr[feature]));
						}
					}
					
					System.out.println("filterd stock features:\n"+Arrays.toString((filteredStockPerDay.toArray())));
					
					System.out.println("Adding stock: " + stocksNameArr.get(stockNameIndex) + " for day: "+day);
					
					/*
					 * Insert or update stocksMap with the new day filtered array of values
					 * 
					 * */
					
					if(stocksMap.containsKey(stocksNameArr.get(stockNameIndex)))
					{
						System.out.println("No need to instantiate new stock. adding the array");
						stocksMap.get(stocksNameArr.get(stockNameIndex)).addDay(day, filteredStockPerDay.stream().mapToDouble(Double::doubleValue).toArray());
					}
					else
					{
						System.out.println("Stock name "+stocksNameArr.get(stockNameIndex)+" doesnt found in stock map, adding new stock");
						Stock s = new Stock(stocksNameArr.get(stockNameIndex), daysToDownload, (Arrays.asList(featuresIndexArr)).stream().mapToInt(i->i).toArray());
						s.addDay(day, filteredStockPerDay.stream().mapToDouble(Double::doubleValue).toArray());
						stocksMap.put(stocksNameArr.get(stockNameIndex), s);
					}
					
						
					System.out.println("Finished day: "+day);
					day++;
					
				}
				in.close();	
				System.out.println("Stock "+stocksNameArr.get(stockNameIndex)+" as Final:");
				System.out.println(stocksMap.get(stocksNameArr.get(stockNameIndex)).toFullString());

			}
			return stocksMap;
			
			
		}


		
		public static void downloadFileFromURL(String urlString, File destination) {    
	        try {
	            URL website = new URL(urlString);
	            ReadableByteChannel rbc;
	            rbc = Channels.newChannel(website.openStream());
	            FileOutputStream fos = new FileOutputStream(destination);
	            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	            fos.close();
	            rbc.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }


		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "URLHandler [stocksAmount=" + stocksAmount + ", daysToDownload="
					+ daysToDownload + "]";
		}


		/**
		 * @return the stocksMap
		 */
		public HashMap<String, Stock> getStocksMap() {
			return stocksMap;
		}


		/**
		 * @param stocksMap the stocksMap to set
		 */
		public void setStocksMap(HashMap<String, Stock> stocksMap) {
			this.stocksMap = stocksMap;
		}

		/**
		 * @return the stocksNameArr
		 */
		public ArrayList<String> getStocksNameArr() {
			return stocksNameArr;
		}


		/**
		 * @param stocksNameArr the stocksNameArr to set
		 */
		public void setStocksNameArr(ArrayList<String> stocksNameArr) {
			this.stocksNameArr = stocksNameArr;
		}


		/**
		 * @return the stocksAmount
		 */
		public int getStocksAmount() {
			return stocksAmount;
		}


		/**
		 * @param stocksAmount the stocksAmount to set
		 */
		public void setStocksAmount(int stocksAmount) {
			this.stocksAmount = stocksAmount;
		}


		/**
		 * @return the daysToDownload
		 */
		public int getDaysToDownload() {
			return daysToDownload;
		}


		/**
		 * @param daysToDownload the daysToDownload to set
		 */
		public void setDaysToDownload(int daysToDownload) {
			this.daysToDownload = daysToDownload;
		}


		/**
		 * @return the featuresIndexArr
		 */
		public Integer[] getFeaturesIndexArr() {
			return featuresIndexArr;
		}


		/**
		 * @param featuresIndexArr the featuresIndexArr to set
		 */
		public void setFeaturesIndexArr(Integer[] featuresIndexArr) {
			this.featuresIndexArr = featuresIndexArr;
		}
		
		

	}
