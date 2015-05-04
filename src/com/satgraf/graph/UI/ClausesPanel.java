/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import com.satlib.graph.Clause;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author zacknewsham
 */
public class ClausesPanel<T extends Clause> extends JPanel{
  protected GraphViewer graph;
  private int count = 0;
  private int longestClausePanelLength = 0;
  public ClausesPanel(){
    
  }
  public ClausesPanel(GraphViewer graph, HashSet<T> clauses) {
    this.graph = graph;
    addAll(clauses);
  }
  public void clear(){
    count = 0; 
    this.removeAll();
  }
  public void addAll(Collection<T> c){
    for(T clause: c){
      addClause(clause);
    }
    this.setLayout(new GridLayout(count, 1));
    this.setPreferredSize(new Dimension(longestClausePanelLength, count * 20));
  }
  public final void addClause(T c){
    ClausePanel l = getClausePanel(c);
    if(longestClausePanelLength < l.getPreferredSize().width){
      longestClausePanelLength = l.getPreferredSize().width;
    }
    
    this.add(l);
    count++;
  }
  public ClausePanel getClausePanel(Clause c){
    return new ClausePanel(graph, c);
  }
}
