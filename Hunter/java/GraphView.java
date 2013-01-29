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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

/* Panel for drawing mote-data GraphViewDirs */
class GraphView extends JPanel
{
  final static int BORDER_LEFT = 0;
  final static int BORDER_RIGHT = 0;
  final static int BORDER_TOP = 10;
  final static int BORDER_BOTTOM = 10;

  final static int TICK_SPACING = 40;
  final static int MAX_TICKS = 16;
  final static int TICK_WIDTH = 10;

  final static int MIN_WIDTH = 50;

  static Color bgColor = Color.WHITE;
  static Color axisColor = Color.BLUE;
  static Color maxColor = Color.GREEN;
  static Color minColor = Color.RED;

  boolean paused = false;
  int gx0, gx1, gy0, gy1;       // graph bounds
  int scale = 6;                // gx1 - gx0 == MIN_WIDTH << scale
  int bins = 45;
  double fadingFactor = 3;
  Window parent;
  public int targetId = 501;

  // Use compass to rotate view
  UsbIssCmps09 compass;
  public void setCompass (UsbIssCmps09 c)
  {
    compass = c;
  }

  /* Graph to screen coordinate conversion support */
  int height, width;
  double xscale, yscale;

  void updateConversion ()
  {
    height = getHeight () - BORDER_TOP - BORDER_BOTTOM;
    width = getWidth () - BORDER_LEFT - BORDER_RIGHT;
    if (height < 1) {
      height = 1;
    }
    if (width < 1) {
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
  int screenX (double gx, int bins)
  {
    if (compass != null) {
      double d = Math.sin (Math.toRadians ((gx * 360 / bins - compass.getYaw () + 360 + 180) % 360 - 180) / 2); //transfomed in [-1..1]
      return (int) (((d + 1) / 2) * width);
    } else {
      return (int) (gx * width / bins);
    }
  }

  int screenY (double gy)
  {
    return (int) (height - yscale * (gy - gy0));
  }

  int graphX (int sx)
  {
    return (int) (sx / xscale + gx0 + 0.5);
  }

  GraphView (Window parent) {
    this.parent = parent;
    gy0 = 0;
    gy1 = 180;
    gx0 = 0;
    gx1 = MIN_WIDTH << scale;

    //create control panel
    final JPanel controls = new JPanel ();
    controls.setVisible (false);
    controls.setBorder (BorderFactory.createEtchedBorder ());
    add (controls);

    JPanel sliderControls = new JPanel (new GridLayout (0, 2));
    sliderControls.setBorder (BorderFactory.
                              createTitledBorder (BorderFactory.createEtchedBorder (), "Controls"));
    controls.add (sliderControls);

    sliderControls.add (new JLabel ("Bins", JLabel.RIGHT));
    final JSlider slx = new JSlider (1, 180);
    slx.setMajorTickSpacing (60);
    slx.setMinorTickSpacing (20);
    slx.setPaintTicks (true);
    slx.setPaintLabels (true);
    slx.addChangeListener (new ChangeListener () {
                           public void stateChanged (ChangeEvent e)
                           {
                           bins = slx.getValue ();
                           };});
    sliderControls.add (slx);

    sliderControls.add (new JLabel ("Gamma", JLabel.RIGHT));
    final JSlider slff = new JSlider (0, 100, (int) (fadingFactor * 10));
    slff.setMajorTickSpacing (50);
    slff.setMinorTickSpacing (10);
    slff.setPaintTicks (true);
    slff.setPaintLabels (true);
    slff.addChangeListener (new ChangeListener () {
                            public void stateChanged (ChangeEvent e)
                            {
                            fadingFactor = slff.getValue () / 10;
                            };});
    sliderControls.add (slff);


    JPanel colorControls = new JPanel (new GridLayout (0, 2));
    colorControls.setBorder (BorderFactory.
                             createTitledBorder (BorderFactory.createEtchedBorder (), "Colors"));
    controls.add (colorControls);

    final JButton bbgcc = new JButton ("Background");
    bbgcc.addActionListener (new ActionListener () {
                             public void actionPerformed (ActionEvent e)
                             {
                             Color newColor =
                             JColorChooser.showDialog (bbgcc, "Background Color", bgColor);
                             if (newColor != null) bgColor = newColor;}
                             }
    );
    colorControls.add (bbgcc);

    final JButton baxiscc = new JButton ("Axis");
    baxiscc.addActionListener (new ActionListener () {
                               public void actionPerformed (ActionEvent e)
                               {
                               Color newColor =
                               JColorChooser.showDialog (baxiscc, "Axis Color", axisColor);
                               if (newColor != null) axisColor = newColor;}
                               }
    );
    colorControls.add (baxiscc);

    final JButton bmaxcc = new JButton ("Max");
    bmaxcc.addActionListener (new ActionListener () {
                              public void actionPerformed (ActionEvent e)
                              {
                              Color newColor =
                              JColorChooser.showDialog (bmaxcc, "Maximum Power Color", maxColor);
                              if (newColor != null) maxColor = newColor;}
                              }
    );
    colorControls.add (bmaxcc);

    final JButton bmincc = new JButton ("Min");
    bmincc.addActionListener (new ActionListener () {
                              public void actionPerformed (ActionEvent e)
                              {
                              Color newColor =
                              JColorChooser.showDialog (bmincc, "Minimum Power Color", minColor);
                              if (newColor != null) minColor = newColor;}
                              }
    );
    colorControls.add (bmincc);


    final JButton bx = new JButton ("Close");
    bx.addActionListener (new ActionListener () {
                          public void actionPerformed (ActionEvent e)
                          {
                          controls.setVisible (false);
                          }
                          });
    controls.add (bx);


    addMouseListener (new MouseAdapter () {
                      public void mouseClicked (MouseEvent e)
                      {
                      controls.setVisible (true);
                      }
                      });
  }

  void rightDrawString (Graphics2D g, String s, int x, int y)
  {
    TextLayout layout = new TextLayout (s, parent.normalFont, g.getFontRenderContext ());
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

      Graphics clipped = makeClip (g2d);
      int count = parent.moteListModel.size ();
      for (int i = 0; i < count; i++) {
        if (parent.moteListModel.get (i) == targetId) {
          drawGraph (clipped, parent.moteListModel.get (i), parent.moteListModel.getColor (i));
        }
      }

      drawYAxis (g2d);
      drawXAxis (g2d);
    }
  }

  /* Draw the Y-axis */
  protected void drawYAxis (Graphics2D g)
  {
    //int axis_x = BORDER_LEFT - 1;
    int axis_x = BORDER_LEFT - 1 + width / 2;
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
    int axis_y = BORDER_BOTTOM + height / 2;

    g.setColor (axisColor);
    g.drawLine (BORDER_LEFT, axis_y, width - BORDER_LEFT - BORDER_RIGHT - 1, axis_y);
    rightDrawString (g, "S", screenX (0, bins), axis_y);
    rightDrawString (g, "W", screenX (bins / 4, bins), axis_y);
    rightDrawString (g, "N", screenX (bins / 2, bins), axis_y);
    rightDrawString (g, "E", screenX (-bins / 4, bins), axis_y);
  }

  /* Draw graph for mote nodeId */
  protected void drawGraph (Graphics g, int nodeId, Color color)
  {
    SingleGraph sg = new SingleGraph (g, nodeId, gx0, gx1, bins);
    sg.plot (g, color);
  }

  /* Inner class to simplify drawing a graph. Simplify initialise it, then
     feed it the X screen and graph coordinates, from left to right. */
  private class SingleGraph
  {
    int lastsx, lastsy, nodeId;
    double deg2power[][];
    double deg2count[][];
    double deg2age[][];
    double power_min, power_max;
    int bins;
    int gx0, gx1;

    /* Start drawing the graph mote id */
      SingleGraph (Graphics g, int id, int from, int to, int bins)
    {
      nodeId = id;
      lastsx = -1;
      lastsy = -1;
      gx0 = from;
      gx1 = to;
      this.bins = bins;
      deg2power = new double[bins][bins];
        deg2count = new double[bins][bins];
        deg2age = new double[bins][bins];
        power_min = 255;
        power_max = 0;

        calcCurve ();
    }

    void calcCurve ()
    {
      for (int gx = gx0; gx <= gx1; gx++) {
        double power = parent.parent.data.getData (nodeId, gx);
        double yaw = parent.parent.data.getData (1000, gx);
        double pitch = parent.parent.data.getData (1001, gx);   // 0-180
        if (power >= 0 && yaw >= 0) {
          int y = (int) ((yaw / 360) * bins);
          int p = (int) ((pitch / 180) * bins);
          if (nodeId == 998 || nodeId == 999) {
            deg2power[y][p] = Math.max (power, deg2power[y][p]);
            deg2count[y][p] = 1;
          } else {
            double weight = (double) (gx - gx0) / (gx1 - gx0);
            deg2power[y][p] += power * weight;
            deg2count[y][p] += weight;
          }
          deg2age[y][p] = (double) (gx - gx0) / (gx1 - gx0);
          power_min = Math.min (power_min, power);
          power_max = Math.max (power_max, power);
        }
      }
    }

    protected Color fadeColor (Color color, double fade)
    {
      return new Color ((int) (255 - (double) (255 - color.getRed ()) * fade),
                        (int) (255 - (double) (255 - color.getGreen ()) * fade),
                        (int) (255 - (double) (255 - color.getBlue ()) * fade));
    }

    protected Color mixColors (Color color1, Color color2, double fade)
    {
      return new
        Color ((int) ((double) color1.getRed () * fade + (double) color2.getRed () * (1 - fade)),
               (int) ((double) color1.getGreen () * fade +
                      (double) color2.getGreen () * (1 - fade)),
               (int) ((double) color1.getBlue () * fade + (double) color2.getBlue () * (1 - fade)));
    }

    protected Color mixColors (Color color1, Color color2, double fade, double factor)
    {
      fade = Math.pow (fade, factor);
      return new
        Color ((int) ((double) color1.getRed () * fade + (double) color2.getRed () * (1 - fade)),
               (int) ((double) color1.getGreen () * fade +
                      (double) color2.getGreen () * (1 - fade)),
               (int) ((double) color1.getBlue () * fade + (double) color2.getBlue () * (1 - fade)));
    }

    void plot (Graphics g, Color color)
    {
      for (int x = 0; x < bins; x++) {
        for (int y = 0; y < bins; y++) {
          int sx = screenX (x, bins);

          int sy = height * y / bins;

          if (deg2count[x][y] > 0) {
            g.setColor (mixColors
                        (maxColor, minColor,
                         ((deg2power[x][y] / deg2count[x][y]) - power_min) / (power_max -
                                                                              power_min),
                         fadingFactor));
            g.fillOval (screenX (x, bins), sy, screenX (x + 1, bins) - sx, height / bins);
          }
          lastsx = sx;
          lastsy = sy;
        }
      }
    }

  }

  /* Update X-axis range in GUI */
  void updateXLabel ()
  {
    parent.xLabel.setText ("X: " + gx0 + " - " + gx1);
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
    updateXLabel ();
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
