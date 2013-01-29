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
 * Based on the TestAcksC TinyOS example. Part of the MoteHunter project.
 * @author David Moss
 * @author Csaba Kiraly
 */

#include <UserButton.h>

#include "Rssi.h"
#include "Commands.h"

module PingerP {
  uses {
    interface Boot;
    interface SplitControl;
    interface AMSend;
    interface Leds;
    interface PacketAcknowledgements;
    interface CC2420Packet;
    interface Timer<TMilli>;

    interface Notify<button_state_t> as UserButtonNotify;

    interface Packet;
    interface SplitControl as SerialControl;
    interface AMSend as UartSend[am_id_t id];

    interface AMPacket;
    interface PacketTimeStamp<TMilli, uint32_t>;

    interface LocalTime<TMilli> as LocalTimeMilli;

    //control interface
    interface Receive as UartControlReceive;
  }
}

implementation {
  
  /** Message to transmit */
  message_t myMsg;
  message_t rssiMsg;

  bool uartBusy;
  am_addr_t id_to_ping;	// accepts AM_BROADCAST_ADDR as well
  
  enum {  
    DELAY_BETWEEN_MESSAGES = 50,
  };

  /******* Global Variables ****************/
  uint8_t rssi_max;
  uint8_t rssi_min;

  uint8_t op_mode;

  /***************** Prototypes ****************/
  task void send();
  
  /***************** Boot Events ****************/
  event void Boot.booted() {
    rssi_max = 0;
    rssi_min = 255;
    op_mode = 0;
    id_to_ping = 5;
    uartBusy = FALSE;
    call SplitControl.start();
    call UserButtonNotify.enable();
    call SerialControl.start();
  }
  
  /***************** SplitControl Events ****************/
  event void SplitControl.startDone(error_t error) {
    post send();
  }
  
  event void SplitControl.stopDone(error_t error) {
  }
  

  /***************** Receive Events ****************/
  event message_t *UartControlReceive.receive(message_t *msg, void *payload, uint8_t len) {
    set_pinger_t *smsg = (set_pinger_t*) payload;

    id_to_ping = smsg->target_id;
    return msg; //release buffer
  }

  /***************** AMSend Events ****************/
  event void AMSend.sendDone(message_t *msg, error_t error) {
    if(call PacketAcknowledgements.wasAcked(msg)) {
      uint8_t delta, rssi;

      rssi = (call CC2420Packet.getRssi(msg)) + 128;	// if called on sent message after ACK, it is the RSSI of the ACK; getRssi gives back -128..127
      rssi_max = rssi > rssi_max ? rssi : rssi_max;
      rssi_min = rssi < rssi_min ? rssi : rssi_min;
      delta = rssi_max - rssi;
      delta = delta > 7 ? 7 : delta;
      if (op_mode == 0) {
        call Leds.set(7 - delta);
      }
      if (! uartBusy){
        am_id_t id;
        rssi_ack_t* rssi_ack;

        // set type
        id = AM_RSSI_ACK;
        call AMPacket.setType(&rssiMsg,id);

        // fill payload
        call Packet.setPayloadLength(&rssiMsg, sizeof(rssi_ack_t));
        rssi_ack = call Packet.getPayload(&rssiMsg, sizeof(rssi_ack_t));
        rssi_ack->id = call AMPacket.destination(msg);
//        rssi_ack->timestamp = call PacketTimeStamp.timestamp(msg); //for some rason this returned timestamps with some different base
        rssi_ack->timestamp = call LocalTimeMilli.get();
        rssi_ack->rssi = (call CC2420Packet.getRssi(msg)) + 128;	// getRssi gives back -128..127
        if (call UartSend.send[id](AM_BROADCAST_ADDR, &rssiMsg, sizeof(rssi_ack_t)) == SUCCESS) {
          uartBusy = TRUE;
        }
      }
    } else {
      if (op_mode == 0) {
        call Leds.set(0);
      }
    }

    if(DELAY_BETWEEN_MESSAGES > 0) {
      call Timer.startOneShot(DELAY_BETWEEN_MESSAGES);
    } else {
      post send();
    }
  }
  
  /***************** Timer Events ****************/
  event void Timer.fired() {
    post send();
  }

  event void UserButtonNotify.notify( button_state_t state ) {
    if ( state == BUTTON_PRESSED ) {
      uint8_t range, power;

      op_mode = 1;

      range = rssi_max - rssi_min;
      power = 0;
      while (range > 0) {
        range = range >> 1;
        power++;
      }
      call Leds.set(power > 7 ? 7 : power);
    } else if ( state == BUTTON_RELEASED ) {
      op_mode = 0;
    }
  }

  event void SerialControl.startDone(error_t error) {}

  event void SerialControl.stopDone(error_t error) {}

  event void UartSend.sendDone[am_id_t id](message_t* msg, error_t error) {
    uartBusy = FALSE;
  }

  /***************** Tasks ****************/
  task void send() {
    call PacketAcknowledgements.requestAck(&myMsg);
    if(call AMSend.send(id_to_ping, &myMsg, 0) != SUCCESS) {
      post send();
    }
  }
}
