/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satgraf.graph.UI.SimpleGraphViewer;
import com.satgraf.graph.placer.Placer;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import com.satlib.implication.ImplicationGraph;
import com.satlib.implication.ImplicationNode;
import java.awt.Color;
import java.util.HashMap;

/**
 *
 * @author zacknewsham
 */
public class ImplicationGraphViewer extends SimpleGraphViewer{

  public ImplicationGraphViewer(Graph graph, HashMap node_lists, Placer placer) {
    super(graph, node_lists, placer);
  }
  
  @Override
  public ImplicationGraph getGraph(){
    return (ImplicationGraph)graph;
  }
  
  public Color getColor(Node node){
    ImplicationNode n = (ImplicationNode) node;
    if(n.isSet() == false){
      return Color.BLUE;
    }
    else if(n.isConflict()){
      return Color.WHITE;
    }
    else if(n.getValue()){
      return Color.GREEN;
    }
    else{
      return Color.RED;
    }
//    return n.getValue() == true ? Color.GREEN : (n.isSet() ? Color.BLACK : Color.WHITE);
  }
}
