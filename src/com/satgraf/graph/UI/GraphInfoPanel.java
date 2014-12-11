/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import com.satlib.graph.Graph;
import com.satlib.graph.GraphViewer;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphInfoPanel <T extends Graph> extends JPanel{
  protected T graph;
  protected int rows = 0;
  public GraphInfoPanel(T graph){
    this.graph = graph;
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
    this.add(new JLabel(String.valueOf(graph.getClausesCount())), c);
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Longest Clause"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    this.add(new JLabel(String.valueOf(graph.getLongestClause().size())), c);
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Vars"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    this.add(new JLabel(String.valueOf(graph.getNodeCount())), c);
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Unique Edges"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    this.add(new JLabel(String.valueOf(graph.getEdgesList().size())), c);
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Total Edges"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    this.add(new JLabel(String.valueOf(graph.getTotalEdges())), c);
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Weight"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    this.add(new JLabel(String.valueOf(graph.getWeight())), c);
  }
  
  public int getRows(){
    return rows;
  }
}
