package solution;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;


//for commit

public class Driver {
	
	
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(Driver.class);
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException,InterruptedException, ClassNotFoundException {
		
		String rootFolder = args[0];
		LOG.info("rootFolder-->"+ rootFolder);
		
		//Read properties from windows
		HadoopProperties properties = new HadoopProperties();
		try {
			System.out.println(rootFolder+"/HadoopProperties.xml");
			XMLDecoder decoder=new XMLDecoder(new BufferedInputStream(new FileInputStream(rootFolder+"/HadoopProperties.xml")));
			properties = ((HadoopProperties)decoder.readObject());
			decoder.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File "+rootFolder+"/HadoopProperties.xml not found");
		}
		
		
		//Configure data path
		String vectorsCSV = properties.getJobServerInputFolderPath()+"/vectors";
		Path vectorsCSVPath = new Path(vectorsCSV);
		
		//setting the output from canopy algorithm
		Path canopyOutputPath = new Path(properties.getJobServerInputFolderPath()+"/canopyOutput");
		Configuration jobConfigurations = new Configuration();
		FileSystem fs = FileSystem.get(jobConfigurations);
		
		if (fs.exists(canopyOutputPath)){fs.delete(canopyOutputPath, true);}

		
		
		//Removing output file if it already exists
		
		//Running Canopy Clustering Job
		Job canopyJob = new Job(jobConfigurations);
		canopyJob.getConfiguration().set("rootFolder",rootFolder);
		//Specify the jar file that contains Driver, Mapper and Reducer.
		canopyJob.setJarByClass(CanopyMapper.class);
		    
		//Configure Canopy Clustering Job name
		canopyJob.setJobName("Canopy Clustering");

		//Specify the paths to the input and output data based on the
		FileInputFormat.setInputPaths(canopyJob, vectorsCSVPath);
		FileOutputFormat.setOutputPath(canopyJob,canopyOutputPath);

		//Specify the Mapper and Reducer classes.
		canopyJob.setMapperClass(CanopyMapper.class);
		canopyJob.setReducerClass(CanopyReducer.class);
		canopyJob.setMapOutputKeyClass(IntWritable.class);
		canopyJob.setMapOutputValueClass(ClusterCenter.class);
		    
		//Specify the job's output key and value classes. 
		canopyJob.setOutputKeyClass(ClusterCenter.class);
		canopyJob.setOutputValueClass(DoubleWritable.class);
		 
		@SuppressWarnings("unused")
		boolean success = canopyJob.waitForCompletion(true);
		//*****************Finish with Canopy, Starting calculate Centroids for each Cluster Center**********//
		
		//Creating random object in order to calculate Centroids of each ClusterCenter by his members ratio
		Random rand = new Random();
		int totalClustersNum=properties.getNumOfClusters();
			
		//HashMap that will hold for each cluster center his number of Centroids to calculate
		HashMap<ClusterCenter,Integer> canopyDivisionOfCentroidsMap = new HashMap<ClusterCenter,Integer>();
		
		//Reading Canopy output in order to set them Centroids by data division.
		Path canopyPath = new Path(rootFolder+ "/files/CanopyClusterCenters/canopyCenters.seq");
		long totalVectorsToDivine = canopyJob.getCounters().findCounter(CanopyMapper.Counter.NUMBER_OF_VECTORS).getValue();
		//Reading the canopy Path with how much neighbors/All he have
		SequenceFile.Reader canopyCentersReader = new SequenceFile.Reader(fs, canopyPath, jobConfigurations);
		ClusterCenter centerReader ;
		IntWritable neighbors ;
		int sum = 0; //Summing all Centroids number for each Canopy Center in order to avoid mistakes.
		
		while (canopyCentersReader.next(centerReader = new ClusterCenter() , neighbors = new IntWritable())) {			
			Double value_of_vectors_for_neighbors = new Double(neighbors.get())/totalVectorsToDivine;
			Double value_of_vector_divided_by_stocks = value_of_vectors_for_neighbors*totalClustersNum;
			//Removing Centers with low amount of data
			if(value_of_vector_divided_by_stocks>=1)
			{
					canopyDivisionOfCentroidsMap.put(centerReader, (value_of_vector_divided_by_stocks).intValue());
					sum = sum + (value_of_vector_divided_by_stocks).intValue();
			}
			else
				System.out.println("Removed: " + value_of_vector_divided_by_stocks);
		}
		canopyCentersReader.close();
		
		//If number of total Centroids that will be calculated is less then user request, sort the array and add the differences in order to get balance
		if(sum<totalClustersNum) {
			ArrayList<Integer> KArr = new ArrayList<Integer>(canopyDivisionOfCentroidsMap.values());
			Collections.sort(KArr);
			Integer[] sortedK = KArr.toArray(new Integer[]{});
			for(ClusterCenter clusterCenter : canopyDivisionOfCentroidsMap.keySet()) {
				if(canopyDivisionOfCentroidsMap.get(clusterCenter)==sortedK[0]) {
					canopyDivisionOfCentroidsMap.put(clusterCenter,sortedK[0]+(totalClustersNum-sum));
					break;
				}
			}
		}	
		if(totalClustersNum>3)
		{
			//Check if there is Canopy with k==1. if there is, take from someone that have 3 or  more.
			while (canopyDivisionOfCentroidsMap.containsValue(1)){
				for (Entry<ClusterCenter, Integer> entrySet : canopyDivisionOfCentroidsMap.entrySet()) {
					Integer value = entrySet.getValue();
					if (value==1||value==0){
						for (ClusterCenter canopyCenter : canopyDivisionOfCentroidsMap.keySet()){
							if (canopyDivisionOfCentroidsMap.get(canopyCenter)>=3 || canopyDivisionOfCentroidsMap.get(canopyCenter)>=3.0 ){
								canopyDivisionOfCentroidsMap.put(canopyCenter, canopyDivisionOfCentroidsMap.get(canopyCenter)-1);
								canopyDivisionOfCentroidsMap.put(entrySet.getKey(), entrySet.getValue()+1);
							}
						}
					}
				}
			}
		}
	
		//Check if still we have Canopies with k=1, if so remove them from list
		int kToAdd=0;
		ArrayList<ClusterCenter> centersToRemove = new ArrayList<ClusterCenter>();
			for(ClusterCenter canopyCenter: canopyDivisionOfCentroidsMap)
			{
				if(canopyDivisionOfCentroidsMap.get(canopyCenter)==1)
				{
					centersToRemove.add(canopyCenter);
				}
			}
		
		
		for(ClusterCenter canopyCenter: centersToRemove)
		{
			kToAdd++;
			canopyDivisionOfCentroidsMap.remove(canopyCenter);
		}
		
		if(kToAdd>0) {
			
			ArrayList<Integer> KArr = new ArrayList<Integer>(canopyDivisionOfCentroidsMap.values());
			Collections.sort(KArr);
			Integer[] sortedK = KArr.toArray(new Integer[]{});
			for(ClusterCenter clusterCenter : canopyDivisionOfCentroidsMap.keySet()) {
				if(canopyDivisionOfCentroidsMap.get(clusterCenter)==sortedK[0]) {
					canopyDivisionOfCentroidsMap.put(clusterCenter,sortedK[0]+kToAdd);
					
				}
			}
			
		}	
		
		
		
		//Create arrayList of of CenterCentroidWritableComparable (tuple combined from CanopyCluster Center and Centroid)
		ArrayList<CenterCentroidWritableComparable> centerCentroidArrayList = new ArrayList<CenterCentroidWritableComparable>();
		int centroidID=0; //Set name of Centroid.
		for (Entry<ClusterCenter, Integer> entrySet : canopyDivisionOfCentroidsMap.entrySet())
		{
			//For each CLuster Center Create Centroids that Close enough to him( distance form Cluster Center to its Centroid is less then T1)
			int centroidNumber = entrySet.getValue().intValue();
				for (int i = 0; i < centroidNumber; i++) {
					int centroidVectorSize = entrySet.getKey().getCenter().getVectorArr().length;
					double[] centroidvectorArr = new double [centroidVectorSize];
					for (int j = 0; j < centroidVectorSize; j++) {
					    double end = DistanceMeasurer.T1/centroidVectorSize/10;
					    double random = rand.nextDouble();
					    double result = random * (end);
					    double feature =  result + entrySet.getKey().getCenter().getVectorArr()[j];
						centroidvectorArr[j] = feature;
					}
					Vector centroid = new Vector(centroidID+"",centroidvectorArr);
					centroidID++;
					CenterCentroidWritableComparable newCenterCentroid = new CenterCentroidWritableComparable(entrySet.getKey(),new ClusterCenter(centroid));
					centerCentroidArrayList.add(newCenterCentroid);
				}
		}
		//Writing the list to file in oreder to have access from KMeans job.
		Path centerToCentroidTuple = new Path(rootFolder+ "/files/KmeansCentroids/centerCentroidTuple.seq");
		final SequenceFile.Writer tupleCenterCentroid = SequenceFile.createWriter(fs,jobConfigurations, centerToCentroidTuple, CenterCentroidWritableComparable.class, IntWritable.class);
		for (CenterCentroidWritableComparable ccw : centerCentroidArrayList) {
			System.out.println(ccw);
			tupleCenterCentroid.append(ccw, new IntWritable(1));
		}
		tupleCenterCentroid.close();
		
		//Starting to run the first KMeans job
		int iteration = 1;
		jobConfigurations.set("num.iteration", iteration + "");
		jobConfigurations.set("rootFolder", rootFolder);
		Job job = new Job(jobConfigurations);
		job.setJobName("KMeans Clustering");
		job.setMapperClass(KMeansMapper.class);
		job.setReducerClass(KMeansReducer.class);
		job.setJarByClass(KMeansMapper.class);
		Path out = new Path(rootFolder+ "/files/clustering/depth_1");
		if (fs.exists(out))
			fs.delete(out, true);

	    FileInputFormat.setInputPaths(job, vectorsCSVPath);
		SequenceFileOutputFormat.setOutputPath(job, out);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setMapOutputKeyClass(CenterCentroidWritableComparable.class);
		job.setMapOutputValueClass(Vector.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.waitForCompletion(true);
		long counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
		
		if(counter>totalClustersNum)
		{
			System.out.println("Warning: totalClustersNum: "+totalClustersNum+", Counter:"+counter);
			System.exit(1);
		}
		iteration++;
		while (counter > 0) {
			jobConfigurations.set("CanopyCentroidTuple.path", centerToCentroidTuple.toString());
			jobConfigurations.set("num.iteration", iteration + "");
			jobConfigurations.set("rootFolder", rootFolder);
			job = new Job(jobConfigurations);
			job.setJobName("KMeans Clustering " + iteration);
			job.setMapperClass(KMeansMapper.class);
			job.setReducerClass(KMeansReducer.class);
			job.setJarByClass(KMeansMapper.class);
			out = new Path(rootFolder+ "/files/clustering/depth_" + iteration);

			SequenceFileInputFormat.addInputPath(job, vectorsCSVPath);
			if (fs.exists(out))
				fs.delete(out, true);
			
			FileOutputFormat.setOutputPath(job, out);
			job.setMapOutputKeyClass(CenterCentroidWritableComparable.class);
			job.setMapOutputValueClass(Vector.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			job.waitForCompletion(true);
			iteration++;
			counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
			if(counter>totalClustersNum)
			{
				System.out.println("Warning: totalClustersNum: "+totalClustersNum+", Counter:"+counter);
				System.exit(1);
			}
		}
		//Running final itteration with KmeansMapper and special reducer that will write the file.
		jobConfigurations.set("CanopyCentroidTuple.path", centerToCentroidTuple.toString());
		jobConfigurations.set("num.iteration", iteration + "");
		jobConfigurations.set("rootFolder", rootFolder);
		job = new Job(jobConfigurations);
		job.setJobName("KMeans Clustering " + iteration+" - Final");
		job.setMapperClass(KMeansMapper.class);
		job.setReducerClass(FinalKMeansReducer.class);
		job.setJarByClass(KMeansMapper.class);
		out = new Path(properties.getJobServerOutputFolderPath());

		SequenceFileInputFormat.addInputPath(job, vectorsCSVPath);
		if (fs.exists(out))
			fs.delete(out, true);
		
		FileOutputFormat.setOutputPath(job, out);
		job.setMapOutputKeyClass(CenterCentroidWritableComparable.class);
		job.setMapOutputValueClass(Vector.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.waitForCompletion(true);
	}
			
}

