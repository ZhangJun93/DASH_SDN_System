package net.floodlightcontroller.meterqos;

import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JohnJD on 16/11/2.
 */
public class SimpleServer implements Runnable{

    private static List<String> staticInet = new ArrayList<>();
    private static Map<String,Socket> doubleClient = new HashMap<>();
    public static SimpleServer simpleServer = null;
    private ServerSocket ss;
    public static Map<String,Integer> isAliveMap = new HashMap<>();

    public SimpleServer() throws Exception{


        //waiting for the connect
        ss = new ServerSocket(40000);
        
        //listening the host whether is alive
        Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("listening model is running...");
				List<String> ipList = SimpleServer.getInetList();
				if(!ipList.isEmpty()){
					for(int n=0;n < ipList.size();n++)
					{
						String ip = ipList.get(n);
						String message = simpleServer.getMessage(ip);
						if(message==null)
						{
							System.out.println("ip " + ip + " receive message null...");
							int count = isAliveMap.get(ip);
							count = count + 1;
							isAliveMap.put(ip, count);				//update count
							if (count == 5) {
								System.out.println("ip " + ip + "is down...");
								removeIp(n);
								deleteIpSocketMap(ip);
								isAliveMap.remove(ip);
//								continue;
							}				
							
						 }
					  }
				}	
			}
		}, 0, 1000);   //1秒循环一次
        
        
        
//
//        int flag = 0;
////        while(true){
//        //accept the connect
//        while(true){
//            Socket socket = ss.accept();
//            flag = 1;
//            String newInet = socket.getInetAddress().getHostAddress();
//            if(!doubleClient.containsKey(newInet)){
//                doubleClient.put(newInet,socket);
//                staticInet.add(newInet);
//                shakeHand(socket);
//            }else if(doubleClient.containsKey(newInet)){
//                send("This ip is already used!!!",socket);
//            }
//            if(flag == 1){
//                System.out.println("current number of client is " + staticInet.size());
////                System.out.println("current map is " + doubleClient.size());
//                displayInet();
//                flag = 0;
//            }
//        }


       // System.out.println("the getInetAddress is " + socket.getInetAddress());

//        InputStream in = socket.getInputStream();
//
//        OutputStream out = socket.getOutputStream();
//
//        byte[] buff = new byte[1024];
//        int count = -1;
//        String req = "";
//
//        //read the data,establish the "shake hand" connect
//        count = in.read(buff);
//
//        req = new String(buff,0,count);
//        System.out.println("握手请求: " + req);
//        //get the key of "shake hand" connect
//        String secKey = getSecWebSocketKey(req);
//        System.out.println("secKey = " + secKey);
//
//        String response = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: "
//                + "websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: "
//                + getSecWebSocketAccept(secKey) + "\r\n\r\n";
//        System.out.println("secAccept = " + getSecWebSocketAccept(secKey));
//        out.write(response.getBytes());

        //above it is the shakehand process

//        //again read the data from client
//        count = in.read(buff);
//        System.out.println("accept count of byte: " + count);
//
//        /*
//        * WebSocket send() data's protocol
//        * byte of 3 to 6 is datamask
//        * from 7 byte is the payload data
//        * */
//        for(int i = 0;i < count - 6;i++){
//            buff[i+6] = (byte)(buff[i%4 + 2] ^ buff[i+6]);
//        }
//
//        //display the got data
//        System.out.println("the data of receive : " + new String(buff,6,count - 6,"UTF-8"));

        /****************************************/

//        while(true){
//           // String r = receive(in);
//            System.out.println("the data is " + r);
//        }
//        String r = receive(in);
//        System.out.println("the data is " + r);
//
//        for(int i = 0 ;i < 6;i++){
//            send("iiii" + i,out);
//        }

        /****************************************/
//        byte[] pushHead = new byte[2];
//        pushHead[0] = buff[0];
////        System.out.println("buff[0] is " + buff[0]);
//        //receive msg under is defined
//        String pushMsg = "HEIHEIHEI,I RECEIVED!!!";
//        //the second byte of pushHead is the length of data
//        pushHead[1] = (byte)pushMsg.getBytes("UTF-8").length;
//
//        out.write(pushHead);
//        out.write(pushMsg.getBytes("UTF-8"));

        //socket.close();
        //ss.close();
