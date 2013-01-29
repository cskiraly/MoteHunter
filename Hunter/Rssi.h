#ifndef RSSI_H
#define RSSI_H

enum {
  AM_RSSI = 0x60,
  AM_RSSI_RX = 0x61,
  AM_RSSI_ACK = 0x62
};

typedef nx_struct rssi {
  nx_uint32_t timestamp;
  nx_uint8_t rssi_avg;
  nx_uint8_t rssi_max;
  nx_uint8_t channel;

} rssi_t;

typedef nx_struct rssi_rx {
  nx_uint32_t timestamp;
  nx_uint16_t id; /* Mote id of sending mote. */
  nx_uint8_t rssi;
} rssi_rx_t;

typedef nx_struct rssi_ack {
  nx_uint32_t timestamp;
  nx_uint16_t id; /* Mote id of sending mote. */
  nx_uint8_t rssi;
} rssi_ack_t;

#endif
