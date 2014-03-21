/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import java.util.HashSet;
import visual.UI.ClausePanel;
import visual.UI.ClausesPanel;
import visual.graph.Clause;
import visual.graph.GraphViewer;

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
