/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import com.satgraf.UI.PaintThread;
import com.satgraf.UI.ThreadPaintable;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;
import com.satlib.graph.Edge;

/**
 *
 * @author zacknewsham
 */
public class SimpleCanvas extends GraphCanvas implements ThreadPaintable{
  public SimpleCanvas(GraphViewer graph){
    super(graph);
  }
  
  @Override
  public void paintThread(PaintThread paint) {
    //synchronized(images){
      Rectangle o = paint.getBounds();
      Graphics image = paint.getGraphics();
      Rectangle i = image.getClipBounds();
      int offsetX = (o.x - i.x);
      int offsetY = (o.x - i.x);
      image.setColor(Color.WHITE);
      image.fillRect(i.x, i.y, i.width, i.height);
      Iterator<Node> nodes = graph.getNodeIterator();
      while(nodes.hasNext()){
        Node next = nodes.next();
        boolean contains = false;
        //if(o.contains(new Point(next.getX(graph) , next.getY(graph)))){
          drawNode(next, o, image);
          contains = true;
        //}
      }
      Iterator<Edge> conns = graph.getEdgeIterator();
      while(conns.hasNext()){
        Edge c = conns.next();
        image.setColor(Color.BLACK);
        if(c.getStart().getX(graph) >= 0 && c.getStart().getY(graph) >= 0 && c.getEnd().getX(graph) >= 0 && c.getEnd().getY(graph) >= 0){
          drawConnection(c, o, image);
        }
      }
    //}
    //painters.remove(paint);
    paint.setFinished(true);
  }
}
