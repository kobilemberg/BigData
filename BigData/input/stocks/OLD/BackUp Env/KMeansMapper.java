package solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

//for commit

public class KMeansMapper extends Mapper<LongWritable,Text, CenterCentroidWritableComparable, Vector> {	 
	
	//ArrayList that will hold all pairs (Canopy Cluster Center,KMeans Centroid)
	ArrayList<CenterCentroidWritableComparable> CenterCentroidArr = new ArrayList<CenterCentroidWritableComparable>();
	//HashMap that will hold all Canopy Cluster Centers as key set, Value for each key will be its owned KMeans Centroids.
	HashMap<ClusterCenter,ArrayList<ClusterCenter>> canopyToCentroidsMap = new HashMap<ClusterCenter, ArrayList<ClusterCenter>>();
    String rootFolder;
	@Override
	protected void setup(Context context) throws IOException,InterruptedException {
		super.setup(context);
		
		//Read last job results and add results to data structures.
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);
		rootFolder = context.getConfiguration().get("rootFolder");
		Path canopyCentroidTuplePath = new Path(rootFolder+ "/files/KmeansCentroids/centerCentroidTuple.seq");
		@SuppressWarnings("deprecation")
		SequenceFile.Reader canopyCentroidtupleReader = new SequenceFile.Reader(fs, canopyCentroidTuplePath,conf);
		CenterCentroidWritableComparable key = new CenterCentroidWritableComparable();
		IntWritable value = new IntWritable();
		
		//Adding the tuples to list 
		while (canopyCentroidtupleReader.next(key, value)) {
			CenterCentroidArr.add(new CenterCentroidWritableComparable(key));
		}
		
		//Adding the Centroids to hashmap with the right key.
		for(CenterCentroidWritableComparable tuple:CenterCentroidArr) {
			if(canopyToCentroidsMap.containsKey(tuple.getCenter())) {
				canopyToCentroidsMap.get(tuple.getCenter()).add(tuple.getCentroid());
			}
			else {
				ArrayList<ClusterCenter> centroids = new ArrayList<ClusterCenter>();
				centroids.add(tuple.getCentroid());
				canopyToCentroidsMap.put(tuple.getCenter(), centroids);
			}
		}
		canopyCentroidtupleReader.close();
	}
	

	@Override
	protected void map(LongWritable key,Text value, Context context)throws IOException, InterruptedException {
		//Read the line, split tha data and build a vector
		String[] data  = value.toString().split(",");
		String name = data[0];
		double[] vectorArr = new double[data.length-1];
		for (int i = 0; i < vectorArr.length; i++) {
			vectorArr[i] = new Double(data[i+1].substring(1, data[i+1].length()-1));
		}				
		Vector newVector = new Vector(name.substring(1, name.length()-1), vectorArr);
		
		//Find nearest Canopy Cluster Center to this vector
		ClusterCenter nearestCenter=null;
		Double distanceFromCanopy =Double.MAX_VALUE;		
		for (ClusterCenter canopyCenter : canopyToCentroidsMap.keySet()) {
				if(nearestCenter==null) {
					//For first time.
					nearestCenter = canopyCenter;
					distanceFromCanopy = DistanceMeasurer.measureDistance(canopyCenter, newVector);
				}
				else if (distanceFromCanopy>DistanceMeasurer.measureDistance(canopyCenter, newVector)) {
					//Reaplace the value
					nearestCenter = canopyCenter;
					distanceFromCanopy = DistanceMeasurer.measureDistance(canopyCenter, newVector);
				}	
		}
		
		//Pass through all KMeans Centroid's and find the closest one to emit for reducing.
		ClusterCenter nearestCentroid=null;
		Double distanceFromCentroid =Double.MAX_VALUE;	
		for (ClusterCenter centroid : canopyToCentroidsMap.get(nearestCenter)) {
				if(nearestCentroid==null) {
					nearestCentroid = centroid;
					distanceFromCentroid = DistanceMeasurer.measureDistance(centroid, newVector);
				}
				else if (distanceFromCentroid>DistanceMeasurer.measureDistance(centroid, newVector) && distanceFromCentroid>DistanceMeasurer.T2) {
					nearestCentroid = centroid;
					distanceFromCentroid = DistanceMeasurer.measureDistance(centroid, newVector);
				}			
		}
		
		CenterCentroidWritableComparable keyToSend = new CenterCentroidWritableComparable(nearestCenter, nearestCentroid);
		context.write(keyToSend,newVector);
	}
}
