/*
 * Copyright (c) 2005-2006 Rincon Research Corporation
 * Copyright (c) 2012 Csaba Kiraly
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the Rincon Research Corporation nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * ARCHED ROCK OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE
 */

/**
 * Test RSSI using radio acknowledgements
 *
 * Based on TestAcksC. Part of the MoteHunter project.
 * @author David Moss
 * @author Csaba Kiraly
 */

#include "Commands.h"
#include "PingPong.h"

configuration PingerC {
}

implementation {
  components PingerP,
      MainC,
      ActiveMessageC,
      new AMSenderC(AM_MOTEHUNTER_PING),
      new TimerMilliC();
  components LedsC as LedsC;
  components CC2420PacketC;

  PingerP.Boot -> MainC;
  PingerP.SplitControl -> ActiveMessageC;
  PingerP.Leds -> LedsC;
  PingerP.AMSend -> AMSenderC;
  PingerP.PacketAcknowledgements -> ActiveMessageC;
  PingerP.Timer -> TimerMilliC;
  PingerP.CC2420Packet -> CC2420PacketC;

  components UserButtonC;
  PingerP.UserButtonNotify -> UserButtonC.Notify;

  components SerialActiveMessageC as Serial;
  PingerP.SerialControl -> Serial;
  PingerP.UartSend -> Serial;
  PingerP.Packet -> ActiveMessageC;

  PingerP.PacketTimeStamp -> ActiveMessageC;
  PingerP.AMPacket -> Serial;

  components LocalTimeMilliC;
  PingerP.LocalTimeMilli -> LocalTimeMilliC;

  // control interface
  PingerP.UartControlReceive -> Serial.Receive[AM_SET_PINGER];
}
