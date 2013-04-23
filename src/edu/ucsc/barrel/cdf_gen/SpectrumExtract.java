package edu.ucsc.barrel.cdf_gen;

/*
SpectrumExtract.java v13.02.28

Description:
   Creates energy bin edges and rebins spectra.

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

Change Log:
   v13.03.28
      -Implimented the peak fitting routine from Apache Commons Math
      -Removed initial guess code from find511()
      -Fixed the way edges_raw[][] is accessed by createBins()
   v13.02.28
      -Updated bin edge creater, removed rebin routine for now. Added imports 
         for Gaussian fit. 
   v12.11.20
      -Added this documentation
      
*/

import org.apache.commons.math3.fitting.GaussianFitter;
import org.apache.commons.math3.optim.nonlinear.vector.
          jacobian.LevenbergMarquardtOptimizer;

public class SpectrumExtract {

   //create uncalibrated bin edges
   public final static double[][] edges_raw = {
      {0, 75, 230, 350, 620},
      {
         42, 46, 50, 53, 57, 60, 64, 70, 78, 84, 92, 100, 
         106 , 114, 120, 128, 140, 156, 168, 184, 200, 212, 228, 
         240, 256, 280, 312, 336, 368, 400, 424, 456, 480, 512, 
         560, 624, 672, 736, 800, 848, 912, 960, 1024, 1120, 
         1248, 1344, 1472, 1600, 1696
      },
      {
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
         16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
         30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 
         44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 
         58, 59, 60, 61, 62, 63, 64, 66, 68, 70, 72, 74, 76, 78, 
         80, 82, 84, 86, 88, 90, 92, 94, 96, 98, 100, 102, 104, 106, 
         108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 128, 132, 
         136, 140, 144, 148, 152, 156, 160, 164, 168, 172, 176, 
         180, 184, 188, 192, 196, 200, 204, 208, 212, 216, 220, 
         224, 228, 232, 236, 240, 244, 248, 252, 256, 264, 272, 
         280, 288, 296, 304, 312, 320, 328, 336, 344, 352, 360, 
         368, 376, 384, 392, 400, 408, 416, 424, 432, 440, 448, 
         456, 464, 472, 480, 488, 496, 504, 512, 528, 544, 560, 
         576, 592, 608, 624, 640, 656, 672, 688, 704, 720, 736, 
         752, 768, 784, 800, 816, 832, 848, 864, 880, 896, 912, 
         928, 944, 960, 976, 992, 1008, 1024, 1056, 1088, 1120, 
         1152, 1184, 1216, 1248, 1280, 1312, 1344, 1376, 1408, 
         1440, 1472, 1504, 1536, 1568, 1600, 1632, 1664, 1696, 
         1728, 1760, 1792, 1824, 1856, 1888, 1920, 1952, 1984, 
         2016, 2048, 2112, 2176, 2240, 2304, 2368, 2432, 2496, 
         2560, 2624, 2688, 2752, 2816, 2880, 2944, 3008, 3072, 
         3136, 3200, 3264, 3328, 3392, 3456, 3520, 3584, 3648, 
         3712, 3776, 3840, 3904, 3968, 4032, 4096
      }
   };
   private static double[] slow_bin_widths;
   private static double[] slow_bin_midpoints;

   public SpectrumExtract(){
      //calculate the nominal slow spectrum bin widths and midpoints
      slow_bin_widths = new double[edges_raw[2].length - 1];     
      for(int edge_i = 1; edge_i < edges_raw[2].length; edge_i++){
         slow_bin_widths[edge_i - 1] = 
            edges_raw[2][edge_i] - edges_raw[2][edge_i - 1];
      }

      slow_bin_midpoints = new double[slow_bin_widths.length];
      for(int bin_i = 0; bin_i < slow_bin_widths.length; bin_i++){
         slow_bin_midpoints[bin_i] = 
            (slow_bin_widths[bin_i] / 2) + edges_raw[2][bin_i];
      }
   }

   //does a simple linear scaling on the data
   public static double[] scaleEdges(int spec_i, double scale){
      double[] result = new double[edges_raw[spec_i].length];
      
      for(int edge_i = 0; edge_i < edges_raw[spec_i].length; edge_i++){
         result[edge_i] = edges_raw[spec_i][edge_i] * scale;
      }
      return result;
   }
   
   //converts from counts in native time scale cnts/keV/sec
   //sps is the number of samples per second
   //edges are the bin edges in energies
   //cnts is the array of counts
   public static double[] convertCnts(double sps, double[] edges, int[] cnts){
      double[] result = new double[cnts.length];

      for(int cnt_i = 0, edge_i = 0; cnt_i < cnts.length; cnt_i++){
         //convert time scale
         result[cnt_i] = cnts[cnt_i] * sps;
         //convert to cnts/kev
         result[cnt_i] = result[cnt_i] / (edges[edge_i + 1] - edges[edge_i]);
      }

      return result;
   }

