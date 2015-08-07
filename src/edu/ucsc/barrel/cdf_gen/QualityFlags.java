/*
Constants.java

Description:
   A static object used as a common place to store the 
   various BARREL CDF Generator constants.

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

public class QualityFlags {

    //Quality flags
    static public final int
        FC_ROLL = 1,//The frame counter has gone beyond 2^21 and started over
        NO_GPS = 2,//There is no GPS signal present so timing info may be off
        FILL_TIME = 4,//This data point was not used to create a timing model
        FILLED_511 = 8,//Could not find the 511 line, fill from last good value
        FILLED_L = 16,//L shell could not be calculated//
        LOW_ALT = 32,//Indicator that the payload has dropped below MIN_SCI_ALT
        INCOMPLETE_MSPC= 64,//The spectrum contains fill values
        INCOMPLETE_SSPC= 128,//The spectrum contains fill values
        OUT_OF_RANGE = 256;//The rare case that the DPU returns unacceptable data
}
