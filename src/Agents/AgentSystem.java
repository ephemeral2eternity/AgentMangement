package Agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


import no.geosoft.cc.geometry.Geometry;
import no.geosoft.cc.graphics.GInteraction;
import no.geosoft.cc.graphics.GObject;
import no.geosoft.cc.graphics.GPosition;
import no.geosoft.cc.graphics.GScene;
import no.geosoft.cc.graphics.GSegment;
import no.geosoft.cc.graphics.GStyle;
import no.geosoft.cc.graphics.GText;
import no.geosoft.cc.graphics.GViewport;
import no.geosoft.cc.graphics.GWindow;

/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Nested GObject hierarchy
 * <li>Style inheritance
 * <li>Simple selection interaction
 * </ul>
 * 
 * @author <a href="mailto:info@geosoft.no">GeoSoft</a>
 */   
public class AgentSystem extends JFrame
  implements GInteraction
{
  private GScene  scene_;
  private int dim_ = 600;
  private int max_server_capacity_ = 100;
  private ArrayList<GObject> clients_;
  private ArrayList<GObject> servers_;
  
  
  public AgentSystem()
  {
    super ("G Graphics Library - Demo 8");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    servers_ = new ArrayList<GObject>();
    clients_ = new ArrayList<GObject>();
    
    // Create the GUI
    JPanel topLevel = new JPanel();
    topLevel.setLayout (new BorderLayout());
    getContentPane().add (topLevel);        

    // JPanel buttonPanel = new JPanel();
    // buttonPanel.add (new JLabel ("Button 1 to select color, button 2 to unselect"));

    JPanel graphicsPanel = new JPanel();
    // topLevel.add (buttonPanel,   BorderLayout.NORTH);

    // Create the graphic canvas
    GWindow window = new GWindow();
    topLevel.add (window.getCanvas(), BorderLayout.CENTER);    
    
    // Create scene with default viewport and world extent settings
    scene_ = new GScene (window, "Scene");

    double w0[] = {0.0,    1200.0, 0.0};
    double w1[] = {1200.0, 1200.0, 0.0};
    double w2[] = {0.0,       0.0, 0.0};    
    scene_.setWorldExtent (w0, w1, w2);

    GStyle style = new GStyle();
    style.setForegroundColor (new Color (0, 0, 0));
    style.setBackgroundColor (new Color (255, 255, 255));
    style.setFont (new Font ("Dialog", Font.BOLD, 14));
    scene_.setStyle (style);
    
    // Create some graphic objects
    int server_dim_sz = 2;
    
    double server_x, server_y;
    for (int server_id = 0; server_id < Math.pow(server_dim_sz, 2); server_id ++)
    {
    	Color color = new Color((float) (server_dim_sz*server_dim_sz - server_id - 0.5)/(server_dim_sz*server_dim_sz),
                (float) (server_id+0.5)/(server_dim_sz*server_dim_sz),
                (float) (server_id+0.5)/(server_dim_sz*server_dim_sz));
    	server_x = (server_id/server_dim_sz + 0.5) * dim_;
    	server_y = (server_id%server_dim_sz + 0.5) * dim_;
    	ServerObject server = new ServerObject (Integer.toString(server_id + 1), server_x, server_y, color);
    	servers_.add(server);
    }
    
    pack();
    setSize (new Dimension (dim_, dim_));
    setVisible (true);
    
    window.startInteraction (this);
  }
  
  public void event (GScene scene, int event, int x, int y)
  {
    if (scene == null) return;
        
    if (event == GWindow.BUTTON1_UP || event == GWindow.BUTTON2_UP)
    {
    	GObject chosenServer_;
    	GObject client_;
    	
        double[] w = scene.getTransformer().deviceToWorld (x, y);

        x = (int) w[0] - 1;
        y = (int) w[1] - 1;
    	
        chosenServer_ = ChooseServer(x, y);
    	client_ = new ClientObject (chosenServer_, x, y);
    	ServerObject curServerObject = chosenServer_ instanceof ServerObject ? (ServerObject) chosenServer_ : null;
    	ClientObject curClient = client_ instanceof ClientObject ? (ClientObject) client_ : null;
    	if (!curServerObject.addConnection())
    	{
    		curClient.setClient_capacity_(1);
    	}
    	else
    	{
    		curClient.setClient_capacity_( max_server_capacity_ / (double) curServerObject.getClient_num());
    	}
    	clients_.add(client_);
    	scene.redraw();
    }
    
    scene.refresh();    
  }
  
  private GObject ChooseServer (double x, double y)
  {
		double sx_, sy_, sdist_, scapacity_;
		
	    // Compute the chosen server
	    double maxServerCapacity_ = 0;
	    int iter_ = 0;
	    double minServerDist_ = 10000000;
	    GObject chosenServer_ = null;
		for (GObject server:servers_)
		{
			ServerObject curServerObject = server instanceof ServerObject ? (ServerObject) server : null;
			sx_ = curServerObject.getX();
			sy_ = curServerObject.getY();
			sdist_ = Math.sqrt(Math.pow(sx_ - x, 2) + Math.pow(sy_ - y, 2));
			scapacity_ = curServerObject.getCapacity();
			if (iter_ == 0)
			{
				chosenServer_ = server;
				minServerDist_ = sdist_;
				maxServerCapacity_ = scapacity_;
				iter_ = iter_ + 1;
			}
			else
			{
				if ( (scapacity_ > maxServerCapacity_) ||
					 ((scapacity_ == maxServerCapacity_ ) && (sdist_ < minServerDist_)) )
				{
					chosenServer_ = server;
					minServerDist_ = sdist_;
					maxServerCapacity_ = scapacity_;
				}
				
				iter_ = iter_ + 1;
			}
		}
		return chosenServer_;
  }

  private class ServerObject extends GObject
  {
	private GObject parent_;
    private double      x_, y_;
    private GSegment    circle_;
    // private GSegment    line_;
    private double		radius_ = 40;
    private double		capacity_ = max_server_capacity_;
    private int			client_num_ = 0;

	ServerObject (String name, double x, double y, Color color)
    {
      parent_ = scene_;
      x_ = x;
      y_ = y;
      
      circle_ = new GSegment();
      addSegment (circle_);

      circle_.setText (new GText (name, GPosition.MIDDLE));
    
      setStyle(new GStyle());
      this.getStyle().setBackgroundColor(color);
      parent_.add (this);
    }

    
    double getX()
    {
      return x_;
    }


    double getY()
    {
      return y_;
    }
    
    public void draw()
    { 
      circle_.setGeometryXy (Geometry.createCircle (x_, y_, radius_));
    }
    
    public double getCapacity()
    {
    	return capacity_;
    }
    
    public Boolean addConnection()
    {
    	Boolean isFull_ = true;
    	if (capacity_ > 0)
    	{
    		capacity_ = capacity_ - 1;
    		client_num_ = client_num_ + 1;
    		isFull_ = false;
    	}
    	else
    	{
    		client_num_ = client_num_ + 1;
    		capacity_ = 0;
    	}
    	return isFull_;
    }
    
    public int getClient_num() {
		return client_num_;
	}
  }
  
  private class ClientObject extends GObject
  {
    private ServerObject  parent_;
    private double      x_, y_;
    private GSegment    circle_;
    private GSegment    line_;
    private double		radius_ = 10;
    private double		client_capacity_ = 0;

	ClientObject (GObject parent, double x, double y)
    {
      parent_ = parent instanceof ServerObject ? (ServerObject) parent : null;
      
      x_ = x;
      y_ = y;

      line_ = new GSegment();
      addSegment (line_);
      
      circle_ = new GSegment();
      addSegment (circle_);
    
      setStyle(new GStyle());
      this.getStyle().setBackgroundColor(parent.getStyle().getBackgroundColor());
      
      parent.add (this);
    }

    
    double getX()
    {
      return x_;
    }


    double getY()
    {
      return y_;
    }
    
    public void draw()
    {
      if (parent_ != null) 
        line_.setGeometry (parent_.getX(), parent_.getY(), x_, y_);
      
      circle_.setGeometryXy (Geometry.createCircle (x_, y_, radius_));
    }
    
    public double getClient_capacity_() {
		return client_capacity_;
	}

	public void setClient_capacity_(double client_capacity_) {
		this.client_capacity_ = client_capacity_;
	}
  }
  


  public static void main (String[] args)
  {
    new AgentSystem();
  }
}
