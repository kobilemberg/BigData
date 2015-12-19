package ac.lemberg.kobi.properties;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class WriteProperties {

	public static void main(String[] args) {
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("Settings/properties.xml")));
			encoder.writeObject(new Properties("GUI", "127.0.0.1", 22, "training", "training","/home/training/input","/home/training/output"));
			encoder.close();
		} catch (Exception e) {
			System.out.println("problem with writing XML");
		}

	}

}
