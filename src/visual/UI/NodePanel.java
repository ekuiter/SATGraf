/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import visual.graph.GraphViewer;
import visual.graph.Node;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author zacknewsham
 */
public class NodePanel<T extends Node> extends JPanel{
  
  private final JLabel nameLabel = new JLabel("Node Name");
  private final JLabel locationLabel = new JLabel("Location");
  private final JLabel name = new JLabel("");
  private final JLabel location = new JLabel("");
  
  protected final JPanel panel = new JPanel();
  protected final JScrollPane scroll = new JScrollPane(panel);
  protected final GraphViewer graph;
  protected T node;
  public NodePanel(GraphViewer graph){
    this.graph = graph;
    
    panel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.gridx = 0;
    c.gridy = 0;
    panel.add(nameLabel, c);
    
    c.weightx = 0.9;
    c.gridx = 1;
    c.gridy = 0;
    panel.add(name, c);
    
    c.weightx = 0.1;
    c.gridx = 0;
    c.gridy = 1;
    panel.add(locationLabel, c);
    
    c.weightx = 0.9;
    c.gridx = 1;
    c.gridy = 1;
    panel.add(location, c);
    
    this.setLayout(new BorderLayout());
    this.add(scroll, BorderLayout.CENTER);
  }
  
  public void setNode(T node){
    this.node = node;
  }
  public void update(){
    if(node == null){
      return;
    }
    name.setText(node.toString());
    location.setText(String.format("(%d, %d)", node.getX(graph), node.getY(graph)));
  }
}
