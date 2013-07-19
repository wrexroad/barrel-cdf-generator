/*
SSPC.java

Description:
   Creates SSPC CDF files.

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

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.util.CDFTT2000;
import gsfc.nssdc.cdf.Variable;
import gsfc.nssdc.cdf.Attribute;
import gsfc.nssdc.cdf.Entry;

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

public class SSPC extends BarrelCDF{
   private CDF cdf;
   private Variable var;
   private long id;
   private String path;
   private int date, lvl;

   private double scale = 2.4414; // keV/bin

   private double[] 
      BIN_EDGES = {
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
         16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
         30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 
         44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 
         58, 59, 60, 61, 62, 63, 64, 66, 68, 70, 72, 74, 76, 78, 
         80, 82, 84, 86, 88, 90, 92, 94, 96, 98, 100, 102, 104, 
         106, 108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 
         128, 132, 136, 140, 144, 148, 152, 156, 160, 164, 168, 
         172, 176, 180, 184, 188, 192, 196, 200, 204, 208, 212, 
         216, 220, 224, 228, 232, 236, 240, 244, 248, 252, 256, 
         264, 272, 280, 288, 296, 304, 312, 320, 328, 336, 344, 
         352, 360, 368, 376, 384, 392, 400, 408, 416, 424, 432, 
         440, 448, 456, 464, 472, 480, 488, 496, 504, 512, 528, 
         544, 560, 576, 592, 608, 624, 640, 656, 672, 688, 704, 
         720, 736, 752, 768, 784, 800, 816, 832, 848, 864, 880, 
         896, 912, 928, 944, 960, 976, 992, 1008, 1024, 1056, 
         1088, 1120, 1152, 1184, 1216, 1248, 1280, 1312, 1344, 
         1376, 1408, 1440, 1472, 1504, 1536, 1568, 1600, 1632, 
         1664, 1696, 1728, 1760, 1792, 1824, 1856, 1888, 1920, 
         1952, 1984, 2016, 2048, 2112, 2176, 2240, 2304, 2368, 
         2432, 2496, 2560, 2624, 2688, 2752, 2816, 2880, 2944, 
         3008, 3072, 3136, 3200, 3264, 3328, 3392, 3456, 3520, 
         3584, 3648, 3712, 3776, 3840, 3904, 3968, 4032, 4096
      },
      BIN_CENTERS = {
         0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 10.5, 
         11.5, 12.5, 13.5, 14.5, 15.5, 16.5, 17.5, 18.5, 19.5, 
         20.5, 21.5, 22.5, 23.5, 24.5, 25.5, 26.5, 27.5, 28.5, 
         29.5, 30.5, 31.5, 32.5, 33.5, 34.5, 35.5, 36.5, 37.5, 
         38.5, 39.5, 40.5, 41.5, 42.5, 43.5, 44.5, 45.5, 46.5, 
         47.5, 48.5, 49.5, 50.5, 51.5, 52.5, 53.5, 54.5, 55.5, 
         56.5, 57.5, 58.5, 59.5, 60.5, 61.5, 62.5, 63.5, 65, 
         67, 69, 71, 73, 75, 77, 79, 81, 83, 85, 87, 89, 91, 
         93, 95, 97, 99, 101, 103, 105, 107, 109, 111, 113, 
         115, 117, 119, 121, 123, 125, 127, 130, 134, 138, 
         142, 146, 150, 154, 158, 162, 166, 170, 174, 178, 
         182, 186, 190, 194, 198, 202, 206, 210, 214, 218, 
         222, 226, 230, 234, 238, 242, 246, 250, 254, 260, 
         268, 276, 284, 292, 300, 308, 316, 324, 332, 340, 
         348, 356, 364, 372, 380, 388, 396, 404, 412, 420, 
         428, 436, 444, 452, 460, 468, 476, 484, 492, 500, 
         508, 520, 536, 552, 568, 584, 600, 616, 632, 648, 
         664, 680, 696, 712, 728, 744, 760, 776, 792, 808, 
         824, 840, 856, 872, 888, 904, 920, 936, 952, 968, 
         984, 1000, 1016, 1040, 1072, 1104, 1136, 1168, 1200, 
         1232, 1264, 1296, 1328, 1360, 1392, 1424, 1456, 1488, 
         1520, 1552, 1584, 1616, 1648, 1680, 1712, 1744, 1776, 
         1808, 1840, 1872, 1904, 1936, 1968, 2000, 2032, 2080, 
         2144, 2208, 2272, 2336, 2400, 2464, 2528, 2592, 2656,
         2720, 2784, 2848, 2912, 2976, 3040, 3104, 3168, 3232, 
         3296, 3360, 3424, 3488, 3552, 3616, 3680, 3744, 3808, 
         3872, 3936, 4000, 4064
      },
      BIN_WIDTHS = {
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 
         2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
         2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
         4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
         4, 4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 
         8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 16, 
         16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 
         16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 
         16, 16, 16, 16, 16, 32, 32, 32, 32, 32, 32, 32, 32, 
         32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 
         32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 64, 64, 
         64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 
         64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 
         64, 64, 64, 64
      };

   public SSPC(final String p, final int d, final int l){
      super(p, l);
      path = p;
      date = d;
      lvl = l;

      try{
         cdf = super.getCDF();
      
         addSspcGlobalAtts();
         addSspcVars();
      }catch(CDFException e){
         System.out.println(e.getMessage());
      }
   }

   private void addSspcGlobalAtts() throws CDFException{
      //Set global attributes specific to this type of CDF
      setAttribute(
         "Logical_source_description", "Slow time resolution X-ray spectrum"
      );
      setAttribute(
         "TEXT", 
         "X-ray spectra each made of 256 energy bins " + 
         "transmitted over 32 frames."
      );
      setAttribute("Instrument_type", "Gamma and X-Rays");
      setAttribute("Descriptor", "Scintillator");
      setAttribute("Time_resolution", "32s");
      setAttribute("Logical_source", "payload_id_l" + lvl  + "_scintillator");
      setAttribute(
         "Logical_file_id",
         "payload_id_l" + lvl  + "_scintillator_20" + date  + 
         "_V" + CDF_Gen.getSetting("rev")
      );
   }

   private void addSspcVars() throws CDFException{
      //create SSPC variable
      //This variable will contain the slow spectrum that is returned over
      //32 frames.
      var = 
         Variable.create(
            cdf, "SSPC", CDF_DOUBLE, 1L, 1L, new  long[] {BIN_CENTERS.length}, 
            VARY, new long[] {VARY}
         );   
      id = var.getID();

      setAttribute("FIELDNAM", "SSPC", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "SSPC", VARIABLE_SCOPE, id);
      setAttribute(
         "VAR_NOTES", 
         "Rebinned, divided by energy bin widths and " +
         "adjusted to /sec time scale.", 
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "cnts/keV/sec", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "log", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "spectrogram", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute("VALIDMAX", 59391.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute(
         "FILLVAL", Constants.DOUBLE_FILL, VARIABLE_SCOPE, id, CDF_DOUBLE
      );
      setAttribute("LABLAXIS", "SSPC", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_1", "energy", VARIABLE_SCOPE, id);

      //Create the "energy" variable
      //This variable lists the starting energy for each channel in keV
      var = 
         Variable.create(
            cdf, "energy", CDF_DOUBLE, 1L, 1L, new  long[] {BIN_CENTERS.length}, 
            NOVARY, new long[] {VARY}
         );
      id = var.getID();

      setAttribute("FIELDNAM", "Energy Level", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "Energy Level", VARIABLE_SCOPE, id);
      setAttribute(
         "VAR_NOTES", "Start of each slow spectrum var channel.",
         VARIABLE_SCOPE, id
      );
      setAttribute("VAR_TYPE", "support_data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "keV", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute("VALIDMAX", 2000.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute(
         "FILLVAL", Constants.DOUBLE_FILL, VARIABLE_SCOPE, id, CDF_DOUBLE
      );
      setAttribute("LABLAXIS", "Energy", VARIABLE_SCOPE, id);
      setAttribute("DELTA_PLUS_VAR", "HalfBinWidth", VARIABLE_SCOPE, id);
      setAttribute("DELTA_MINUS_VAR", "HalfBinWidth", VARIABLE_SCOPE, id);

      //Fill the "energy" variable
      for(int bin_i = 0; bin_i < BIN_CENTERS.length; bin_i++){
         var.putSingleData(
            0L, new long[] {bin_i}, BIN_CENTERS[bin_i] * scale
         );
      }

      //Create a variable that will track each energy channel width
      var = 
         Variable.create(
            cdf, "HalfBinWidth", CDF_DOUBLE, 1L, 1L, new  long[] {256}, 
            NOVARY, new long[] {VARY}
         );
      id = var.getID();

      setAttribute("FIELDNAM", "Bin Width", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "Width of energy channel", VARIABLE_SCOPE, id);
      
      setAttribute("VAR_TYPE", "support_data", VARIABLE_SCOPE, id);
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "keV", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute("VALIDMAX", 200.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute(
         "FILLVAL", Constants.DOUBLE_FILL, VARIABLE_SCOPE, id, CDF_DOUBLE
      );
      setAttribute("LABLAXIS", "Width", VARIABLE_SCOPE, id);

      //Fill the "BinWidth" variable
      for(int bin_i = 0; bin_i < BIN_WIDTHS.length; bin_i++){
         var.putSingleData(
            0L, new long[] {bin_i}, BIN_WIDTHS[bin_i] * scale / 2
         );
      }

      //Create a variable that will track the 511 line peak
      var = 
         Variable.create(
            cdf, "Peak_511", CDF_DOUBLE, 1L, 0L, new  long[] {1}, 
            VARY, new long[] {NOVARY}
         );
      id = var.getID();

      setAttribute("FIELDNAM", "Peak_511", VARIABLE_SCOPE, id);
      setAttribute("CATDESC", "Location of the 511 line", VARIABLE_SCOPE, id);
      setAttribute("VAR_TYPE", "data", VARIABLE_SCOPE, id);
      setAttribute(
         "VAR_NOTES", 
         "This is the detector channel (0-4096) " + 
         "which appears to contain the 511",
         VARIABLE_SCOPE, id
      );
      setAttribute("DEPEND_0", "Epoch", VARIABLE_SCOPE, id);
      setAttribute("FORMAT", "%f", VARIABLE_SCOPE, id);
      setAttribute("UNITS", "ch", VARIABLE_SCOPE, id);
      setAttribute("SCALETYP", "linear", VARIABLE_SCOPE, id);
      setAttribute("DISPLAY_TYPE", "time_series", VARIABLE_SCOPE, id);
      setAttribute("VALIDMIN", 0.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute("VALIDMAX", 4096.0, VARIABLE_SCOPE, id, CDF_DOUBLE);
      setAttribute(
         "FILLVAL", Constants.DOUBLE_FILL, VARIABLE_SCOPE, id, CDF_DOUBLE
      );
      setAttribute("LABLAXIS", "Peak_511", VARIABLE_SCOPE, id);  
   }
}