package net.floodlightcontroller.meterqos;

import java.util.ArrayList;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMeterMod;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.IOFSwitch;

public class DropMeter {
	
	public static enum Cmd {
        ADD(0),
        MODIFY(1),
        DELETE(2);
  
        int cmd;
        Cmd(int code) {
            cmd = code;
        }
  
        public int getCode() {
            return cmd;
        }
    }
  
    protected DatapathId swId; /* Switch ID */
    protected int flags,       /* Meter flags */
                  rate,        /* Meter band drop rate */
                  id,          /* Meter ID */
                  burstSize;   /* Burst control rate */
 
 
    public DropMeter(DatapathId swId, int id, int flags, int rate, int burst) {
        this.swId = swId;
        this.flags = flags;
        this.rate = rate;
        this.id = id;
        this.burstSize = burst;
    }
    public void write(Cmd cmd) {
        OFFactory meterFactory = OFFactories.getFactory(OFVersion.OF_13);
        OFMeterMod.Builder meterModBuilder = meterFactory.buildMeterMod()
            .setMeterId(id)
            .setCommand(cmd.getCode());
 
        switch(cmd) {
        case ADD:
        case MODIFY:
            /* Create and set meter band */
            OFMeterBandDrop.Builder bandBuilder = meterFactory.meterBands().buildDrop()
                .setRate(rate);
            if (this.burstSize != 0) {
                bandBuilder = bandBuilder.setBurstSize(this.burstSize);
            }
            OFMeterBand band = bandBuilder.build();
            List<OFMeterBand> bands = new ArrayList<OFMeterBand>();
            bands.add(band);
  
            /* Create meter modification message */
            meterModBuilder.setMeters(bands)
                .setFlags(flags)
                .build();
  
            break;
        case DELETE:;
        }
  
        /* Send meter modification message to switch */
        IOFSwitch sw = MeterQos.switchService.getSwitch(swId); /* The IOFSwitchService */
        sw.write(meterModBuilder.build());
    }
}
