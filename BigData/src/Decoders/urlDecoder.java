package Decoders;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class urlDecoder {
	String fileName;
	InputStream in;
	ByteArrayOutputStream out;
	
	public urlDecoder(String fileName) {
		super();
		this.fileName = fileName;
	}

	public void Read(String link,String path) throws IOException {
		BufferedInputStream inputStream = null;
		  OutputStream out = null;

		  String fileName1 = fileName;
		  File savedFile = null;

		      // Replace your URL here.
		      URL fileURL = new URL(link);
		      URLConnection connection = fileURL.openConnection();
		      connection.connect();
		      
		      try {
				inputStream = new java.io.BufferedInputStream(connection.getInputStream());
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
			}
		      if (inputStream!=null){
		    	// Replace your save path here.
			      File fileDir = new File(path);
			      fileDir.mkdirs();
			      savedFile = new File(path, fileName1);
			      out = new FileOutputStream(savedFile);

			      byte buf[] = new byte[1024];
			      int len;

			      long total = 0;

			      while ((len = inputStream.read(buf)) != -1)
			      {
			        total += len;
			        out.write(buf, 0, len);
			      }

			      out.close();
			      inputStream.close();

		      }
		      
	}

}
