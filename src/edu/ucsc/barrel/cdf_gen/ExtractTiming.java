package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.util.CDFTT2000;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
ExactTiming.java v12.10.27

Description:
   Uses a block of gps time info to create a more exact time variable.
   Ported from MPM's C code.

v12.12.31
   -Removed redundant calculation of rec_i when checking if BarrelTimes array is full
   
   
v12.11.28
   -Fixed null pointer error caused by trying to process data without a linear model

v12.11.27
   -Grabs DataHolder object from CDF_Gen as a member rather than having it passed through all function
   -Rewroked a number of routines to add ability to fill missing time 
   
v12.11.26
   -Fixed lots and lots of bug.
   -Changed offset to adjust the GPS start time to J2000.
   -Uses Calendar date object to calculate time relative to GPS start time
   -Writes values to DataHolder object now.
   
v12.11.20
   -Changed references to Level_Generator to CDF_Gen
   
v12.10.15
   -First Version

*/

public class ExtractTiming {
   //Set some constant values
   private static final int MAX_RECS = 2000;// max number of frames into model           
   private static final double NOM_RATE = 999.89;// nominal ms per frame
   private static final double SPERWEEK = 604800.0;// #seconds in a week
   private static final byte FILLED = 1;//quality bit---ms time filled
   private static final byte NEW_WEEK = 2;// quality bit---replaced week
   private static final byte NEW_MSOFWEEK = 4;// quality bit---replaced msofweek
   private static final short BADFC = 256;// quality bit---bad fc value 
   private static final short BADWK = 512;// quality bit---bad week value
   private static final short BADMS = 1024;// quality bit---bad msofweek
   private static final short BADPPS = 2048;// quality bit---bad PPS
   private static final short NOINFO = 4096;// quality bit---not enough info
   private static final long MSFILL = 0xFFFFFFFF;// fill value for ms_of_week
   private static final byte WKFILL = 0;// fill value for week
   private static final short MINWEEK = 1200;
   private static final short MAXWEEK = 1880;
   private static long GPS_START_TIME =
      new GregorianCalendar(1980, 00, 06).getTime();
   
   //model parameters for a linear fit
   //Example: ms = rate * (fc + offset);
   public class Model{
      private double rate; 
      private double offset;
      
      public Model(double r, double o){
         rate = r;
         offset = o;
      }
      
      public void setRate(double r){rate = r;}
      public void setOffset(double o){offset = o;}
      
      public double getRate(){return rate;}
      public double getOffset(){return offset;}
   }
   
   private class TimePair{
      private double ms;// frame time; ms after GPS 00:00:00 6 Jan 1980
      private long fc;//frame counter

      public void setMS(double t){ms = t;}
      public void setFrame(long f){fc = f;}
      
      public double getMS(){return ms;}
      public long getFrame(){return fc;}
   }
   
   private class BarrelTime{
      private double ms = -1.;//frame time; ms after GPS 00:00:00 1 Jan 2010
      private long fc = -1;//frame counter
      private double ms_of_week = -1.;// ms since 00:00 on Sunday
      private int week = -1;//weeks since 6-Jan-1980
      private long pps = -1;//ms into frame when GPS pps comes
      private short quality = 0;//quality of recovered time
      private long flag = -1;//unused for now
     
      public void setMS(double t){ms = t;}
      public void setFrame(long f){fc = f;}
      public void setMS_of_week(double msw){ms_of_week = msw;}
      public void setWeek(int w){week = w;}
      public void setPPS(long p){pps = p;}
      public void setQuality(short q){quality |= q;}
      public void setFlag(long f){flag = f;}
      
      public double getMS(){return ms;}
      public long getFrame(){return fc;}
      public double getMS_of_week(){return ms_of_week;}
      public int getWeek(){return week;}
      public long getPPS(){return pps;}
      public short getQuality(){return quality;}
      public long getFlag(){return flag;}
      
