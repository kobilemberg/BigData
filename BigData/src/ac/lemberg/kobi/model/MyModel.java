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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import com.opencsv.CSVWriter;
import ac.lemberg.kobi.properties.HadoopProperties;
import ac.lemberg.kobi.properties.Properties;
import ac.lemberg.kobi.ssh.SSHAdapter;
import ac.lemberg.kobi.stocks.MinMaxNormalizer;
import ac.lemberg.kobi.stocks.Stock;
import ac.lemberg.kobi.stocks.URLStockHandler;


public class MyModel extends Observable implements Model{
	private SSHAdapter sshConnection;
	private Object data;
	private int modelCompletedCommand=0;
	private boolean flag = false;
	private Properties userProperties;
	private HadoopProperties hadoopProperties;
	
	
	
	
	
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

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void exit() {
		if(flag == true)
			sshConnection.disconnect();
		
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
		hadoopProperties.setJobServerInputFolderPath("/home/training");
		/*Writing Hadoop properties*/
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("Settings/HadoopProperties.xml")));
			encoder.writeObject(hadoopProperties);
			encoder.close();
		} catch (Exception e) {
			System.out.println("problem with writing XML");
		}
		
		
		
		
		//check if it work!
				try {
					
					URLStockHandler urlStockHandler = new URLStockHandler(new Integer(numberOfStocks), new Integer(analyze),(String[])features.toArray(new String[features.size()]));
					HashMap<String, Stock> stocksMap = urlStockHandler.connectAndReadStocks(userProperties.getCsvFilePathForStockSymbols().
							substring(0, (userProperties.getCsvFilePathForStockSymbols().length())-"/vector.csv".length())+"/nasdaqlisted.txt");
					
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
					

					CSVWriter csvWriter = new CSVWriter(new FileWriter(userProperties.getCsvFilePathForStockSymbols()), ',' );
					for(Stock s: (stocksMap.values()) )
					{
						System.out.println(s.getVectorString());
						csvWriter.writeNext(s.getVectorString().split(","));
					}
					csvWriter.close();
					
					/*Connecting to cloudera hadoop and transfering files*/
					System.out.println("Connected!");
					sshConnect(this.userProperties.getHost(), this.userProperties.getUserName(), this.userProperties.getPassword());
					executeCommand("mv /home/training/hadoopProperties.xml ; mv /home/training/vectors.csv"); 
					transferFile("Settings/HadoopProperties.xml", "/home/training");
					System.out.println("All fine");
					
					transferFile(hadoopProperties.getStockCSVFileName(), "/home/training");
					
					
					
					//Connecting to Hadoop host
					//viewCommandMap.get("Connect").doCommand(new String[]{(properties.getHost()),(properties.getUserName()),(properties.getPassword())});
					System.out.println("Connected!");
					/*
					//Delete on the linux the input and output path to avoid errors.
					viewCommandMap.get("Execute").doCommand(new String[]{"rm -Rf "+properties.getJobServerInputFolderPath()});
					viewCommandMap.get("Execute").doCommand(new String[]{"rm -Rf "+properties.getJobServerOutputFolderPath()});
					
					//Delete the output folder from Hadoop fs to avoid errors.
					viewCommandMap.get("Execute").doCommand(new String[]{"hadoop fs -rmr output"});
					System.out.println("Removed all folders");
					
					//Creating a new input directory to copy the files from windows
					viewCommandMap.get("Execute").doCommand(new String[]{"mkdir "+properties.getJobServerInputFolderPath()});
					viewCommandMap.get("Execute").doCommand(new String[]{"cd "+properties.getJobServerInputFolderPath()});
					
					//For each file in windows transfer it to linux
					File inputFolder = new File("input");
					File[] listOfFiles = inputFolder.listFiles();
					for(File log:listOfFiles)
					{
						viewCommandMap.get("Transfer").doCommand(new String[]{"input/"+log.getName(),properties.getJobServerInputFolderPath()});
					}*/
					
					
					
		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Finished!");
	}

	public HadoopProperties getHadoopProperties() {
		return hadoopProperties;
	}

	public void setHadoopProperties(HadoopProperties hadoopProperties) {
		this.hadoopProperties = hadoopProperties;
	}
	

	
}
