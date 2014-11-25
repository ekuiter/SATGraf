/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.community.UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.satgraf.UI.PaintThread;
import com.satgraf.graph.UI.GraphCanvas;
import com.satlib.community.CommunityEdge;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class CommunityCanvas extends GraphCanvas{
	
  private Thread thread = null;
	
  public CommunityCanvas(GraphViewer graph){
    super(graph);
  }
  
  public void start() {
	  
  }
  
  @Override
  public void paintThread(PaintThread paint) {
	Rectangle o = paint.getBounds();
    List<Node> nodes = graph.getOrderedUpdatedNodes(o, true);
    
    if (!nodes.isEmpty() || paint.getForceDraw()) {
    	Iterator<Node> nodIt = nodes.iterator();
    	Graphics image = paint.getGraphics();
        Rectangle i = image.getClipBounds();
        image.setColor(Color.BLACK);
        image.fillRect(i.x, i.y, i.width, i.height);
        List<CommunityEdge> drawnEdges = new ArrayList<CommunityEdge>();
        
        while(nodIt.hasNext()){
          Node next = nodIt.next();
          if(!next.isVisible()){
            continue;
          }
          drawNode(next, o, image);
          
          Iterator<CommunityEdge> eit = next.getEdges();
          while(eit.hasNext()) {
        	  CommunityEdge e = eit.next();
        	  
        	  if (drawnEdges.contains(e))
        		  continue;
        	  
        	  if (e.isConflictEdge()) {
        		image.setColor(Color.RED);
        	  } else if(e.getStart().getCommunity() == e.getEnd().getCommunity() && e.getStart().getCommunity() != -1) {
        	    image.setColor(e.getColor(graph));
        	  } else {
        	    image.setColor(Color.WHITE);
        	  }
        	  drawConnection(e, o, image);
        	  
        	  drawnEdges.add(e);
          }
        }
    }
    
    paint.setFinished(true);
  }
  
  @Override
  public void paint(Graphics arg0) {
	super.paint(arg0);
  }
}
