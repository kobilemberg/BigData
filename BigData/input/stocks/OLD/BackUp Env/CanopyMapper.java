package solution;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class CanopyMapper extends Mapper<LongWritable, Text, IntWritable, ClusterCenter> {

	List<ClusterCenter> centers;
	public static enum Counter{
		NUMBER_OF_VECTORS;
	}
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		this.centers = new LinkedList<ClusterCenter>();
	}

	@Override
	protected void map(LongWritable key,Text value, Context context)throws IOException, InterruptedException 
	{
		//Take the data, split the line and build vector
		String[] data  = value.toString().split(",");
		String name = data[0];
		double[] vectorArr = new double[data.length-1];
		for (int i = 0; i < vectorArr.length; i++) {
			vectorArr[i] = new Double(data[i+1].substring(1, data[i+1].length()-1));
		}
		Vector newVector = new Vector(name.substring(1, name.length()-1), vectorArr);
		//If this is first vector or the distance is higher then T1: set new Cluster Center and increment data counter.
		//Else: Mark it as neighbor and increment vectors counter.
		boolean isClose = false;
		for (ClusterCenter center : centers) {
			double distance = DistanceMeasurer.measureDistance(center, newVector);
			if ( distance<= DistanceMeasurer.T1 ) {
				isClose = true;    				 
				if(distance > DistanceMeasurer.T2) {
					context.getCounter(Counter.NUMBER_OF_VECTORS).increment(1);
					center.setNeighbors(center.getNeighbors()+1);
				}
				break;
			}
		}
		if (!isClose) {
			context.getCounter(Counter.NUMBER_OF_VECTORS).increment(1);
		   	ClusterCenter center = new ClusterCenter(newVector);
		    centers.add(center);
		    center.setNeighbors(center.getNeighbors()+1);
		}
	}
	@Override
	//Finally send to one Reducer each time (Number of vectors,Canopy Cluster Center)
	protected void cleanup(Context context) throws IOException,InterruptedException {
		super.cleanup(context);
		for (ClusterCenter center : centers)
		{
			
			context.write(new IntWritable(1),center);
		}			
		
	}
}