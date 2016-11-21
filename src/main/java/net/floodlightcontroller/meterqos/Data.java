package net.floodlightcontroller.meterqos;

public class Data {						//用于保存算法返回值
	boolean isChanged ;        //判读数据是否更新,读取后设为false，得到算法后设为true
	int person;
	
	float[] R ;
	float[] W ;
	
	public Data(int x)
	{
		person = x;
		R = new float[person];
		W = new float[person];
		isChanged = false;
	}
	
    public void setR(float[] a)    
    {
    	int k=0;
    	for(;k< a.length;k++)
    	{
    		R[k] = a[k];
    	}
    	isChanged = true;
    }
    public void setW(float[] b)
    {
    	int k=0;
    	for(;k<b.length;k++)
    	{
    		W[k] = b[k];
    	}
    	isChanged = true;
    }
	
	
}
