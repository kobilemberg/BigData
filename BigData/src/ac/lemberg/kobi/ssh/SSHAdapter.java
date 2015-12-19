package ac.lemberg.kobi.ssh;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author Kobi Lemberg
 * @version 1.1
 * <h1>SSHAdapter</h1>
 * SSHAdapter class goal is to handle SSH connections.
 * @see http://www.jcraft.com/jsch/ 
 */
public class SSHAdapter {
	
	protected String userName;
	protected String password;
	protected String host;
	protected final int PORT =22;
	private JSch jsch;
	private ChannelSftp sftpChannel;
	private Channel  channelExec;
	protected Session session;
	
	/**
	 * Instantiate a new SSH adapter.
	 * @param userName represent the U.N of the SSH server.
	 * @param password represent the password of the SSH server.
	 * @param host represent the SSH server IPv4 address or Host-Name.
	 */
	public SSHAdapter(String userName, String password, String host) {
		super();
		this.userName = userName;
		this.password = password;
		this.host = host;
		
		//Init session
		jsch = new JSch();
		try 
		{
			session = jsch.getSession(userName, host, PORT);
			session.setConfig("StrictHostKeyChecking", "no");
			System.out.println("Establishing Connection...");
			session.setPassword(password);
		    session.connect();
		    System.out.println("Connection established.");
		} 
		catch (JSchException e) 
		{
			System.out.println("Connection faild");
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will open a command executer terminal to SSH server.
	 * @return true if the terminal was opened properly.
	 */
	public Boolean commandExecutorConnect()
	{
		try 
		{
			channelExec=(ChannelExec) session.openChannel("exec");
			return true;
		}
		catch (JSchException e) 
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * This method will send a String represent a command to execute on the server.
	 * Before using this method you must user "commandExecutorConnect()" method first.
	 * @param command string represent the command to execute on the server.
	 * @return string represent the command result from the server.
	 */
	public String sendCommand(String command) 
	{
		String result="";
		try 
		{	
			((ChannelExec)channelExec).setCommand(command);
			channelExec.setInputStream(null);
		    ((ChannelExec)channelExec).setErrStream(System.err);
		    
		    InputStream in=channelExec.getInputStream();
			channelExec.connect();
			
			InputStreamReader inputReader = new InputStreamReader(in);
	         BufferedReader bufferedReader = new BufferedReader(inputReader);
	         String line = "";
	         while((line = bufferedReader.readLine()) != null){
	             result+=line+"\n";
	         }
	         bufferedReader.close();
	         inputReader.close();
		
		}catch (IOException e) 
		{
			e.printStackTrace();
		} catch (JSchException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	/**
	 * This method will open SFTP terminal to SSH server
	 * @return true is the SFTP terminal was opened properly
	 */
	public Boolean SFTPConnect()
	{
		System.out.println("Crating SFTP Channel.");
        
        try 
        {
        	if(sftpChannel!=null)
        	{
        		if(!sftpChannel.isConnected())
        		{
        			sftpChannel = (ChannelSftp) session.openChannel("sftp");
        			sftpChannel.connect();
        		}
        			
        	}
        	else
        	{
        		sftpChannel = (ChannelSftp) session.openChannel("sftp");
        		sftpChannel.connect();
        	}

        	
			
			System.out.println("SFTP Channel created.");
	        return true;
		} 
        catch (JSchException e) 
        {
			System.out.println("SFTP Channel connection faild");
			e.printStackTrace();
		}
        return false;
	}
	/**
	 * This method will send a String represent a path of existing file to the server.
	 * Before using this method you must user "SFTPConnect()" method first.
	 * @param filePath string represent the path of the file to send.
	 * @return true if the file was sent to the server.
	 */
	public Boolean sendFile(String filePath,String path)
	{
		try 
		{
			File file = new File(filePath);
			//file.getName() is adding " " so we need to delete this suffix
			String newFileName = file.getName().substring(0, file.getName().length()-1);
			sftpChannel.cd(path);
			sftpChannel.put(new FileInputStream(file),newFileName);
			return true;
		} 
		catch (FileNotFoundException | SftpException e) 
		{
			System.out.println("Exception found while tranfer the response.");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * This method will download a file that represent an Object from the server.
	 * Before using this method you must user "SFTPConnect()" method first.
	 * @param filePath string represent the path of the file to download.
	 * @return True if the file had been download from the server. 
	 */
	public boolean getFile(String filePath) 
	{
	   boolean flag=false;
       try 
       {
    	   System.out.println("Getting file!");
    	   sftpChannel = (ChannelSftp) session.openChannel("sftp");
           sftpChannel.connect();
    	   sftpChannel.get(filePath, "Output");
    	   flag=true;
    	   return flag;
		} 
       catch (SftpException e) 
       {
			e.printStackTrace();
       } catch (JSchException e) {
		e.printStackTrace();
       }
       return flag;
    }
        
	
	/**
	 * This method will close all active channels.
	 * @return true if all active channels were closed properly.
	 */
	public Boolean disconnect()
	{
		sftpChannel.disconnect();
		channelExec.disconnect();
		session.disconnect();
		return true;
	}
	
	
	

}
