/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import javax.swing.JLabel;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class NodeLabel<T extends Node> extends JLabel{
  protected T node;
  protected GraphViewer graph;
  public NodeLabel(GraphViewer g,T n, boolean tf){
    super(String.format("%s%s",tf ? "" : "-", n.getName()));
    this.node = n;
    this.graph = g;
  }
}
