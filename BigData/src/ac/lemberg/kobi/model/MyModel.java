package ac.lemberg.kobi.model;
/**
 * @author Kobi Lemberg
 * @version 1.0
 * <h1> MyModel </h1>
 * MyModel class implements Model interface, 
 * class goal is to act as MVC/MVP Model and perform all business logic calculations.
 */
 


import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import com.opencsv.CSVWriter;

import ac.lemberg.kobi.ssh.SSHAdapter;
import ac.lemberg.kobi.stocks.MinMaxNormalizer;
import ac.lemberg.kobi.stocks.Stock;
import ac.lemberg.kobi.stocks.URLStockHandler;
import solution.HadoopProperties;
import solution.Properties;


public class MyModel extends Observable implements Model{
	private SSHAdapter sshConnection;
	private Object data;
	private int modelCompletedCommand=0;
	private boolean flag = false;
	private Properties userProperties;
	private HadoopProperties hadoopProperties;
	HashMap<String, Stock> finalstocksMap;
	Thread t;
	
	
	
	
	public MyModel(Properties properties) {
		this.userProperties=properties;
		XMLDecoder decoder=null;
		try 
		{
			decoder=new XMLDecoder(new BufferedInputStream(new FileInputStream("Settings/HadoopProperties.xml")));
			setHadoopProperties((HadoopProperties)decoder.readObject());
			decoder.close();			
		} catch (FileNotFoundException e) 
		{
			System.out.println("ERROR: File Settings/HadoopProperties.xml not found");
		}
	}

