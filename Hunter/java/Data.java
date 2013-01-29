/*
 * Copyright (c) 2006 Intel Corporation
 * Copyright (c) 2012 Csaba Kiraly
 * All rights reserved.
 *
 * This file is distributed under the terms of The GNU Affero General
 * Public License, version 3 or later. It is based on code distribted
 * as part of TinyOS, which was licensed under the terms of the INTEL
 * LICENSE.
 */

import java.util.*;

/* Hold all data received from motes */
class Data
{
  /* The mote data is stored in a flat array indexed by a mote's identifier.
     A null value indicates no mote with that identifier. */
  private Node[] nodes = new Node[256];
  private MoteHunter parent;

  public enum MeasureType
  { RCV, ACK,
    RXPOWER_AVG, RXPOWER_MAX,
    YAW, PITCH, ROLL
  }

  Data (MoteHunter parent)
  {
    this.parent = parent;
  }

  /* Data received from mote nodeId Tell parent if this is a new node. */
  void update (MeasureType type, int nodeId, int messageId, int reading)
  {
    switch (type) {
    case ACK:
      nodeId += 500;
      break;
    case RXPOWER_AVG:
      nodeId = 998;
      break;
    case RXPOWER_MAX:
      nodeId = 999;
      break;
    case YAW:
      nodeId = 1000;
      break;
    case PITCH:
      nodeId = 1001;
      break;
    case ROLL:
      nodeId = 1002;
      break;
    }
    if (nodeId >= nodes.length) {
      int newLength = nodes.length * 2;
      if (nodeId >= newLength) {
        newLength = nodeId + 1;
      }

      Node newNodes[] = new Node[newLength];
      System.arraycopy (nodes, 0, newNodes, 0, nodes.length);
      nodes = newNodes;
    }
    Node node = nodes[nodeId];
    if (node == null) {
      nodes[nodeId] = node = new Node (nodeId);
      parent.newNode (nodeId);
    }
    node.update (messageId, reading);
  }

  String getNodeName (int nodeId)
  {
    if (nodeId < 500) {
      return Integer.toString (nodeId) + " RCV";
    } else if (nodeId < 998) {
      return Integer.toString (nodeId - 500) + " ACK";
    } else if (nodeId == 998) {
      return "RX Power AVG";
    } else if (nodeId == 999) {
      return "RX Power MAX";
    } else if (nodeId == 1000) {
      return "Yaw";
    } else if (nodeId == 1001) {
      return "Pitch";
    } else if (nodeId == 1002) {
      return "Roll";
    } else {
      return "unknown" + "(" + Integer.toString (nodeId) + ")";
    }
  }

  /* Return value of sample x for mote nodeId, or -1 for missing data */
  double getData (int nodeId, int x)
  {
    if (nodeId >= nodes.length || nodes[nodeId] == null)
      return -1;
    return nodes[nodeId].getData (x);
  }

  /* Return number of last known sample on mote nodeId. Returns 0 for
     unknown motes. */
  int maxX (int nodeId)
  {
    if (nodeId >= nodes.length || nodes[nodeId] == null)
      return 0;
    return nodes[nodeId].maxX ();
  }

  /* Return number of largest known sample on all motes (0 if there are no
     motes) */
  int maxX ()
  {
    int max = 0;

    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i] != null) {
        int nmax = nodes[i].maxX ();

        if (nmax > max)
          max = nmax;
      }
    }

    return max;
  }
}
