/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satgraf.graph.UI.GraphInfoPanel;
import com.satlib.graph.Graph;

/**
 *
 * @author zacknewsham
 */
public class ImplicationGraphInfoPanel extends GraphInfoPanel<ImplicationGraphViewer>{

  public ImplicationGraphInfoPanel(ImplicationGraphViewer graphViewer) {
    super(graphViewer);
  }

  @Override
  public Graph getGraph() {
    return graph;
  }
  
}
