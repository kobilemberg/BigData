package solution;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
//for commit

//extends Vector
public class ClusterCenter  implements WritableComparable<ClusterCenter> {
	private Vector center;
	private int neighbors;

	public ClusterCenter(Vector center, ClusterCenter[] centroids, int neighbors) {
		super();
		this.center = new Vector(center);
		this.neighbors = neighbors;
	}

	public ClusterCenter() {
		super();
		this.neighbors=0;
		this.center = new Vector();
	}

	public ClusterCenter(ClusterCenter other) {
		super();
		this.neighbors = other.neighbors;
		this.center = new Vector(other.center);
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(((ClusterCenter)obj).toString());
	}

	public ClusterCenter(Vector other) {
		super();
		this.center = new Vector(other);
		this.neighbors=0;
	}

	public boolean converged(ClusterCenter c) {
		return compareTo(c) == 0 ? false : true;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		IntWritable neighborsToSet = new IntWritable(neighbors);
		neighborsToSet.write(out);
		center.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		IntWritable neighborsToSet = new IntWritable();
		neighborsToSet.readFields(in);
		neighbors = neighborsToSet.get();
		center = new Vector();		
		this.center.readFields(in);
	}

	@Override
	public int compareTo(ClusterCenter o) {
		return center.compareTo(o.getCenter());
	}
	
	/**
	 * @return the center
	 */
	public Vector getCenter() {
		return center;
	}

	@Override
	public String toString() {
		return "ClusterCenter:[\ncenter=" + center + "\nneighbors=" + neighbors + "]";
	}

	public int getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(int neighbors) {
		this.neighbors = neighbors;
	}

	public void setCenter(Vector center) {
		this.center = center;
	}
}
