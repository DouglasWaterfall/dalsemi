
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
 * CRC16 is a class containing an implementation of the
 * Cyclic-Redundency-Check (CRC) CRC16.  The CRC16 is used in
 * iButton memory packet structure.
 * <p>
 * CRC16 is based on the polynomial = X^16 + X^15 + X^2 + 1.
 *
 * @version    1.01, 14 July 2000
 * @author     DS
 */
public class CRC16
{

   //--------
   //-------- Variables
   //--------
   private static native int CRC16 (byte[] barr, int off, int len, int crc);

   private static byte[] temparr = new byte [1];

   //--------
   //-------- Constructor
   //--------

   /**
    * Private constructor to prevent instantiation.
    */
   private CRC16 ()
   {
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Perform the CRC16 on the data element based on a zero seed.
    * <p>
    * CRC16 is based on the polynomial = X^16 + X^15 + X^2 + 1.
    *
    * @param  dataToCrc     data element to perform crc16 on
    */
   public static int compute (int dataToCrc)
   {
      synchronized (temparr)
      {
         temparr [0] = ( byte ) dataToCrc;

         return CRC16(temparr, 0, 1, 0);
      }
   }

   /**
    * Perform the CRC16 on the data element based on the provided seed.
    * <p>
    * CRC16 is based on the polynomial = X^16 + X^15 + X^2 + 1.
    *
    * @param  dataToCrc     data element to perform crc16 on
    */
   public static int compute (int dataToCrc, int seed)
   {
      synchronized (temparr)
      {
         temparr [0] = ( byte ) dataToCrc;

         return CRC16(temparr, 0, 1, seed);
      }
   }

   /**
    * Perform the CRC16 on an array of data elements based on a
    * zero seed.
    * <p>
    * CRC16 is based on the polynomial = X^16 + X^15 + X^2 + 1.
    *
    * @param   dataToCrc   array of data elements to perform crc16 on
    *
    * @return  CRC16 value
    */
   public static int compute (byte dataToCrc [])
   {
      return CRC16(dataToCrc, 0, dataToCrc.length, 0);
   }

   /**
    * Perform the CRC16 on an array of data elements based on a
    * zero seed.
    * <p>
    * CRC16 is based on the polynomial = X^16 + X^15 + X^2 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform crc16
    * @param   off         offset into the data array
    * @param   len         length of data to CRC16
    *
    * @return  CRC16 value
    */
   public static int compute (byte dataToCrc [], int off, int len)
   {
      return CRC16(dataToCrc, off, len, 0);
   }

   /**
    * Perform the CRC16 on an array of data elements based on the
    * provided seed.
    * <p>
    * CRC16 is based on the polynomial = X^16 + X^15 + X^2 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform crc16
    * @param   off         offset into the data array
    * @param   len         length of data to CRC16
    * @param   seed        seed to use for CRC16
    *
    * @return  CRC16 value
    */
   public static int compute (byte dataToCrc [], int off, int len, int seed)
   {
      return CRC16(dataToCrc, off, len, seed);
   }

   /**
    * Perform the CRC16 on an array of data elements based on the
    * provided seed.
    * <p>
    * CRC16 is based on the polynomial = X^16 + X^15 + X^2 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the crc8
    * @param   seed        seed to use for crc16
    */
   public static int compute (byte dataToCrc [], int seed)
   {
      return CRC16(dataToCrc, 0, dataToCrc.length, seed);
   }
}
