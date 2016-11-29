package net.floodlightcontroller.meterqos;

import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModify;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionMeter;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

import ch.qos.logback.classic.Logger;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.FlowModUtils;

public class MeterQos extends JFrame implements IFloodlightModule, IOFMessageListener,KeyListener {

	protected IFloodlightProviderService floodlightService;
	protected IDeviceService deviceService;
	protected  static IStaticFlowEntryPusherService flowPusher;  //使用流下发函数
	protected IStorageSourceService storageSource; //消息储存
	protected static IOFSwitchService switchService;    //获取交换机	
    protected IStatisticsService statisticsService;//收集带宽信息 
	
	protected static  OFFactory my13Factory;
	protected NodePortTuple portNum;
	protected static Logger logger;
	protected static DatapathId dpid;
	protected IOFSwitch sw;
	
	protected Algorithm algorithm;
	
	protected static int ID = 1;
	
	protected static Map<Integer,Long> meterFlowMap;
	protected Map<Integer, Integer> rateMap;
	protected static Map<String,Integer> IpMeterMap;
	
	IPv4Address serverIP;
	
	protected static SimpleServer simpleServer;
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return MeterQos.class.getName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		boolean bool = false;
		if((type==OFType.PACKET_IN)&&(name.equals("forwarding")|| name.equals("devicemanager")))
		{
			bool = true;
			System.out.println("bool true****");
		}
		return bool;
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		// TODO Auto-generated method stub
		OFPacketIn packetIn = (OFPacketIn) msg;
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		switch(msg.getType()){
		case PACKET_IN:
			logger.info("packet_in recieved!...");
			
			logger.info("eth type : "+ eth.getEtherType());
	//		logger.info(" ipv4 type: " + EthType.IPv4.getValue());
	//		if(eth.getPayload() instanceof IPv4)
			if(eth.getEtherType() == EthType.IPv4)
			{
			//	logger.debug("ipv4...");
				System.out.println("PACKET instanceof IPv4 : true");
				/* We got an IPv4 packet; get the payload from Ethernet */
	            IPv4 ipv4 = (IPv4) eth.getPayload();
	            IPv4Address srcIPAddress = ipv4.getSourceAddress();
            	IPv4Address dstIPAddress = ipv4.getDestinationAddress();
	            /* Various getters and setters are exposed in IPv4 */
   //         	logger.info("srcIP is: " + srcIPAddress + " dstIP is: " + dstIPAddress + " packet: "  );
            	        	
	           // if(ipv4.getProtocol().equals(IpProtocol.TCP)&&(srcIPAddress.equals(iPv4Address1)||srcIPAddress.equals(iPv4Address3)||srcIPAddress.equals(iPv4Address3)||srcIPAddress.equals(iPv4Address4)))
	            //if(ipv4.getProtocol().equals(IpProtocol.TCP)&&dstIPAddress.equals(serverIP))
            	if(ipv4.getProtocol().equals(IpProtocol.TCP))
            	{
//            		if(srcIPAddress.equals(serverIP)||dstIPAddress.equals(serverIP))
//            		{
            			
            		
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
//		            	if(IpMeterMap.containsKey(dstIPAddress.toString()))
//		            	{
//		            		//Do None
//		            	}else		            	
		            	if(srcIPAddress.equals(serverIP)||dstIPAddress.equals(serverIP)){
		            	//	creatFlowTable(sw, packetIn, eth, cntx);
		            		String info = "收到一个PAKET_IN消息，IP：" + srcIPAddress + " to " + dstIPAddress ;
			                logger.info(info);
		    				//addFlowModLim(sw, packetIn, eth, cntx,dstTcp,flowState,flowState_rev);
			                IpMeterMap.put(dstIPAddress.toString(), ID);
			                creatFlowTable(sw, packetIn, eth, cntx);
		            	}	
            		
	    				
	            }else if(ipv4.getProtocol().equals(IpProtocol.UDP))
	            {
	            	logger.info("udp packet recieved ...");
	            	
	       //     	UDP udp = (UDP) ipv4.getPayload();
	            	
	            	String info = "收到一个PAKET_IN消息，IP：" + srcIPAddress + " to " + dstIPAddress ;
	            	
	            	System.out.println(info);
//	            	Map<String, OFFlowMod> iflows = flowPusher.getFlows(dpid);
//	    			logger.info("flow table size is " + iflows.size());
//	    			for(String s : iflows.keySet())
//	    			{
//	    				OFFlowMod flowMod = iflows.get(s);
//	    				IPv4Address srcAddress = flowMod.getMatch().get(MatchField.IPV4_SRC);
//	    				if(srcAddress.equals(ipv4.getSourceAddress()))
//	    				{
//	    					creatFlowTable(sw, packetIn, eth, cntx);
//	    				}
//	    			}
	    			
//	    			if(IpMeterMap.containsKey(srcIPAddress.toString()))
//	            	{
//	            		//Do None
//	            	}else{
//	            	//	creatFlowTable(sw, packetIn, eth, cntx);
//	            	}
	            	
	            }
			}else if (eth.getEtherType() == EthType.ARP) {
				logger.info("arp recieved...");
				logger.debug("arp...");
			}			
			
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
		// TODO Auto-generated method stub
		floodlightService = context.getServiceImpl(IFloodlightProviderService.class);
		flowPusher = context.getServiceImpl(IStaticFlowEntryPusherService.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		deviceService = context.getServiceImpl(IDeviceService.class);
		statisticsService = context.getServiceImpl(IStatisticsService.class);
		meterFlowMap = new HashMap<Integer,Long>();
		IpMeterMap = new HashMap<String,Integer>();
		
		rateMap = new HashMap<>();
		int rate = 10000;
		rateMap.put(1, rate);
		rateMap.put(2, rate);
		
		
		logger = (Logger) LoggerFactory.getLogger(MeterQos.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		//1
		//dpid = DatapathId.of("5e:3e:48:6e:73:02:02:f6");	
		
		//dpid = DatapathId.of("00:00:00:00:00:00:00:01");
		logger.info("MeterQos model is running...");
		
		//2
		dpid = DatapathId.of("5e:3e:48:6e:73:02:03:7a");
		floodlightService.addOFMessageListener(OFType.PACKET_IN, this);
		
		serverIP = IPv4Address.of("192.168.100.10");
		sw = switchService.getActiveSwitch(dpid);
		my13Factory = OFFactories.getFactory(OFVersion.OF_13);
		
		//start algorithm
		algorithm = new Algorithm().getInstance();
	    Thread t = new Thread(algorithm);
	    t.start();
//		
//		try {
//			simpleServer = new SimpleServer();
//			SimpleServer.setInstance(simpleServer);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			logger.info("simpleServer start failed...");
//		}
		
		
	
		
		
		this.setSize(1000, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.addKeyListener(this);
	}
		
	
	private void creatFlowTable(IOFSwitch sw, OFPacketIn pi, Ethernet eth, FloodlightContext cntx )
	{
		DropMeter.Cmd cmd = DropMeter.Cmd.ADD;
		int rate;
		
		IPv4 ip = (IPv4) eth.getPayload();
		MacAddress srcMacAddress = eth.getSourceMACAddress();
		MacAddress dstMacAddress = eth.getDestinationMACAddress();
		
		//catch null outport exception
		try{
			OFPort outPort = deviceService.findDevice(dstMacAddress, VlanVid.ZERO, IPv4Address.NONE
				,IPv6Address.NONE, DatapathId.NONE, OFPort.ZERO)
						 .getAttachmentPoints()[0].getPort();
		
		
		logger.info("ID: "+ ID + " recieved a PacketIn，src IP is " + ip.getSourceAddress() + " ,dst IP is " + ip.getDestinationAddress());
		
//		String info = "ID: "+ ID + " recieved a PacketIn，src IP is " + ip.getSourceAddress() + " ,dst IP is " + ip.getDestinationAddress() + ",please input the rate of meter：";
//		rate = Integer.parseInt(JOptionPane.showInputDialog(info));
//		if((ID % 4 == 1)||(ID % 4 == 2))
//		{
//			rate = rateMap.get(1);
//		}else{
//			rate = rateMap.get(2);
//		}
		rate = 700;
		int burst = 2000;
		DropMeter dropMeter = new DropMeter(dpid, ID, 0, rate, burst);
		dropMeter.write(cmd);
		
		
	//	OFPort inPort = pi.getInPort();

		VlanVid vlanVid = VlanVid.ofVlan(eth.getVlanID());
	//	OFFlowAdd.Builder flow = my13Factory.buildFlowAdd();
		Match.Builder match = my13Factory.buildMatch();
		
		ArrayList<OFAction> actions = new ArrayList<OFAction>();
		
	//	match.setExact(MatchField.IN_PORT, inPort);
		match.setExact(MatchField.ETH_DST, dstMacAddress);
		match.setExact(MatchField.ETH_SRC, srcMacAddress);
		
		
		
		//OFPort ofPort = pi.getMatch().
		
		if(!vlanVid.equals(VlanVid.ZERO))
		{
			match.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlanVid));
			logger.info("vlan id is : " + vlanVid);
		}
	//	IPv4 ip = (IPv4) eth.getPayload();
		match.setExact(MatchField.IPV4_DST, ip.getDestinationAddress());
		match.setExact(MatchField.IPV4_SRC, ip.getSourceAddress());
		match.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		
	//	TCP tcp = (TCP) ip.getPayload();
		match.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
		
		List<OFInstruction> instructions = new ArrayList<OFInstruction>();
		
		OFInstructionMeter meter = my13Factory.instructions().buildMeter()
			    .setMeterId(ID)
			    .build();
			  

		OFActionOutput  output =  my13Factory.actions().buildOutput()
				.setPort(outPort)
				.setMaxLen(0xffFFffFF)
				.build();
		
		actions.add(output);
		OFInstructionApplyActions applyActions = my13Factory.instructions().buildApplyActions()
				.setActions(actions)
				.build();
		
		//OFInstructionApplyActions output = my13Factory.actions().buildOutput()
			  
			/*
			 * Regardless of the instruction order in the flow, the switch is required 
			 * to process the meter instruction prior to any apply actions instruction.
			 */
			instructions.add(meter);
			//TODO
			instructions.add(applyActions);
			  
			/* Flow will send matched packets to meter ID 1 and then possibly output on port 2 */
			OFFlowAdd flowAdd = my13Factory.buildFlowAdd()
				.setMatch(match.build())
			    .setInstructions(instructions)
			    .setPriority(32675)
			    .build();
			flowPusher.addFlow(String.valueOf(flowAdd.getXid()), flowAdd, dpid);
			logger.info("add flow: match is " + flowAdd.getMatch() );
			meterFlowMap.put(ID,flowAdd.getXid());
			ID++;
		}catch (NullPointerException e) {
			// TODO: handle exception
			logger.info("can not find outport ,retry to create table...");
		}
	}
	
	public static void  FlowModify(String ip,int rate,int burst)
	{
		int id;               //meter id
		try{
			id = IpMeterMap.get(ip);
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("can not find meter id");
			e.printStackTrace();
			return;
		}
//		Map<String,OFFlowMod>  FlowModmap = flowPusher.getFlows(dpid);
		
		OFFlowMod flowMod = getFlow(ip);
		
		if(flowMod == null)
		{
			System.out.println("ip: " + ip +" not find...");
			return;
		}else{
	//		xid = flowMod.getXid();
			List<OFInstruction> ofinstructions = new ArrayList<OFInstruction>();
			ofinstructions = flowMod.getInstructions();

			
			DropMeter.Cmd cmd = DropMeter.Cmd.MODIFY;
		
			DropMeter dropMeter = new DropMeter(dpid, id, 0, rate, burst);
			dropMeter.write(cmd);
			OFInstructionMeter meter = my13Factory.instructions().buildMeter()
				    .setMeterId(id)
				    .build();
			
			
			
			ArrayList<OFAction> actions = new ArrayList<OFAction>();
			OFActionOutput  output =  my13Factory.actions().buildOutput()
					.setPort(flowMod.getOutPort())
					.setMaxLen(0xffFFffFF)
					.build();
			actions.add(output);
			OFInstructionApplyActions applyActions = my13Factory.instructions().buildApplyActions()
					.setActions(actions)
					.build();
			
			ofinstructions.add(meter);
			ofinstructions.add(applyActions);
			
			OFFlowAdd flowAdd = my13Factory.buildFlowAdd()
					.setMatch(flowMod.getMatch())
				    .setInstructions(ofinstructions)
				    .setPriority(32675)
				    .build();
			
			OFFlowModify flowModify = FlowModUtils.toFlowModify(flowAdd);
			flowPusher.addFlow(String.valueOf(flowModify.getXid()), flowModify, dpid);
		}
		
		
	}
	
	public static void meterModify(String ip,int rate,int burst)
	{
		logger.info("metermodify: ip " + ip + "rate : " + rate);
		int id=-1;
		try{
			 id= IpMeterMap.get(ip);
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("can not find the meter id of "+ ip);
			System.err.println("can not find the meter id of "+ ip);
		}
		if(id==-1)
		{
			logger.error(" can not find the meter id");
			System.err.println("can not find the meter id");
			return;
		}
		logger.info("trying to modify host ip: " + ip + ",meter id is "+id);
		
		
		DropMeter.Cmd cmd = DropMeter.Cmd.MODIFY;
		DropMeter dropMeter = new DropMeter(dpid, id, 0, rate, burst);
		dropMeter.write(cmd);
		logger.info("modify meter: "+ id +" success...");
		
	}
	private static OFFlowMod getFlow(String ip)
	{

		
		Map<String,OFFlowMod>  FlowModmap = flowPusher.getFlows(dpid);
		
		int meterId;
		long xid;
		try{
			meterId = IpMeterMap.get(ip);
		    xid = meterFlowMap.get(meterId);
		    
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

		for(String k : FlowModmap.keySet())
		{
			OFFlowMod flowMod = FlowModmap.get(k);
			logger.info("match src:"+ String.valueOf(flowMod.getMatch().get(MatchField.IPV4_SRC))+" ip: "+ip);
//			if(String.valueOf(flowMod.getMatch().get(MatchField.IPV4_SRC)).equals(ip))
//			{
//				return flowMod;
//			}
			if(flowMod.getXid()==xid)
			{
				return flowMod;
			}
		}
		
		
		return null;
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode()==KeyEvent.VK_I)
		{
			System.out.println("press button I");
		}
		if(e.getKeyCode()==KeyEvent.VK_C)
		{
			try {
			algorithm.bWriter.close();
			algorithm.fWriter.close();
			System.out.println("success to close fileWriter...");
			} catch (Exception e2) {
			// TODO: handle exception
			e2.printStackTrace();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
