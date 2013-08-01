/*
Magn.java

Description:
   Creates Magnetometer CDF files.

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

public class Magn extends DataProduct{
   private int date, lvl;

   public Magn(final String p, final int d, final int l){
      setCDF(new BarrelCDF(p, l));

      this.date = d;
      this.lvl = l;

      addGAttributes();
      addVars();
   }
   
   @Override
   protected void addGAttributes(){
      //Set global attributes specific to this type of CDF
      this.cdf.attribute(
         "Logical_source_description", "MAG X, Y, and Z"
      );
      this.cdf.attribute(
         "TEXT", "Three axis magnetometer reading with nominal conversion. " +
         "Data are neither gain corrected nor despun."
      );
      this.cdf.attribute("Instrument_type", "Magnetic Fields (space)");
      this.cdf.attribute("Descriptor", "Magnetometer");
      this.cdf.attribute("Time_resolution", "4Hz");
      this.cdf.attribute(
         "Logical_source", "payload_id_l" + lvl  + "_magnetometer"
      );
      this.cdf.attribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_magnetometer_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   @Override
   protected void addVars(){
      addAxisVar("X");
      addAxisVar("Y");
      addAxisVar("Z");

      CDFVar var = new CDFVar(cdf, "Total", CDFConstants.CDF_FLOAT);

      var.attribute("FIELDNAM", "B_Tot");
      var.attribute(
         "CATDESC", "Magnitude of magnetic field." 
      );
      var.attribute("LABLAXIS", "B_tot");
      var.attribute(
         "VAR_NOTES", 
         "Bt = sqrt(Bx^2 + By^2 + Bz^2)..Value has variations due to payload" +
         " rotations and Bx, By, and Bz not being gain corrected." 
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "uT");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", -1e31f);
      var.attribute("VALIDMAX", 1e31f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("Total", var);

      var = new CDFVar(cdf, "error", CDFConstants.CDF_FLOAT, false);
      var.attribute("FIELDNAM", "Error");
      var.attribute(
         "CATDESC", "Standard error according to the manufacturer's spec." 
      );
      var.attribute("LABLAXIS", "Error");
      var.attribute("VAR_TYPE", "support_data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "uT");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", 0f);
      var.attribute("VALIDMAX", 1e31f);
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("error", var);

      var.writeData("error", new float[] {0.1f});
   }

   private void addAxisVar(final String axis){
      CDFVar var;

      var = new CDFVar(
         cdf, "MAG_" + axis + "_uncalibrated", CDFConstants.CDF_FLOAT
      );
      var.attribute("FIELDNAM", axis + "_axis");
      var.attribute(
         "CATDESC", "Uncalibrated " + axis + " axis of magnetic field."
      );
      var.attribute("LABLAXIS", "B_" + axis);
      var.attribute(
         "VAR_NOTES", 
         "Calculated as (raw_value - 8388608) / 83886.070. " +
         "Contains fluctuations due to payload rotations." 
      );
      var.attribute("VAR_TYPE", "data");
      var.attribute("DEPEND_0", "Epoch");
      var.attribute("FORMAT", "%f");
      var.attribute("UNITS", "uT");
      var.attribute("SCALETYP", "linear");
      var.attribute("DISPLAY_TYPE", "time_series");
      var.attribute("VALIDMIN", -1e31f);
      var.attribute("VALIDMAX", 1e31f);
      var.attribute("DELTA_PLUS_VAR", "error");
      var.attribute("DELTA_MINUS_VAR", "error");
      var.attribute("FILLVAL", CDFVar.getIstpVal("FLOAT_FILL"));
      this.cdf.addVar("MAG_" + axis, var);
   }
}
