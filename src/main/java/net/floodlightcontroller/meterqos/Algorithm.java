package net.floodlightcontroller.meterqos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
		FileIO fileIO = new FileIO(5);
		FileIO.setInstance(fileIO);
		SimpleServer server = SimpleServer.getInstance();
		
		Thread thread = new Thread(server);
		thread.start();  					//start socket server
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
		
			
		//初始化值
			Object[] x1 ={ 0.23, 0.23, 0.23, 0.23, 0};
			Object x2 = 4;
			
			
			Object x5 = 5;
			Object x6 = 1;
			Object[] x7 = {4, 4, 4, 4, 4};
			Object[] x8 = {0, 0, 0, 0, 2};
			Object x9 = 1;
			Object x10 = 1;
			Object[][] x11 = {{0.1, 0.2 ,0.4, 0.8 ,0.0,0.0},{ 0.23, 0.45, 0.75, 1.0, 1.5,2.5},{0.3,0 ,0 ,0 ,0 , 0},{ 0.1, 0.3, 0.5, 0.7, 0,0},{0.1, 0.3 , 0.5, 0.7,0, 0}};
			Object[] x12 = {4, 6, 1, 4, 3};
			Object[] x13 = {2,2,2,2,2};
			
			Object[] lastBuffer = { 0, 0, 0, 0, 0};
			
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
	//			System.out.println("running...");
			   try{
				   //initialize x3,x4
				    Object[] x3 = {0, 0, 0, 0, 0};
				    Object[] x4 = {0, 0, 0, 0, 0};
				    
				    //TODO
				    
					long time = System.currentTimeMillis();
					SimpleServer simpleServer = SimpleServer.getInstance();
					
					int failHost = -1;
					String downIp = null;
					
					synchronized (simpleServer) {
						List<String> ipList = simpleServer.getInetList();
						if(ipList.isEmpty())
						{
							System.out.println("host is empty...");
						}else{
	//						int id;				//the index of ip in the ipSocketMap
							int size = ipList.size();      	//the number of host
							System.out.println("host num is: "+ size);
							bWriter.write("Host num :" + size);
							bWriter.newLine();
		//					//for test
		//					x3[0] = 6;
		//					int size = 5;
		//					// for test
							
							for(int id=0;id<size;id++)
							{
								String ip = ipList.get(id);
								String message = null;
								if(simpleServer.isConnected(ip)){
									message = simpleServer.getHostMessage(ip);
									//TODO
									if(message==null)
									{
										System.out.println("Algorithm: receive null...");
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
										
										
										int lastValue = 0;
										if(buffer > ((int)lastBuffer[id] + 15))
										{
											lastValue = buffer;
											buffer = 2;
										}
										x3[id] = buffer;
										lastBuffer[id] = lastValue;
										x4[id] = 1;
										x9 = size;
										if(buffer==0)
										{
											//TODO										
											x8[id] = 4;												
										}else{
											x8[id] = 0;
											}
										}
								}else{
									// if socket is closed , delete the ip and reset the data
								
									// do not delete ipList				
					//				simpleServer.removeIp(id);
									simpleServer.deleteIpSocketMap(ip);
									failHost = id;
									downIp = ip;
									x3[id] = 0;
									x4[id] = 0;
									x9 = size - 1;
									
								}
							
							 }
							int j = size;
							for(;j < 5; j++)
							{
								x4[j] = 0;
								lastBuffer[j] = 0;
							}
								FileIO fileIO = FileIO.getInstance();
								fileIO.writeFile(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13);
								Thread.sleep(1000);
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
										System.out.println("1 :detect a host closed...");
										continue;
									}else{
										String ip = ipList.get(k);
										if(simpleServer.isConnected(ip))
										{
											
											System.out.println("ip: "+ ip +" , r is "+ fileIO.data.R[k]+" ,w is "+fileIO.data.W[k]);
											//TODO
											//send r						
											String msg = "HelloWorld"+String.valueOf(fileIO.data.R[k])+"HelloWorld"+String.valueOf(fileIO.data.W[k])+"HelloWorldd"+x3[k];
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
											int rate =(int) (fileIO.data.W[k]*1000);
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
