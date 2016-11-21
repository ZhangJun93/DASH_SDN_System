package net.floodlightcontroller.bandwidthlim;

import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

import ch.qos.logback.classic.Logger;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;

public class IFlowState {

	private IOFSwitch sw;
	private OFPacketIn pi;
	private Ethernet eth;
	private FloodlightContext cntx;
	private  int n;          //选择的队列编号
	private String s;        //指明是“DST",还是”SRC"
	
	protected int id;
	protected IPv4 ipv4;	
	protected IPv4Address srcIPAddress;
	protected IPv4Address dstIPAddress;
    protected TransportPort srcPort;
    protected TransportPort dstPort;
    protected TCP tcp;
	protected OFFlowAdd.Builder flow;
    
	//protected static Logger logger;
	
	
	public IFlowState(IOFSwitch sw,OFPacketIn pi,Ethernet eth,FloodlightContext cntx,String s,int id) {
		// TODO Auto-generated constructor stub
		this.sw = sw;
		this.pi = pi;
		this.eth = eth;
		this.cntx = cntx;
		this.s = s;
		this.id = id;
		
		this.ipv4 = (IPv4) eth.getPayload();

		this.srcIPAddress = ipv4.getSourceAddress();
		this.tcp = (TCP) ipv4.getPayload();
		this.dstIPAddress = ipv4.getDestinationAddress();
		this.srcPort = tcp.getSourcePort();
		this.dstPort = tcp.getDestinationPort();
		//logger.info("流"+this.id+": 从" + srcIPAddress +":" + srcPort + " to " + dstIPAddress + ":" +dstPort + " 正在被监听...");
	}
	
	
	public IOFSwitch getSwitch() {
		return sw;
	}
	public void setSwitch(IOFSwitch sw) {
		this.sw = sw;
	}
	public OFPacketIn getPacketIn() {
		return pi;
	}
	public void setPacketIn(OFPacketIn pi) {
		this.pi = pi;
	}
	public Ethernet getEth() {
		return eth;
	}
	public void setEth(Ethernet eth) {
		this.eth = eth;
	}
	public FloodlightContext getCntx() {
		return cntx;
	}
	public void setCntx(FloodlightContext cntx) {
		this.cntx = cntx;
	}
	public int getQueneNum() {
		return n;
	}
	public void setQueneNum(int n) {
		this.n = n;
	}
	public String getState() {
		return s;
	}
	public void setState(String s) {
		this.s = s;
	}

}
