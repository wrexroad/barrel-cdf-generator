/*
QualityFlags.java

Description:
   Contains definitions of quality flag vales.

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
        FC_ROLLOVER = 1,//The frame counter has gone beyond 2^21 and started over
        NO_GPS = 2,//There is no GPS signal present so timing info may be off
        FILLED_TIME = 4,//a neighboring timing model was used for this record
        FAKE_TIME = 8,//We could not generate a timing model,used nominal values
        FILLED_511 = 16,//Could not find the 511 line, fill from last good value
        LOW_ALT = 36,//Indicator that the payload has dropped below MIN_SCI_ALT
        INCOMPLETE_MSPC= 64,//The spectrum contains fill values
        INCOMPLETE_SSPC= 128,//The spectrum contains fill values
        OUT_OF_RANGE = 256;//The rare case that the DPU returns unacceptable data
}
