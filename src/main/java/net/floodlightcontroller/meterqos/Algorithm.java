package net.floodlightcontroller.meterqos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class Algorithm implements Runnable{

	public static Algorithm algorithm = null;
	public static Map<Integer, String> meterIpMap;
	
	public Algorithm() {
		// TODO Auto-generated constructor stub
		//xxx
		meterIpMap = new HashMap<Integer,String>();
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
			Object[] x3 = {1, 2, 2, 2, 0};
			Object[] x4 = {1, 0, 0, 0, 0};
			Object x5 = 5;
			Object x6 = 5;
			Object[] x7 = {4, 4, 4, 4, 4};
			Object[] x8 = {4, 0, 0, 0, 2};
			Object x9 = 1;
			Object x10 = 1;
			Object[][] x11 = {{0.1, 0.2 ,0.4, 0.8 ,0.0,0.0},{ 0.23, 0.45, 0.75, 1.0, 1.5,2.5},{ 0.3, 0, 0 ,0 ,0},{ 0.1, 0.3, 0.5, 0.7, 0,0},{0.1, 0.3 , 0.5, 0.7,0, 0}};
			Object[] x12 = {4, 6, 1, 4, 3};
			Object[] x13 = {2,2,2,2,2};
			@Override
			public void run() {
				// TODO Auto-generated method stub
	//			System.out.println("running...");
				long time = System.currentTimeMillis();
				SimpleServer simpleServer = SimpleServer.getInstance();
				List<String> ipList = simpleServer.getInetList();
				if(ipList.isEmpty())
				{
					System.out.println("socket is empty...");
				}else{
					int id;				//the index of ip in the ipSocketMap
					int size = ipList.size();      //the number of host
					System.out.println("host num is: "+ size);
					for(id=0;id<size;id++)
					{
						String ip = ipList.get(id);
						String message = simpleServer.getMessage(ip);
						
						if(message.subSequence(0, 10).equals("HelloWorld"))
						{
							int buffer = 0;
							int index = message.substring(10).indexOf("HelloWorld");
//							System.out.println("message: "+ message.substring(10) + "length:" + message.length());
							buffer = Float.valueOf(message.substring(10,10+index)).intValue();
							System.out.println("buffer:" + buffer);
							
							x3[id] = buffer;
							if(buffer==0)
							{
								//TODO
							}
						}
					}
						FileIO fileIO = FileIO.getInstance();
						fileIO.writeFile(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13);
						fileIO.readFile();
//						//print data infomation
//						for(int xx=0;xx<fileIO.data.R.length;xx++)
//						{
//							System.out.println("r: "+fileIO.data.R[xx]+ "w: "+ fileIO.data.W[xx]);
//						}
						for(int k=0;k<size;k++)
						{
							System.out.println("id:"+k+" , r is "+ fileIO.data.R[k]+" ,w is "+fileIO.data.W[k]);
							//TODO
							//send r
							String ip = ipList.get(k);
							String msg = "HelloWorld"+String.valueOf(fileIO.data.R[k]);
							simpleServer.sendMessage(msg, ip);
						}
						for(int m=0;m<size;m++)
						{
							//TODO:get w and get meter rate
							int rate =(int) (fileIO.data.W[m]*1000);
							String ip = ipList.get(m);
							int limitRate;
							int burst;
							if(rate < 777)
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
							MeterQos.meterModify(ip, limitRate,burst);
						}   
						
					long total_time = System.currentTimeMillis() - time;
					System.out.println("algorithm execute time is : " + total_time +" milliseconds");
				}
			}
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
