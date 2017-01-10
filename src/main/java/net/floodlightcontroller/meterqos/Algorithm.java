package net.floodlightcontroller.meterqos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class Algorithm implements Runnable{

	public static Algorithm algorithm = null;
	public static Map<Integer, String> meterIpMap;
	public FileWriter fWriter = null;
	public BufferedWriter bWriter = null;
	
	
	public Algorithm() {
		// TODO Auto-generated constructor stub
		//xxx
		meterIpMap = new HashMap<Integer,String>();
		try {
			fWriter = new FileWriter("E:\\CtoM\\log.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bWriter = new BufferedWriter(fWriter);
		
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println("algorithm is running...");
		FileIO fileIO = new FileIO();            
		FileIO.setInstance(fileIO);
		SimpleServer server = SimpleServer.getInstance();
		
		Thread thread = new Thread(server);
		thread.start();  					//start socket server
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
		
			
		//初始化值
//			Object[] x1 ={ 0.23, 0.23, 0.23, 0.23, 0};
//			Object x2 = 5;			//多少秒算一次
//			
//			
//			Object x5 = 5;
//			Object x6 = 1;
//			Object[] x7 = {4, 4, 4, 4, 4};
//			Object[] x8 = {0, 0, 0, 0, 2};
//			Object x9 = 1;
//			Object x10 = 1;
			Object[][] x11 = {{0.1, 0.2 ,0.4, 0.8 ,0.0,0.0},{ 0.23, 0.45, 0.75, 1.0, 1.5,2.5},{0.3,0 ,0 ,0 ,0 , 0},{ 0.1, 0.3, 0.5, 0.7, 0,0},{0.1, 0.3 , 0.5, 0.7,0, 0}};
			Object[] x12 = {4, 6, 1, 4, 3};
//			Object[] x13 = {2,2,2,2,2};
			
//			Object[] lastBuffer = { 0, 0, 0, 0, 0};
			ArrayList<Integer> lastBuffer = new ArrayList<>();
			ArrayList<Float> lastR = new ArrayList<>();
			
			//update data structure
			int  hostNum = 0;									//x5
			float lowestRate = (float) 0.23;					
			int CaculateTime = 5;														//x2    多少秒算一次  
			float totalBandwidth = (float) 2.0;					//x6
			int segLength = 4;									//x7										
			//x11 x12
			int group = 2;	
			float pA = 150;
			float pB = 100;
			float pC = 0;
			
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
	//			System.out.println("running...");
			   try{
				   	long time = System.currentTimeMillis();
				    
				   	//init data
					ArrayList<Float> lowestRateList = new ArrayList<>();     	//x1
					ArrayList<Integer> bufferList = new ArrayList<>();			//x3
					ArrayList<Integer> isActive = new ArrayList<>();			//x4
					ArrayList<Integer> isBuffer = new ArrayList<>();			//x8
					ArrayList<Integer> groupClass = new ArrayList<>();			//x13 用户级别
					
				    //TODO
					
					SimpleServer simpleServer = SimpleServer.getInstance();
					
					int failHost = -1;
					String downIp = null;
					
					synchronized (simpleServer) {
						List<String> ipList = simpleServer.getInetList();
						if(ipList.isEmpty())
						{
							System.out.println("host is empty...");
						}else{
							int size = ipList.size();      	//the number of host
							hostNum = size;
							System.out.println("host num is: "+ size);
							bWriter.write("Host num :" + size);
							bWriter.newLine();
							
							FileIO fileIO = FileIO.getInstance();
							
							for(int id=0;id<size;id++)
							{
								String ip = ipList.get(id);
								String message = null;
								if(simpleServer.isConnected(ip)){
									message = simpleServer.getHostMessage(ip);
									//TODO
									if(message==null)
									{
										System.err.println("Algorithm: receive null...");
										continue;
									}
									if(message.subSequence(0, 10).equals("HelloWorld"))
									{
										int buffer = 0;
										int index = message.substring(10).indexOf("HelloWorld");
				//						System.out.println("message: "+ message.substring(10) + "length:" + message.length());
										buffer = Float.valueOf(message.substring(10,10+index)).intValue();
										System.out.println("ip: " + ip + " buffer:" + buffer);
										
										if(buffer > 2){
											buffer = buffer - 2;
										}
										
		//								int lastValue = buffer;
										if(id >= lastBuffer.size())
										{
											//init lastBuffer
											lastBuffer.add(id,buffer);
										}else{//TODO111
											if(buffer > ((int)lastBuffer.get(id) + 15))
											{
												buffer = 2;
											}
											lastBuffer.remove(id);
											lastBuffer.add(id,buffer);
										}
										
										bufferList.add(id, buffer);				
										isActive.add(id,1);
										lowestRateList.add(id,lowestRate);
										groupClass.add(id, group);
										
										if(fileIO.data.Rlist.size()  < (id + 1))
										{
											lastR.add(id, (float)0);
										}
										else{
											lastR.remove(id);
											lastR.add(id,fileIO.data.Rlist.get(id));
										}
										
										if(buffer==0)
										{
											//TODO										
											isBuffer.add(id, 4);
											
										}else{
											isBuffer.add(id, 0);
											}
										}
								}else{
									// if socket is closed , delete the ip and reset the data
								
									// do not delete ipList				
					//				simpleServer.removeIp(id);
									System.out.println("algorithm:host ip " + ip + " exit...");
									simpleServer.deleteIpSocketMap(ip);
									failHost = id;
									downIp = ip;
									bufferList.remove(id);
									isActive.remove(id);
									isBuffer.remove(id);
									lastBuffer.remove(id);
									lowestRateList.remove(id);
									groupClass.remove(id);
									hostNum = hostNum - 1;
									
								}
							
							 }
							
//							int j = size;
//							int length = bufferList.size();
//							System.out.println("bufferlist size " + String.valueOf(bufferList.size()));
//							if(size == length)
//							{
//								System.out.println("check data : true...");
//							}
//							
//							//TODO
//							if(size < length){
//								for(;j < length - size; j++)
//								{
//									System.out.println("bufferlist size " + bufferList.size() + " index " + j);
//									lastR.remove(lastR.size() - 1);
//								}
//							}
								
								fileIO.writeFile(hostNum, lowestRateList, CaculateTime, bufferList, isActive, totalBandwidth, segLength, isBuffer, x11, x12, groupClass,pA,pB,pC,lastR);
								Thread.sleep(100);			//wait 0.1 second
								fileIO.readFile();
		//						//print data infomation
		//						for(int xx=0;xx<fileIO.data.R.length;xx++)
		//						{
		//							System.out.println("r: "+fileIO.data.R[xx]+ "w: "+ fileIO.data.W[xx]);
		//						}
								long wtime = System.currentTimeMillis();
								
								for(int k = 0;k < size; k++)
								{
									if(k==failHost)
									{
									   //TODO
										System.out.println("1 : detect a host closed...");
										continue;
									}else{
										String ip = ipList.get(k);
										if(simpleServer.isConnected(ip))
										{
				//							System.out.println("r size :" + fileIO.data.Rlist.size() + " w size :" + fileIO.data.Wlist.size() );
				//							System.out.println("ip: "+ ip +" , r is "+ fileIO.data.Rlist.get(k)+" ,w is "+fileIO.data.Wlist.get(k));
											//TODO
											//send r						
											String msg = "HelloWorld"+String.valueOf(fileIO.data.Rlist.get(k))+"HelloWorld"+String.valueOf(fileIO.data.Wlist.get(k))+"HelloWorldd"+bufferList.get(k);
											bWriter.write(ip+" : "+ msg);
											bWriter.newLine();
											System.out.println(msg);
//											System.out.println("x3: "+x3[k]);
//											System.out.println("x4: " + x4[k]);
//											System.out.println("x8: " + x8[k]);
											//String msg = "HelloWorld"+String.valueOf(fileIO.data.R[k])+"HelloWorld"+"0";
											simpleServer.sendMessage(msg, ip);
		//								}
		//								for(int m = 0;m < size;m++)
		//								{
											//TODO:get w and get meter rate
											int rate =(int) (fileIO.data.Wlist.get(k)*1000 + 200);
			//								String ip = ipList.get(m);
											int limitRate;
											int burst;
											if(rate < 100)
											{
												limitRate = 100 ; 
												burst = 2000;
											}else if(rate < 777)
											{
												 limitRate = (int) (0.63*rate - 90) ; 
												 burst = 2000;
											}else if (rate < 5390) {
												limitRate = (int) (1.1*rate - 450);
												burst = 2000;
											}else if (rate < 7720 ) {
												limitRate = (int) (1.1*rate - 650);
												burst = 5000;
											}else {
												limitRate = (int) (0.95*rate + 250);
												burst = 10000;
											}
					//						String ips = MeterQos.simpleServer.getInetList().get(m);
											MeterQos.meterModify(ip, limitRate, burst);
											
										}else{
											System.out.println("2 :detect a host closed...");
											simpleServer.deleteIpSocketMap(ip);
											failHost = k;							//in general,every time usually at most one ip socket is closed
											downIp = ip;
											
											bufferList.remove(k);
											isActive.remove(k);
											isBuffer.remove(k);
											lastBuffer.remove(k);
											lowestRateList.remove(k);
											groupClass.remove(k);
											hostNum = hostNum - 1;
											continue;
										}
									}
								} 
								System.out.println("Send r and modify w time: " + (System.currentTimeMillis() - wtime));
						}
						if(failHost != -1)
						{
							simpleServer.removeIp(downIp);
						}
						
					}
					
						long total_time = System.currentTimeMillis() - time;
						System.out.println("algorithm execute time is : " + total_time +" milliseconds");
					
				}catch(Exception e)
				{
					e.printStackTrace();
				}
//			   finally {
//					try {
//						bWriter.close();
//						fWriter.close();
//					} catch (Exception e2) {
//						// TODO: handle exception
//						e2.printStackTrace();
//					}
//				}
			   
			}   //
		}, 0, 5000);   //5秒循环一次
	}
	
	
	public Algorithm getInstance()
	{
		if(algorithm==null)
		{
			algorithm = new Algorithm();
			
		}
		return algorithm;
	}

//	public static void main(String[] args)
//	{
//		Algorithm algorithmx = new Algorithm().getInstance();
//	    Thread t = new Thread(algorithm);
//	    t.start();
//	}
}
