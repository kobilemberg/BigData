package ac.lemberg.kobi.stocks;

import java.util.Collection;

public class MinMaxNormalizer {
	
	double min = Integer.MAX_VALUE;
	double max = Integer.MIN_VALUE;
	
	public void normalizeData(@SuppressWarnings("rawtypes") Collection<? extends Vector> values,double new_max,double new_min)
	{	
		for(@SuppressWarnings("rawtypes") Vector v:values)
		{
			double[] vector=v.getVector();
			for(double num:vector)
			{
				if(num<min)
				{
					min=num;
				}
				if(num>max)
				{
					max=num;
				}
			}
			
			System.out.println("Min:"+min+" , Max:"+max);
			for (int i = 0; i < vector.length; i++) {
				System.out.println("vector[i] before:"+vector[i]);
				vector[i] = (((vector[i]-min)/(max-min))*(new_max-new_min))+new_min;
				System.out.println("vector[i] after:"+vector[i]);
			}
			min = Integer.MAX_VALUE;
			max = Integer.MIN_VALUE;
			
		}
	}
}
