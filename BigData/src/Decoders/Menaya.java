package Decoders;

public class Menaya {

	String date;
	String close;
	String high;
	String open;
	String low;
	String volume;
	
	

	public String getDate() {
		return date;
	}



	public void setDate(String date) {
		this.date = date;
	}



	public String getClose() {
		return close;
	}



	public void setClose(String close) {
		this.close = close;
	}



	public String getHigh() {
		return high;
	}



	public void setHigh(String high) {
		this.high = high;
	}



	public String getOpen() {
		return open;
	}



	public void setOpen(String open) {
		this.open = open;
	}



	public String getLow() {
		return low;
	}



	public void setLow(String low) {
		this.low = low;
	}



	public String getVolume() {
		return volume;
	}



	public void setVolume(String volume) {
		this.volume = volume;
	}



	public Menaya(String date, String country, String country2, String country3, String country4, String country5) {
		super();
		this.date = date;
		this.close = country;
		this.high = country2;
		this.open = country3;
		this.low = country4;
		this.volume = country5;
	}

}
