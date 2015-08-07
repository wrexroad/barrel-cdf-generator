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
      //The frame counter has gone beyond 2^21 and started over
      FC_ROLLOVER       = 1,
      //There is no GPS signal present so timing info may be off
      NO_GPS            = 2,
      //a neighboring timing model was used for this record
      FILLED_TIME       = 4,
      //We could not generate a timing model,used nominal values
      FAKE_TIME         = 8,
      //Could not find the 511 line, fill from last good value
      FILLED_511        = 16,
      //Indicator that the payload has dropped below MIN_SCI_ALT
      LOW_ALT           = 36,
      //The spectrum contains fill values
      INCOMPLETE_MSPC   = 64,
      INCOMPLETE_SSPC   = 128,
      //The DPU returns unacceptable data
      RCNT_OUT_OF_RANGE = 256,
      FSPC_OUT_OF_RANGE = 512,
      MSPC_OUT_OF_RANGE = 1024,
      SSPC_OUT_OF_RANGE = 2048,
      HKPG_OUT_OF_RANGE = 4096,
      EPHM_OUT_OF_RANGE = 8192,
      MAGN_OUT_OF_RANGE = 16384,
      MISC_OUT_OF_RANGE = 32768;
}
