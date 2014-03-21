/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import visual.UI.ClausePanel;
import visual.UI.NodeLabel;
import visual.graph.Clause;
import visual.graph.GraphViewer;
import visual.graph.Node;

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
