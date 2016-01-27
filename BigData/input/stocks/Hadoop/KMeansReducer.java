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
		System.out.println("\n***************************Kmeans reducer Setup**************************************");
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
			System.out.println("before adding to old Vectors:");
			System.out.println(key);
			oldVector.add(new CenterCentroidWritableComparable(key));
			oldVectorToUpdate.add(new CenterCentroidWritableComparable(key));
		}
		canopyCentroidtupleReader.close();
		System.out.println("Size of oldVector tuple centroid list: "+oldVector.size() );
		System.out.println("Validate that oldVector contains all tuples:");
		for(CenterCentroidWritableComparable oldCentroid: oldVector) {
			System.out.println(oldCentroid);
		}
		System.out.println("Done with oldvectors");
		System.out.println("****************EOKmeans reducer Setup**************************************************8");
	}

	@Override
	protected void reduce(CenterCentroidWritableComparable key, Iterable<Vector> values,Context context) throws IOException, InterruptedException {
		System.out.println("********************************8KmeansReducer - reduce*******************");
		System.out.println("---------------------------------");
		System.out.println("Key in reducer");
		System.out.println(key);
		System.out.println("---------------------------------");
		//Build new vector as AVG from values
		ClusterCenter centroidToUpdate = key.getCentroid();
		System.out.println("Now working on");
		System.out.println("tuple:"+centroidToUpdate);
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
	
		System.out.println("\n\n");
		//Build new Centroid with the vector and inset new Centroid to the HashMap (with old one)
		ClusterCenter comparisonCentroid = new ClusterCenter(centroidToUpdate);//Build
		comparisonCentroid.getCenter().setVectorArr(newVectorArr);//Set vector Arr
		CenterCentroidWritableComparable comparisonTuple = new CenterCentroidWritableComparable(key.getCenter(),comparisonCentroid);
		
		System.out.println("\nComparison Centroid:\n"+comparisonCentroid);
		System.out.println("old Centroid()\n"+key.getCentroid());
		//System.out.println("Is key==null?: "+key==null);
		//System.out.println("Is comparisonTuple==null?: "+comparisonTuple==null);
		//System.out.println("Is comparisonCentroid");
		
		System.out.println("Validate HashMap insetion:");
		System.out.println("old tuple:"+key);
		System.out.println("is inserted to hashMap:" + comparisonMap.containsKey(key));
		System.out.println("value in HashMap:" + comparisonMap.get(key));
		System.out.println("ComparisonMap size before : "+comparisonMap.size());
		comparisonMap.put(new CenterCentroidWritableComparable(key), comparisonTuple);
		if (centroidToUpdate.converged(comparisonCentroid)) {
			//Check that the is changes need to be done - Vectors are not close yet
			System.out.println("Checking the distance");
			System.out.println("Convereged!");
			context.getCounter(Counter.CONVERGED).increment(1);
		}
		System.out.println("ComparisonMap size After : "+comparisonMap.size());
		context.write(new Text(comparisonCentroid.getCenter().getName().toString()), new Text(comparisonCentroid.toString()));
	}

	@SuppressWarnings({ "deprecation", "resource" })
	@Override
	protected void cleanup(Context context) throws IOException,InterruptedException {
		//Go over updated vector list, find the updated vector from the HashMap, find the index of the new updated vector,
		//Swap old vector with new one
		//Write to file.
		super.cleanup(context);
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);
		//Path outPath = new Path("files/KmeansCentroids/centerCentroidTuple.seq");

		System.out.println("\n***************************KmeansReducerCleanUp**************************************");
		System.out.println("Size of old centroid list: "+oldVector.size() );
		System.out.println("Old Centroid before update:");
		for(CenterCentroidWritableComparable oldCentroid: oldVector) {
			System.out.println(oldCentroid);
		}
		
		System.out.println("\n Updating oldVectors");
		System.out.println("**********************************************************************");
		System.out.println("HashMap with size:"+comparisonMap.size());
		
		for(CenterCentroidWritableComparable c:comparisonMap.keySet()) {
			System.out.println("C as key:"+c.getCentroid());
			System.out.println("C as value"+comparisonMap.get(c).getCentroid());
		}
		for(CenterCentroidWritableComparable oldCentroid: oldVector) {
			System.out.println("oldCentroid:"+oldCentroid.getCentroid());
			System.out.println("**********************************************************************");
			System.out.println("***********************Validating old vector update*******************");
			if(comparisonMap.containsKey(oldCentroid)) {
				
				int index = oldVectorToUpdate.indexOf(oldCentroid);
				System.out.println("Updating old vector in index: "+index);
				System.out.println("Validate indexes are equlas:"+ (index == oldVector.indexOf(oldCentroid)));
				System.out.println("oldCentroid:"+oldCentroid);
				System.out.println("oldCentroid in old vector:"+oldVector.get(index));
				oldVectorToUpdate.remove(index);
				oldVectorToUpdate.add(index, comparisonMap.get(oldCentroid));
				System.out.println("New Centroid in index:"+index);
				System.out.println(oldVectorToUpdate.get(index));
			}			
		}
		System.out.println("\nSize of old centroid list after update: "+oldVector.size());
		System.out.println("Old Centroid after update:");
		comparisonMap.clear();
		for(CenterCentroidWritableComparable oldCentroid: oldVectorToUpdate) {
			System.out.println(oldCentroid.getCentroid());
		}
		System.out.println("\n Updating oldVectors");
		final SequenceFile.Writer out = SequenceFile.createWriter(fs,context.getConfiguration(), outPath, CenterCentroidWritableComparable.class,IntWritable.class);		
		IntWritable value = new IntWritable(0);
		for (CenterCentroidWritableComparable center : oldVectorToUpdate) {
			out.append(center, value);
		}
		out.close();
		
		System.out.println("************************Finished reducer**********************8");
		System.out.println("Validate file is equal to old vector to update");
		
		SequenceFile.Reader canopyCentroidtupleReader = new SequenceFile.Reader(fs, outPath,conf);
		CenterCentroidWritableComparable key = new CenterCentroidWritableComparable();
		value = new IntWritable();
		while (canopyCentroidtupleReader.next(key , value)) {
			System.out.println("from file:"+key);
		}
		System.out.println("oldVectorsToUpdate");
		for(CenterCentroidWritableComparable c: oldVectorToUpdate){
			System.out.println(c);
		}
	}
}