   public static double[] createBinEdges(
      int spec_i, double xtal_temp, double dpu_temp, double peak511
   ){
      double factor1, factor2, scale;
      double[] edges_nonlin = new double[edges_raw[spec_i].length];
      double[] edges_cal = new double[edges_raw[spec_i].length];

      //a rational function approximates energy non-linearity
      for(int edge_i = 0; edge_i < edges_nonlin.length; edge_i++){
         edges_nonlin[edge_i] = 
            edges_raw[spec_i][edge_i] * (
               1 - 11.6 / (edges_raw[spec_i][edge_i] + 10.8) + 
               0.000091 * edges_raw[spec_i][edge_i]
            );
      }

      //quadratic function for crystal gain drift with temperature
      factor1 = 1;
      if(xtal_temp != 0){
         factor1 += (0.00014 * xtal_temp - 0.0068) * xtal_temp;
      }

      //linear function for peak detect gain drift with temperature
      factor2 = 1;
      if(dpu_temp != 0){
         factor2 += (23 - dpu_temp) * 0.00034;
      }

      //set an overall scale factor to position 511keV line
      //this also helps compensate incorrect temperature values
      scale = 2.5; //nominal keV/bin
      if(peak511 != -1){
         scale = 
            511. * factor2 / factor1 / peak511 / 
            (1.0 - 11.6 / (peak511 + 10.8) + 0.000091 * peak511);
      }

      //apply corrections to energy bin edges
      scale = scale * factor1 / factor2;
      for(int edge_i = 0; edge_i < edges_cal.length; edge_i++){
         edges_cal[edge_i] = scale * (edges_nonlin[edge_i]);
      }
if(spec_i==2){
for(int i=1; i<edges_cal.length; i++){
CDF_Gen.log.writeln(edges_cal[i-1] + ((edges_cal[i] - edges_cal[i-1]) / 2) + " ");
}
}
      return edges_cal;
   }
   
   public static double[] rebin(
      double[] oldVals, double[] oldBins, double[] newBins, 
      int n, int m, boolean flux
   ){

      if ((n < 2) || (m < 2)){
         System.out.println(
            "length(s) violation: SpectraExtract.rebin(): " + n + ", " + m
         );
         System.exit(1);
      }
     
      double[] result = new double[m - 1];
      double oldLo = oldBins[0];
      double oldHi = oldBins[1];
      double newLo = newBins[0];
      double newHi = newBins[1];
      int newIndex = 0;
      int oldIndex = 0;
      double total = 0;

      while(true){
         if (oldHi <= newLo){
            oldIndex++;
            if(oldIndex >= (n - 1)){return result;}
            oldLo = oldHi;
            oldHi = oldBins[oldIndex + 1];
            continue;
         }
         if (newHi <= oldLo){
            if(flux){
               result[newIndex] = (float) (total / (newHi - newLo));
            }else{
               result[newIndex] = (float) (total / (oldHi - oldLo));
            }
            
            total = 0;
            newIndex++;
            
            if(newIndex >= (m - 1)){return result;}
            
            newLo = newHi;
            newHi = newBins[newIndex + 1];

            continue;
         }

         if (newHi < oldHi){
            total += (newHi - Math.max(oldLo, newLo)) * oldVals[oldIndex];
            if(flux){
               result[newIndex] = (float) (total / (newHi-newLo));
            }else{
               result[newIndex] = (float) (total / (oldHi-oldLo));
            }
            total = 0;
            newIndex++;
            if(newIndex >= (m - 1)){return result;}
            newLo = newHi;
            newHi = newBins[newIndex + 1];
            continue;
         }else{
            total += (oldHi - Math.max(oldLo,newLo)) * oldVals[oldIndex];
            oldIndex++;
            if (oldIndex >= (n - 1)){
               if(flux){
                  result[newIndex] = (float) (total / (newHi-newLo));
               }
               else{
                  result[newIndex] = (float) (total / (oldHi-oldLo));
               }
               return result;
            }
            oldLo = oldHi;
            oldHi = oldBins[oldIndex + 1];
         }
      }
   }

   public static double find511(double[] slow, int offset){
      GaussianFitter fitter = 
         new GaussianFitter(new LevenbergMarquardtOptimizer());
      
      //nominal range for 511 line
      int width = 25;
      
      //add points in the search range to the fitter object
      double x = 0, y = 0;
      for(int pnt_i = 0; pnt_i < width; pnt_i++){
         y = slow[pnt_i + offset] / slow_bin_widths[pnt_i + offset];
         x = slow_bin_midpoints[pnt_i + offset];
         fitter.addObservedPoint(x, y);
      }
      double[] fit_params = fitter.fit();

      if(
         fit_params[1] < slow_bin_midpoints[offset] || 
         fit_params[1] > slow_bin_midpoints[offset+width]
      ){
         return -1;
      }

     return fit_params[1];
   }
}
