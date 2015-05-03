/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JPanel;
import com.satlib.graph.Clause;

/**
 *
 * @author zacknewsham
 */
public class ClausesPanel<T extends Clause> extends JPanel{
  protected GraphViewer graph;
  private int count = 0;
  public ClausesPanel(){
    
  }
  public ClausesPanel(GraphViewer graph, HashSet<T> clauses) {
    this.graph = graph;
    Iterator<T> cs = clauses.iterator();
    while(cs.hasNext()){
      addClause(cs.next());
    }
    this.setLayout(new GridLayout(count, 1));
  }
  
  public final void addClause(T c){
    ClausePanel l = getClausePanel(c);
    this.add(l);
    count++;
  }
  public ClausePanel getClausePanel(Clause c){
    return new ClausePanel(graph, c);
  }
}
