package ac.lemberg.kobi.properties;

import java.io.Serializable;

public class userProperties implements Serializable{
	

	private static final long serialVersionUID = 1L;
	String csvFilesPath;
	String urlGetData;

	
	public userProperties() {
		super();
	}


	public userProperties(String csvFilesPath, String urlGetData) {
		super();
		this.csvFilesPath = csvFilesPath;
		this.urlGetData = urlGetData;
	}


	public String getCsvFilesPath() {
		return csvFilesPath;
	}


	public void setCsvFilesPath(String csvFilesPath) {
		this.csvFilesPath = csvFilesPath;
	}


	public String getUrlGetData() {
		return urlGetData;
	}


	public void setUrlGetData(String urlGetData) {
		this.urlGetData = urlGetData;
	}





}