      public boolean testQuality(short test_pattern){
         if((getQuality() & test_pattern) == test_pattern){
            return true;
         }else{return false;}
      }
   }
   
   //set initial time model
   private Model time_model;
   //set an array of time pairs
   private TimePair[] time_pairs;
   
   //holder for BarrelTime objects
   public BarrelTime[] timeRecs;
   
   private DataHolder data;
   
   public ExtractTiming(){
      //get DataHolder storage object
      data = CDF_Gen.getDataSet();
      
      time_model = null;
      time_pairs = new TimePair[MAX_RECS];
      
      int temp, day, fc, week, ms, pps, cnt;
      timeRecs = new BarrelTime[MAX_RECS];
      //data set index reference
      int FC = 0, DAY = 1, WEEK = 2, MS = 3, PPS = 4;
      int rec_i = 0, data_i = 0;
      
      //loop through all of the frames and generate time models
      for(data_i = 0; data_i < data.getSize(); data_i++){
         rec_i = data_i % MAX_RECS;
         
         //check if the BarrelTimes array is full
         if(rec_i == 0 && data_i > 1){
            //generate a model and fill BarrelTime array
            fillTime(data_i, MAX_RECS);
           
            timeRecs = new BarrelTime[MAX_RECS];
         }
         
         //initialize the BarrelTime object
         timeRecs[rec_i] = new BarrelTime();
         
         //fill a BarrelTime object with data values
         timeRecs[rec_i].setFrame(data.frameNum[data_i]);
         timeRecs[rec_i].setWeek(data.weeks[data_i]);
         
         if(data.ms_of_week[data_i] == 0){ 
            timeRecs[rec_i].setMS_of_week(MSFILL);
         }else{
            timeRecs[rec_i].setMS_of_week(data.ms_of_week[data_i]);
         }
         timeRecs[rec_i].setPPS(data.pps[data_i]);
      }
      
      //process any remaining records
      fillTime(data_i, (rec_i + 1));

      backFillModels();
      
      //calculate epoch data for the DataHolder object
      fillEpoch();
            
      System.out.println("Time corrected.\n");
   }
   
   public void fillTime(int current_data_i, int num_of_recs){
      double temp;
      Model q;
      int pair_cnt, good_cnt, goodfit;
      if (!check(num_of_recs)) return;
   
      pair_cnt = makePairs(num_of_recs);
      
      if(pair_cnt < 2) {
         if(time_model == null){System.out.println(current_data_i);}
         if(evaluateModel(time_model, pair_cnt)){
            updateTimes(current_data_i, num_of_recs);
         }
         else{
            for (int rec_i = 0; rec_i < num_of_recs; rec_i++){
               timeRecs[rec_i].setQuality(NOINFO);
               data.quality[current_data_i - num_of_recs + rec_i] |= NOINFO; 
            }
         }
      }else{
         good_cnt = selectPairs(pair_cnt);
         
         //Try to create a new model
         q = genModel(good_cnt);
         if (evaluateModel(q, good_cnt)) {
            time_model = new Model(q.getRate(), q.getOffset());
         }
         
         //Make sure we have a model
         if(time_model != null){
            updateTimes(current_data_i, num_of_recs);
         }else{//or just set quality bits to noinfo
            for (int rec_i = 0; rec_i < num_of_recs; rec_i++){
               timeRecs[rec_i].setQuality(NOINFO);
               data.quality[current_data_i - num_of_recs + rec_i] |= NOINFO; 
            }
         }
      }
      
      // printf("Using model time(ms) = %17.13lf(fc + %19.9lf)\n",
      //model.rate, model.offset);
   }
   