	//Functionality
	@Override
	public void sshConnect(String host, String userName, String password) {
		sshConnection = new SSHAdapter(userName, password, host);
		data = "Connection has been created.";
		flag = true;
		setModelCommand(1);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void executeCommand(String command) {
		if(sshConnection!= null){
			if(sshConnection.commandExecutorConnect()){
					data = sshConnection.sendCommand(command);
			}
			else
				data = "Something is wrong with command.";
		}
		else
			data = "No active SSH connections.";
		setModelCommand(2);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void transferFile(String filePath,String targetPath) {
		if(sshConnection!= null)
		{
			if(sshConnection.SFTPConnect())
			{
				if(sshConnection.sendFile(filePath,targetPath))
				{
					data = "File has been sent succesfully.";
				}
				else
					data = "Error while sending file.";
			}
			else
				data = "Error with SFTP connection.";
		}
		else
			data = "No active SSH connections.";
		setModelCommand(3);
		
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void getFIleByName(String filePath) {
		if(sshConnection!= null)
		{
			
			if(sshConnection.SFTPConnect())
			{
				if(sshConnection.getFile(filePath))
				{
					data = "Object has been readed, you can look at Output folder.";
				}
				else
				{
					data  = "Problem while reading the file.";
				}
			}
			else
				data = "Error with SFTP connection";
		}
		else
			data = "No active SSH connections.";
		setModelCommand(4);
	}

	@SuppressWarnings("deprecation")
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void exit() {
		if(flag == true)
			sshConnection.disconnect();
		if(t!=null)
			t.stop();
		
	}
	//Getters and setters
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Object getData() {
		return data;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public int getModelCompletedCommand() {
		return modelCompletedCommand;
	}

	private void setModelCommand(int commandNum) {
		this.setChanged();
		modelCompletedCommand = commandNum;
		notifyObservers();
	}

	@Override
	public void analyzeData(String numberOfStocks, String analyze, String clusters, String open, String high,String low, String close) {
		t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				/*Prepering features array for stock filter*/
				ArrayList<String> features= new ArrayList<String>();
				if(open.equals("true"))
					features.add("OPEN");
				if(high.equals("true"))
					features.add("HIGH");
				if(low.equals("true"))
					features.add("LOW");
				if(close.equals("true"))
					features.add("CLOSE");
				

				
				/*Setting user changes in hadoopProperties file*/
				hadoopProperties.setNumOfStocks(new Integer(numberOfStocks));
				hadoopProperties.setNumOfDays(new Integer(analyze));
				hadoopProperties.setNumOfClusters(new Integer(clusters));
				hadoopProperties.setOpen(new Boolean(open));
				hadoopProperties.setClose(new Boolean(close));
				hadoopProperties.setHigh(new Boolean(high));
				hadoopProperties.setLow(new Boolean(low));
				hadoopProperties.setNumOfFeatures(features.size());
				hadoopProperties.setJobServerInputFolderPath("/home/training/clustering");
				/*Writing Hadoop properties*/
				try {
					XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("Settings/HadoopProperties.xml")));
					encoder.writeObject(hadoopProperties);
					encoder.close();
				} catch (Exception e) {
					System.out.println("problem with writing XML");
				}
				try {
					
					URLStockHandler urlStockHandler = new URLStockHandler(new Integer(numberOfStocks), new Integer(analyze),(String[])features.toArray(new String[features.size()]));
					HashMap<String, Stock> stocksMap = urlStockHandler.connectAndReadStocks(userProperties.getCsvFilePathForStockSymbols()+"/nasdaqlisted.txt");
					for(Stock s: (stocksMap.values()) )
					{
						s.setVctor(s.getAlldaysFeatures());

					}
					MinMaxNormalizer minMax= new MinMaxNormalizer();
					minMax.normalizeData(stocksMap.values(), 100.0, 0.0);
					for(Stock s: (stocksMap.values()) )
					{
						System.out.println(s.toFullString());
					}
					
					
					int numOfFiles=0;
					int lineCounter=0;
					
					CSVWriter csvWriter = new CSVWriter(new FileWriter(userProperties.getCsvFilePathForStockSymbols()+"/vectors_"+numOfFiles+".csv"), ',' );
					for(Stock s:(stocksMap.values()) )
					{
						
						if(lineCounter<10)
						{
							
							System.out.println("File number:" + numOfFiles);
							System.out.println("Inserting line:" + lineCounter);
							System.out.println(s.getVectorString());
							lineCounter++;
							csvWriter.writeNext(s.getVectorString().split(","));
						}
						else
						{
							csvWriter.close();
							numOfFiles++;
							lineCounter=0;
							csvWriter = new CSVWriter(new FileWriter(userProperties.getCsvFilePathForStockSymbols()+"/vectors_"+numOfFiles+".csv"), ',' );
							System.out.println("File number:" + numOfFiles);
							System.out.println("Inserting line:" + lineCounter);
							System.out.println(s.getVectorString());
							lineCounter++;
							csvWriter.writeNext(s.getVectorString().split(","));
						}
					}
					csvWriter.close();
					
					csvWriter = new CSVWriter(new FileWriter("Output/vectors.csv"), ',' );
					for(Stock s: (stocksMap.values()) )
					{
						csvWriter.writeNext(s.getVectorString().split(","));
					}
					csvWriter.close();
					System.out.println("Finish with CSV's");

					String rootFolder = hadoopProperties.getJobServerInputFolderPath();
					String outputFolder = hadoopProperties.getJobServerOutputFolderPath();
					String vectorsFolder = rootFolder+"/vectors";
					String jarpath = rootFolder+"/solution";
					
					//Connecting to cloudera hadoop and transfering files
					sshConnect(userProperties.getHost(),userProperties.getUserName(), userProperties.getPassword());
					System.out.println("Connected to Hadoop cloudera");
					executeCommand("rm -Rf "+rootFolder);
					executeCommand("mkdir "+rootFolder+"; mkdir "+vectorsFolder);
					executeCommand("rm -Rf "+outputFolder+" ;mkdir "+outputFolder); 
					System.out.println("Created: "+rootFolder+ vectorsFolder+" ,"+outputFolder);
					transferFile("Settings/HadoopProperties.xml", rootFolder);
					System.out.println("Created hadoopProperties.xml - "+rootFolder);
					for(int file=0;file<=numOfFiles;file++)
					{
						String fileName = userProperties.getCsvFilePathForStockSymbols()+"/vectors_"+file+".csv";
						File f= new File(fileName);
						if(f.exists())
						{
							System.out.println("Transfering: "+f.getName() + " To: " +vectorsFolder);
							transferFile(fileName, vectorsFolder);
							System.out.println("File has been transfered successfuly");
						}
					}
					
					System.out.println("All files was transfered");
					executeCommand("hadoop fs -rmr /home");
					System.out.println("Creating folders in HDFS");
					executeCommand("hadoop fs -mkdir home; hadoop fs -mkdir /home/training; hadoop fs -mkdir /home/training/clustering; hadoop fs -mkdir /home/training/clustering/vectors");
					System.out.println("Uploading files to HDFS");
					executeCommand("hadoop fs -put "+rootFolder+ "/HadoopProperties.xml "+ hadoopProperties.getJobServerInputFolderPath());
					executeCommand("hadoop fs -put "+vectorsFolder+" "+ hadoopProperties.getJobServerInputFolderPath());
					System.out.println("Building the jar");
					
					executeCommand("mkdir "+jarpath);
					File f= new File("input/stocks/Hadoop");
					if(f.isDirectory())
					{
						File[] javaFiles = f.listFiles();
						for(File javaFile : javaFiles)
						{
							System.out.println("Transfering " + javaFile.getPath());
							transferFile(javaFile.getPath(), jarpath);
						}
						
					}
					//We are adding permissions because we edit Hadoop java files in windows.
					executeCommand("chmod -R 777"+rootFolder);
					System.out.println("Transfered all java files, compiling them");
					executeCommand("cd "+ rootFolder+ " ; export HCP=`hadoop classpath`; javac -classpath $HCP solution/*.java; jar cvf clustering.jar solution/*.class");
					System.out.println("Running the job.");
					executeCommand("hadoop jar "+rootFolder+"/clustering.jar solution.Driver /home/training/clustering");
					System.out.println("Move the files from HDFS to linux FS.");
					executeCommand("hadoop fs -get "+hadoopProperties.getJobServerOutputFolderPath()+"/part-r-00000 "+ hadoopProperties.getJobServerOutputFolderPath());
					System.out.println("Copy result file from linux to windows.");
					getFIleByName(hadoopProperties.getJobServerOutputFolderPath()+"/part-r-00000");
					//Return the analyzed stocks to view in order to create the user graphs.
					finalstocksMap = stocksMap;
					setModelCommand(5);
		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Finished!");
			}
		});
		t.start();
	}

	public HadoopProperties getHadoopProperties() {
		return hadoopProperties;
	}

	public void setHadoopProperties(HadoopProperties hadoopProperties) {
		this.hadoopProperties = hadoopProperties;
	}

	@Override
	public HashMap<String, Stock> getStocksMap() {
		return this.finalstocksMap;
	}
	

	
}
