/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.placer;

import com.satgraf.graph.placer.jung.GraphWrapper;
import com.satlib.graph.Clause;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import java.awt.Dimension;

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
    return null;
  }

  double progress = 0;
  @Override
  public void init() {
    int steps = 0;
    layout.setSize(new Dimension(2500,2500));
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
