/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public interface GraphViewerObserver <N extends Node, E extends Edge, G extends Graph, V extends GraphViewer>{
  public enum Action{
    selectnode,
    setscale,
    actionPerformed,
    scaler,
    updatedNodes,
    updatedEdges,
    decisionVariable;
    
  }
  void notify(GraphViewer graph, Action action);
  void initFromJson(JSONObject json);
  
  String toJson();
  String JsonName();
}
