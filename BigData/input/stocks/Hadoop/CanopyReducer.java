package solution;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Reducer;

//for commit

// calculate a new clustercenter for these vertices
public class CanopyReducer extends Reducer<IntWritable,ClusterCenter,ClusterCenter, DoubleWritable> {

	List<ClusterCenter> canopyClusterCenters;
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		System.out.println("Setup");
		super.setup(context);
		this.canopyClusterCenters = new LinkedList<ClusterCenter>();
	}

	@Override
	protected void reduce(IntWritable key, Iterable<ClusterCenter> values,Context context) throws IOException, InterruptedException {
		for(ClusterCenter valueCenter : values) {
			System.out.println("Value:"+valueCenter);
			boolean isClose=false;
			for (ClusterCenter centerList : canopyClusterCenters) {
				double distance = DistanceMeasurer.measureDistance(centerList, valueCenter.getCenter());
				if ( distance <= DistanceMeasurer.T1 ) {
					isClose = true;
					if(distance > DistanceMeasurer.T2) {
						centerList.setNeighbors(centerList.getNeighbors()+ valueCenter.getNeighbors());
					}
					break;
				}
			}
			if (!isClose) {
				ClusterCenter newClusterCenterVector = new ClusterCenter(valueCenter);
				newClusterCenterVector.getCenter().setName("Center");
				newClusterCenterVector.setNeighbors(valueCenter.getNeighbors());
				canopyClusterCenters.add(newClusterCenterVector);
			}	
		}
	}
	@Override
	protected void cleanup(Context context) throws IOException,InterruptedException
	{
		System.out.println("CleanUp");
		System.out.println("canopyClusterCenters:"+canopyClusterCenters);
		//Write sequence file with results of Canopy Cluster Center, Number of Cluster Center neighbors.
		super.cleanup(context);
		//System.out.println("NUm Of Vectors:"+ numOfVectors);
		FileSystem fs = FileSystem.get(context.getConfiguration());
		String rootFolder = context.getConfiguration().get("rootFolder");
		System.out.println(rootFolder);
		Path canopy = new Path(rootFolder+"/files/CanopyClusterCenters/canopyCenters.seq");
		//Path canopy = new Path("files/CanopyClusterCenters/canopyCenters.seq");
		context.getConfiguration().set("canopy.path",canopy.toString());
		
		@SuppressWarnings("deprecation")
		final SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs,context.getConfiguration(), canopy , ClusterCenter.class,IntWritable.class);
		System.out.println("before for");
		for (ClusterCenter center : canopyClusterCenters) {
			System.out.println("Adding CanopyClustercENTER:");
			System.out.println(center);
			System.out.println("nEIGHBORS:"+center.getNeighbors());
			centerWriter.append(center,new IntWritable(center.getNeighbors()));
		}
		centerWriter.close();	
		
	}
}
