/*
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

#include "PingPong.h"

#define PREY_LPL_OFF_MILLISEC 30000

module PreyP {
  uses {
    interface Receive;
    interface AMSend;
    interface AMPacket;
    interface Timer<TMilli> as MilliTimer;
    interface PacketAcknowledgements;
    interface CC2420Packet;
    interface LowPowerListening;
  }
}

implementation {

  message_t pong_packet;
  uint16_t lpl_orig = 0;
  bool lpl_saved = FALSE;

  bool locked;
  uint16_t counter = 0;

  event message_t* Receive.receive(message_t* bufPtr, void* payload, uint8_t len) {
    am_id_t sender = call AMPacket.source(bufPtr);

    //set power of this packet to maximum. Power values used by the host application are not changed.
    call CC2420Packet.setPower(&pong_packet, 31);

    //no need for ACK on the app level pong
    call PacketAcknowledgements.noAck(&pong_packet);

    call AMSend.send(sender, &pong_packet, sizeof(motehunter_pong_t));

    //save the old LPL interval value
    if (! lpl_saved) {
      lpl_orig = call LowPowerListening.getLocalWakeupInterval();
      lpl_saved = TRUE;
    }
    //disable LPL temporarily
    call LowPowerListening.setLocalWakeupInterval(0);

    //setup/update timer
    call MilliTimer.startOneShot(PREY_LPL_OFF_MILLISEC);

    return bufPtr;
  }

  event void AMSend.sendDone(message_t* bufPtr, error_t error) {
  }

  event void MilliTimer.fired() {
    if (lpl_saved) {
      //restoring LPL value to the saved value
      call LowPowerListening.setLocalWakeupInterval(lpl_orig);
      lpl_saved = FALSE;
    }
  }
}




