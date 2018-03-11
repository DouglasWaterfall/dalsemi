
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

package com.dalsemi.onewire.adapter;



// imports
import java.util.Arrays;
import java.util.Enumeration;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;
import java.lang.InterruptedException;
import java.util.Hashtable;


/**
 * Port adapter for the TINI 1-Wire bus.
 *
 * @version    1.01, 11 July 2000
 * @author     BA
 */
abstract class TINIAdapter
   extends DSPortAdapter
{

   //-------------------------------------------------------------------------
   //-------- Variables
   //-------------------------------------------------------------------------
   protected static final int OW_EXTERNAL = 0;
   protected static final int OW_INTERNAL = 1;
   protected int              portType;
   protected boolean          portSelected      = false;
   private byte[]             RomDta            = new byte [8];
   private byte[]             tempRomDta        = new byte [8];
   private boolean            bitPrime          = false;
   private boolean            bytePrime         = false;
   private int                lastdescrepancy   = 0;
   private int                failnext          = 0;
   private byte[]             accessarray       = new byte [9];
   private boolean            doAlarmSearch     = false;
   private boolean            skipResetOnSearch = false;
   private Object             lockObject        = null;

   /**
    * Version string for this adapter class
    */
   protected String classVersion;

   /* Native one wire methods */

   /**
    * Method declaration
    *
    *
    * @param port
    *
    * @return
    */
   private static native boolean OWStrongPUActivateNative (int portType,
           int interval) throws OneWireException,OneWireIOException;

   /**
    * Method declaration
    *
    *
    * @param port
    *
    * @return
    */
   private static native int OWResetNative (int port) throws OneWireException,OneWireIOException;

   /**
    * Method declaration
    *
    *
    * @param port
    * @param bit
    *
    * @return
    */
   private static native int OWBitNative (int port, int bit,
                                          boolean checkReturn)
      throws OneWireException,OneWireIOException;

   /**
    * Method declaration
    *
    *
    * @param port
    * @param bit
    *
    * @return
    */
   private static native int OWStrongPUBitNative (int port, int bit,
           boolean checkReturn)
       throws OneWireException,OneWireIOException;

   /**
     * Method declaration
     *
     *
     * @param port
     * @param bit
     *
     * @return
     */
   private static native int OWStrongPUByteNative (int port, int data,
           boolean checkReturn)
       throws OneWireException,OneWireIOException;

   /**
    * Method declaration
    *
    *
    * @param port
    * @param onebyte
    *
    * @return
    */
   private static native int OWByteNative (int port, int onebyte,
                                           boolean checkReturn)
       throws OneWireException,OneWireIOException;

   /**
    * Method declaration
    *
    *
    * @param port
    * @param barr
    * @param length
    *
    * @return
    */
   private static native int OWReadBlockNative (int port, byte[] barr,
           int off, int length) throws OneWireException,OneWireIOException;

   /**
    * Method declaration
    *
    *
    * @param port
    * @param barr
    * @param length
    *
    * @return
    */
   private static native int OWBlockNative (int port, byte[] barr,
                                            int off, int length) throws OneWireException,OneWireIOException;

   /**
    * Method declaration
    *
    *
    * @param port
    * @param RomDta
    * @param lastdescrepancy
    *
    * @return
    */
   private static native int OWSearchNative (int port, byte[] RomDta,
                                             int lastdescrepancy,
                                             int searchcommand,
                                             boolean skipResetOnSearch) throws OneWireException,OneWireIOException;

   protected static final int OWDISPATCH_SETSPEED         = 0;
   protected static final int OWDISPATCH_GETSPEED         = 1;
   protected static final int OWDISPATCH_CANPROGRAM       = 2;
   protected static final int OWDISPATCH_DOPROGRAMPULSE   = 3;
   protected static final int OWDISPATCH_CANCELSTRONGPU   = 4;
   protected static final int OWDISPATCH_GETVERSION       = 5;
   protected static final int OWDISPATCH_ISPRESENT        = 6;
   protected static native int OWDispatchNative(int function,int port,int value);

   /**
    * Method declaration
    *
    *
    * @param port
    */
   private static native int OWOpenNative (int port);

   /**
    * Method declaration
    *
    *
    * @param port
    */
   private static native int OWCloseNative (int port);

   /**
    * Default constructor
    */
   public TINIAdapter ()
   {
      if (lockObject == null)
         lockObject = new Object();
      try
      {
         this.setSpeed(DSPortAdapter.SPEED_REGULAR);
      }
      catch (OneWireException e){}
   }

   /**
    * Free ownership of the selected port if it is currently owned back
    * to the system.  This should only be called if the recently
    * selected port does not have an adapter or at the end of
    * your application's use of the port.
    *
    * @throws OneWireException
    */
   public void freePort ()
      throws OneWireException
   {
      portSelected = false;
   }

   /**
    * Retrieves the version of the adapter.
    *
    * @return  <code>String</code> of the adapter version.  It will return
    * "<na>" if the adapter version is not or cannot be known.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public String getAdapterVersion ()
      throws OneWireIOException, OneWireException
   {
      int retVal = OWDispatchNative(OWDISPATCH_GETVERSION,portType,0);
      byte[] barr = new byte[4];
      barr[0] = (byte)(retVal & 0xFF);
      barr[1] = (byte)((retVal >>> 8) & 0xFF);
      barr[2] = (byte)((retVal >>> 16) & 0xFF);
      barr[3] = (byte)((retVal >>> 24) & 0xFF);
      return new String(barr);
   }

   /**
    * Detect adapter presence on the selected port.
    *
    * @return  <code>true</code> if the adapter is confirmed to be connected to
    * the selected port, <code>false</code> if the adapter is not connected.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean adapterDetected ()
      throws OneWireIOException, OneWireException
   {
      if (!portSelected)
        return false;

      int retVal = OWDispatchNative(OWDISPATCH_ISPRESENT,portType,0);
      if (retVal == 1)
        return true;
      else
        return false;
   }

   /**
    * Returns whether adapter can physically support 12 volt power mode.
    *
    * @return  <code>true</code> if this port adapter can do Program voltage,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean canProgram ()
      throws OneWireIOException, OneWireException
   {
      if (OWDispatchNative(OWDISPATCH_CANPROGRAM,portType,0) == 1)
        return true;
      else
        return false;
   }

   /**
    * Returns <code>true</code> if the first iButton or 1-Wire device
    * is found on the 1-Wire Network.
    * If no devices are found, then <code>false</code> will be returned.
    *
    * @return  <code>true</code> if an iButton or 1-Wire device is found.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean findFirstDevice ()
      throws OneWireIOException, OneWireException
   {
      synchronized (RomDta)
      {
        // reset the internal rom buffer
        Arrays.fill(RomDta, 0, 8, ( byte ) 0x00);

        lastdescrepancy = 0;
        failnext        = 0;

        return findNextDevice();
      }
   }

   /**
    * Returns <code>true</code> if the next iButton or 1-Wire device
    * is found. The previous 1-Wire device found is used
    * as a starting point in the search.  If no more devices are found
    * then <code>false</code> will be returned.
    *
    * @return  <code>true</code> if an iButton or 1-Wire device is found.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean findNextDevice ()
      throws OneWireIOException, OneWireException
   {
      int retval;

      synchronized (RomDta)
      {
        while (true)
        {
           if (failnext == 1)
           {
              failnext        = 0;
              lastdescrepancy = 0;

              return false;
           }
           else
           {
              retval = RomSearch(RomDta, doAlarmSearch);

              if (retval != 0)
              {
                 if (retval == 2)
                    failnext = 1;

                 if (isValidFamily(RomDta))
                    return true;

                 // Else, loop to the top and do another search.
              }
              else
                 return false;
           }
        }
      }
   }

   /**
    * Copies the 'current' iButton address being used by the adapter into
    * the array.  This address is the last iButton or 1-Wire device found
    * in a search (findNextDevice()...).
    *
    * @param  address An array to be filled with the current iButton address.
    */
   public void getAddress (byte[] address)
   {
      synchronized (RomDta)
      {
        System.arraycopy(RomDta, 0, address, 0, 8);
      }
   }

   /**
    * Method declaration
    *
    *
    * @return
    */
   private int RomSearch (byte[] iButtonID, boolean doAlarmSearch) throws OneWireException, OneWireIOException
   {
      int i  = 0;
      int x  = 0;
      int ld = lastdescrepancy;
      int Mask;
      int RomBit;

      if (doAlarmSearch)
         lastdescrepancy = OWSearchNative(portType, iButtonID,
                                          lastdescrepancy, 0xEC,
                                          skipResetOnSearch);
      else
         lastdescrepancy = OWSearchNative(portType, iButtonID,
                                          lastdescrepancy, 0xF0,
                                          skipResetOnSearch);

      if (lastdescrepancy > 64)
      {
         lastdescrepancy = 0;

         return 0;
      }

      if (ld == lastdescrepancy)
         return 2;
      else
         return 1;
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present on
    * the 1-Wire Network. This does not affect the 'current' device
    * state information used in searches (findNextDevice...).
    *
    * @param  address  device address to verify is present
    *
    * @return  <code>true</code> if device is present else
    *         <code>false</code>.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean isPresent (byte[] address)
      throws OneWireIOException, OneWireException
   {
      try
      {
         synchronized (tempRomDta)
         {
            System.arraycopy(address, 0, tempRomDta, 0, 8);

            int descrepancy = OWSearchNative(portType, tempRomDta, 64, 0xF0,
                                            skipResetOnSearch);

            if (descrepancy != 65)
            {
                if (ArrayUtils_arrayComp(tempRomDta, 0, address, 0, 8))
                return true;
            }
         }
      }
      catch (Exception e)
      {
         return false;
      }

      return false;
   }

   private boolean ArrayUtils_arrayComp(byte[] a, int aStart, byte[] b, int bStart, int length) {
      if (((aStart + length) > a.length) || ((bStart + length) > b.length)) {
         return false;
      }
      for (int i = 0; i < length; i++) {
         if (a[aStart + i] != b[bStart + i]) {
            return false;
         }
      }
      return true;
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present
    * on the 1-Wire Network and in an alarm state. This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * @param  address  device address to verify is present and alarming
    *
    * @return  <code>true</code> if device is present and alarming else
    * <code>false</code>.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean isAlarming (byte[] address)
      throws OneWireIOException, OneWireException
   {
      try
      {
         synchronized (tempRomDta)
         {
            System.arraycopy(address, 0, tempRomDta, 0, 8);

            int descrepancy = OWSearchNative(portType, tempRomDta, 64, 0xEC,
                                            skipResetOnSearch);

            if (descrepancy != 65)
            {
                if (ArrayUtils_arrayComp(tempRomDta, 0, address, 0, 8))
                return true;
            }
         }
      }
      catch (Exception e)
      {
         return false;
      }

      return false;
   }

   /**
    * Selects the specified iButton or 1-Wire device by broadcasting its
    * address.  This operation is refered to a 'MATCH ROM' operation
    * in the iButton and 1-Wire device data sheets.  This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * Warning, this does not verify that the device is currently present
    * on the 1-Wire Network (See isPresent).
    *
    * @param address device to select
    *
    * @return  <code>true</code> if device address was sent,<code>false</code>
    * otherwise.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[] address)
    */
   public boolean select (byte[] address)
      throws OneWireIOException, OneWireException
   {
      int retVal = OWResetNative(portType);
      if ((retVal == 0) || (retVal == 3))
         return false;

      synchronized (accessarray)
      {
        accessarray [0] = 0x55;

        System.arraycopy(address, 0, accessarray, 1, 8);

        OWBlockNative(portType, accessarray, 0, 9);
      }

      return true;
   }

   //--------
   //-------- Finding iButton options
   //--------

   /**
    * Set the 1-Wire Network search to find only iButtons and 1-Wire
    * devices that are in an 'Alarm' state that signals a need for
    * attention.  Not all iButton types
    * have this feature.  Some that do: DS1994, DS1920, DS2407.
    * This selective searching can be canceled with the
    * 'setSearchAllDevices()' method.
    */
   public void setSearchOnlyAlarmingDevices ()
   {
      doAlarmSearch = true;
   }

   /**
    * Set the 1-Wire Network search to not perform a 1-Wire
    * reset before a search.  This feature is chiefly used with
    * the DS2409 1-Wire coupler.
    * The normal reset before each search can be restored with the
    * 'setSearchAllDevices()' method.
    */
   public void setNoResetSearch ()
   {

      // Turn off resets before searches
      skipResetOnSearch = true;
   }

   /**
    * Set the 1-Wire Network search to find all iButtons and 1-Wire
    * devices whether they are in an 'Alarm' state or not and
    * restores the default setting of providing a 1-Wire reset
    * command before each search. (see setNoResetSearch() method).
    */
   public void setSearchAllDevices ()
   {
      doAlarmSearch = false;

      // Turn on resets before searches
      skipResetOnSearch = false;
   }

   //--------
   //-------- MicroLan Semaphore methods
   //--------

   /**
    * Gets exclusive use of the 1-Wire to communicate with an iButton or
    * 1-Wire Device.
    * This method should be used for critical sections of code where a
    * sequence of commands must not be interrupted by communication of
    * threads with other iButtons, and it is permissible to sustain
    * a delay in the special case that another thread has already been
    * granted exclusive access and this access has not yet been
    * relinquished. <p>
    *
    * It can be called through the OneWireContainer
    * class by the end application if they want to ensure exclusive
    * use.  If it is not called around several methods then it
    * will be called inside each method.
    *
    * @param blocking <code>true</code> if want to block waiting
    *                 for an excluse access to the adapter
    * @return <code>true</code> if blocking was false and a
    *         exclusive session with the adapter was aquired
    *
    * @throws OneWireException
    */
   public boolean beginExclusive (boolean blocking)
      throws OneWireException
   {
      if (blocking)
      {
         // Make other threads queue up behind the one churning.
         synchronized (lockObject)
         {
           while (OWOpenNative(portType) != 0)
           {
              try
              {
                 Thread.sleep(20);
              }
              catch (InterruptedException e){}
           }
         }

         return true;
      }
      else
      {
         if (OWOpenNative(portType) == 0)
            return true;
         else
            return false;
      }
   }

   /**
    * Relinquishes exclusive control of the 1-Wire Network.
    * This command dynamically marks the end of a critical section and
    * should be used when exclusive control is no longer needed.
    */
   public void endExclusive ()
   {
      OWCloseNative(portType);
   }

   /**
    * Sends a bit to the 1-Wire Network.
    *
    * @param  bitValue  the bit value to send to the 1-Wire Network.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void putBit (boolean bitValue)
      throws OneWireIOException, OneWireException
   {
      try
      {
         if (bitPrime)
         {
            bitPrime = false;

            if (bitValue)
               OWStrongPUBitNative(portType, 0x01, true);
            else
               OWStrongPUBitNative(portType, 0x00, true);
         }
         else
         {
            if (bitValue)
               OWBitNative(portType, 0x01, true);
            else
               OWBitNative(portType, 0x00, true);
         }
      }
      catch (OneWireIOException e)
      {
         throw new OneWireIOException("Error during putBit()");
      }
   }

   /**
    * Gets a bit from the 1-Wire Network.
    *
    * @return  the bit value recieved from the the 1-Wire Network.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean getBit ()
      throws OneWireIOException, OneWireException
   {
      if (bitPrime)
      {
         bitPrime = false;

         return (OWStrongPUBitNative(portType, 1, false) != 0);
      }
      else
         return (OWBitNative(portType, 1, false) != 0);
   }

   /**
    * Sends a byte to the 1-Wire Network.
    *
    * @param  byteValue  the byte value to send to the 1-Wire Network.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void putByte (int byteValue)
      throws OneWireIOException, OneWireException
   {
      int retVal = 0;
      int bit;

      try
      {
         if (bytePrime)
         {
            bytePrime = false;
            OWStrongPUByteNative(portType,byteValue,true);
         }
         else
            OWByteNative(portType, byteValue, true);
      }
      catch (OneWireIOException e)
      {
         throw new OneWireIOException("Error during putByte()");
      }
   }

   /**
    * Gets a byte from the 1-Wire Network.
    *
    * @return  the byte value received from the the 1-Wire Network.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public int getByte ()
      throws OneWireIOException, OneWireException
   {
      return OWByteNative(portType, 0xFF, false);
   }

   /**
    * Get a block of data from the 1-Wire Network.
    *
    * @param  len  length of data bytes to receive
    *
    * @return  the data received from the 1-Wire Network.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public byte[] getBlock (int len)
      throws OneWireIOException, OneWireException
   {
      byte[] barr = new byte [len];

      getBlock(barr, 0, len);

      return barr;
   }

   /**
    * Get a block of data from the 1-Wire Network and write it into
    * the provided array.
    *
    * @param  arr     array in which to write the received bytes
    * @param  len     length of data bytes to receive
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void getBlock (byte[] arr, int len)
      throws OneWireIOException, OneWireException
   {
      getBlock(arr, 0, len);
   }

   /**
    * Get a block of data from the 1-Wire Network and write it into
    * the provided array.
    *
    * @param  arr     array in which to write the received bytes
    * @param  off     offset into the array to start
    * @param  len     length of data bytes to receive
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void getBlock (byte[] arr, int off, int len)
      throws OneWireIOException, OneWireException
   {
      OWReadBlockNative(portType, arr, off, len);
   }

   /**
    * Sends a block of data and returns the data received in the same array.
    * This method is used when sending a block that contains reads and writes.
    * The 'read' portions of the data block need to be pre-loaded with 0xFF's.
    * It starts sending data from the index at offset 'off' for length 'len'.
    *
    * @param  dataBlock  array of data to transfer to and from the 1-Wire Network.
    * @param  off        offset into the array of data to start
    * @param  len        length of data to send / receive starting at 'off'
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void dataBlock (byte dataBlock [], int off, int len)
      throws OneWireIOException, OneWireException
   {
      OWBlockNative(portType, dataBlock, off, len);
   }

   /**
    * Sends a Reset to the 1-Wire Network.
    *
    * @return  the result of the reset. Potential results are:
    * <ul>
    * <li> 0 (RESET_NOPRESENCE) no devices present on the 1-Wire Network.
    * <li> 1 (RESET_PRESENCE) normal presence pulse detected on the 1-Wire
    *        Network indicating there is a device present.
    * <li> 2 (RESET_ALARM) alarming presence pulse detected on the 1-Wire
    *        Network indicating there is a device present and it is in the
    *        alarm condition.  This is only provided by the DS1994/DS2404
    *        devices.
    * <li> 3 (RESET_SHORT) inticates 1-Wire appears shorted.  This can be
    *        transient conditions in a 1-Wire Network.  Not all adapter types
    *        can detect this condition.
    * </ul>
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public int reset ()
      throws OneWireIOException, OneWireException
   {
      int retVal = OWResetNative(portType);

      switch (retVal)
      {
        case 0:
          return DSPortAdapter.RESET_SHORT;
        case 1:
          return DSPortAdapter.RESET_PRESENCE;
        case 2:
          return DSPortAdapter.RESET_ALARM;
        case 3:
        default:
          return DSPortAdapter.RESET_NOPRESENCE;
      }
   }

   //--------
   //-------- MicroLan power methods
   //--------

   /**
    * Sets the duration to supply power to the 1-Wire Network.
    * This method takes a time parameter that indicates the program
    * pulse length when the method startPowerDelivery().<p>
    *
    * Note: to avoid getting an exception,
    * use the canDeliverPower() and canDeliverSmartPower()
    * method to check it's availability. <p>
    *
    * @param timeFactor
    * <ul>
    * <li>   0 (DELIVERY_HALF_SECOND) provide power for 1/2 second.
    * <li>   1 (DELIVERY_ONE_SECOND) provide power for 1 second.
    * <li>   2 (DELIVERY_TWO_SECONDS) provide power for 2 seconds.
    * <li>   3 (DELIVERY_FOUR_SECONDS) provide power for 4 seconds.
    * <li>   4 (DELIVERY_SMART_DONE) provide power until the
    *          the device is no longer drawing significant power.
    * <li>   5 (DELIVERY_INFINITE) provide power until the
    *          setBusNormal() method is called.
    * </ul>
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void setPowerDuration (int timeFactor)
      throws OneWireIOException, OneWireException
   {
     // Right now we only support infinite pull up.
     if (timeFactor != DELIVERY_INFINITE)
        throw new OneWireException(
           "No support for other than infinite power duration");
   }

   /**
    * Sets the 1-Wire Network voltage to supply power to an iButton device.
    * This method takes a time parameter that indicates whether the
    * power delivery should be done immediately, or after certain
    * conditions have been met. <p>
    *
    * Note: to avoid getting an exception,
    * use the canDeliverPower() and canDeliverSmartPower()
    * method to check it's availability. <p>
    *
    * @param changeCondition
    * <ul>
    * <li>   0 (CONDITION_NOW) operation should occur immediately.
    * <li>   1 (CONDITION_AFTER_BIT) operation should be pending
    *           execution immediately after the next bit is sent.
    * <li>   2 (CONDITION_AFTER_BYTE) operation should be pending
    *           execution immediately after next byte is sent.
    * </ul>
    *
    * @return <code>true</code> if the voltage change was successful,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean startPowerDelivery (int changeCondition)
      throws OneWireIOException, OneWireException
   {
      if (portType == OW_EXTERNAL)
      {
         switch (changeCondition)
         {

            case CONDITION_NOW :

               // Set to infinite pull up duration.  And pull the sucker up!
               OWStrongPUActivateNative(portType, 0x07);

               return true;

            case CONDITION_AFTER_BIT :
               bitPrime = true;

               return true;

            case CONDITION_AFTER_BYTE :
               bytePrime = true;

               return true;

            default :
               return false;
         }
      }
      else
        return true;  // For internal one wire port pin.
   }

   /**
    * Sets the 1-Wire Network voltage to normal level.  This method is used
    * to disable 1-Wire conditions created by startPowerDelivery and
    * startProgramPulse.  This method will automatically be called if
    * a communication method is called while an outstanding power
    * command is taking place.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void setPowerNormal ()
      throws OneWireIOException, OneWireException
   {
      if (portType == OW_EXTERNAL)
      {
        OWDispatchNative(OWDISPATCH_CANCELSTRONGPU,portType,0);
      }
   }

   //--------
   //-------- MicroLan bus speed methods
   //--------

   /**
    * This method takes an int representing the new speed of data
    * transfer on the 1-Wire Network. <p>
    *
    * @param speed
    * <ul>
    * <li>     0 (SPEED_REGULAR) set to normal communciation speed
    * <li>     1 (SPEED_FLEX) set to flexible communciation speed used
    *            for long lines
    * <li>     2 (SPEED_OVERDRIVE) set to normal communciation speed to
    *            overdrive
    * <li>     3 (SPEED_HYPERDRIVE) set to normal communciation speed to
    *            hyperdrive
    * <li>    >3 future speeds
    * </ul>
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void setSpeed (int speed)
      throws OneWireIOException, OneWireException
   {
      switch (speed)
      {

         case DSPortAdapter.SPEED_REGULAR :
            OWDispatchNative(OWDISPATCH_SETSPEED,portType, 0x00);
            break;

         case DSPortAdapter.SPEED_OVERDRIVE :
            OWDispatchNative(OWDISPATCH_SETSPEED,portType, 0x01);
            break;

         case DSPortAdapter.SPEED_FLEX :
            if (portType == OW_EXTERNAL)
              OWDispatchNative(OWDISPATCH_SETSPEED,portType, 0x02);
            else
              throw new OneWireException();
            break;

         default :
            throw new OneWireException();
      }
   }

   /**
    * This method returns the current data transfer speed through a
    * port to a 1-Wire Network. <p>
    *
    * @return
    * <ul>
    * <li>     0 (SPEED_REGULAR) set to normal communication speed
    * <li>     1 (SPEED_FLEX) set to flexible communication speed used
    *            for long lines
    * <li>     2 (SPEED_OVERDRIVE) set to normal communication speed to
    *            overdrive
    * <li>     3 (SPEED_HYPERDRIVE) set to normal communication speed to
    *            hyperdrive
    * <li>    >3 future speeds
    * </ul>
    */
   public int getSpeed ()
   {
      int retVal = OWDispatchNative(OWDISPATCH_GETSPEED,portType, 0xFF);
      if (retVal == 1)
        return DSPortAdapter.SPEED_OVERDRIVE;
      else if (retVal == 0)
        return DSPortAdapter.SPEED_REGULAR;
      else// if (retVal == 2)
        return DSPortAdapter.SPEED_FLEX;
   }
}
