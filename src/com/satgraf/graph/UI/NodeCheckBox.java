/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import com.satlib.graph.Node;
import javax.swing.JCheckBox;

/**
 *
 * @author zacknewsham
 */
public class NodeCheckBox extends JCheckBox{
  private Node node;
  public NodeCheckBox(Node node){
    super(node.getName());
    this.node = node;
  }
  public Node getNode(){
    return this.node;
  }
}
