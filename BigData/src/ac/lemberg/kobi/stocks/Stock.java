package ac.lemberg.kobi.stocks;

import java.util.Arrays;

@SuppressWarnings("rawtypes")
public class Stock implements Vector{
	
	String name;
	int duration;
	int[] featuresIndxs;
	double[][] featuresArr;
	double[] vector;






	public Stock(String name, int duration, int[] featuresIndxs, double[][] featuresArr) {
		super();
		this.name = name;
		this.duration = duration;
		this.featuresIndxs = featuresIndxs;
		this.featuresArr = featuresArr;
	}
	
	
	
	public Stock(String name, int duration, int[] featuresIndxs) {
		super();
		this.name = name;
		this.duration = duration;
		this.featuresIndxs = featuresIndxs;
		this.featuresArr = new double[duration][featuresIndxs.length];
	}

	public void addDay(int dayIndex, double[] feautres)
	{
		featuresArr[dayIndex-1] = feautres;
	}

	public double[] getAlldaysFeatures()
	{
		
		double[] arrToRet = new double[duration*featuresIndxs.length];
		int LastIDX=0;
		for(int day=0;day<featuresArr.length;day++)
		{
			for(double featureValue:featuresArr[day])
			{
				arrToRet[LastIDX]=featureValue;
				LastIDX++;
			}
		}
		return arrToRet;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	/**
	 * @return the featuresIndxs
	 */
	public int[] getFeaturesIndxs() {
		return featuresIndxs;
	}
	/**
	 * @param featuresIndxs the featuresIndxs to set
	 */
	public void setFeaturesIndxs(int[] featuresIndxs) {
		this.featuresIndxs = featuresIndxs;
	}
	/**
	 * @return the featuresArr
	 */
	public double[][] getFeaturesArr() {
		return featuresArr;
	}
	/**
	 * @param featuresArr the featuresArr to set
	 */
	public void setFeaturesArr(double[][] featuresArr) {
		this.featuresArr = featuresArr;
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Stock [name=" + name + ", duration=" + duration + ", featuresIndxs=" + Arrays.toString(featuresIndxs)
				+ "]";
	}



	public String toFullString() {
		String strToRet= "Stock [name=" + name + ", duration=" + duration + ", featuresIndxs=" + Arrays.toString(featuresIndxs)+ "]\n";
		strToRet+="Original v: ["+Arrays.toString(this.getAlldaysFeatures())+"]\n";
		strToRet+="normalized v: ["+Arrays.toString(this.getVector())+"]\n";
		return strToRet;
		
	}



	@Override
	public double[] getVector() {
		// TODO Auto-generated method stub
		return this.vector;
	}
	
	
	/**
	 * @param vctor the vctor to set
	 */
	public void setVctor(double[] vctor) {
		this.vector = Arrays.copyOf(vctor, vctor.length);
	}



	@Override
	public String getVectorString() {
		

	    //String strToRet= this.name+",";
		return this.name+","+Arrays.toString(vector).substring(1,Arrays.toString(vector).length()-1 );

	}
	

}
