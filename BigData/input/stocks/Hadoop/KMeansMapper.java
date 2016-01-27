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
		System.out.println(rootFolder);
		Path canopyCentroidTuplePath = new Path(rootFolder+ "/files/KmeansCentroids/centerCentroidTuple.seq");
		@SuppressWarnings("deprecation")
		SequenceFile.Reader canopyCentroidtupleReader = new SequenceFile.Reader(fs, canopyCentroidTuplePath,conf);
		CenterCentroidWritableComparable key = new CenterCentroidWritableComparable();
		IntWritable value = new IntWritable();
		System.out.println("***************************Kmeans mapper******************8");
		while (canopyCentroidtupleReader.next(key, value)) {
			System.out.println("When reading");
			System.out.println(key);
			CenterCentroidArr.add(new CenterCentroidWritableComparable(key));
		}
		
		for(CenterCentroidWritableComparable tuple:CenterCentroidArr) {
			if(canopyToCentroidsMap.containsKey(tuple.getCenter())) {
				System.out.println("canopyToCentroidsMap contained tuple center");
				canopyToCentroidsMap.get(tuple.getCenter()).add(tuple.getCentroid());
			}
			else {
				System.out.println("canopyToCentroidsMap did not contains tuple center");
				ArrayList<ClusterCenter> centroids = new ArrayList<ClusterCenter>();
				centroids.add(tuple.getCentroid());
				canopyToCentroidsMap.put(tuple.getCenter(), centroids);
			}
		}
		canopyCentroidtupleReader.close();
		System.out.println("******************Kmeans Mapper: Validating reader***********");
		for(CenterCentroidWritableComparable c:CenterCentroidArr) {
			System.out.println(c);
		}
		System.out.println("******************Kmeans Mapper:Done with Validating reader***********");
		System.out.println("******************Kmeans Mapper:Validating canopyToCentroidsMap***********");
		System.out.println("Validate canopyToCentroidsMap has 2 keys: "+ canopyToCentroidsMap.keySet().size());
		for(ClusterCenter clusterCenterKey:canopyToCentroidsMap.keySet()) {
			System.out.println("For cluster Center: " +clusterCenterKey);
			System.out.println("\t-----------------------------------");
			for(ClusterCenter centroid: canopyToCentroidsMap.get(clusterCenterKey)) {
				System.out.println("\t Centroid:");
				System.out.println("\t "+centroid);
			}
			System.out.println("\t-----------------------------------");
		}
		System.out.println("******************Kmeans Mapper:Done Validating canopyToCentroidsMap***********");
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
		if(!CenterCentroidArr.contains(keyToSend)) {
			System.out.println("****************************************************");
			System.out.println("********************Logical error 1!****************");
			System.out.println("****************************************************");
			System.exit(1);
		}
		if(!canopyToCentroidsMap.containsKey(keyToSend.getCenter())) {
			System.out.println("****************************************************");
			System.out.println("**********************Logical error 2!**************");
			System.out.println("****************************************************");
			System.exit(1);
		}
		if(canopyToCentroidsMap.containsKey(keyToSend.getCenter())) {
			if(!(canopyToCentroidsMap.get(keyToSend.getCenter()).contains(keyToSend.getCentroid()))) {
				System.out.println("****************************************************");
				System.out.println("********************Logical error 3!****************");
				System.out.println("****************************************************");
				System.exit(1);
			}
		}
		context.write(keyToSend,newVector);
	}
}
