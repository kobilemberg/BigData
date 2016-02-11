package solution;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;




public class FinalKMeansReducer extends Reducer<CenterCentroidWritableComparable, Vector, Text, Text> {


	@Override
	protected void reduce(CenterCentroidWritableComparable key, Iterable<Vector> values,Context context) throws IOException, InterruptedException {
		
		
		String stock ="";
		System.out.println(key.getCentroid().getCenter().getName());
		for(Vector v: values)
		{
			stock +=v.getName().toString()+",";
		}
		stock=stock.substring(0,stock.length()-1);
		context.write(new Text(key.getCentroid().getCenter().getName()), new Text(stock));
	}

	
}
