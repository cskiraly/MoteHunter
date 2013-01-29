
/*
 * Copyright (c) 2012 Csaba Kiraly
 * All rights reserved.
 */
 
#include "RssiToSerial.h"
#include "Commands.h"

 /**
  * This is the Hunter application of the MoteHunter project.
  *
  * Install this Hunter application on a node with a CC2420 chip and a
  * directional antenna.
  * The node will work both in standalone mode or connected to a computer.
  * In standalone mode, it gives relative RSSI readings on the leds.
  * In connected mode, it gives more detailed info about the prey.
  *
  * @author Csaba Kiraly
  * @date   February 2012
  */
 
 
configuration HunterC {}
implementation {
  components RssiToSerialC;
  components SnifferC;
  components PingerC;

  // control interface
  components HunterP;
  components SerialActiveMessageC as Serial;
  components CC2420ControlC;
  HunterP.UartControlReceive -> Serial.Receive[AM_SET_CHANNEL];
  HunterP.CC2420Config -> CC2420ControlC;
}


