/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satlib.graph.Clause;
import com.satgraf.graph.UI.GraphViewer;
import com.satlib.graph.Node;
import com.satlib.implication.ImplicationNode;
import com.satgraf.graph.UI.ClausePanel;
import com.satgraf.graph.UI.NodeLabel;

/**
 *
 * @author zacknewsham
 */
public class ImplicationClausePanel extends ClausePanel{

  public ImplicationClausePanel(GraphViewer graph, Clause c) {
    super(graph, c);
  }
  protected NodeLabel createNodeLabel(Node n, boolean isTrue){
    return new ImplicationNodeLabel(this.graph, (ImplicationNode)n, isTrue);
  }
  
}
