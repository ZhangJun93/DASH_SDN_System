package net.floodlightcontroller.meterqos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class FileIO {

	private static FileWriter fWriter = null; 
	private static BufferedWriter bWriter = null;
	
	private static FileReader fReader = null;
	private static BufferedReader bReader = null;
    public Data data = null;
  //  public int isCount;
    public static int id = 1;
    public static FileIO fileIO = null;
    public int count = 1;
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		//Data data = new Data();
//		
//	}
	
	public FileIO()
	{
		data = new Data();
	}
	
	 public static FileIO getInstance(){
	    	if(fileIO == null){
	    		fileIO = new FileIO();
	    	//	System.out.println("simpleServer is running...");
	    	}
	    	
			return fileIO;
	    	
	    }
	 
	 public static void setInstance(FileIO io)
	 {
		 FileIO.fileIO = io;
	 }
//	Object[] x1 ={ 0.35, 0.35, 0.35, 0, 0};
//	Object x2 = 2;
//	Object[] x3 = {6, 2, 0, 0, 0};
//	Object[] x4 = {1, 1, 1, 0, 0};
//	Object x5 = 5;
//	Object x6 = 5;
//	Object[] x7 = {2, 2, 2, 2, 2};
//	Object[] x8 = {0,0,2,2,2};
//	Object x9 = 3;
//	Object x10 = 1;
//	Object[][] x11 = {{0.1, 0.2 ,0.4, 0.8 ,0},{ 0.35, 0.6, 1.0, 2.0, 3.0},{ 0.3, 0, 0 ,0 ,0},{ 0.1, 0.3, 0.5, 0.7, 0},{0.1, 0.2 , 0.3, 0, 0}};
//	Object[] x12 = {4, 5, 1, 4, 3};
//	Object[] x13 = {2,2,2,2,2};
	public void writeFile(int hostNum,ArrayList<Float> rateList,int caculateTime,ArrayList<Integer> bufferList,ArrayList<Integer> isActive,float totalBandwidth,int segLength,ArrayList<Integer> isBuffer,Object[][] x11,Object[] x12,ArrayList<Integer> groupClass,float pA,float pB,float pC,ArrayList<Float> lastR)
	{					
			try {
				fWriter = new FileWriter("E:\\CtoM\\ctom.txt");
				bWriter = new BufferedWriter(fWriter);		
				
				synchronized (fWriter) {
					bWriter.write(String.valueOf(id));
					System.out.println("write flag: " + id);
					bWriter.newLine();
					
					bWriter.write(String.valueOf(hostNum));
					bWriter.newLine();
		//			id++;
				//	System.out.println("id :"+id);
					
					for(int i = 0; i<hostNum;i++)
					{
						if(i==hostNum-1)
						{
							bWriter.write(String.valueOf(rateList.get(i)));
						}else {
							bWriter.write(String.valueOf(rateList.get(i))+" ");
						}
					}
					
					bWriter.newLine();
					bWriter.write(String.valueOf(caculateTime));
					
					bWriter.newLine();
					for(int i = 0; i< hostNum;i++)
					{
						if(i==hostNum-1)
						{
							bWriter.write(String.valueOf(bufferList.get(i)));
						}else {
							bWriter.write(String.valueOf(bufferList.get(i))+" ");
						}
					}
					
					bWriter.newLine();
					for(int i = 0; i<hostNum;i++)
					{
						if(i==hostNum-1)
						{
							bWriter.write(String.valueOf(isActive.get(i)));
						}else {
							bWriter.write(String.valueOf(isActive.get(i))+" ");
						}
					}
					
					bWriter.newLine();
					bWriter.write(String.valueOf(totalBandwidth));
					
					bWriter.newLine();
					bWriter.write(String.valueOf(segLength));
					
					bWriter.newLine();
					for(int i = 0; i<hostNum;i++)
					{
						if(i==hostNum-1)
						{
							bWriter.write(String.valueOf(isBuffer.get(i)));
						}else {
							bWriter.write(String.valueOf(isBuffer.get(i))+" ");
						}
					}
					
		//			bWriter.newLine();
		//			for(int i = 0; i<x8.length;i++)
		//			{
		//				if(i==x8.length-1)
		//				{
		//					bWriter.write(String.valueOf(x8[i]));
		//				}else {
		//					bWriter.write(String.valueOf(x8[i])+" ");
		//				}
		//			}
		//			
		//			bWriter.newLine();
		//			bWriter.write(String.valueOf(x9));
		//			
		//			bWriter.newLine();
		//			bWriter.write(String.valueOf(x10));
					
					bWriter.newLine();
					for(int i = 0; i<x11.length;i++)
					{
						for(int j=0;j<x11[i].length;j++)
						{
							if(j==x11[i].length-1)
							{
								bWriter.write(String.valueOf(x11[i][j]));
							}else {
								bWriter.write(String.valueOf(x11[i][j])+" ");
							}
						}
						if(i==x11.length-1)
						{
							
						}else {
							bWriter.newLine();
						}
					}
					
					bWriter.newLine();
					for(int i = 0; i<x12.length;i++)
					{
						if(i==x12.length-1)
						{
							bWriter.write(String.valueOf(x12[i]));
						}else {
							bWriter.write(String.valueOf(x12[i])+" ");
						}
					}
					
					bWriter.newLine();
					for(int i = 0; i<hostNum;i++)
					{
						if(i==hostNum-1)
						{
							bWriter.write(String.valueOf(groupClass.get(i)));
						}else {
							bWriter.write(String.valueOf(groupClass.get(i))+" ");
						}
					}
					
					bWriter.newLine();
					bWriter.write(String.valueOf(pA));
					
					bWriter.newLine();
					bWriter.write(String.valueOf(pB));
					
					bWriter.newLine();
					bWriter.write(String.valueOf(pC));
					
					bWriter.newLine();
					for(int i = 0; i<hostNum;i++)
					{
						if(i==hostNum-1)
						{
							bWriter.write(String.valueOf(lastR.get(i)));
						}else {
							bWriter.write(String.valueOf(lastR.get(i))+" ");
						}
					}
				  }
			
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}finally {
					try {
						bWriter.close();
						fWriter.close();
					} catch (Exception e2) {
						// TODO: handle exception
						e2.printStackTrace();
					}
				}
		
			
		
	}
	
	
	public void readFile()
	{
		try{
			
			fReader = new FileReader("E:\\MtoC\\mtoc.txt");
			bReader = new BufferedReader(fReader);
			String value = bReader.readLine();
			
			//read flag
			int flag = Integer.parseInt(value);
			System.out.println("flag :" + flag);
			if(flag == id)
			{
				value = bReader.readLine();
				if(!value.isEmpty())
				{
					String []xyz = value.split(" ");
					int length = xyz.length;
					ArrayList<Float> rList = new ArrayList<>();
					for(int i=0;i<length;i++)
					{
						//System.out.println(Float.valueOf(xyz[i]).getClass().getName());
			//			first[i] = Float.valueOf(xyz[i]);
						rList.add(i,Float.parseFloat(xyz[i]));
	//					first[i] = String.valueOf(xyz[i]);
						System.out.println("r" + i + ": " + xyz[i]);

					}
					data.setR(rList);
				}
				
				value = bReader.readLine();
				if(!value.isEmpty())
				{
					String []qwe = value.split(" ");
					int length = qwe.length;
					ArrayList<Float> wList = new ArrayList<>();
					for(int i=0;i<length;i++)
					{
						wList.add(i,Float.parseFloat(qwe[i]));
						System.out.println("w" + i + ": " + Float.parseFloat(qwe[i]));
					}
					data.setW(wList);
					
				}
				
				id++;
				count = 1;
				
			}else{
				System.out.println("id " + id + " ,flag " + flag + " FileIO: retry to read file...");
				Thread.sleep(50);
				count++;
				if(count==5)
				{
					id = flag;
					System.err.println("id not equel flag : let id = flag");
					count = 1;
				}
				this.readFile();
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			//e.printStackTrace();
			try {
				bReader.close();
				fReader.close();
				this.readFile();
			} catch (IOException e0) {
				// TODO Auto-generated catch block
				e0.printStackTrace();
				
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			this.readFile();
			//System.out.println("no File to read");
		} finally {
				//后打开先关闭
				try {
					bReader.close();
					fReader.close();

					}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
			}

	}
}
