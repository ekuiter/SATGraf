/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import com.satlib.graph.Graph;
import com.satlib.graph.LiteralGraph;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphInfoPanel <T extends GraphViewer> extends JPanel{
  protected T graphViewer;
  protected Graph graph;
  protected int rows = 0;
  public GraphInfoPanel(T graphViewer){
    this.graphViewer = graphViewer;
    this.graph = graphViewer.graph;
  }
  public Graph getGraph(){
    return graph;
  }
  
  public void init(){
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = getInsets();
    c.insets.left = 10;
    c.insets.right = 10;
    c.anchor = GridBagConstraints.PAGE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weighty = 1.0;
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 4;
    c.gridheight = 1;
    this.add(new JLabel(graph.getName()), c);
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Clauses"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(graph.getClausesCount())), c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Longest Clause"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(graph.getLongestClause().size())), c);
    }
    catch(UnsupportedOperationException | NullPointerException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    if(graph instanceof LiteralGraph){
      this.add(new JLabel("Literals"), c);
    }
    else{
      this.add(new JLabel("Vars"), c);
    }
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(graph.getNodeCount())), c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Unique Edges"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(graph.getEdges().size())), c);
    }
    catch(UnsupportedOperationException | NullPointerException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Total Edges"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(graph.getTotalEdges())), c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Weight"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(graph.getWeight())), c);
    }
    catch(UnsupportedOperationException e){
      
    }
  }
  
  public int getRows(){
    return rows;
  }
}