//        }

    }

    public static List<String> getInetList(){
        return staticInet;
    }
    
    public static void removeIp(int index)
    {
    	System.out.println("remove ip " + staticInet.get(index) + " success");
    	staticInet.remove(index);
    }

    public static Map<String,Socket> getMapInettoSocket(){
        return doubleClient;
    }
    
    public static void deleteIpSocketMap(String ip) {
		try{
			Socket socket = doubleClient.get(ip);
			socket.close();
			doubleClient.remove(ip);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
    
    public static SimpleServer getInstance()
    {
    	if(simpleServer==null)
    	{
    		try {
				simpleServer = new SimpleServer();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return simpleServer;
    }
    
    public static void setInstance(SimpleServer server)
    {
    	simpleServer = server;
    }
    
    
    
    public void displayInet(){
        Iterator it = staticInet.iterator();
        while(it.hasNext()){
            System.out.println("inet is " + it.next());
//            if(doubleClient.size() != 0){
//                if(doubleClient.containsKey(it.next())){
//                    send("123",doubleClient.get(it.next()));
//                }else {
//                    System.out.println("Error!It is not " + it.next());
//                }
//            }
        }

        Iterator iit = doubleClient.keySet().iterator();
        Object tmp = null;
        while(iit.hasNext()){
            tmp = iit.next();
            System.out.println("map key is " + tmp);
            System.out.println("map value is " + doubleClient.get(tmp));
        }
//        Iterator iiit = doubleClient.values().iterator();
//        while(iiit.hasNext()){
//            System.out.println("map value is " + iiit.next());
//        }
    }

    private void shakeHand(Socket socket) throws Exception{
        InputStream in = socket.getInputStream();

        OutputStream out = socket.getOutputStream();

        byte[] buff = new byte[1024];
        int count = -1;
        String req = "";

        //read the data,establish the "shake hand" connect
        count = in.read(buff);

        req = new String(buff,0,count);
        System.out.println("握手请求: " + req);
        //get the key of "shake hand" connect
        String secKey = getSecWebSocketKey(req);
        System.out.println("secKey = " + secKey);

        String response = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: "
                + "websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: "
                + getSecWebSocketAccept(secKey) + "\r\n\r\n";
        System.out.println("secAccept = " + getSecWebSocketAccept(secKey));
        out.write(response.getBytes());
    }

//    private InputStream getInputfromSocket(InetAddress inet4){
//
//    }

    private String receive(Socket socket){
        try{
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int position = -1;
            position = in.read(buffer);

            for(int i = 0;i < position - 6;i++){
                buffer[i+6] = (byte)(buffer[i%4 + 2] ^ buffer[i+6]);
            }
            return new String(buffer,6,position - 6,"UTF-8");

        }catch(Exception e) {//	public static void main(String[] args)
//        	{
//    		Algorithm algorithmx = new Algorithm().getInstance();
//    	    Thread t = new Thread(algorithm);
//    	    t.start();
//    	}
//            return "the return is null";
//        	e.printStackTrace();
            //null
        }
		return null;
    }
    
    public String getMessage(String ip)
    {
    	try {
	    	Socket socket = doubleClient.get(ip);
	    	String message = null;
	    	message = receive(socket);
	    	if(message==null)
	    	{
	    		System.err.println("ip: "+ip+", receive message null");
	    	}
	    	return message;
    	}catch(Exception e)
    	{
    		//TDDO
    	}
    	return "can not get message";
    }
    
    public void sendMessage(String msg,String ip)
    {
    	try{
	    	Socket socket = doubleClient.get(ip);
	    	send(msg, socket);
    	}catch(Exception e){
    		// ip is down
    		// don't send message
    	}
    }

    private void send(String msg,Socket socket){
        try{
            OutputStream out = socket.getOutputStream();
            byte[] pushHead = new byte[2];
            pushHead[0] = -127;
//        System.out.println("buff[0] is " + buff[0]);
            //receive msg under is defined
            String pushMsg = msg;
            //the second byte of pushHead is the length of data
            pushHead[1] = (byte)pushMsg.getBytes("UTF-8").length;

            out.write(pushHead);
            out.write(pushMsg.getBytes("UTF-8"));
        }catch(Exception e) {
            //null
        	e.printStackTrace();
        }
    }

    private String getSecWebSocketKey(String req){
        //generate a normal expression to get the data after Sec-Websocket-Key :
        Pattern p = Pattern.compile("^(Sec-Websocket-Key:).+",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(req);
        if(m.find()){
            String foundstring = m.group();
            return foundstring.split(":")[1].trim();
        }else{
            return null;
        }
    }

    private String getSecWebSocketAccept(String secKey) throws Exception{
        String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        secKey += guid;

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(secKey.getBytes("ISO-8859-1"),0,secKey.length());

        byte[] sha1Hash = md.digest();

        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(sha1Hash);
    }
//	public static void main(String[] args)
//	{
//		Algorithm algorithmx = new Algorithm().getInstance();
//	    Thread t = new Thread(algorithm);
//	    t.start();
//	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		 
		System.out.println("socket server is running...");
	        int flag = 0;
	        
	        
//	        while(true){
	        //accept the connect
	        while(true){
	            Socket socket = null;
				try {
					socket = ss.accept();
					System.out.println("accept socket...");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            flag = 1;
	            String newInet = socket.getInetAddress().getHostAddress();
	            if(!doubleClient.containsKey(newInet)){
	                doubleClient.put(newInet,socket);
	                staticInet.add(newInet);
	                isAliveMap.put(newInet, 0);
	                try {
						shakeHand(socket);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }else if(doubleClient.containsKey(newInet)){
	                send("This ip is already used!!!",socket);
	            }
	            if(flag == 1){
	                System.out.println("current number of client is " + staticInet.size());
//	                System.out.println("current map is " + doubleClient.size());
	                displayInet();
	                flag = 0;
	            }
	        }
	}

//    public static void main(String[] args) throws Exception{
//
//        new SimpleServer();
//    }


}
