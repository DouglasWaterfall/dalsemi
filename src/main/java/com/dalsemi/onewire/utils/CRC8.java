
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.dalsemi.onewire.utils;



/**
 * CRC8 is a class to contain an implementation of the
 * Cyclic-Redundency-Check CRC8 for the iButton.  The CRC8 is used
 * in the 1-Wire Network address of all iButtons and 1-Wire
 * devices.
 * <p>
 * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
 *
 * @version    1.01, 14 July 2000
 * @author     DS
 *
 */
public class CRC8
{

   //--------
   //-------- Variables
   //--------

   private static native int CRC8 (byte[] barr, int off, int len, int crc);

   private static byte[] temparr = new byte [1];

   //--------
   //-------- Constructor
   //--------

   /**
    * Private constructor to prevent instantiation.
    */
   private CRC8 ()
   {
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Perform the CRC8 on the data element based on the provided seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   data element on which to perform the crc8
    * @param   seed        seed the crc8 with this value
    */
   public static int compute (int dataToCrc, int seed)
   {
      synchronized (temparr)
      {
         temparr [0] = ( byte ) dataToCrc;

         return CRC8(temparr, 0, 1, seed);
      }
   }

   /**
    * Perform the CRC8 on the data element based on a zero seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   data element on which to perform the crc8
    */
   public static int compute (int dataToCrc)
   {
      synchronized (temparr)
      {
         temparr [0] = ( byte ) dataToCrc;

         return CRC8(temparr, 0, 1, 0);
      }
   }

   /**
    * Perform the CRC8 on an array of data elements based on a
    * zero seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the crc8
    */
   public static int compute (byte dataToCrc [])
   {
      return CRC8(dataToCrc, 0, dataToCrc.length,0);
   }

   /**
    * Perform the CRC8 on an array of data elements based on a
    * zero seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the crc8
    * @param   off         offset into array
    * @param   len         length of data to crc
    */
   public static int compute (byte dataToCrc [], int off, int len)
   {
      return CRC8(dataToCrc, off, len, 0);
   }

   /**
    * Perform the CRC8 on an array of data elements based on the
    * provided seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the crc8
    * @param   off         offset into array
    * @param   len         length of data to crc
    * @param   seed        seed to use for crc8
    */
   public static int compute (byte dataToCrc [], int off, int len, int seed)
   {
      return CRC8(dataToCrc, off, len, seed);
   }

   /**
    * Perform the CRC8 on an array of data elements based on the
    * provided seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the crc8
    * @param   seed        seed to use for crc8
    */
   public static int compute (byte dataToCrc [], int seed)
   {
      return CRC8(dataToCrc, 0, dataToCrc.length, seed);
   }
}
