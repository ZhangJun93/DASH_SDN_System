package net.floodlightcontroller.bandwidthlim;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.projectfloodlight.openflow.protocol.OFFlowAdd.Builder;
import org.projectfloodlight.openflow.protocol.OFFlowModify;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionEnqueue;
import org.python.antlr.PythonParser.return_stmt_return;


import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.util.FlowModUtils;


//就是一个提示作用
public class ChooseFlowQueue extends JPanel implements Runnable
{
	int times = 0;

	public void paint(Graphics g) 
	{
	   super.paint(g);
	   g.fillRect(0, 0, 1000, 800);
	   
	   //提示信息
	   if(times%2==0)
	   {   
		   g.setColor(Color.yellow);
		   //开关信息的字体
		   Font myFont = new Font("华文新魏", Font.BOLD, 25);
		   g.setFont(myFont);
		   g.drawString("Please press botton 'I' to modify flow table", 200, 150);
	   }
	   if(times > 100)
	   {
		   times = 0;
	   }
		 
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.times++;
			  this.repaint();
			
			  //read file
			  if(readTxtFile("/home/test/data/a.txt"))
			  {
				  BandwidthLim.logger.info("modify flow automatically...     Total time cost is "+ BandwidthLim.TimeMillis + " milliseconds");
			  }
		}
	}
	

	 public  boolean readTxtFile(String filePath){
	        try {
	        		BandwidthLim.timeNow = System.currentTimeMillis();
	                String encoding="utf-8";
	                File file=new File(filePath);
	                if(file.isFile() && file.exists()){ //判断文件是否存在
	                	BandwidthLim.logger.info("find change queue file ");
	                    InputStreamReader read = new InputStreamReader(
	                    new FileInputStream(file),encoding);//考虑到编码格式
	                    BufferedReader bufferedReader = new BufferedReader(read);
	                    String lineTxt = null;
	                    while((lineTxt = bufferedReader.readLine()) != null){
	                    	String []strings = lineTxt.split(" ");
	                    	try{
	                    		int a = Integer.parseInt(strings[0]);
	                    		int b = Integer.parseInt(strings[1]);
	                        	if(inQueue((long)a, (long)b)==1)               //配置流表
	                        	{
	                        		BandwidthLim.logger.info("inQueue success...");
	                        	}
	                        	else
	                        	{
	                        		BandwidthLim.logger.info("inQueue failed...");
	                        	}
	                    	}catch(Exception e)
	                    	{
	                    		BandwidthLim.logger.info("error: content is not a number...");
	                    	}
	                    	
	                    }
	                    read.close();
	                    BandwidthLim.TimeMillis = System.currentTimeMillis() - BandwidthLim.timeNow;
	                    file.delete();
	                    
	                  //  file.delete();   
	                    return true;    //read success
	        }else{
	            //System.out.println("找不到指定的文件");
	        	return false;
	        }
	        } catch (Exception e) {
	            BandwidthLim.logger.error("读取文件内容出错");
	            //e.printStackTrace();
	            
	        }
	        return false;
	    }
	 
	 public int inQueue(Long xid, Long queueID) {
		  
		    IFlowState flowState=null;
		    int n;
		    for(n=0;n<BandwidthLim.iFlows.size();n++)
		    {
			     IFlowState iflow = BandwidthLim.iFlows.get(n);  
			     if(iflow.flow.getXid()==xid)
			     {
			    	 flowState = iflow;
			    	 break;
			     }			     
		    }
		    if(flowState==null)
		    {

		    	  BandwidthLim.logger.error("error: can not find the flow which Xid is : " + xid);
			      return 0;
			     
		    }
		    
			String info = "找到流: " + flowState.srcIPAddress + " to " + flowState.dstIPAddress + "\n请输入分配的队列号：\n(1代表200M，2代表400M，3代表600M，4代表800M,5代表1000M)";
			
			
			
			ArrayList<OFAction> actions = new ArrayList<OFAction>();
			flowState.flow.getActions().clear();      //清除action
			
			OFActionEnqueue enqueue = BandwidthLim.factory.actions().buildEnqueue()
					.setPort(flowState.getPacketIn().getInPort())
					.setQueueId(queueID)
					.build();
			actions.add(enqueue);
//			BandwidthLim.logger.info("action clear is : "+ flowState.flow.getActions());
			flowState.flow.setActions(actions);
			//BandwidthLim.logger.info("flow table is modified，the Actions is " + actions);

			OFFlowModify flowModify = FlowModUtils.toFlowModify(flowState.flow.build());     //将FlowAdd转换为FlowModify
			//flowPusher.deleteFlow(Integer.toString(flowName));
			BandwidthLim.flowPusher.addFlow(Integer.toString(flowState.id), flowModify, BandwidthLim.dpid);
			BandwidthLim.logger.info("flow table is modified，the flowModify.Actions is " + flowModify.getActions());
			//BandwidthLim.iFlows.get(n).flow = (Builder) flowModify;
			
			return 1;
	}
	 
}