/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.satlib.graph.Clause;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class ClausePanel extends JPanel{
  private Clause c;
  protected GraphViewer graph;
  protected NodeLabel createNodeLabel(Node n, boolean isTrue){
    return new NodeLabel(graph, n, isTrue);
  }
  public ClausePanel(GraphViewer g, Clause c){
    this.c = c;
    this.graph = g;
    int count = 0;
    /*if(c.isConflict()){
      this.setBorder(new LineBorder(Color.RED));
    }
    else if(c.isSatisfied()){
      this.setBorder(new LineBorder(Color.GREEN));
    }*/
    Iterator<Node> ns = c.getNodes();
    while(ns.hasNext()){
      Node n = ns.next();
      JPanel panel = new JPanel();
      JLabel label = createNodeLabel(n, c.getValue(n));
      if(n.getValue()){
        label.setForeground(Color.GREEN);
      }
      else if(n.isSet()){
        label.setForeground(Color.RED);
      }
      
      panel.add(label);
      if(ns.hasNext()){
        panel.add(new JLabel(","));
      }
      this.add(panel);
      count++;
    }
    this.setLayout(new GridLayout(1, count));
  }
}