   public int makePairs(int cnt){
      int goodcnt = 0;
      Calendar date = Calendar.getInstance();
      
      //Make sure there are a good number of records
      if (cnt <= 0 || cnt > MAX_RECS){return 0;}

      for(int rec_i = 0; rec_i < cnt; rec_i++) {
         if(timeRecs[rec_i].getMS_of_week() != MSFILL 
            && timeRecs[rec_i].getWeek() != WKFILL
         ) {
            time_pairs[goodcnt] = new TimePair();
            
            //set a date object to gps start time
            date.setTimeInMillis(GPS_START_TIME);
            date.add(Calendar.WEEK_OF_YEAR, timeRecs[rec_i].getWeek());
            
            if (timeRecs[rec_i].getPPS() < 241) {
               date.add(
                  Calendar.MILLISECOND, 
                  (int)(
                     timeRecs[rec_i].getMS_of_week() - 
                     timeRecs[rec_i].getPPS()
                  )
               );
               time_pairs[goodcnt].setMS(date.getTimeInMillis());
            } else {
               date.add(
                  Calendar.MILLISECOND, 
                  (int)(
                     timeRecs[rec_i].getMS_of_week() + 1000 - 
                     timeRecs[rec_i].getPPS()
                  )
               );
               time_pairs[goodcnt].setMS(date.getTimeInMillis());
            }
            time_pairs[goodcnt].setFrame(timeRecs[rec_i].getFrame());
            goodcnt++;
         }
      }
      return goodcnt;
   }
   
   public boolean check(int last){
      boolean status = true; //false means the list of times was rejected
      
      if (last < 1){
         //reject the list of times if it is empty 
         return false;
      }

      for(int rec_i = 0; rec_i < last; rec_i++) {
         if(timeRecs[rec_i].getFrame() < 0) {
            timeRecs[rec_i].setQuality(BADFC);
            status = false;
         }
         if(
            timeRecs[rec_i].getWeek() != WKFILL && 
            (
               timeRecs[rec_i].getWeek() < MINWEEK || 
               timeRecs[rec_i].getWeek() > MAXWEEK
            )
         ) {
            timeRecs[rec_i].setQuality(BADWK);
            timeRecs[rec_i].setWeek(WKFILL);
         }
         if(
            timeRecs[rec_i].getMS_of_week() != MSFILL &&
            (
               timeRecs[rec_i].getMS_of_week() < 0 || 
               timeRecs[rec_i].getMS_of_week() > (SPERWEEK * 1000)
            )
         ) {
            timeRecs[rec_i].setQuality(BADMS);
            timeRecs[rec_i].setMS_of_week(MSFILL);
         }
         if(timeRecs[rec_i].getPPS() == 0xFFFF){timeRecs[rec_i].setPPS(0);}
         
         if(timeRecs[rec_i].getPPS() > 1000){timeRecs[rec_i].setQuality(BADPPS);}
      }
      
      return status;
   }
   
   public int selectPairs(int m){
      double[] offsets = new double[m];
      double med;
      int last_pair_i;

      //not enough pairs to continue
      if (m < 2){return m;}
      
      //get offsets from set of time pairs
      for (int pair_i = 0; pair_i < m; pair_i++){
         offsets[pair_i] = 
            time_pairs[pair_i].getMS() - 
            (NOM_RATE * time_pairs[pair_i].getFrame());
      }
      
      med = median(offsets);
      
      last_pair_i = 0;
      for (int pair_i = 0; pair_i < m; pair_i++) {
         if (Math.abs(offsets[pair_i] - med) > 200.0){continue;}
         
         if (last_pair_i != pair_i){
            time_pairs[last_pair_i] = time_pairs[pair_i];
         }
         
         last_pair_i++;
      }
      
      return last_pair_i;
   }
   
   public double median(double[] list){
      double[] sortedList = new double[list.length];
      
      //copy the input list and sort it
      System.arraycopy(list, 0, sortedList, 0, list.length);
      
      Arrays.sort(sortedList);
      
      if(list.length > 2){
         return sortedList[(int) (sortedList.length / 2) + 1];
      }else{
         return sortedList[0];
      }
   }
   
