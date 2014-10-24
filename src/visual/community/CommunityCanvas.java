/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.community;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import visual.UI.GraphCanvas;
import visual.UI.PaintThread;
import visual.graph.Edge;
import visual.graph.GraphViewer;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class CommunityCanvas extends GraphCanvas{
  public CommunityCanvas(GraphViewer graph){
    super(graph);
  }
  
  @Override
  public void paintThread(PaintThread paint) {
    Rectangle o = paint.getBounds();
    List<Node> nodes = graph.getOrderedUpdatedNodes(o, true);
    boolean hashUpdatedNode = graph.doesOrderedNodesHaveUpdate();
    
    if (hashUpdatedNode || nodes.isEmpty()) {
    	Iterator<Node> nodIt = nodes.iterator();
    	Graphics image = paint.getGraphics();
        Rectangle i = image.getClipBounds();
        image.setColor(Color.BLACK);
        image.fillRect(i.x, i.y, i.width, i.height);
        List<CommunityEdge> drawnEdges = new ArrayList<CommunityEdge>();
        
        while(nodIt.hasNext()){
          Node next = nodIt.next();
          drawNode(next, o, image);
          
          Iterator<CommunityEdge> eit = next.getEdges();
          while(eit.hasNext()) {
        	  CommunityEdge e = eit.next();
        	  
        	  if (drawnEdges.contains(e))
        		  continue;
        	  
        	  if(e.getStart().getCommunity() == e.getEnd().getCommunity() && e.getStart().getCommunity() != -1) {
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
}
