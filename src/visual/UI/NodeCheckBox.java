/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.UI;

import visual.graph.Node;
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
