
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



import java.util.Vector;
import java.util.Enumeration;
import com.dalsemi.onewire.OneWireException;


/**
 * Port adapter for the TINI external 1-Wire bus.
 *
 * @version    1.01, 11 July 2000
 * @author     BA
 */
public class TINIExternalAdapter
   extends TINIAdapter
{

   /**
    * Constructor TINIExternalAdapter
    *
    *
    */
   public TINIExternalAdapter ()
   {
      portType = OW_EXTERNAL;
   }

   /**
    * Retrieve the name of the port adapter as a string.  The 'Adapter'
    * is a device that connects to a 'port' that allows one to
    * communicate with an iButton or other 1-Wire device.  As example
    * of this is 'DS9097U'.
    *
    * @return  <code>String</code> representation of the port adapter.
    */
   public String getAdapterName ()
   {
      return "TINIExternalAdapter";
   }

   /**
    * Retrieve a description of the port required by this port adapter.
    * An example of a 'Port' would 'serial communication port'.
    *
    * @return  <code>String</code> description of the port type required.
    */
   public String getPortTypeDescription ()
   {
      return "<na>";
   }

   /**
    * Retrieve a version string for this class
    *
    *  @return version string
    */
   public String getClassVersion ()
   {
      if (classVersion == null)
         classVersion = "1.01";

      return classVersion;
   }

   /**
    * Retrieve a list of the platform appropriate port names for this
    * adapter.  A port must be selected with the method 'selectPort'
    * before any other communication methods can be used.  Using
    * a communcation method before 'selectPort' will result in
    * a <code>OneWireException</code> exception.
    *
    * @return  enumeration of type <code>String</code> that contains the port
    * names
    */
   public Enumeration getPortNames ()
   {
      Vector portname_vector = new Vector(1);

      portname_vector.addElement("serial1");

      return portname_vector.elements();
   }

   /**
    * Specify a platform appropriate port name for this adapter.  Note that
    * even though the port has been selected, it's ownership may be relinquished
    * if it is not currently held in a 'exclusive' block.  This class will then
    * try to re-aquire the port when needed.  If the port cannot be re-aquired
    * then the exception <code>PortInUseException</code> will be thrown.
    *
    * @param  portName  name of the target port, retrieved from
    * getPortNames()
    *
    * @return <code>true</code> if the port was aquired, <code>false</code>
    * if the port is not available.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean selectPort (String portName)
      throws OneWireIOException, OneWireException
   {
      if (portName.equals("serial1"))
      {
         portSelected = true;
      }
      else
      {
         portSelected = false;
      }

      return portSelected;
   }

   /**
    * Retrieve the name of the selected port as a <code>String</code>.
    *
    * @return  <code>String</code> of selected port
    *
    * @throws OneWireException
    */
   public String getPortName ()
      throws OneWireException
   {
      if (portSelected)
         return "serial1";
      else
         throw new OneWireException();
   }

   /* The following interogative methods are provided so that client code
    * can react selectively to underlying states without generating an
    * exception.
    */

   /**
    * Returns whether adapter can physically support overdrive mode.
    *
    * @return  <code>true</code> if this port adapter can do OverDrive,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean canOverdrive ()
      throws OneWireIOException, OneWireException
   {
      return true;
   }

   /**
    * Returns whether the adapter can physically support hyperdrive mode.
    *
    * @return  <code>true</code> if this port adapter can do HyperDrive,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean canHyperdrive ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

   /**
    * Returns whether the adapter can physically support flex speed mode.
    *
    * @return  <code>true</code> if this port adapter can do flex speed,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean canFlex ()
      throws OneWireIOException, OneWireException
   {
      return true;
   }

   /**
    * Returns whether the adapter can physically support strong 5 volt power
    * mode.
    *
    * @return  <code>true</code> if this port adapter can do strong 5 volt
    * mode, <code>false</code> otherwise.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean canDeliverPower ()
      throws OneWireIOException, OneWireException
   {
      return true;
   }

   /**
    * Sets the duration for providing a program pulse on the
    * 1-Wire Network.
    * This method takes a time parameter that indicates the program
    * pulse length when the method startProgramPulse().<p>
    *
    * Note: to avoid getting an exception,
    * use the canDeliverPower() method to check it's
    * availability. <p>
    *
    * @param timeFactor
    * <ul>
    * <li>   7 (DELIVERY_EPROM) provide program pulse for 480 microseconds
    * <li>   5 (DELIVERY_INFINITE) provide power until the
    *          setPowerNormal() method is called.
    * </ul>
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void setProgramPulseDuration (int timeFactor)
      throws OneWireIOException, OneWireException
   {
      if (timeFactor != DELIVERY_EPROM)
        throw new OneWireException(
           "Infinite program pulse delivery not supported by this adapter type");
   }

   /**
    * Sets the 1-Wire Network voltage to eprom programming level.
    * This method takes a time parameter that indicates whether the
    * power delivery should be done immediately, or after certain
    * conditions have been met. <p>
    *
    * Note: to avoid getting an exception,
    * use the canProgram() method to check it's
    * availability. <p>
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
   public boolean startProgramPulse (int changeCondition)
      throws OneWireIOException, OneWireException
   {
      if (changeCondition != CONDITION_NOW)
        throw new OneWireException(
           "After bit/byte program pulse delivery not supported by this adapter type");

      OWDispatchNative(OWDISPATCH_DOPROGRAMPULSE,portType,0);

      return true;
   }


}
