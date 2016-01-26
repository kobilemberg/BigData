package solution;

import java.io.Serializable;

public class HadoopProperties implements Serializable{
	

	private static final long serialVersionUID = 1L;
	private String jobServerInputFolderPath;
	private String jobServerOutputFolderPath;
	private int numOfFeatures;
	private int numOfDays;
	private int numOfStocks;
	private String StockCSVFileName;
	private String StockCSVFilePath;
	private int numOfClusters;
	private boolean high;
	private boolean close;
	private boolean low;
	private boolean open;
	
	public HadoopProperties() {
		super();
	}

	public HadoopProperties(String jobServerInputFolderPath, String jobServerOutputFolderPath, int numOfFeatures,
			int numOfDays, int numOfStocks, String stockCSVFileName, int numOfClusters, boolean high, boolean close,
			boolean low, boolean open) {
		super();
		this.jobServerInputFolderPath = jobServerInputFolderPath;
		this.jobServerOutputFolderPath = jobServerOutputFolderPath;
		this.numOfFeatures = numOfFeatures;
		this.numOfDays = numOfDays;
		this.numOfStocks = numOfStocks;
		StockCSVFileName = stockCSVFileName;
		this.numOfClusters = numOfClusters;
		this.high = high;
		this.close = close;
		this.low = low;
		this.open = open;
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
	 * @return the numOfFeatures
	 */
	public int getNumOfFeatures() {
		return numOfFeatures;
	}

	/**
	 * @param numOfFeatures the numOfFeatures to set
	 */
	public void setNumOfFeatures(int numOfFeatures) {
		this.numOfFeatures = numOfFeatures;
	}

	/**
	 * @return the numOfDays
	 */
	public int getNumOfDays() {
		return numOfDays;
	}

	/**
	 * @param numOfDays the numOfDays to set
	 */
	public void setNumOfDays(int numOfDays) {
		this.numOfDays = numOfDays;
	}

	/**
	 * @return the numOfStocks
	 */
	public int getNumOfStocks() {
		return numOfStocks;
	}

	/**
	 * @param numOfStocks the numOfStocks to set
	 */
	public void setNumOfStocks(int numOfStocks) {
		this.numOfStocks = numOfStocks;
	}

	/**
	 * @return the stockCSVFileName
	 */
	public String getStockCSVFileName() {
		return StockCSVFileName;
	}

	/**
	 * @param stockCSVFileName the stockCSVFileName to set
	 */
	public void setStockCSVFileName(String stockCSVFileName) {
		StockCSVFileName = stockCSVFileName;
	}

	/**
	 * @return the stockCSVFilePath
	 */
	public String getStockCSVFilePath() {
		return StockCSVFilePath;
	}

	/**
	 * @param stockCSVFilePath the stockCSVFilePath to set
	 */
	public void setStockCSVFilePath(String stockCSVFilePath) {
		StockCSVFilePath = stockCSVFilePath;
	}

	/**
	 * @return the numOfClusters
	 */
	public int getNumOfClusters() {
		return numOfClusters;
	}

	/**
	 * @param numOfClusters the numOfClusters to set
	 */
	public void setNumOfClusters(int numOfClusters) {
		this.numOfClusters = numOfClusters;
	}

	/**
	 * @return the high
	 */
	public boolean isHigh() {
		return high;
	}

	/**
	 * @param high the high to set
	 */
	public void setHigh(boolean high) {
		this.high = high;
	}

	/**
	 * @return the close
	 */
	public boolean isClose() {
		return close;
	}

	/**
	 * @param close the close to set
	 */
	public void setClose(boolean close) {
		this.close = close;
	}

	/**
	 * @return the low
	 */
	public boolean isLow() {
		return low;
	}

	/**
	 * @param low the low to set
	 */
	public void setLow(boolean low) {
		this.low = low;
	}

	/**
	 * @return the open
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * @param open the open to set
	 */
	public void setOpen(boolean open) {
		this.open = open;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	

}
