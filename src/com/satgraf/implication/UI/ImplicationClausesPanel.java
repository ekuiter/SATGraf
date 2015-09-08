/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satlib.graph.Clause;
import com.satgraf.graph.UI.GraphViewer;
import com.satlib.implication.ImplicationClause;
import java.util.HashSet;
import com.satgraf.graph.UI.ClausePanel;
import com.satgraf.graph.UI.ClausesPanel;

/**
 *
 * @author zacknewsham
 */
public class ImplicationClausesPanel extends ClausesPanel<ImplicationClause>{
  public ImplicationClausesPanel(){
    super();
  }
  public ImplicationClausesPanel(GraphViewer graph, HashSet<ImplicationClause> clauses) {
    super(graph, clauses);
  }
  
  @Override
  public ClausePanel getClausePanel(Clause c){
    return new ImplicationClausePanel(this.graph, c);
  }
}
