#ifndef PINGPONG_H
#define PINGPONG_H

enum {
  AM_MOTEHUNTER_PING = 0x70,
  AM_MOTEHUNTER_PONG = 0x71
};

typedef nx_struct ping {
} motehunter_ping_t;

typedef nx_struct pong {
  motehunter_ping_t ping;
  nx_uint8_t rssi_ping;
} motehunter_pong_t;

#endif
