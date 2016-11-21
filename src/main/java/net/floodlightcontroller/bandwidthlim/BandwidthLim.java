package net.floodlightcontroller.bandwidthlim;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModify;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFRequest;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionEnqueue;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.python.modules.thread.thread;
import org.restlet.util.StringReadingListener;
import org.simpleframework.transport.Transport;
import org.slf4j.LoggerFactory;


import com.google.common.util.concurrent.ListenableFuture;

import ch.qos.logback.classic.Logger;
import io.netty.handler.codec.http.HttpHeaders.Values;
import javafx.scene.chart.PieChart.Data;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.internal.TableFeatures;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.FlowModUtils;
import net.floodlightcontroller.statistics.StatisticsCollector;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;


public class BandwidthLim extends JFrame implements IFloodlightModule, IOFMessageListener ,KeyListener{

	//net.floodlightcontroller.bandwidthlim.BandwidthLim
	protected IFloodlightProviderService floodlightService;
	protected IDeviceService deviceService;
	protected  static IStaticFlowEntryPusherService flowPusher;  //使用流下发函数
	protected IStorageSourceService storageSource; //消息储存
	protected IOFSwitchService switchService;    //获取交换机	
    protected IStatisticsService statisticsService;//收集带宽信息 
	
	protected static  OFFactory factory;
	protected NodePortTuple portNum;
	protected static Logger logger;
	protected static DatapathId dpid;
	protected IOFSwitch sw;
	protected final TransportPort TCP0 = TransportPort.of(1935);
	protected final String dstTcp = "DST";
	protected final String srcTcp = "SRC";
	protected static long timeNow;				// j记录当前时间
	protected static long TimeMillis;         //记录时间差
	
	protected static ArrayList<IFlowState> iFlows ; 		//储存每一个流表
	protected static int ID = 1;           //为每个流分配一个id
	IPv4Address iPv4Address1;
	IPv4Address iPv4Address2;
	IPv4Address iPv4Address3;
	IPv4Address iPv4Address4;
	
	protected ChooseFlowQueue chooseQueue = null;
	
	Thread t;   //用于收集带宽线程
	private static BufferedWriter bWriter = null;   //用于输出带宽数据
	private static FileWriter fWriter = null;
	
