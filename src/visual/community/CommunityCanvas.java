/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.community;

import visual.UI.PaintThread;
import visual.UI.GraphCanvas;
import visual.graph.GraphViewer;
import visual.graph.Node;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;

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
    Graphics image = paint.getGraphics();
    Rectangle i = image.getClipBounds();
    int offsetX = (o.x - i.x);
    int offsetY = (o.x - i.x);
    image.setColor(Color.WHITE);
    image.fillRect(i.x, i.y, i.width, i.height);
    Iterator<Node> nodes = graph.getNodes("All", o).iterator();
    while(nodes.hasNext()){
      Node next = nodes.next();
      boolean contains = false;
      //if(o.contains(new Point(next.getX(graph) , next.getY(graph)))){
        drawNode(next, o, image);
        contains = true;
      //}
    }
    Iterator<CommunityEdge> conns = graph.getConnections(o);
    while(conns.hasNext()){
      CommunityEdge c = conns.next();
      if(c.getStart().getCommunity() == c.getEnd().getCommunity() && c.getStart().getCommunity() != -1){
        image.setColor(c.getColor(graph));
      }
      else{
        image.setColor(Color.BLACK);
      }
      drawConnection(c, o, image);
    }
    paint.setFinished(true);
  }
}
