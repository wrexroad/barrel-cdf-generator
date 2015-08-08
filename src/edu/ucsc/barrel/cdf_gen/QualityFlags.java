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
      FC_ROLLOVER       = 0b00000000000000000000000000000001,
      //There is no GPS signal present so timing info may be off
      NO_GPS            = 0b00000000000000000000000000000010,
      //a neighboring timing model was used for this record
      FILLED_TIME       = 0b00000000000000000000000000000100,
      //We could not generate a timing model,used nominal values
      FAKE_TIME         = 0b00000000000000000000000000001000,
      //Indicator that the payload has dropped below MIN_SCI_ALT
      LOW_ALT           = 0b00000000000000000000000000010000,
      //Could not find the 511 line, fill from last good value
      FILLED_511        = 0b00000000000000000000000000100000,
      //The spectrum contains fill values
      INCOMPLETE_SPEC   = 0b00000000000000000000000001000000,
      //The DPU returns unacceptable data
      RCNT_OUT_OF_RANGE = 0b00000000000000000000000010000000,
      FSPC_OUT_OF_RANGE = 0b00000000000000000000000100000000,
      MSPC_OUT_OF_RANGE = 0b00000000000000000000001000000000,
      SSPC_OUT_OF_RANGE = 0b00000000000000000000010000000000,
      HKPG_OUT_OF_RANGE = 0b00000000000000000000100000000000,
      EPHM_OUT_OF_RANGE = 0b00000000000000000001000000000000,
      MAGN_OUT_OF_RANGE = 0b00000000000000000010000000000000,
      MISC_OUT_OF_RANGE = 0b00000000000000000100000000000000;
}
