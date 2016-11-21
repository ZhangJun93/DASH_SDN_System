package net.floodlightcontroller.meterqos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FileIO {

	private static FileWriter fWriter = null; 
	private static BufferedWriter bWriter = null;
	
	private static FileReader fReader = null;
	private static BufferedReader bReader = null;
    private int watcher;
    public Data data = null;
  //  public int isCount;
    public static int id = 1;
    public static FileIO fileIO = null;
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		//Data data = new Data();
//		
//	}
	
	public FileIO(int HostNum)
	{
		data = new Data(HostNum);
	    watcher = HostNum;
	}
	
	 public static FileIO getInstance(){
	    	if(fileIO == null){
	    		fileIO = new FileIO(5);
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
	public void writeFile(Object[] x1,Object x2,Object[] x3,Object[] x4,Object x5,Object x6,Object[] x7,Object[] x8,Object x9,Object x10,Object[][] x11,Object[] x12,Object[] x13)
	{
		try {
			fWriter = new FileWriter("E:\\CtoM\\ctom.txt");
			bWriter = new BufferedWriter(fWriter);
			
			bWriter.write(String.valueOf(id));
			bWriter.newLine();
			id++;
		//	System.out.println("id :"+id);
			
			for(int i = 0; i<x1.length;i++)
			{
				if(i==x1.length-1)
				{
					bWriter.write(String.valueOf(x1[i]));
				}else {
					bWriter.write(String.valueOf(x1[i])+" ");
				}
			}
			bWriter.newLine();
			bWriter.write(String.valueOf(x2));
			
			bWriter.newLine();
			for(int i = 0; i<x3.length;i++)
			{
				if(i==x3.length-1)
				{
					bWriter.write(String.valueOf(x3[i]));
				}else {
					bWriter.write(String.valueOf(x3[i])+" ");
				}
			}
			
			bWriter.newLine();
			for(int i = 0; i<x4.length;i++)
			{
				if(i==x4.length-1)
				{
					bWriter.write(String.valueOf(x4[i]));
				}else {
					bWriter.write(String.valueOf(x4[i])+" ");
				}
			}
			
			bWriter.newLine();
			bWriter.write(String.valueOf(x5));
			
			bWriter.newLine();
			bWriter.write(String.valueOf(x6));
			
			bWriter.newLine();
			for(int i = 0; i<x7.length;i++)
			{
				if(i==x7.length-1)
				{
					bWriter.write(String.valueOf(x7[i]));
				}else {
					bWriter.write(String.valueOf(x7[i])+" ");
				}
			}
			
			bWriter.newLine();
			for(int i = 0; i<x8.length;i++)
			{
				if(i==x8.length-1)
				{
					bWriter.write(String.valueOf(x8[i]));
				}else {
					bWriter.write(String.valueOf(x8[i])+" ");
				}
			}
			
			bWriter.newLine();
			bWriter.write(String.valueOf(x9));
			
			bWriter.newLine();
			bWriter.write(String.valueOf(x10));
			
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
			for(int i = 0; i<x13.length;i++)
			{
				if(i==x13.length-1)
				{
					bWriter.write(String.valueOf(x13[i]));
				}else {
					bWriter.write(String.valueOf(x13[i])+" ");
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
			if(!value.isEmpty())
			{
				String []xyz = value.split(" ");
				int length = xyz.length;
				float[] first = new float[length];
				for(int i=0;i<length;i++)
				{
					//System.out.println(Float.valueOf(xyz[i]).getClass().getName());
					first[i] = Float.valueOf(xyz[i]);
//					first[i] = String.valueOf(xyz[i]);
			//		System.out.println(first[i]);
					if(length!=watcher)
					{
						System.err.println("length not equel watcher");
					}
					data.setR(first);
				}
			}
			value = bReader.readLine();
			if(!value.isEmpty())
			{
				String []qwe = value.split(" ");
				int length = qwe.length;
				float[] second = new float[length];
				for(int i=0;i<length;i++)
				{
					second[i] = Float.parseFloat(qwe[i]);
				//	System.out.println(second[i]);
					if(length!=watcher)
					{
						System.err.println("second length not equel watcher");
					}
					data.setW(second);
				}
				
				
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
//					File file = new File("E:\\MtoC\\mtoc.txt");
//						if(file.exists())
//						{
//							if(file.delete()){
//						System.out.println("delete");
//							}else{
//								System.out.println("not delete");
//							}
//						}
					}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
			}

	}
}
