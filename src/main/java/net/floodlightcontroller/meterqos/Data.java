package net.floodlightcontroller.meterqos;

import java.util.ArrayList;

public class Data {						//用于保存算法返回值
	
	public ArrayList<Float> Rlist ;
	public ArrayList<Float> Wlist ;
	
	public Data()
	{
		Rlist = new ArrayList<>();
		Wlist = new ArrayList<>();
	}
	
    public void setR(ArrayList<Float> rList)    
    {
    	Rlist.clear();
    	Rlist = rList;
 
    }
    public void setW(ArrayList<Float> wList)
    {
    	Wlist.clear();
    	Wlist = wList;
    	if(Wlist.size()!=Rlist.size())
    	{
    		System.err.println("Data error: the length of r not equels w");
    	}
    }
	
	
}
