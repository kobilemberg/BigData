package solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class KMeansReducer extends Reducer<CenterCentroidWritableComparable, Vector, Text, Text> {
	
	
	Path outPath ;
	String rootFolder;
	//ArrayList with Tuples that should assist to update result file for next iteration.
	ArrayList<CenterCentroidWritableComparable> oldVectorToUpdate = new ArrayList<CenterCentroidWritableComparable>();
	//HashMap for comparison between old tuple (old KMeans Centroid) and new Tuple (ne wKMeans Centroid), same Canopy Claster Center
	HashMap<CenterCentroidWritableComparable, CenterCentroidWritableComparable> comparisonMap= new HashMap<CenterCentroidWritableComparable, CenterCentroidWritableComparable>();
	//ArrayList with Tuples that will hold latest iteration results.
	ArrayList<CenterCentroidWritableComparable> oldVector = new ArrayList<CenterCentroidWritableComparable>();
	public static enum Counter {
		CONVERGED
	}
	
	protected void setup(Context context) throws IOException,InterruptedException {
		//Read the file and insert the data to list
		super.setup(context);
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);
		rootFolder  = context.getConfiguration().get("rootFolder");
		outPath = new Path(rootFolder+ "/files/KmeansCentroids/centerCentroidTuple.seq");
		@SuppressWarnings("deprecation")
		SequenceFile.Reader canopyCentroidtupleReader = new SequenceFile.Reader(fs, outPath,conf);
		CenterCentroidWritableComparable key = new CenterCentroidWritableComparable();
		IntWritable value = new IntWritable();
		while (canopyCentroidtupleReader.next(key , value)) {
			oldVector.add(new CenterCentroidWritableComparable(key));
			oldVectorToUpdate.add(new CenterCentroidWritableComparable(key));
		}
		canopyCentroidtupleReader.close();
	}

	@Override
	protected void reduce(CenterCentroidWritableComparable key, Iterable<Vector> values,Context context) throws IOException, InterruptedException {
		//Build new vector as AVG from values
		ClusterCenter centroidToUpdate = key.getCentroid();
		double[] newVectorArr = new double[centroidToUpdate.getCenter().getVectorArr().length];
		int numOfVectors = 0;
		for(Vector v:values) {
			numOfVectors++;
			for (int i = 0; i < newVectorArr.length; i++) {
				newVectorArr[i] += v.getVectorArr()[i];
			}
		}
		for (int i = 0; i < newVectorArr.length; i++) {
			newVectorArr[i] = newVectorArr[i]/numOfVectors;
		}
	
		//Build new Centroid with the vector and inset new Centroid to the HashMap (with old one)
		ClusterCenter comparisonCentroid = new ClusterCenter(centroidToUpdate);//Build
		comparisonCentroid.getCenter().setVectorArr(newVectorArr);//Set vector Arr
		CenterCentroidWritableComparable comparisonTuple = new CenterCentroidWritableComparable(key.getCenter(),comparisonCentroid);
		comparisonMap.put(new CenterCentroidWritableComparable(key), comparisonTuple);
		
		if (centroidToUpdate.converged(comparisonCentroid)) {
			//Check that the is changes need to be done - Vectors are not close yet
			context.getCounter(Counter.CONVERGED).increment(1);
		}
		//context.write(new Text(comparisonCentroid.getCenter().getName().toString()), new Text(comparisonCentroid.toString()));
	}

	@SuppressWarnings({ "deprecation"})
	@Override
	protected void cleanup(Context context) throws IOException,InterruptedException {
		//Go over updated vector list, find the updated vector from the HashMap, find the index of the new updated vector,
		//Swap old vector with new one
		//Write to file.
		super.cleanup(context);
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);

		for(CenterCentroidWritableComparable oldCentroid: oldVector) {

			if(comparisonMap.containsKey(oldCentroid)) {
				
				int index = oldVectorToUpdate.indexOf(oldCentroid);
				oldVectorToUpdate.remove(index);
				oldVectorToUpdate.add(index, comparisonMap.get(oldCentroid));

			}			
		}
		comparisonMap.clear();

		//Updating old Centroids
		final SequenceFile.Writer out = SequenceFile.createWriter(fs,context.getConfiguration(), outPath, CenterCentroidWritableComparable.class,IntWritable.class);		
		IntWritable value = new IntWritable(0);
		for (CenterCentroidWritableComparable center : oldVectorToUpdate) {
			out.append(center, value);
		}
		out.close();
	}
}