	private static long queueID = 1L;
	
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return BandwidthLim.class.getName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		// TODO Auto-generated method stub
		OFPacketIn packetIn = (OFPacketIn) msg;
		switch(msg.getType()){
		case PACKET_IN:
			logger.info("packet_in recieved!...");
//			for(OFPortDesc p : sw.getPorts())
//			{
//				// SwitchPortBandwidth bandwidth = icCollector.getBandwidthConsumption(dpid, p.getPortNo());
//				logger.info("bandwidth is : " + p);
//			}
			
			//打印流表信息
			//TODO   338
//			Map<String, OFFlowMod> iswflows = flowPusher.getFlows(dpid);
//			if(iswflows != null)
//			{
//				for(String s:iswflows.keySet())
//				{
//					logger.info(s+":flow size is "+ iswflows.size()+ ",flow Xid is:" + iswflows.get(s).getXid() +" actions is : " + iswflows.get(s).getActions());
//				}
//			}
//			else{
//				logger.info("flow size is empty!..." );
//				
//			}
//			
		
			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
			if(eth.getEtherType() == EthType.IPv4)
			{
				/* We got an IPv4 packet; get the payload from Ethernet */
	            IPv4 ipv4 = (IPv4) eth.getPayload();
	            IPv4Address srcIPAddress = ipv4.getSourceAddress();
            	IPv4Address dstIPAddress = ipv4.getDestinationAddress();
	            /* Various getters and setters are exposed in IPv4 */
            	logger.info("srcIP is: " + srcIPAddress + " dstIP is: " + dstIPAddress + " packet: "  );
            	
            	
	            if(ipv4.getProtocol().equals(IpProtocol.TCP)&&(srcIPAddress.equals(iPv4Address1)||srcIPAddress.equals(iPv4Address3)||srcIPAddress.equals(iPv4Address3)||srcIPAddress.equals(iPv4Address4)))
	            {
	            	TCP tcp = (TCP) ipv4.getPayload();
	            	logger.info("receive the right flow....");
	            //   TransportPort srcPort = tcp.getSourcePort();
	                TransportPort dstPort = tcp.getDestinationPort();
	                //	TODO
	               /*
	    			if(srcPort.equals(TCP0))
	    			{
	    				String info = "流ID：" + ID +":收到一个PAKET_IN消息,源端口为1935,IP: " + srcIPAddress + " to " + dstIPAddress ;
		                logger.info(info);
	    				IFlowState flowState = new IFlowState(sw, packetIn, eth, cntx, 5, srcTcp, ID);//default add to 5th queue
	    				addFodLim(sw, packetIn, eth, cntx,srcTcp,flowState);
	    				ID++;
	    			}
	    			*/
	                //TODO
	    			//if(dstPort.equals(TCP0))    //发往TCP端口1935的流
	                if(dstIPAddress.equals(iPv4Address2) )
	    			{
	    				
	    				String info = "流ID：" + ID +":收到一个PAKET_IN消息,目的端口为1935，IP：" + srcIPAddress + " to " + dstIPAddress ;
		                logger.info(info);
	    				IFlowState flowState = new IFlowState(sw, packetIn, eth, cntx, dstTcp, ID);
	    				ID++;
	    				IFlowState flowState_rev = new IFlowState(sw, packetIn, eth, cntx, srcTcp, ID);
	    				ID++;
	    				addFlowModLim(sw, packetIn, eth, cntx,dstTcp,flowState,flowState_rev);
	    				
	    			}
	            }
			}

			break;
		case STATS_REPLY:	
			logger.info("stats_reply");
			break;
		default:
			break;
		
		}
		return Command.CONTINUE;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IStatisticsService.class);
		l.add(IStorageSourceService.class);
		l.add(IOFSwitchService.class);
		l.add(IDeviceService.class);

		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated methoEd stub
		floodlightService = context.getServiceImpl(IFloodlightProviderService.class);
		flowPusher = context.getServiceImpl(IStaticFlowEntryPusherService.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		deviceService = context.getServiceImpl(IDeviceService.class);
		statisticsService = context.getServiceImpl(IStatisticsService.class);
		
		logger = (Logger) LoggerFactory.getLogger(BandwidthLim.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightService.addOFMessageListener(OFType.PACKET_IN, this);
		
		//logger.info("BandwidthLim model is running...");
		dpid = DatapathId.of("5e:3e:48:6e:73:02:02:f6");
		//dpid = DatapathId.of("00:00:00:00:00:00:00:01");
		iPv4Address1 = IPv4Address.of("192.168.100.111");
		iPv4Address2 = IPv4Address.of("192.168.100.10");
//		iPv4Address1 = IPv4Address.of("192.168.100.10");
//		iPv4Address2 = IPv4Address.of("192.168.100.9");
		iPv4Address3 = IPv4Address.of("192.168.100.222");
		iPv4Address4 = IPv4Address.of("192.168.100.3");
		
		sw = switchService.getSwitch(dpid);
		factory = OFFactories.getFactory(OFVersion.OF_10);
		iFlows = new ArrayList<IFlowState>();
		
		
		
		
		
		//isService.collectStatistics(true);
		
		//监听是否要修改流表
		chooseQueue=new ChooseFlowQueue();
		Thread t2 = new Thread(chooseQueue);
		t2.start();
		logger.info("chooseQueue model in running...");
		this.add(chooseQueue);
		this.setSize(1000, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.addKeyListener(this);
	}
	
	public void addFlowModLim(IOFSwitch sw,OFPacketIn pi,Ethernet eth,FloodlightContext cntx,String s,IFlowState flowState,IFlowState flowState_rev){
		OFPort inPort = pi.getInPort();
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();
		VlanVid vlanVid = VlanVid.ofVlan(eth.getVlanID());
		
		OFFlowAdd.Builder flow = factory.buildFlowAdd();
		Match.Builder match = factory.buildMatch();
		
		ArrayList<OFAction> actions = new ArrayList<OFAction>();
		
		match.setExact(MatchField.IN_PORT, inPort);
		match.setExact(MatchField.ETH_DST, dstMac);
		match.setExact(MatchField.ETH_SRC, srcMac);
	
		//TODO
		OFPort outPort = deviceService.findDevice(dstMac, VlanVid.ZERO, IPv4Address.NONE
				,IPv6Address.NONE, DatapathId.NONE, OFPort.ZERO)
						 .getAttachmentPoints()[0].getPort();
		
		if(!vlanVid.equals(VlanVid.ZERO))
		{
			match.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlanVid));
			logger.info("vlan id is : " + vlanVid);
		}
		IPv4 ip = (IPv4) eth.getPayload();
		//logger.info("match information: " + ip.getSourceAddress() + " -> "+ip.getDestinationAddress());
		match.setExact(MatchField.IPV4_DST, ip.getDestinationAddress());
		match.setExact(MatchField.IPV4_SRC, ip.getSourceAddress());
		match.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		
		TCP tcp = (TCP) ip.getPayload();
		match.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
//		if(s.equals(dstTcp)){
////			match.setExact(MatchField.TCP_DST, TCP0);
////			match.setExact(MatchField.TCP_SRC, tcp.getSourcePort());
//			match.setExact(MatchField.TCP_SRC, TransportPort.NO_MASK);
//			match.setExact(MatchField.TCP_DST, TransportPort.NO_MASK);
//		}else if(s.equals(srcTcp)){
//	//		match.setExact(MatchField.TCP_DST, tcp.getDestinationPort());
//	//		match.setExact(MatchField.TCP_SRC, TCP0);
//			match.setExact(MatchField.TCP_SRC, TransportPort.NO_MASK);
//			match.setExact(MatchField.TCP_DST, TransportPort.NO_MASK);
//			//TODO
//		}
		//logger.info("Match is" + match.toString());
		
		OFActionEnqueue enqueue = factory.actions().buildEnqueue()
					.setPort(outPort)   //TODO
					.setQueueId(queueID)     //首次默认分配队列1
					.build();
//		OFActionOutput output =factory.actions().buildOutput()
//					.setPort(outPort)
//					.build();

		actions.add(enqueue);
		//actions.add(output);
		flow.setBufferId(OFBufferId.NO_BUFFER)
	    .setActions(actions)
	    .setIdleTimeout(0)
	    .setHardTimeout(0)
	    .setMatch(match.build())
	    .setOutPort(outPort)
	    .setPriority(32675);
		
		//增加反向流表
		OFFlowAdd.Builder flow_rev = factory.buildFlowAdd();
		Match.Builder match_rev = factory.buildMatch();
		
		ArrayList<OFAction> actions_rev = new ArrayList<OFAction>();
		
		
		match_rev.setExact(MatchField.IN_PORT, outPort);
		match_rev.setExact(MatchField.ETH_DST, srcMac);
		match_rev.setExact(MatchField.ETH_SRC, dstMac);
		
		if(!vlanVid.equals(VlanVid.ZERO))
		{
			match_rev.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlanVid));
			logger.info("vlan id is : " + vlanVid);
		}
		

		match_rev.setExact(MatchField.IPV4_SRC, ip.getDestinationAddress());		
		match_rev.setExact(MatchField.IPV4_DST, ip.getSourceAddress());
		match_rev.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		match_rev.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
		
