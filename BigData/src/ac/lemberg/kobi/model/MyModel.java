package ac.lemberg.kobi.model;
/**
 * @author Kobi Lemberg
 * @version 1.0
 * <h1> MyModel </h1>
 * MyModel class implements Model interface, 
 * class goal is to act as MVC/MVP Model and perform all business logic calculations.
 */
 

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;

import com.opencsv.CSVWriter;

import ac.lemberg.kobi.properties.userProperties;
import ac.lemberg.kobi.ssh.SSHAdapter;
import ac.lemberg.kobi.stocks.MinMaxNormalizer;
import ac.lemberg.kobi.stocks.Stock;
import ac.lemberg.kobi.stocks.URLStockHandler;
import ac.lemberg.kobi.stocks.Vector;


public class MyModel extends Observable implements Model{
	private SSHAdapter sshConnection;
	private Object data;
	private int modelCompletedCommand=0;
	private boolean flag = false;
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
	public void analyzeData(String csvFilesPath,String urlData,String numberOfStocks, String analyze, String clusters, String open, String high,String low, String close) {
		Integer numberStocks = new Integer(numberOfStocks);
		Integer numberAnalyze = new Integer(analyze);
		Integer numberOfClusters = new Integer(clusters);
		
		ArrayList<String> features= new ArrayList<String>();
		if(open.equals("true"))
			features.add("OPEN");
		if(high.equals("true"))
			features.add("HIGH");
		if(low.equals("true"))
			features.add("LOW");
		if(close.equals("true"))
			features.add("CLOSE");
		
		
		
		//check if it work!
				try {
					
					URLStockHandler urlStockHandler = new URLStockHandler(new Integer(numberOfStocks), new Integer(analyze),(String[])features.toArray(new String[features.size()]));
					HashMap<String, Stock> stocksMap = urlStockHandler.connectAndReadStocks(urlData);
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
					
					File folder = new File(csvFilesPath);
					if((folder.exists()&&folder.isDirectory()))
					{
						{
							String strOfDir ="Files and Directories in: "+csvFilesPath+"\n";
							for (String fileOrDirectory: folder.list()){strOfDir+=fileOrDirectory+"\n";}
							System.out.println(strOfDir);
						}
					}
					CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFilesPath+"/vectors.csv"), ',' );
					for(Stock s: (stocksMap.values()) )
					{
						System.out.println(s.getVectorString());
						csvWriter.writeNext(s.getVectorString().split(","));
					}
					csvWriter.close();

		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Finished!");
	}
	

	
}
