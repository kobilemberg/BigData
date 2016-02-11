package solution;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class CenterCentroidWritableComparable  implements WritableComparable<CenterCentroidWritableComparable> {
	private ClusterCenter center;
	private ClusterCenter centroid;

	public CenterCentroidWritableComparable() {
		super();
	}
	
	public CenterCentroidWritableComparable(ClusterCenter center, ClusterCenter centroid) {
		super();
		this.center = new ClusterCenter(center);
		this.centroid =new ClusterCenter(centroid);
	}

	public CenterCentroidWritableComparable(CenterCentroidWritableComparable other) {
		super();
		this.center = new ClusterCenter(other.center);
		this.centroid = new ClusterCenter(other.centroid);
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		center.write(out);
		centroid.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		center = new ClusterCenter();
		center.readFields(in);
		centroid = new ClusterCenter();
		
		centroid.readFields(in);
	}
	
    public Double m_length() {
        int centerSize = center.getCenter().getVectorArr().length;
        int centroidSize = centroid.getCenter().getVectorArr().length;
        double sum = 0;
       
        for (int i = 0; i < centerSize; i++) {
         double v1 = center.getCenter().getVectorArr()[i];
         sum += v1;
        }
        for (int i = 0; i < centroidSize; i++) {
         double v1 = centroid.getCenter().getVectorArr()[i];
         sum+=v1;
        }
       
        return sum;
       }
     @Override
     public int compareTo(CenterCentroidWritableComparable o) {
      double diff = this.m_length() - o.m_length();
        if (diff > 0.001) {
         return 1;
        }else if (diff < -0.001) {
         return -1;
        }
        return 0;
     }



	@Override
	public String toString() {
		return "CenterCentroidWritable: [\ncenter=" + center.toString() + "\ncentroid="+ centroid.toString() + "\n]";
	}
	

	public ClusterCenter getCenter() {
		return center;
	}

	public void setCenter(ClusterCenter center) {
		this.center = center;
	}

	public ClusterCenter getCentroid() {
		return centroid;
	}

	public void setCentroid(ClusterCenter centroid) {
		this.centroid = centroid;
	}
}
