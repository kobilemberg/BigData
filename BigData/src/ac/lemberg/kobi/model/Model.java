package ac.lemberg.kobi.model;
/**
 * @author Kobi Lemberg
 * @version 1.1
 * <h1>Model</h1>
 * Model interface represent a generally model layer of MVC/MVP.
 */

import java.io.IOException;
import java.util.HashMap;

import com.jcraft.jsch.JSchException;

import ac.lemberg.kobi.stocks.Stock;


public interface Model {
	
	/**
	 * This method will open SSH connection.
	 * @param host represent Host name or IP address of the SSH server.
	 * @param userName represent the user name of the server.
	 * @param password represent the password of the server.
	 */
	public void sshConnect(String host,String userName, String password);
	/**
	 * This method will remotely execute command on SSH server.
	 * @param command represent a string of command to execute.
	 */
	public void executeCommand(String command);
	/**
	 * This method will transfer file from the local host to the SSH server.
	 * @param filePath represent full file name including his path.
	 */
	public void transferFile(String filePath,String targetPath);
	/**
	 * This method will download files into "Output folder".
	 * The method doesn't have the option of copying files to dynamically local locations due to convenience.
	 * @param filePath represent full file name including his path.
	 * @throws IOException in case of problems with writing the file.
	 * @throws JSchException in case of SSH connection error.
	 */
	public void getFIleByName(String filePath);
	/**
	* This close all files and threads and will terminate the model activity
	*/
	public void exit();
	/**
	 * This method will return model calculation
	 * @return object represent a model calculation
	 */
	public Object getData();
	/**
	 * @return model command id
	 */
	public int getModelCompletedCommand();
	public void analyzeData(String numberOfStocks, String analyze, String clusters, String open, String high,
			String low, String close) ;
	public HashMap<String, Stock> getStocksMap();
	

}
