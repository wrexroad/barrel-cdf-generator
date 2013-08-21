/*
FSPC.java

Description:
   Creates FSPC CDF files.

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

import gsfc.nssdc.cdf.CDFConstants;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;

public class FSPC extends DataProduct{
   private int date, lvl;
   private String payload_id;
   private double scale = 2.4414; // keV/bin

   public final static double[] 
      BIN_EDGES = {0, 75, 230, 350, 620},
      BIN_CENTERS = {37.5, 77.5, 410, 485},
      BIN_WIDTHS = {75, 155, 120, 250};

   public FSPC(final String path, final String pay, int d, int l){
      this.date = d;
      this.lvl = l;
      this.payload_id = pay;

      setCDF(new BarrelCDF(path, this.payload_id, this.lvl));

      //if this is a new cdf file, fill it with the default attributes
      if(getCDF().newFile == true){
         addGAttributes();
      }
      addVars();
   }

   @Override
   protected void addGAttributes(){
      //Set global attributes specific to this type of CDF
      cdf.attribute(
         "Logical_source_description", 
         "Fast time resolution Bremsstrahlung X-ray spectrum."
      );
      cdf.attribute(
         "TEXT", 
         "Fast time resolution Bremsstrahlung X-ray spectrum from " +
         "NaI Scintillator, four channels at 20 MHz."
      );
      cdf.attribute("Instrument_type", "Electron Precipitation Bremsstrahlung");
      cdf.attribute("Descriptor", "FSPC>Fast SPeCtrum");
      cdf.attribute("Time_resolution", "20Hz");
      cdf.attribute(
         "Logical_source", this.payload_id + "_l" + this.lvl  + "_fspc"
      );
      cdf.attribute(
         "Logical_file_id",
         this.payload_id + "_l" + this.lvl  + "_fspc_20" + this.date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   @Override
   protected void addVars(){
      addLC(1);
      addLC(2);
      addLC(3);
      addLC(4);
   }

   private void addLC(final int ch){
      CDFVar var;

      //create FSPC variable
      var = new CDFVar(cdf, "LC" + ch, CDFConstants.CDF_INT4);

      var.attribute("FIELDNAM", "LC" + ch);
      var.attribute("CATDESC", "Fast spectra ch. " + ch);
      var.attribute("LABLAXIS", "LC" + ch);
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "I5");
      var.attribute("UNITS", "cnts/50ms");
      var.attribute("SCALETYP", "log");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0);
      var.attribute("VALIDMAX", 65535);
      var.attribute("FILLVAL", CDFVar.getIstpVal("INT4_FILL"));
      var.attribute("DELTA_PLUS_VAR", "cnt_error" + ch);
      var.attribute("DELTA_MINUS_VAR", "cnt_error" + ch);
      this.cdf.addVar("LC" + ch, var);

      //Create the count error variable
      var = new CDFVar(cdf, "cnt_error" + ch, CDFConstants.CDF_DOUBLE);
      var.attribute("FIELDNAM", "Count Error " + ch);
      var.attribute(
         "CATDESC", "Count error based on Poisson statistics for LC " + ch + "."
      );
      var.attribute("LABLAXIS", "Error");
      var.attribute("VAR_NOTES", "Error only valid for large count values.");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "F6.3");
      var.attribute("UNITS", "keV");
      var.attribute("SCALETYP", "linear");
      var.attribute("VALIDMIN", 0.0);
      var.attribute("VALIDMAX", 10000.0);
      var.attribute("FILLVAL", CDFVar.getIstpVal("DOUBLE_FILL"));
      this.cdf.addVar("cnt_error" + ch, var);
   }
}
