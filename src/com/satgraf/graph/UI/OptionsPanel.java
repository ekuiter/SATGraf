/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class OptionsPanel extends JPanel{
  private final NodePanel nodePanel;
  private final JPanel content = new JPanel();
  private final JScrollPane scroll = new JScrollPane(content);
  private final JPanel options = new JPanel();
  private final GraphScaler scale;
  private final GraphViewer graph;
  private JCheckBox hideAllNodes = new JCheckBox("All Nodes", true);
  private JCheckBox hideAllEdges = new JCheckBox("All Edges", true);
  public OptionsPanel(GraphFrame frame, GraphViewer graph, NodePanel nodePanel){
    this.nodePanel = nodePanel;
    this.graph = graph;
    hideAllEdges.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(!hideAllEdges.isSelected()){
          OptionsPanel.this.graph.hideEdgeSet("All");
        }
        else{
          OptionsPanel.this.graph.showEdgeSet("All");
        }
      }
    });
    hideAllNodes.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(!hideAllNodes.isSelected()){
          OptionsPanel.this.graph.hideNodeSet("All");
        }
        else{
          OptionsPanel.this.graph.showNodeSet("All");
        }
      }
    });
    this.setLayout(new BorderLayout());
    this.add(scroll, BorderLayout.CENTER);
    scale = new GraphScaler(frame, graph);
    options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
    //options.setPreferredSize(new Dimension(300, 700));
    
    JPanel neOptions = new JPanel();
    neOptions.setLayout(new GridLayout(1, 2, 10, 0));
    neOptions.add(hideAllNodes);
    neOptions.add(hideAllEdges);

    this.options.add(scale);
    this.options.add(neOptions);
    this.options.add(Box.createRigidArea(new Dimension(0,10)));
    //this.options.setPreferredSize(new Dimension(100,100));
    
    content.setLayout(new BorderLayout());
    content.add(options, BorderLayout.NORTH);
    content.add(nodePanel, BorderLayout.SOUTH);
  }
  
  public void fromJson(JSONObject json){
    boolean allNodes = (Boolean)json.get("hideAllNodes");
    boolean allEdges = (Boolean)json.get("hideAllEdges");
    if(hideAllEdges.isSelected() != allEdges){
      hideAllEdges.setSelected(allEdges);
    }
    if(hideAllNodes.isSelected() != allNodes){
      hideAllNodes.setSelected(allNodes);
    }
    //scale.setScale(((Long)json.get("scale")).intValue());
  }
  
  public String toJson(){
    StringBuilder json = new StringBuilder();
    json.append("{\"hideAllNodes\":").append(hideAllNodes.isSelected()).append(",");
    //json.append(",\"scale\":").append(scale.getScale());
    json.append("\"hideAllEdges\":").append(hideAllEdges.isSelected()).append("}");
    return json.toString();
  }
  
  public NodePanel getNodePanel(){
    return this.nodePanel;
  }
  public void update(){
    nodePanel.update();
  }

  public void setGraph(GraphViewer graph) {
    scale.setGraph(graph);
  }
  public void setCustomComponent(Component c){
    options.add(c);
  }
  
  public void test() {
	  options.revalidate();
	  options.repaint();
	  options.updateUI();
  }
}