//		if(s.equals(dstTcp)){
////			match_rev.setExact(MatchField.TCP_DST, tcp.getSourcePort());
////			match_rev.setExact(MatchField.TCP_SRC, TCP0);
//			match_rev.setExact(MatchField.TCP_SRC, TransportPort.NO_MASK);
//			match_rev.setExact(MatchField.TCP_DST, TransportPort.NO_MASK);
//
//		}else if(s.equals(srcTcp)){
////			match_rev.setExact(MatchField.TCP_DST, TCP0);
////			match_rev.setExact(MatchField.TCP_SRC, tcp.getDestinationPort());
//			match_rev.setExact(MatchField.TCP_SRC, TransportPort.NO_MASK);
//			match_rev.setExact(MatchField.TCP_DST, TransportPort.NO_MASK);
//		}
		
		OFActionEnqueue enqueue_rev = factory.actions().buildEnqueue()
				.setPort(inPort)   //TODO
				.setQueueId(queueID)     //首次默认分配队列1
				.build();
		
		actions_rev.add(enqueue_rev);
		
		flow_rev.setBufferId(OFBufferId.NO_BUFFER)
	    .setActions(actions_rev)
	    .setIdleTimeout(0)
	    .setHardTimeout(0)
	    .setMatch(match_rev.build())
	    .setOutPort(inPort)
	    .setPriority(32675);

		
		
		

		flowState.flow = flow;
		flowState_rev.flow = flow_rev;
		iFlows.add(flowState);
		iFlows.add(flowState_rev);
		logger.info("the first Actions is " + actions + " the reverse Actions is " + actions_rev);
		logger.info("flow table size is :" + iFlows.size());
		flowPusher.addFlow(Integer.toString(flowState.id), flow.build(), dpid);
		flowPusher.addFlow(Integer.toString(flowState_rev.id), flow_rev.build(), dpid);
		
	
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	
	}

	@SuppressWarnings("unchecked")
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode()==KeyEvent.VK_I)
		{
			int k;
			long j;
			int flowName;
			int flowName_rev;
			String info = "流表大小为"+ iFlows.size()/2 + "请输入要修改的流的ID：";
			k = Integer.parseInt(JOptionPane.showInputDialog(info));
			while(k <= 0||k > iFlows.size()/2)
			{
				logger.error("流的ID输入错误，ID必须小于等于"+iFlows.size()/2+" 请重新输入ID:");
				k = Integer.parseInt(JOptionPane.showInputDialog(info));
			}
			
			//TODO
			
			IFlowState iflow = iFlows.get((k-1)*2);  //流ID是从1开始，而index从0开始
			IFlowState iflow_rev = iFlows.get((k-1)*2+1);
			info = "找到流Xid是："+ iflow.flow.getXid() +",  " + iflow.srcIPAddress + " to " + iflow.dstIPAddress + "\n请输入分配的队列号：\n(1代表200M，2代表400M，3代表600M，4代表800M,5代表1000M)";
			
			j = (long)Integer.parseInt(JOptionPane.showInputDialog(info));
			
			flowName = iflow.id;
			flowName_rev = iflow_rev.id;
			
			ArrayList<OFAction> actions = new ArrayList<OFAction>();
			iflow.flow.getActions().clear();      //清除action
			
			OFActionEnqueue enqueue = factory.actions().buildEnqueue()
					.setPort(iflow.flow.getOutPort())
					.setQueueId(j)
					.build();
//			
//			OFActionOutput output =factory.actions().buildOutput()
//					.setPort(iflow.flow.getOutPort())
//					.build();
			
			actions.add(enqueue);
		//	actions.add(output);
			logger.info("action clear is : "+ iflow.flow.getActions());
			iflow.flow.setActions(actions);
			//logger.info("flow table is modified，the Actions is " + actions);
			
			//修改反向流表
			ArrayList<OFAction> actions_rev = new ArrayList<OFAction>();
			iflow_rev.flow.getActions().clear();      //清除action
			
			OFActionEnqueue enqueue_rev = factory.actions().buildEnqueue()
					.setPort(iflow_rev.flow.getOutPort())
					.setQueueId(j)
					.build();
			

			
			actions_rev.add(enqueue_rev);

		//	logger.info("action clear is : "+ iflow.flow.getActions());
			iflow.flow.setActions(actions);
			iflow_rev.flow.setActions(actions_rev);
			
			
			
			
			//TODO
			//OFFlowModify.Builder flowModify = factory.buildFlowModify();
			//Map<String, OFFlowMod> mapFlows = flowPusher.getFlows(dpid);
			OFFlowModify flowModify = FlowModUtils.toFlowModify(iflow.flow.build());     //将FlowAdd转换为FlowModify
			OFFlowModify flowModify_rev = FlowModUtils.toFlowModify(iflow_rev.flow.build());
			//flowPusher.deleteFlow(Integer.toString(flowName));
			
			flowPusher.addFlow(Integer.toString(flowName), flowModify, dpid);
			flowPusher.addFlow(Integer.toString(flowName_rev), flowModify_rev, dpid);
			logger.info("flow table is modified，the flowModify.Actions is " + flowModify.getActions());
			logger.info("reverse flow table is modified，the flowModify.Actions is " + flowModify_rev.getActions());
			//iFlows.get(k-1).flow = (Builder) flowModify;
			
		}
		
		if(e.getKeyCode()==KeyEvent.VK_Z)
		{ 
			//TODO
			//打印流表信息
			Map<String, OFFlowMod> iswflows = flowPusher.getFlows(dpid);
			if(iswflows != null)
			{
				for(String s:iswflows.keySet())
				{
					logger.info("flow size is "+ iswflows.size()+ ",flow Xid is:" + iswflows.get(s).getXid() +" actions is : " + iswflows.get(s).getActions());
				}
			}
			else{
				logger.info("flow size is empty!..." );
				
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_L)
		{
			IOFSwitch swi = switchService.getSwitch(dpid);
			logger.info("sw action is: " + swi.getActions());
			ListenableFuture<?> future;
			List<OFStatsReply> values = null;
			OFStatsRequest<?> req = null;
			req = swi.getOFFactory().buildPortStatsRequest()
					.setPortNo(OFPort.ANY)
					.build();
			try {
				if (req != null) {
					future = swi.writeStatsRequest(req); 
					values = (List<OFStatsReply>) future.get(8 / 2, TimeUnit.SECONDS);
				}
			} catch (Exception e1) {
				logger.error("Failure retrieving statistics from switch {}. {}", sw, e1);
			}
			if(values==null)
			{
				logger.info("null information!");
			}
			else{
				for(OFStatsReply reply:values)
				{
					logger.info("reply xid is:　" + reply.getXid());
				}
			}
		}
		
		if(e.getKeyCode()==KeyEvent.VK_Q)
		{
			String info ="请输入要分配的队列值(0-4):";
			queueID = (long)Integer.parseInt(JOptionPane.showInputDialog(info));
			logger.info("queue id is :" + queueID);
			flowPusher.deleteFlowsForSwitch(dpid);
			iFlows.clear();
		}

		if(e.getKeyCode()==KeyEvent.VK_T)
		{
			//收集带宽线程
			bandwidthCollector bandCollector = new bandwidthCollector();

			t = new Thread(bandCollector);
			t.start();
			logger.info("collect bandwidth thread is running...");
		}
		if(e.getKeyCode()==KeyEvent.VK_S)
		{
			t.stop();
			try {
					bWriter.close();
					fWriter.close();
			} catch (Exception e2) {
					// TODO: handle exception
				e2.printStackTrace();
			}
			
			logger.info("bandwidth collector stopped ...");
		}
		if(e.getKeyCode() == KeyEvent.VK_F)
		{
			//打印流表
			sw = switchService.getSwitch(dpid);
			Map<String, OFFlowMod> iflows = flowPusher.getFlows(dpid);
			logger.info("flow table size is " + iflows.size());
			for(String s : iflows.keySet())
			{
				OFFlowMod flowMod = iflows.get(s);
				//OFFlowStatsRequest req = null;
				OFStatsRequest<?> req = null;
				ListenableFuture<?> future;
				List<OFFlowStatsReply> values = null;
				Match match;
				match = sw.getOFFactory().buildMatch().build();
				match = flowMod.getMatch();
				req = factory.buildFlowStatsRequest()
						.setMatch(match)
						.setOutPort(OFPort.ANY)
						.setTableId(TableId.ALL)
						.build();
				try {
					if (req != null) {
						future = sw.writeStatsRequest(req);
						values = (List<OFFlowStatsReply>) future.get(2, TimeUnit.SECONDS);
					}
				} catch (Exception ex) {
					logger.error("Failure retrieving statistics from switch {}. {}", sw, ex);
				}
				if(values != null)
				{
					for(OFFlowStatsReply statsReply : values)
					{
						
						List<OFFlowStatsEntry> flowStatsEntries = statsReply.getEntries();
						for(OFFlowStatsEntry entry : flowStatsEntries)
						{
							logger.info("flow xid is " + flowMod.getXid() + "flow match is " + flowMod.getMatch() + "reply action is " + entry.getActions() + "byteCount is " + entry.getByteCount().getValue()  );
						}
				}
				}else{
					logger.info("reply is null...");
				}
			
			}	
		}
		
		if(e.getKeyCode() == KeyEvent.VK_M)
		{
			//开始收集flow byte 信息
			numByteCollector byteCollector = new numByteCollector();

			t = new Thread(byteCollector);
			t.start();
			logger.info("collect flow byte thread is running...");
		}
		
		if(e.getKeyCode() == KeyEvent.VK_N)
		{
			t.stop();
			try {
				bWriter.close();
				fWriter.close();
		} catch (Exception e2) {
				// TODO: handle exception
			e2.printStackTrace();
		}
		
		logger.info("byteNum collector stopped ...");
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	private class bandwidthCollector implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			OFPort p1 = OFPort.of(1);
			OFPort p2 = OFPort.of(3);
			OFPort p3 = OFPort.of(3);
			try {
				fWriter = new FileWriter("/home/test/data/bandwidth.txt");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			bWriter = new BufferedWriter(fWriter);
			while(true)
			{
				try{
					Thread.sleep(2000);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				logger.info("port 1 rx bandwidth is : " + statisticsService.getBandwidthConsumption(dpid, p1).getBitsPerSecondRx().getValue()+" port 1 tx bandwidth is : "+statisticsService.getBandwidthConsumption(dpid, p1).getBitsPerSecondTx().getValue());
				logger.info("port 2 rx bandwidth is : " + statisticsService.getBandwidthConsumption(dpid, p2).getBitsPerSecondRx().getValue()+" port 2 tx bandwidth is : "+statisticsService.getBandwidthConsumption(dpid, p2).getBitsPerSecondTx().getValue());
				logger.info("port 3 rx bandwidth is : " + statisticsService.getBandwidthConsumption(dpid, p3).getBitsPerSecondRx().getValue()+" port 3 tx bandwidth is : "+statisticsService.getBandwidthConsumption(dpid, p3).getBitsPerSecondTx().getValue());
				
				try {
					bWriter.write("port 1 rx bandwidth is : " + statisticsService.getBandwidthConsumption(dpid, p1).getBitsPerSecondRx().getValue()+"  port 1 tx bandwidth is : "+statisticsService.getBandwidthConsumption(dpid, p1).getBitsPerSecondTx().getValue());
					bWriter.newLine();
					bWriter.write("port 2 rx bandwidth is : " + statisticsService.getBandwidthConsumption(dpid, p2).getBitsPerSecondRx().getValue()+"  port 2 tx bandwidth is : "+statisticsService.getBandwidthConsumption(dpid, p2).getBitsPerSecondTx().getValue());
					bWriter.newLine();
					bWriter.write("port 3 rx bandwidth is : " + statisticsService.getBandwidthConsumption(dpid, p3).getBitsPerSecondRx().getValue()+"  port 3 tx bandwidth is : "+statisticsService.getBandwidthConsumption(dpid, p3).getBitsPerSecondTx().getValue());
					bWriter.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private class numByteCollector implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			OFPort p1 = OFPort.of(1);
			OFPort p2 = OFPort.of(3);
			OFPort p3 = OFPort.of(3);
			try {
				fWriter = new FileWriter("/home/test/data/numByte.txt");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			bWriter = new BufferedWriter(fWriter);
			sw = switchService.getSwitch(dpid);
			while(true)
			{
				try{
					Thread.sleep(4000);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				Map<String, OFFlowMod> iflows = flowPusher.getFlows(dpid);
				logger.info("flow table size is " + iflows.size());
				for(String s : iflows.keySet())
				{
					OFFlowMod flowMod = iflows.get(s);
					//OFFlowStatsRequest req = null;
					OFStatsRequest<?> req = null;
					ListenableFuture<?> future;
					List<OFFlowStatsReply> values = null;
					Match match;
					match = sw.getOFFactory().buildMatch().build();
					match = flowMod.getMatch();
					req = factory.buildFlowStatsRequest()
							.setMatch(match)
							.setOutPort(OFPort.ANY)
							.setTableId(TableId.ALL)
							.build();
					try {
						if (req != null) {
							future = sw.writeStatsRequest(req);
							values = (List<OFFlowStatsReply>) future.get(2, TimeUnit.SECONDS);
						}
					} catch (Exception ex) {
						logger.error("Failure retrieving statistics from switch {}. {}", sw, ex);
					}
					if(values != null)
					{
						for(OFFlowStatsReply statsReply : values)
						{
							List<OFFlowStatsEntry> flowStatsEntries = statsReply.getEntries();
							for(OFFlowStatsEntry entry : flowStatsEntries)
							{
								logger.info("flow xid is " + flowMod.getXid() + "flow match is " + flowMod.getMatch() + "reply action is " + entry.getActions() + "byteCount is " + entry.getByteCount().getValue()  );
								try {
									bWriter.write("flow xid is " + flowMod.getXid() + "flow match is " + flowMod.getMatch() + "reply action is " + entry.getActions() + " byteCount is " + entry.getByteCount().getValue());
									bWriter.newLine();
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
					}
					}else{
						logger.info("reply is null...");
					}
				
				}	
			}
			
		}
	}
}
