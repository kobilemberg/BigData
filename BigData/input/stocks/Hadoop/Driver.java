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
	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		
		
		
		//Read properties from windows
		String rootFolder = args[0];
		LOG.info("rootFOlder-->"+ rootFolder);
		
		
		HadoopProperties properties = new HadoopProperties();
		try {
			System.out.println(rootFolder+"/HadoopProperties.xml");
			XMLDecoder decoder=new XMLDecoder(new BufferedInputStream(new FileInputStream(rootFolder+"/HadoopProperties.xml")));
			properties = ((HadoopProperties)decoder.readObject());
			decoder.close();
			System.out.println("Num Of stocks: " + properties.getNumOfStocks());
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File "+rootFolder+"/HadoopProperties.xml not found");
		}
		
		
		//Configure data path
		String vectorsCSV = properties.getJobServerInputFolderPath()+"/vectors";
		System.out.println("Vector path:" + vectorsCSV);
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
		System.out.println("*******Finished Canopy Algorithm*********");

		System.out.println("****************Starting with Centroids calculation*************");
		//Creating random object in order to calculate Centroids of each ClusterCenter by his members ratio
		Random rand = new Random();
		int totalClustersNum=properties.getNumOfClusters();
			
		//HashMap that will hold for each cluster center his number of Centroids to calculate
		HashMap<ClusterCenter,Integer> canopyDivisionOfCentroidsMap = new HashMap<ClusterCenter,Integer>();
		
		//Need to fix it getting the path of canopyCenters
		//Reading Canopy output in order to set them Centroids by data division.
		Path canopyPath = new Path(rootFolder+ "/files/CanopyClusterCenters/canopyCenters.seq");
		long totalVectorsToDivine = canopyJob.getCounters().findCounter(CanopyMapper.Counter.NUMBER_OF_VECTORS).getValue();
		//Reading the canopy Path with how much neighbors/All he have
		SequenceFile.Reader canopyCentersReader = new SequenceFile.Reader(fs, canopyPath, jobConfigurations);
		ClusterCenter centerReader ;
		IntWritable neighbors ;
		int sum = 0; //Summing all Centroids number for each Canopy Center in order to avoid mistakes.
		
		while (canopyCentersReader.next(centerReader = new ClusterCenter() , neighbors = new IntWritable())) {
			
			System.out.println("This Canopy Cluster Center has "+neighbors.get());
			
			Double value_of_vectors_for_neighbors = new Double(neighbors.get())/totalVectorsToDivine;
			System.out.println("value_of_vectors_for_neighbors:"+value_of_vectors_for_neighbors);
			Double value_of_vector_divided_by_stocks = value_of_vectors_for_neighbors*totalClustersNum;
			System.out.println("value_of_vector_divided_by_stocks:"+value_of_vector_divided_by_stocks);
			//According to instructions each Cluster Center will get Minimum 2 centroids in order to save KMeans Iterations
			if(value_of_vector_divided_by_stocks>=1)
			{
					canopyDivisionOfCentroidsMap.put(centerReader, (value_of_vector_divided_by_stocks).intValue());
					sum = sum + (value_of_vector_divided_by_stocks).intValue();
					System.out.println("Setted k to: "+canopyDivisionOfCentroidsMap.get(centerReader));
			}
			else
				System.out.println("Less then 1: " + value_of_vector_divided_by_stocks);
			
		}
		canopyCentersReader.close();
		System.out.println("NUmber of Total Centroids that will be calaulated is: "+sum);
		
		//If number of total Centroids that will be calculated is less then user request, sort the array and add the differences in order to get balance
		if(sum<totalClustersNum) {
			
			ArrayList<Integer> KArr = new ArrayList<Integer>(canopyDivisionOfCentroidsMap.values());
			Collections.sort(KArr);
			Integer[] sortedK = KArr.toArray(new Integer[]{});
			//Integer[] sortedK = Collections.sort((Integer[])((canopyDivisionOfCentroidsMap.values()).toArray(new Integer[]{})));
			for(ClusterCenter clusterCenter : canopyDivisionOfCentroidsMap.keySet()) {
				if(canopyDivisionOfCentroidsMap.get(clusterCenter)==sortedK[0]) {
					canopyDivisionOfCentroidsMap.put(clusterCenter,sortedK[0]+(totalClustersNum-sum));
					break;
				}
			}
		}	
		System.out.println("***************Number of Centroids for each Canopy Center to calculate before Handele in 0,1 cases**************");
		System.out.println("Total Centroids=:"+totalClustersNum);
		int t=0;
		for(ClusterCenter clusterCenter : canopyDivisionOfCentroidsMap.keySet())
		{
			System.out.println("Canopy number: "+t);
			System.out.println("Number of Centroids that will be calculated is: "+canopyDivisionOfCentroidsMap.get(clusterCenter));
			t++;
		}
		System.out.println("***********************************************Done************************************************************");
		System.out.println("Finish with reading output file from Canopy reducer. Starting normalize k");
		
		//Check if there is Canopy with k==1/0. if there is, take from someone that have 3 or  more.
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
		
		System.out.println("***************Number of Centroids for each Canopy Center to calculate After Handele in 0,1 cases**************");
		System.out.println("Total Centroids=:"+totalClustersNum);
		t=0;
		for(ClusterCenter clusterCenter : canopyDivisionOfCentroidsMap.keySet())
		{
			System.out.println("Canopy number: "+t);
			System.out.println("Number of Centroids that will be calculated is: "+canopyDivisionOfCentroidsMap.get(clusterCenter));
			t++;
		}
		System.out.println("***********************************************Done************************************************************");
		
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
		
		System.out.println("**************************************Main: Adding to Kmeans File:***********************************************");
		Path centerToCentroidTuple = new Path(rootFolder+ "/files/KmeansCentroids/centerCentroidTuple.seq");
		final SequenceFile.Writer tupleCenterCentroid = SequenceFile.createWriter(fs,jobConfigurations, centerToCentroidTuple, CenterCentroidWritableComparable.class, IntWritable.class);
		for (CenterCentroidWritableComparable ccw : centerCentroidArrayList) {
			System.out.println(ccw);
			tupleCenterCentroid.append(ccw, new IntWritable(1));
		}
		tupleCenterCentroid.close();
		System.out.println("**************************************Done adding to KmeansFile*************************************************");
		
		System.out.println("**************************************Validation tests after Kmeans file insertion******************************");
		ArrayList<CenterCentroidWritableComparable> validationList = new ArrayList<CenterCentroidWritableComparable>();
		SequenceFile.Reader canopyCentroidtupleReader = new SequenceFile.Reader(fs, centerToCentroidTuple,jobConfigurations);
		CenterCentroidWritableComparable key = new CenterCentroidWritableComparable();
		IntWritable value = new IntWritable();
		while (canopyCentroidtupleReader.next(key, value)) {
			System.out.println(key);
			validationList.add(new CenterCentroidWritableComparable(key));
			if(!centerCentroidArrayList.contains(key))
				System.out.println("It seems that reading or writing proccess of your CenterCentroidWritableComparable is bad!");
		}
		canopyCentroidtupleReader.close();
		System.out.println("******************************************Done validate adding to KmeansFile*************************************");
		System.out.println("******************************************Starting to validate validationList************************************");
		for(CenterCentroidWritableComparable c:validationList)
		{
			System.out.println(c);
		}
		System.out.println("**********************************************Done validate validationList***************************************");
		
		System.out.println("**************************************************Main: Starting Kmeans******************************************");
		int iteration = 1;
		jobConfigurations.set("num.iteration", iteration + "");
		jobConfigurations.set("rootFolder", rootFolder);
		//output path for depth1
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
		if(counter>7)
		{
			System.out.println("Counter:"+counter);
			System.out.println("Error! look on last job.");
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
			if(counter>7) {
				System.out.println("Counter:"+counter);
				System.out.println("Error! look on last job.");
				System.exit(1);
			}
		}
		//Running final itteration with KmeansMapper and special reducer that will write the file.
		jobConfigurations.set("CanopyCentroidTuple.path", centerToCentroidTuple.toString());
		jobConfigurations.set("num.iteration", iteration + "");
		jobConfigurations.set("rootFolder", rootFolder);
		job = new Job(jobConfigurations);
		job.setJobName("KMeans Clustering " + iteration);

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

