/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.placer.jung;

import com.satgraf.graph.placer.AbstractPlacer;
import com.satgraf.graph.placer.PlacerFactory;
import com.satlib.graph.Clause;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */
public class JungWrapper extends AbstractPlacer<Node, Graph<Node, Edge, Clause>>{
  static{
    PlacerFactory.getInstance().register("jung", "use Jung", JungWrapper.class);
  }
  ISOMLayout<Node, Edge> layout;
  
  public JungWrapper(Graph g){
      super(g);
      layout = new ISOMLayout<>(new GraphWrapper(g));
  }
  @Override
  public Node getNodeAtXY(int x, int y, double scale) {
    x /= scale;
    y /= scale;
    Iterator<Node> nodes = graph.getNodes("All").iterator();
    Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
    while(nodes.hasNext()){
        Node node = (Node)nodes.next();
        r.x = getX(node) - DrawableNode.NODE_DIAMETER / 2;
        r.y = getY(node) - DrawableNode.NODE_DIAMETER / 2;
        if(r.contains(x, y)){
            return node;
        }
    }
    return null;
  }

  double progress = 0;
  @Override
  public void init() {
    int steps = 0;
    layout.setSize(new Dimension(10000,10000));
    layout.initialize();
    while(!layout.done()){
      layout.step();
      steps ++;
    progress = (double)steps / 2000.0;
    }
  }

  @Override
  public int getX(Node node) {
    return (int)layout.getX(node);
  }

  @Override
  public int getY(Node node) {
    return (int)layout.getY(node);
  }

  @Override
  public String getProgressionName() {
    return "Jung layout";
  }

  @Override
  public double getProgress() {
    return progress;
  }
  
}
