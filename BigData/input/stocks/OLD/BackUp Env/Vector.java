package solution;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.WritableComparable;


public class Vector implements WritableComparable<Vector> {

	
	private double[] vectorArr;
	private String name;
	
	public Vector() {
		super();
		this.name="vector";
		this.vectorArr=new double[1];
	}

	public Vector(Vector v) {
		super();
		int l = v.vectorArr.length;
		this.vectorArr = new double[l];
		System.arraycopy(v.vectorArr, 0, this.vectorArr, 0, l);
		this.name = new String(v.name);
	}

	public Vector(double[] a) {
		super();
		int l = a.length;
		this.vectorArr = new double[l];
		System.arraycopy(a, 0, this.vectorArr, 0, l);
		this.name="vector";
	}
	
	public Vector(String name, double[] arr) {
		int l = arr.length;
		this.vectorArr = new double[l];
		System.arraycopy(arr, 0, this.vectorArr, 0, l);
		this.name = new String(name);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(this.vectorArr.length);
		for (int i = 0; i < vectorArr.length; i++) {
			out.writeDouble(vectorArr[i]);
		}
		out.writeUTF(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		vectorArr = new double[size];
		for (int i = 0; i < vectorArr.length; i++) {
			vectorArr[i] = in.readDouble();
		}
		this.name = in.readUTF();
	}

	@Override
	public int compareTo(Vector o) {
		@SuppressWarnings("unused")
		boolean equals = true;
		for (int i = 0; i < vectorArr.length; i++) {
			double c = vectorArr[i] - o.vectorArr[i];
			if (c!= 0.0d) {
				return (int)c;
			}		
		}
		return 0;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(((Vector)obj).toString());
	}

	public double[] getVectorArr() {
		return vectorArr;
	}

	public void setVectorArr(double[] vector) {
		this.vectorArr = vector;
	}

	@Override
	public String toString() {
		return "Vector: [vectorArr=" + Arrays.toString(vectorArr) + "name="+ name + "]";
	}

	
}
