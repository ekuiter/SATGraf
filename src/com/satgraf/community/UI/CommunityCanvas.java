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
	
    if (paint.getForceDraw() || graph.checkIfNodesCauseUpdate(o) || graph.checkIfEdgesCauseUpdate(o)) {
    	Graphics image = paint.getGraphics();
        Rectangle i = image.getClipBounds();
        image.setColor(Color.BLACK);
        image.fillRect(i.x, i.y, i.width, i.height);
        
        drawEdges(image, o);
        drawNodes(image, o);
    }
    
    paint.setFinished(true);
  }
  
  private void drawNodes(Graphics image, Rectangle o) {
	Iterator<Node> nodIt = graph.getNodesInBounds(o, true).iterator();
	
    while(nodIt.hasNext()){
      Node next = nodIt.next();
      if(!next.isVisible()){
        continue;
      }
      drawNode(next, o, image);
    }
  }
  
  private void drawEdges(Graphics image, Rectangle o) {
	  Iterator<CommunityEdge> eit = (Iterator<CommunityEdge>) graph.getEdgesInBounds(o).iterator();
      while(eit.hasNext()) {
    	  CommunityEdge e = eit.next();
    	  
    	  if (!e.getStart().isVisible() || !e.getEnd().isVisible())
    		  continue;
    	  
    	  if (e.isConflictEdge()) {
    		image.setColor(Color.RED);
    	  } else if(e.getStart().getCommunity() == e.getEnd().getCommunity() && e.getStart().getCommunity() != -1) {
    	    image.setColor(e.getColor(graph));
    	  } else {
    	    image.setColor(Color.WHITE);
    	  }
    	  drawConnection(e, o, image);
      }
  }
  
  @Override
  public void paint(Graphics arg0) {
	super.paint(arg0);
  }
}
