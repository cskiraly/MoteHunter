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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

/* Panel for drawing mote-data graphs */
class Graph extends JPanel
{
  final static int BORDER_LEFT = 40;
  final static int BORDER_RIGHT = 0;
  final static int BORDER_TOP = 10;
  final static int BORDER_BOTTOM = 10;

  final static int TICK_SPACING = 40;
  final static int MAX_TICKS = 16;
  final static int TICK_WIDTH = 10;

  final static int MIN_WIDTH = 50;

  final static Color bgColor = Color.WHITE;
  final static Color axisColor = Color.BLACK;

  boolean paused = false;
  int gx0, gx1, gy0, gy1;       // graph bounds
  int scale = 6;                // gx1 - gx0 == MIN_WIDTH << scale
  Window parent;

  /* Graph to screen coordinate conversion support */
  int height, width;
  double xscale, yscale;

  void updateConversion ()
  {
    height = getHeight () - BORDER_TOP - BORDER_BOTTOM;
    width = getWidth () - BORDER_LEFT - BORDER_RIGHT;
    if (height < 1)
    {
      height = 1;
    }
    if (width < 1)
    {
      width = 1;
    }
    xscale = (double) width / (gx1 - gx0 + 1);
    yscale = (double) height / (gy1 - gy0 + 1);
  }

  Graphics makeClip (Graphics g)
  {
    return g.create (BORDER_LEFT, BORDER_TOP, width, height);
  }

  // Note that these do not include the border offset!
  int screenX (int gx)
  {
    return (int) (xscale * (gx - gx0) + 0.5);
  }

  int screenY (int gy)
  {
    return (int) (height - yscale * (gy - gy0));
  }

  int graphX (int sx)
  {
    return (int) (sx / xscale + gx0 + 0.5);
  }

  Graph (Window parent) {
    this.parent = parent;
    gy0 = 80;
    gy1 = 140;
    gx0 = 0;
    gx1 = MIN_WIDTH << scale;
  }

  void rightDrawString (Graphics2D g, String s, int x, int y)
  {
    TextLayout layout = new TextLayout (s, parent.smallFont, g.getFontRenderContext ());
    Rectangle2D bounds = layout.getBounds ();
    layout.draw (g, x - (float) bounds.getWidth (), y + (float) bounds.getHeight () / 2);
  }

  protected void paintComponent (Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g;

    /* Repaint. Synchronize on MoteHunter to avoid data changing.
       Simply clear panel, draw Y axis and all the mote graphs. */
    synchronized (parent.parent) {
      updateConversion ();
      g2d.setColor (bgColor);
      g2d.fillRect (0, 0, getWidth (), getHeight ());
      drawYAxis (g2d);
      drawXAxis (g2d);

      Graphics clipped = makeClip (g2d);
      int count = parent.moteListModel.size ();
      for (int i = 0; i < count; i++) {
        clipped.setColor (parent.moteListModel.getColor (i));
        if (parent.moteListModel.getShowTime (i)) {
          drawGraph (clipped, parent.moteListModel.get (i));
        }
      }
    }
  }

  /* Draw the Y-axis */
  protected void drawYAxis (Graphics2D g)
  {
    int axis_x = BORDER_LEFT - 1;
    int height = getHeight () - BORDER_BOTTOM - BORDER_TOP;

    g.setColor (axisColor);
    g.drawLine (axis_x, BORDER_TOP, axis_x, BORDER_TOP + height - 1);

    /* Draw a reasonable set of tick marks */
    int nTicks = height / TICK_SPACING;
    if (nTicks > MAX_TICKS) {
      nTicks = MAX_TICKS;
    }

    int tickInterval = (gy1 - gy0 + 1) / nTicks;
    if (tickInterval == 0) {
      tickInterval = 1;
    }

    /* Tick interval should be of the family A * 10^B,
       where A = 1, 2 * or 5. We tend more to rounding A up, to reduce
       rather than increase the number of ticks. */
    int B = (int) (Math.log (tickInterval) / Math.log (10));
    int A = (int) (tickInterval / Math.pow (10, B) + 0.5);
    if (A > 2) {
      A = 5;
    } else if (A > 5) {
      A = 10;
    }

    tickInterval = A * (int) Math.pow (10, B);

    /* Ticks are printed at multiples of tickInterval */
    int tick = ((gy0 + tickInterval - 1) / tickInterval) * tickInterval;
    while (tick <= gy1) {
      int stick = screenY (tick) + BORDER_TOP;
      rightDrawString (g, "" + tick, axis_x - TICK_WIDTH / 2 - 2, stick);
      g.drawLine (axis_x - TICK_WIDTH / 2, stick, axis_x - TICK_WIDTH / 2 + TICK_WIDTH, stick);
      tick += tickInterval;
    }

  }

