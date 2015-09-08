/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.placer;

import com.satlib.graph.Clause;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class FruchGPUFalloverPlacer extends AbstractPlacer<Node, Graph<Node, Edge, Clause>>{
  private AbstractPlacer fruchImpl;
  
  public FruchGPUFalloverPlacer(Graph graph){
    super(graph);
    fruchImpl = new FruchGPUPlacer(graph);
  }
  
  @Override
  public Node getNodeAtXY(int x, int y, double scale) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void init() {
    try{
      fruchImpl.init();
    }
    catch(Exception e){
      e.printStackTrace();
      System.err.println("GPU Placer not supported, falling back to CPU placer");
      fruchImpl = new FruchPlacer(this.graph);
      fruchImpl.init();
    }
  }

  @Override
  public int getX(Node node) {
    return fruchImpl.getX(node);
  }

  @Override
  public int getY(Node node) {
    return fruchImpl.getY(node);
  }

  @Override
  public String getProgressionName() {
    return fruchImpl.getProgressionName();
  }

  @Override
  public double getProgress() {
    return fruchImpl.getProgress();
  }
  
}
