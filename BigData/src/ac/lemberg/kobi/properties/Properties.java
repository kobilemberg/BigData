package ac.lemberg.kobi.properties;

import java.io.Serializable;

public class Properties implements Serializable{
	

	private static final long serialVersionUID = 1L;
	private String UI;
	private String host;
	private int port;
	private String userName;
	private String password;
	private String jobServerInputFolderPath;
	private String jobServerOutputFolderPath;

	
	public Properties() {
		super();
	}

	/**
	 * @return the uI
	 */
	public String getUI() {
		return UI;
	}

	/**
	 * @param uI the uI to set
	 */
	public void setUI(String uI) {
		UI = uI;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	

	public Properties(String uI, String host, int port, String userName, String password,
			String jobServerInputFolderPath, String jobServerOutputFolderPath) {
		super();
		UI = uI;
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.jobServerInputFolderPath = jobServerInputFolderPath;
		this.jobServerOutputFolderPath = jobServerOutputFolderPath;
	}

	/**
	 * @return the jobServerInputFolderPath
	 */
	public String getJobServerInputFolderPath() {
		return jobServerInputFolderPath;
	}

	/**
	 * @param jobServerInputFolderPath the jobServerInputFolderPath to set
	 */
	public void setJobServerInputFolderPath(String jobServerInputFolderPath) {
		this.jobServerInputFolderPath = jobServerInputFolderPath;
	}

	/**
	 * @return the jobServerOutputFolderPath
	 */
	public String getJobServerOutputFolderPath() {
		return jobServerOutputFolderPath;
	}

	/**
	 * @param jobServerOutputFolderPath the jobServerOutputFolderPath to set
	 */
	public void setJobServerOutputFolderPath(String jobServerOutputFolderPath) {
		this.jobServerOutputFolderPath = jobServerOutputFolderPath;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
	

}
