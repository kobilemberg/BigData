package solution;
//for commit


public class DistanceMeasurer {

	public static final double T1 = 4125;
	public static final double T2 = 0;


	public static final double measureDistance(ClusterCenter center, Vector v) {
		double sum = 0;
		int length = v.getVectorArr().length;
		for (int i = 0; i < length; i++) {
			sum += Math.abs(center.getCenter().getVectorArr()[i]
					- v.getVectorArr()[i]);
		}
		return sum;
	}

}