  /* Draw the X-axis */
  protected void drawXAxis (Graphics2D g)
  {
    int axis_y = BORDER_BOTTOM + height * 9 / 10;
    int width = getWidth () - BORDER_LEFT - BORDER_RIGHT;

    g.setColor (axisColor);
    g.drawLine (BORDER_LEFT, axis_y, width - BORDER_LEFT - BORDER_RIGHT - 1, axis_y);

    /* Draw a reasonable set of tick marks */
    int nTicks = width / TICK_SPACING;
    if (nTicks > MAX_TICKS) {
      nTicks = MAX_TICKS;
    }

    int tickInterval = (gx1 - gx0 + 1) / nTicks;
    if (tickInterval == 0) {
      tickInterval = 1;
    }

    /* Tick interval should be of the family A * 10^B,
       where A = 1, 2 * or 5. We tend more to rounding A up, to reduce
       rather than increase the number of ticks. */
    int B = (int) (Math.log (tickInterval) / Math.log (10));
    int A = (int) (tickInterval / Math.pow (10, B) + 0.5);
    if (A > 2) {
      A = 5;
    } else if (A > 5) {
      A = 10;
    }

    tickInterval = A * (int) Math.pow (10, B);

    /* Ticks are printed at multiples of tickInterval */
    int tick = ((gx0 + tickInterval - 1) / tickInterval) * tickInterval;
    while (tick <= gx1) {
      int stick = screenX (tick) + BORDER_LEFT;
      rightDrawString (g, "" + tick * (double) parent.parent.interval / 1000 + " s",stick, axis_y + TICK_WIDTH / 2 + 2);
      g.drawLine (stick, axis_y - TICK_WIDTH / 2, stick, axis_y - TICK_WIDTH / 2 + TICK_WIDTH);
      tick += tickInterval;
    }
  }

  /* Draw graph for mote nodeId */
  protected void drawGraph (Graphics g, int nodeId)
  {
    SingleGraph sg = new SingleGraph (g, nodeId);

//    if (gx1 - gx0 >= width) {
//        for (int sx = 0; sx < width; sx++)
//        sg.nextPoint(g, graphX(sx), sx);
//    } else {
    for (int gx = gx0; gx <= gx1; gx++)
      sg.nextPoint (g, gx, screenX (gx));
//    }
  }

  /* Inner class to simplify drawing a graph. Simplify initialise it, then
     feed it the X screen and graph coordinates, from left to right. */
  private class SingleGraph
  {
    int lastsx, lastsy, nodeId;

    /* Start drawing the graph mote id */
      SingleGraph (Graphics g, int id)
    {
      nodeId = id;
      lastsx = -1;
      lastsy = -1;
    }

    /* Next point in mote's graph is at x value gx, screen coordinate sx */
    void nextPoint (Graphics g, int gx, int sx)
    {
      double gy = parent.parent.data.getData (nodeId, gx);
      int sy = -1;

      if (gy >= 0) {            // Ignore missing values

        if (nodeId == 1000) {
          sy = (int) (height * (gy / 360));
        } else if (nodeId == 1001) {
          sy = (int) (height * (gy / 180));
        } else if (nodeId == 1002) {
          sy = (int) (height * (gy / 180));
        } else {
          double rsy = height - yscale * (gy - gy0);

          // Ignore problem values
          if (rsy >= -1e6 && rsy <= 1e6) {
            sy = (int) (rsy + 0.5);
          }
        }

        if (lastsy >= 0 && sy >= 0 && Math.abs (sx - lastsy) < height / 2) {    //avoid too big jumps
          g.drawLine (lastsx, lastsy, sx, sy);
        }
        g.fillOval (sx, sy, 5, 5);
      }
      lastsx = sx;
      lastsy = sy;
    }
  }

  /* Pause/restart X-axis recentering */
  void pauseXUpdate (Boolean b)
  {
    paused = b;
  }

  /* Ensure that graph is nicely positioned on screen. max is the largest 
     sample number received from any mote. */
  private void recenter (int max)
  {
    // New data will show up at the 3/4 point
    // The 2nd term ensures that gx1 will be >= max
    int scrollby = ((gx1 - gx0) >> 2) + (max - gx1);
    gx0 += scrollby;
    gx1 += scrollby;
    if (gx0 < 0) {              // don't bother showing negative sample numbers
      gx1 -= gx0;
      gx0 = 0;
    }
  }

  /* New data received. Redraw graph, scrolling if necessary */
  void newData ()
  {
    int max = parent.parent.data.maxX ();

    if (!paused && max > gx1 || max < gx0) {
      recenter (max);
    }
    repaint ();
  }

  /* User set the X-axis scale to newScale */
  void setScale (int newScale)
  {
    gx1 = gx0 + (MIN_WIDTH << newScale);
    scale = newScale;
    recenter (parent.parent.data.maxX ());
    repaint ();
  }

  /* User attempted to set Y-axis range to newy0..newy1. Refuse bogus
     values (return false), or accept, redraw and return true. */
  boolean setYAxis (int newy0, int newy1)
  {
    if (newy0 >= newy1 || newy0 < 0 || newy0 > 65535 || newy1 < 0 || newy1 > 65535) {
      return false;
    }
    gy0 = newy0;
    gy1 = newy1;
    repaint ();
    return true;
  }
}
