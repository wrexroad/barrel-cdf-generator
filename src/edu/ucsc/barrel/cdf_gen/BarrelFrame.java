//BarrelFrame.java

/*
   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   This file is part of The BARREL CDF Generator.

   The BARREL CDF Generator is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   The BARREL CDF Generator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along with 
   The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*/

package edu.ucsc.barrel.cdf_gen;

import java.math.BigInteger;
import java.util.Arrays;

public class BarrelFrame {

   static public final int 
      LAST_DAY_FC = 2010752,
      FC_OFFSET   = 2097152;
   public int
      mod4, mod32, mod40;
   private int
      pps      = Misc.PPS_FILL,
      payID    = Misc.PAYLOADID_FILL, 
      ver      = Misc.VERSION_FILL,
      sats     = HKPG.SATS_FILL,
      offset   = HKPG.UTC_OFFSET_FILL,
      termStat = HKPG.TERM_STAT_FILL,
      modemCnt = HKPG.MODEM_CNT_FILL,
      cmdCnt   = HKPG.CMD_CNT_FILL,
      dcdCnt   = HKPG.DCD_CNT_FILL,
      hkpg     = HKPG.RAW_SENSOR_FILL,
      week     = HKPG.WEEK_FILL,
      rcnt     = RCNT.RAW_CNT_FILL,
      gps      = Ephm.RAW_GPS_FILL;
   private long
      fc       = BarrelCDF.FC_FILL;
   public int[]
      mspc     = new int[12],
      sspc     = new int[8];
   public int[][]
      mag      = {
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL},
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL},
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL},
                  {Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL, Magn.RAW_MAG_FILL}
               },
      fspc     = null;

   private boolean
      valid       = true,
      fc_rollover = false;
   private FrameHolder frame_holder;
 
   public BarrelFrame(final BigInteger frame, final FrameHolder fh){
      this.frame_holder = fh;

      //Breakdown frame counter words: 
      //save the frame counter parts as temp variables,
      //they will be written to the main structure once rec_num is calculated.
      //First 5 bits are version, next 6 are id, last 21 are FC
      this.valid = this.setDPUVersion(
         frame.shiftRight(1691).and(BigInteger.valueOf(31)).intValue()
      );

      //make sure this frame belongs to this payload
      int dpuID = this.frame_holder.getDpuId();
      this.valid = this.setPayloadID(
         frame.shiftRight(1685).and(BigInteger.valueOf(63)).intValue(), dpuID
      );
      if(!this.valid){
         CDF_Gen.log.writeln(
            "Found frame from dpu " + this.payID + " should be dpu " + dpuID
         );
         return;
      }

      this.valid = this.setFrameCounter(
         frame.shiftRight(1664).and(BigInteger.valueOf(2097151)).intValue()
      );
      if(!this.valid){return;}

      this.valid = this.setGPS(
         frame.shiftRight(1632).and(BigInteger.valueOf(4294967295L)).intValue()
      );
/*
      //sets the current record number
      rec_num_1Hz++;
      rec_num_4Hz = (rec_num_1Hz) * 4;
      rec_num_20Hz = (rec_num_1Hz) * 20;
      try{
         if((tmpFC - mod4) != frame_mod4[rec_num_mod4]){
            //check if the medium spectrum is complete
            if(mspc_frames != 4){
               mspc_q[rec_num_mod4] = Constants.PART_SPEC;
            }
            mspc_frames = 0;

            rec_num_mod4++;
         }
         if((tmpFC - mod32) != frame_mod32[rec_num_mod32]){
            //check if the medium spectrum is complete
            if(sspc_frames != 32){
               sspc_q[rec_num_mod32] = Constants.PART_SPEC;
            }
            sspc_frames = 0;

            rec_num_mod32++;
         }
         if((tmpFC - mod40) != frame_mod40[rec_num_mod40]){
            rec_num_mod40++;
         }
      }catch(ArrayIndexOutOfBoundsException ex){
         rec_num_mod4 = 0;
         rec_num_mod32 = 0;
         rec_num_mod40 = 0;
      }

      //save the info from the frame counter word
      ver[rec_num_1Hz] = this.version;
      payID[rec_num_1Hz] = tmpPayID;
      frame_1Hz[rec_num_1Hz] = (int)tmpFC;

      //figure out the other time scale frame counters
      for(int rec_i = rec_num_4Hz; rec_i < rec_num_4Hz + 4; rec_i++){
         frame_4Hz[rec_i] = frame_1Hz[rec_num_1Hz];
      }
      for(int rec_i = rec_num_20Hz; rec_i < rec_num_20Hz + 20; rec_i++){
         frame_20Hz[rec_i] = frame_1Hz[rec_num_1Hz];
      }
     
      //calculate and save the first frame number of the current group
      frame_mod4[rec_num_mod4] = frame_1Hz[rec_num_1Hz] - mod4;
      frame_mod32[rec_num_mod32] = frame_1Hz[rec_num_1Hz] - mod32;
      frame_mod40[rec_num_mod40] = frame_1Hz[rec_num_1Hz] - mod40;
*/    
    
      //GPS PPS
      this.valid = this.setPPS(
         frame.shiftRight(1616).and(BigInteger.valueOf(65535)).intValue()
      );

      //mag data 4 sets of xyz vectors. 24 bits/component
      for(int sample_i = 0; sample_i < this.mag.length; sample_i++){
         this.valid = this.setMag(
            Magn.X_AXIS,
            sample_i,
            frame.shiftRight(1592 - (72 * sample_i)).
               and(BigInteger.valueOf(16777215)).intValue()
         );
         this.valid = this.setMag(
            Magn.Y_AXIS,
            sample_i,
            frame.shiftRight(1568 - (72 * sample_i)).
               and(BigInteger.valueOf(16777215)).intValue()
         );
         this.valid = this.setMag(
            Magn.Z_AXIS,
            sample_i,
            frame.shiftRight(1544 - (72 * sample_i)).
               and(BigInteger.valueOf(16777215)).intValue()
         );
      }
      
      //mod40 housekeeping data: 16bits
      this.valid = this.setHousekeeping(
         frame.shiftRight(1312).and(BigInteger.valueOf(65535)).intValue()
      );
      
      //fast spectra: 20Hz data
      //initialize the fspc array with the correct number of channels
      Channel[] channels = FSPC.getChannels(this.ver);
      this.fspc = new int[channels.length][20];
      for(int sample = 0; sample < 20; sample++){
         this.valid = this.setFSPC(
            channels,
            sample, 
            frame.shiftRight(1264 - sample * 48).and(
            //frame.shiftRight(1296 - sample * 48).and(
               BigInteger.valueOf(281474976710655L)
            )
         );
      }
       
      //medium spectra: 12 channels per frame, 16 bits/channel
      Arrays.fill(this.mspc, MSPC.RAW_CNT_FILL);
      for(int chan_i = 0; chan_i < this.mspc.length; chan_i++){
         this.setMSPC(
            chan_i,
            frame.shiftRight(336 - (16 * chan_i)).
            and(BigInteger.valueOf(65535)).intValue()
         );
      }

      //slow spectra: 8 channels per frame, 16 bits/channel
      Arrays.fill(this.sspc, SSPC.RAW_CNT_FILL);
      for(int chan_i = 0; chan_i < this.sspc.length; chan_i++){
         this.valid = this.setSSPC(
            chan_i,
            frame.shiftRight(144 - (16 * chan_i))
               .and(BigInteger.valueOf(65535)).intValue()
         );
      }

      //rate counter: mod4 data, 16bits
      this.valid = this.setRateCounter(
         frame.shiftRight(16).and(BigInteger.valueOf(65535)).intValue()
      );
   }

   public boolean setFrameCounter(final int f){
      long last_fc = this.frame_holder.getLastFC();
      String payload = this.frame_holder.getPayload();

      //validate frame number
      if (f < Constants.FC_MIN || f > Constants.FC_MAX) {
         this.fc = BarrelCDF.FC_FILL;
         System.out.println("Bad frame counter: " + fc);
         return false;
      } else {
         this.fc = f;
      }

      //check for fc rollover
      if(this.frame_holder.fcRollover()){
         this.fc_rollover = true;
         this.fc += FC_OFFSET;
      } else {
         if ((last_fc - this.fc) > LAST_DAY_FC) {
            //rollover detected
            this.fc_rollover = true;
            
            this.fc += FC_OFFSET;

            System.out.println(
               "Payload " + payload + " rolled over after fc = " + last_fc 
            );

            //create an empty file to indicate rollover
            (new Logger("fc_rollovers/" + payload)).close();
         }
      }

      //get multiplex info
      this.mod4  = (int)this.fc % 4;
      this.mod32 = (int)this.fc % 32;
      this.mod40 = (int)this.fc % 40;

      return true;
   }

   public boolean setPayloadID(final int payID, final int dpuID){
      this.payID = payID;
      if(payID != dpuID){
         return false;
      }
      return true;
   }

   public boolean setPPS(final int pps){
      this.pps = pps;
      if((this.pps < Constants.PPS_MIN) || (this.pps > Constants.PPS_MAX)){
         //make sure the value is not out of range because of an early pps
         if(this.pps != 65535){
            this.pps = Constants.PPS_FILL;
            //pps_q[rec_num_1Hz] |= Constants.OUT_OF_RANGE;
            return false;
         }
      }

      return true;
   }

   public boolean setDPUVersion(final int ver){
      this.ver = ver;
      return true;
   }

   public boolean setNumSats(final int sats){
      this.sats = sats;
      return true;
   }

   public boolean setUTCOffset(final int offset){
      this.offset = offset;
      return true;
   }

   public boolean setTermStatus(final int termStat){
      this.termStat = termStat;
      return true;
   }

   public boolean setModemCount(final int modemCnt){
      this.modemCnt = modemCnt;
      return true;
   }

   public boolean setDcdCount(final int dcdCnt){
      this.dcdCnt = dcdCnt;
      return true;
   }

   public boolean setWeek(final int week){
      this.week = week;
      return true;
   }

   public boolean setCommandCounter(final int cmdCnt){
      this.cmdCnt = cmdCnt;
      return true;
   }

   public boolean setFSPC(Channel[] channels, int sample, BigInteger raw){
      boolean valid = true;
      for(int ch_i = 0; ch_i < channels.length; ch_i++){
         this.fspc[ch_i][sample] = 
            raw.
            shiftRight(channels[ch_i].start).
            and(BigInteger.valueOf(channels[ch_i].width))
            .intValue();
         if(
            (this.fspc[ch_i][sample] < Constants.FSPC_RAW_MIN) ||
            (this.fspc[ch_i][sample] > Constants.FSPC_RAW_MAX)
         ){
            this.fspc[ch_i][sample] = FSPC.CNT_FILL;
            //fspc_q[ch_i] |= Constants.OUT_OF_RANGE;
            valid = false;
         }
      }
      return valid;

   }

   public boolean setMSPC(final int chan_i, final int mspc){
      if ((mspc < Constants.MSPC_RAW_MIN) || (mspc > Constants.MSPC_RAW_MAX)) {
         this.mspc[chan_i] = MSPC.RAW_CNT_FILL;
         //this.mspc_q |= Constants.OUT_OF_RANGE;
         return false;
      } else {
         this.mspc[chan_i] = mspc;
         return true;
      }
   }

   public boolean setSSPC(final int chan_i, final int sspc){
      if ((sspc < Constants.SSPC_RAW_MIN) || (sspc > Constants.SSPC_RAW_MAX)) {
         this.sspc[chan_i] = SSPC.RAW_CNT_FILL;
         //sspc_q |= Constants.OUT_OF_RANGE;
         return false;
      } else {
         this.sspc[chan_i] = sspc;
         return true;
      }
   }

   public boolean setHousekeeping(final int hkpg){
      boolean valid = true;
      if((hkpg < Constants.HKPG_MIN) || (hkpg > Constants.HKPG_MAX)){
         this.hkpg = HKPG.RAW_SENSOR_FILL;
         //this.hkpg_q |= Constants.OUT_OF_RANGE;
         return false;
      } else {
         this.hkpg = hkpg;
      }

      switch(this.mod40){
         case 36:
            this.sats   = this.hkpg >> 8;
            this.offset = this.hkpg & 255;

            if((this.sats < Constants.SATS_MIN) ||
               (this.sats > Constants.SATS_MAX)
            ){
               this.sats = Constants.SATS_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }

            if(
               (this.offset < Constants.LEAP_SEC_MIN) ||
               (this.offset > Constants.LEAP_SEC_MAX)
            ){
               this.offset = Constants.LEAP_SEC_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }

            break;

         case 37:
            this.week = this.hkpg;
            if(
               (this.week < Constants.WEEKS_MIN) ||
               (this.week > Constants.WEEKS_MAX)
            ){
               this.week = HKPG.WEEK_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }

            break;

         case 38:
            this.termStat = this.hkpg >> 15;
            this.cmdCnt = this.hkpg & 32767;

            if(
               (this.termStat < Constants.TERM_STAT_MIN) ||
               (this.termStat > Constants.TERM_STAT_MAX)
            ){
               this.termStat = Constants.TERM_STAT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            if(
               (this.cmdCnt < Constants.CMD_CNT_MIN) ||
               (this.cmdCnt > Constants.CMD_CNT_MAX)
            ){
               this.cmdCnt = Constants.CMD_CNT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            break;
         case 39:
            this.dcdCnt = this.hkpg >> 8;
            this.modemCnt = this.hkpg & 255;
            if(
               (this.dcdCnt < Constants.DCD_CNT_MIN) ||
               (this.dcdCnt > Constants.DCD_CNT_MAX)
            ){
               this.dcdCnt = Constants.DCD_CNT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            if(
               (this.modemCnt < Constants.MODEM_CNT_MIN) ||
               (this.modemCnt > Constants.MODEM_CNT_MAX)
            ){
               this.modemCnt = Constants.MODEM_CNT_FILL;
               //this.hkpg_q |= Constants.OUT_OF_RANGE;
               valid = false;
            }
            break;
         default:
            break;
      }

      return valid;
   }

   public boolean setRateCounter(final int r){
      if((this.rcnt < Constants.RCNT_MIN) || (this.rcnt > Constants.RCNT_MAX)){
         this.rcnt = RCNT.RAW_CNT_FILL;
         //this.rcnt_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;
         return false;
      } 

      this.rcnt = r;
      return true;
   }

   public boolean setGPS(final int gps){
      boolean valid = true;
      this.gps = gps;

      //get gps info: 32 bits of mod4 gps data followed by 16 bits of pps data
      switch(this.mod4){
         case Ephm.ALT_I:
            if(
               (this.gps < Constants.ALT_RAW_MIN) ||
               (this.gps > Constants.ALT_RAW_MAX)
            ){
               this.gps = Ephm.RAW_GPS_FILL;
               //gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
               valid = false;
            }
            else if(this.gps < Constants.MIN_SCI_ALT){
               /*gps_q[rec_num_mod4] |= Constants.LOW_ALT;
               pps_q[rec_num_1Hz] |= Constants.LOW_ALT;
               hkpg_q[rec_num_mod40] |= Constants.LOW_ALT;
               rcnt_q[rec_num_mod4] |= Constants.LOW_ALT;
               mspc_q[rec_num_mod4] |= Constants.LOW_ALT;
               sspc_q[rec_num_mod32] |= Constants.LOW_ALT;
               for(int lc_i = 0; lc_i < 20; lc_i++){
                  fspc_q[rec_num_1Hz + lc_i] |= Constants.LOW_ALT;
               }
               for(int mag_i = 0; mag_i < 4; mag_i++){
                  magn_q[rec_num_1Hz + mag_i] |= Constants.LOW_ALT;
               }*/
               valid = false;
            }
            break;
         case Ephm.TIME_I:
            if(
               (this.gps < Constants.MS_WEEK_MIN) ||
               (this.gps > Constants.MS_WEEK_MAX)
            ){
               this.gps = Ephm.RAW_GPS_FILL;
               //gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
               valid = false;
            }
            break;
         case Ephm.LAT_I:
         case Ephm.LON_I:
            break;
         default:
            valid = false;
            break;
      }
      return valid;
   }

   public boolean setMag(final int axis, final int sample, final int mag) {
      if((mag < Constants.MAG_MIN) || (mag > Constants.MAG_MAX)){
         this.mag[sample][axis] = Magn.RAW_MAG_FILL;
         //magn_q[rec_num_4Hz] |= Constants.OUT_OF_RANGE;
         return false;
      }

      this.mag[sample][axis] = mag;
      return true;
   }

   public boolean fcRollover() {
      return this.fc_rollover;
   }

   public long getFrameCounter(){
      return this.fc;
   }

   public int getPayloadID(){
      return this.payID;
   }

   public int getPPS(){
      return this.pps;
   }

   public int getDPUVersion(){
      return this.ver;
   }

   public int getNumSats(){
      return this.sats;
   }

   public int getUTCOffset(){
      return this.offset;
   }

   public int getTermStatus(){
      return this.termStat;
   }

   public int getModemCount(){
      return this.modemCnt;
   }

   public int getDcdCount(){
      return this.dcdCnt;
   }

   public int getWeek(){
      return this.week;
   }

   public int getCommandCounter(){
      return this.cmdCnt;
   }

   public int[][] getFSPC(){
      return this.fspc;
   }

   public int[] getMSPC(){
      return this.mspc;
   }

   public int[] getSSPC(){
      return this.sspc;
   }

   public int getHousekeeping(){
      return this.hkpg;
   }

   public int getRateCounter(){
      return this.rcnt;
   }

   public int getGPS(){
      return this.gps;
   }

   public int[][] getMag(){
      return this.mag;
   }
}
