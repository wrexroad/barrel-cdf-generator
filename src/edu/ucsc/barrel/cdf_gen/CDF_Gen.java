/*
CDF_Gen.java

Description:
   Entry point for .jar file.
   Reads ini file.
   Creates all objects needed for operation. 
   
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CDF_Gen{

   public static FrameHolder frames;
   public static ExtractSpectrum spectra;
   public static ExtractTiming barrel_time;

   private static DataCollector dataPull;
   private static LevelZero L0;

   private static ArrayList<String> servers = new ArrayList<String>();
   private static ArrayList<String> payloads = new ArrayList<String>();
   private static Map<String, String> settings = new HashMap<String, String>();

   //Directory and file settings
   private static String output_Dir = "out";
   public static String tlm_Dir;
	public static String L0_Dir;
   public static String L1_Dir;
   public static String L2_Dir;
   public static Logger log;

   public static void main(String[] args){
      int time_cnt = 0;

      float min_alt;

      //array to hold payload id, lauch order, and launch site
		String[] payload = new String[3];

      //create a log file
      log = new Logger("log.txt");

      //ensure there is some user input
      if(args.length == 0){
         System.out.println(
            "Usage: java -jar cdf_gen.jar ini=<ini file> date=<date> L=<levels>"
         );
         System.exit(0);
      }

      //read the ini file and command line arguments
      loadConfig(args);

      //for each payload, create an object to download the files,
      // read the list of data files on each server, then download the files
      for(String payload_i : payloads){
         String
            id = "00",
            flt = "00",
            stn = "0",
            mag = "0000";
         Integer
            date = Integer.valueOf(getSetting("date")),
            start_date = 000000,
            end_date = 999999,
            dpu = 0;


			//break payload apart into id, flight number and launch station
			String[] payload_parts = payload_i.split(",");
			if(payload_parts[0] != null){id = payload_parts[0];}
			if(payload_parts[1] != null){flt = payload_parts[1];}
			if(payload_parts[2] != null){stn = payload_parts[2];}
			if(payload_parts[3] != null){mag = payload_parts[3];}
			if(payload_parts[4] != null){
            dpu = Integer.valueOf(payload_parts[4]);
         }
			if(payload_parts.length > 5 && payload_parts[5] != null){
            start_date = Integer.valueOf(payload_parts[5]);
         }
			if(payload_parts.length > 6 && payload_parts[6] != null){
            end_date = Integer.valueOf(payload_parts[6]);
         }

         //make sure the date we are trying to process is valid for the payload
         if(date < start_date || date > end_date){
            continue;
         }

         //set output paths
         if(getSetting("outDir") != ""){
            //check if user specified a place to store the files
            output_Dir = getSetting("outDir");
         }
         tlm_Dir =
            output_Dir + "/tlm/" + id + "/" + date + "/";
         L0_Dir =
            output_Dir + "/l0/" + id + "/" + date + "/";
         L1_Dir =
            output_Dir + "/l1/" + id + "/";
         L2_Dir =
            output_Dir + "/l2/" + id + "/";

         //set working payload
         settings.put("currentPayload", payload_i);

         //check if we have a minimum reject altitude
         min_alt = (
            getSetting("min_alt").equals("") ?
            5 : Float.parseFloat(settings.get("min_alt"))
         );

         //create a new storage object
         frames = new FrameHolder(payload_i, dpu, min_alt);

         //Figure out where the input files are coming from
         if(getSetting("local") == ""){
            dataPull =
               new DataCollector(tlm_Dir, servers, id, settings.get("date"));

            //read each repository and build a list of data file URLs
            dataPull.getFileList();

            //download each file after the URL list is made
            dataPull.getFiles();
         }else{
            //a telemetry file was provided so instead of creating one we will
            //just change the tlm directory
            tlm_Dir = getSetting("local");
         }

         //Create level zero object and convert the data files to a level 0 file
         try{
            System.out.println("Creating Level Zero...");
            L0 = new LevelZero(
               Integer.parseInt(settings.get("frameLength")),
               settings.get("syncWord"),
               tlm_Dir,
               L0_Dir,
               id,
               flt,
               stn,
               getSetting("date"),
               dpu
            );
            L0.processRawFiles();
            L0.finish();
            System.out.println(
               "Completed Level 0 for payload " + getSetting("currentPayload")
            );

            //If we didn't get enough data, move on to the next payload.
            if(frames.size() > 100){
               int[] fc_range = frames.getFcRange();

               //calculate throughput value
               System.out.println(
                  "Payload " + getSetting("currentPayload") +
                  " Throughput: " +
                  ((100 * frames.size() - 1) / (fc_range[1] - fc_range[0]))
   			      + " %"
   			   );

               //Fill the time variable
               barrel_time = new ExtractTiming(frames);
               if(barrel_time.getTimeRecs() > 2) {
                  barrel_time.fixWeekOffset();
                  barrel_time.fillModels();
               }
               /*
               if(getSetting("L").indexOf("1") > -1){
                  //create Level One 
                  LevelOne L1 = new LevelOne(
                     getSetting("date"),id,flt,stn,L1_Dir,getSetting("dpu")
                  );
                  L1 = null;
               }
               */
               if(getSetting("L").indexOf("2") > -1){
                  //create a set of linear models that track the location of
                  //the 511 line and store them in the DataHolder object
                  int
                     start_i = 0,
                     stop_i = 0,
                     max_recs = 20;

                  System.out.println("Starting Level Two...");

                  //calibrate the mspc and sspc bins
                  System.out.println("Locating 511 line...");
                  spectra = new ExtractSpectrum(frames);

                  spectra.do511Fits(max_recs);
                  //fill511Gaps();

                  //create Level Two
                  LevelTwo L2 =
						   new LevelTwo(
                        getSetting("date"),id,flt,stn,L2_Dir
                     );

                  L2 = null;
               }
            }
         }catch(IOException ex){
            System.out.println(
               ex.getMessage()
            );
         }
      }

      //close the log file
      log.close();
   }

   private static void loadConfig(String[] args){
	   String[] setPair;

	   //read command line arguments
	   for(String arg_i : args){
		   setPair = arg_i.split("=");
		   settings.put(setPair[0].trim(), setPair[1].trim());
	   }

	   //load configuration from ini
	   try{
         FileReader fr = new FileReader(settings.get("ini"));
         BufferedReader iniFile = new BufferedReader(fr);

         String line;
         while( (line = iniFile.readLine()) != null){

            //split off any comments
        	   setPair = line.split("#");
            line = setPair[0];

            //get the key and value pair.
            //Make sure there is only one pair per line
            setPair = line.split("=");
            if(setPair.length == 2){
               //remove leading and trailing whitespace from key and value
            	setPair[0] = setPair[0].trim();
            	setPair[1] = setPair[1].trim();

               if(setPair[0].equals("payload")){
                  //Determine what payloads to read
                   payloads.add(setPair[1]);
               }else if(setPair[0].equals("server")){
                  //Add data file servers to the list
                  servers.add(setPair[1]);
               }else{
                  //anything else just add to the settings map
             	  settings.put(setPair[0], setPair[1]);
             	}
            }
         }

         iniFile.close();
      }catch(IOException ex){
         System.out.println(
            "Could not read config file: " + ex.getMessage()
         );
      }
   }

   public static String getSetting(String key){
      if(settings.get(key) != null) return settings.get(key);
      else return "";
   }
}
