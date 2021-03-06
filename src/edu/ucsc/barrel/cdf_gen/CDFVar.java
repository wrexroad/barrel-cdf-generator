/*
CDFVar.java

Description:
   CDFComponent subclass that impements  
   methods for reading and writing CDF variables.

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
import gsfc.nssdc.cdf.Variable;

public class CDFVar implements CDFComponent{
   private CDF cdf;
   private long type;
   private String name;
   private long num_of_dims;
   private long rec_vary;
   private Variable var;

   //ISTP defined fill values
   static public final int
      UINT1_FILL  = 255,
      UINT2_FILL  = 65535,
      INT1_FILL   = -128,
      INT2_FILL   = -32768,
      INT4_FILL   = -2147483648;
   static public final long
      INT8_FILL   = -9223372036854775808L,
      UINT4_FILL  = 4294967295L;
   static public final float
      FLOAT_FILL  = -1.0e+31f;
   static public final double
      DOUBLE_FILL = -1.0e+31;

   public CDFVar(
      final CDFFile owner, final String n, long t,
      boolean r_v, final long[] dim_sizes
   ){
      this.cdf = owner.getCDF();
      this.name = n;
      this.type = t;

      //figure out the number of dimensions
      this.num_of_dims =
         (dim_sizes.length == 1 && dim_sizes[0] == 0L) ? 0L : dim_sizes.length;

      this.rec_vary = r_v ? CDFConstants.VARY : CDFConstants.NOVARY;

      try{
         if(this.cdf.getVariableID(this.name) != -1L){
            //variable already exists, open it
            this.var = this.cdf.getVariable(this.name);
            System.out.println("Using existing var:" + this.name);
         }else{
            //this variable is new, create it.
            this.var = Variable.create(
               this.cdf, this.name, this.type, 1L, this.num_of_dims,
               dim_sizes, this.rec_vary, new long[]{CDFConstants.VARY}
            );
         }
      }catch(CDFException e){
         System.out.println("Could not create variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }

   public CDFVar(final CDFFile c, final String n, long t, boolean r_v){
      //assume this is a scalar if there is no size specified
      this(c, n, t, r_v, new long[]{0L});
   }
   
   public CDFVar(final CDFFile c, final String n, long t){
      //assume this is a scalar with record variance = VARY 
      this(c, n, t, true, new long[]{0L});
   }
   
   public CDF getCDF(){return this.cdf;}
   public long getID(){return this.var.getID();}
   public String getName(){return this.name;}
   public long getType(){return this.type;}
   public boolean getRecordVariance(){
      return (this.rec_vary == CDFConstants.VARY);
   }
   
   //forwarding functions for creating and selecting attributes in this variable
   public CDFAttribute attribute(
      final String name, final Object value, long type
   ){
      return new CDFAttribute(this, name, value, type);
   }
   public CDFAttribute attribute(final String name, final Object value){
      return new CDFAttribute(this, name, value);
   }
   public CDFAttribute attribute(final String name){
      return new CDFAttribute(this, name);
   }

   //functions for over writing current attribute values in this variable 
   public void editAttribute(final String name, final String value){
      new CDFAttribute(this, name, value);
   }

   public void writeData(String name, short[] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, new long[] {1L}, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, int[] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, new long[] {1L}, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, long[] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, new long[] {1L}, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, float[] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, new long[] {1L}, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, double[] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, new long[] {1L}, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, String[] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, new long[] {1L}, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, short[][] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         long[] dimCnts = {data[0].length, 1};
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, dimCnts, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, int[][] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         long[] dimCnts = {data[0].length, 1};
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, dimCnts, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, long[][] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         long[] dimCnts = {data[0].length, 1};
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, dimCnts, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, float[][] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         long[] dimCnts = {data[0].length, 1};
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, dimCnts, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, double[][] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         long[] dimCnts = {data[0].length, 1};
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, dimCnts, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
   public void writeData(String name, String[][] data){
      try{
         long start = var.getNumWrittenRecords();
         long size = data.length;
         long[] dimCnts = {data[0].length, 1};
         var.putHyperData(
            start, size, 1, 
            new long[] {0L}, dimCnts, new long[] {1L},
            data
         );
      }catch(CDFException e){
         System.out.println("Could not fill variable " + name + ":");
         System.out.println(e.getMessage());
      }
   }
}
