package Demo;

/*
 * (C) 2004 - Geotechnical Software Services
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */
import java.io.File;
import java.awt.*;

import javax.swing.*;

import no.geosoft.cc.geometry.*;
import no.geosoft.cc.graphics.*;
import no.geosoft.cc.*;
import no.geosoft.cc.io.*;


/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Simple image handling
 * <li>Simple drawing interaction
 * </ul>
 * 
 * @author <a href="mailto:info@geosoft.no">GeoSoft</a>
 */   
public class Demo10 extends JFrame implements GInteraction
{
  private GScene    scene_;
  private GSegment  route_;
  private int[]     xy_;
  
  
  public Demo10 (String imageFileName)
  {
    super ("G Graphics Library - Demo 10");    
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    
    getContentPane().setLayout (new BorderLayout());
    getContentPane().add (new JLabel ("Draw line on map using mouse button 1"),
                          BorderLayout.NORTH);
    
    // Create the graphic canvas
    GWindow window = new GWindow();
    getContentPane().add (window.getCanvas(), BorderLayout.CENTER);
    
    // Create scane with default viewport and world extent settings
    scene_ = new GScene (window);

    // Create a graphic object
    GObject object = new TestObject (imageFileName);
    scene_.add (object);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window.startInteraction (this);
  }


  
  public void event (GScene scene, int event, int x, int y)
  {
    switch (event) {
      case GWindow.BUTTON1_DOWN :
        xy_ = new int[] {x, y};
        route_.setGeometry (xy_);
        scene_.refresh();
        break;

      case GWindow.BUTTON1_DRAG :
        int[] a = new int[xy_.length + 2];
        System.arraycopy (xy_, 0, a, 0, xy_.length);
        a[a.length-2] = x;
        a[a.length-1] = y;
        xy_ = a;
        route_.setGeometry (xy_);
        scene_.refresh();
        break;
    }
  }
  
  
  
  /**
   * Defines the geometry and presentation for a sample
   * graphic object.
   */   
  private class TestObject extends GObject
  {
    private GSegment segment_;
    
    
    TestObject (String imageFileName)
    {
      segment_ = new GSegment();
      addSegment (segment_);

      GImage image = new GImage (new File (imageFileName));
      image.setPositionHint (GPosition.SOUTHEAST);
      segment_.setImage (image);

      route_ = new GSegment();
      addSegment (route_);

      GStyle routeStyle = new GStyle();
      routeStyle.setForegroundColor (new Color (255, 0, 0));
      routeStyle.setLineWidth (4);
      routeStyle.setAntialiased (true);
      route_.setStyle (routeStyle);
    }
    

  
    public void draw()
    {
      segment_.setGeometry (0, 0);
      route_.setGeometry (xy_);
    }
  }
  


  public static void main (String[] args)
  {
    if (args.length != 1) {
      System.out.println ("Provide an image file name");
      System.exit (0);
    }
    
    new Demo10 (args[0]);
  }
}
