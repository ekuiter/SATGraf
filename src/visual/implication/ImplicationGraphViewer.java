/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import java.awt.Color;
import java.util.HashMap;
import visual.graph.Graph;
import visual.graph.Node;
import visual.graph.SimpleGraphViewer;

/**
 *
 * @author zacknewsham
 */
public class ImplicationGraphViewer extends SimpleGraphViewer{

  public ImplicationGraphViewer(Graph graph, HashMap node_lists) {
    super(graph, node_lists);
  }
  
  @Override
  public ImplicationGraph getGraph(){
    return (ImplicationGraph)graph;
  }
  
  protected Color getFillColor(Node node){
    ImplicationNode n = (ImplicationNode) node;
    if(n.isSet() == false){
      return Color.WHITE;
    }
    else if(n.isConflict()){
      return Color.RED;
    }
    else if(n.getValue()){
      return Color.GREEN;
    }
    else{
      return Color.BLACK;
    }
//    return n.getValue() == true ? Color.GREEN : (n.isSet() ? Color.BLACK : Color.WHITE);
  }
}
