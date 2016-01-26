package solution;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class WriteProperties {

	public static void main(String[] args) {
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("Settings/properties.xml")));
			Properties p = new Properties("GUI", "127.0.0.1",22, "training", "training", "input/stocks/vectors.csv");
			HadoopProperties hp = new HadoopProperties("/home/training/input","/home/training/output",4,50,100,"input/stock/stocks.csv",7,true,true,true,true);
			encoder.writeObject(p);
			encoder.close();
			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("Settings/HadoopProperties.xml")));
			encoder.writeObject(hp);
			encoder.close();
		} catch (Exception e) {
			System.out.println("problem with writing XML");
		}

	}

}
