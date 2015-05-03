/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public interface GraphViewerObserver {
  public enum Action{
    selectnode,
    setscale,
    actionPerformed,
    scaler,
    updatedNodes,
    updatedEdges,
    decisionVariable;
    
  }
  public void notify(GraphViewer graph, Action action);
  public void initFromJson(JSONObject json);
  public String toJson();
  public String JsonName();
}