   public Model genModel(int n){
      Model linfit = new Model(0.0,0.0);
      double  mux = 0., muy = 0., xy = 0., xx = 0.;
      double m, x, y;

      //not enough points to generate model
      if (n < 3) {return null;}

      //get mean x and y values
      for (int pnt_i = 0; pnt_i < n; pnt_i++) {
         mux += time_pairs[pnt_i].getFrame();
         muy += time_pairs[pnt_i].getMS();
      }
      mux /= n;
      muy /= n;
      
      //calculate the least square regression
      for (int pnt_i = 0; pnt_i < n; pnt_i++) {
         x = time_pairs[pnt_i].getFrame() - mux;
         y = time_pairs[pnt_i].getMS() - muy;
         xx += x * x;
         xy += x * y;
      }
      m = xy / xx;
      
      //save the model
      linfit.setRate(m);
      linfit.setOffset(muy / m - mux);
      
      System.out.println(linfit.getRate() + " " + linfit.getOffset());
      
      return linfit;
   }

   public boolean evaluateModel(Model test_model, int cnt){
      if(test_model == null || cnt < 2){
         return false;
      }
      else if (cnt == 2) {
         double ms1 = 
            test_model.getRate() * 
               (time_pairs[0].getFrame() + test_model.getOffset());
         double ms2 = 
            time_model.getRate() * 
               (time_pairs[1].getFrame() + time_model.getOffset());
      
         //test to see if model gives less than 50ms change
         if(Math.abs(ms1 - ms2) < 0.5 && 
            Math.abs(ms2 - time_pairs[0].getMS()) < 0.5
         ){
            return true;
         }
         
         else{return false;}
      }
      else{
          //FIX THIS
          return true;
       }
   }
   
   public void updateTimes(int current_data_i, int num_of_recs){
      int data_i=0;
      if(true){
         for(int rec_i = 0; rec_i < num_of_recs; rec_i++) {
            data_i = current_data_i - num_of_recs + rec_i;
            
            timeRecs[rec_i].setMS( 
               time_model.getRate() * 
               (timeRecs[rec_i].getFrame() + time_model.getOffset())
            );
            timeRecs[rec_i].setQuality(FILLED);
            
            data.time_model_offset[data_i] = time_model.getOffset();
            data.time_model_rate[data_i] = time_model.getRate();
            data.ms_since_epoch[data_i] = timeRecs[rec_i].getMS();
         }
      }
   }
   
   public void backFillModels(){
      double last_offset = -999, last_rate = -999;
      
      for(int data_i = data.getSize() - 1; data_i >= 0 ; data_i--){
         if((data.quality[data_i] & NOINFO) != NOINFO){
            last_offset = data.time_model_offset[data_i];
            last_rate = data.time_model_rate[data_i];
         }
         else if(last_offset != -999 && last_rate != -999){
            data.time_model_offset[data_i] = last_offset;
            data.time_model_rate[data_i] = last_rate;
            
            data.ms_since_epoch[data_i] = 
               last_rate * (data.frameNum[data_i] + last_offset);
         }
        
      }
   }
   
   public void fillEpoch(){
      Calendar date = Calendar.getInstance();
      
      for(int data_i = 0; data_i < data.getSize(); data_i++){
         
         date.setTimeInMillis((long) data.ms_since_epoch[data_i]);
         
         try{
            data.epoch[data_i] =
               CDFTT2000.fromUTCparts(
                  (double) date.get(Calendar.YEAR), 
                  (double) (date.get(Calendar.MONTH) +1 ), 
                  (double) date.get(Calendar.DAY_OF_MONTH), 
                  (double) date.get(Calendar.HOUR), 
                  (double) date.get(Calendar.MINUTE),
                  (double) date.get(Calendar.SECOND),
                  (double) date.get(Calendar.MILLISECOND)
               );
         }catch(CDFException ex){
            data.epoch[data_i] = 
               data.epoch[data_i - 1] + 1000000; 
         }
      }
   }
}
