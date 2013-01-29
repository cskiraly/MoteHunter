#ifndef COMMANDS_H
#define COMMANDS_H

enum {
  AM_SET_CHANNEL = 0x80,
  AM_SET_PINGER = 0x81
};

typedef nx_struct set_channel {
  nx_uint8_t channel; /* radio channel in 11..26 range */
} set_channel_t;

typedef nx_struct set_pinger {
  nx_uint16_t target_id; /* Mote id of target node, can also be broadcast */
} set_pinger_t;

#endif
