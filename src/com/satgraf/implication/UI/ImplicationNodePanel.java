/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satgraf.graph.UI.EdgeCheckBoxPanel;
import com.satgraf.graph.UI.GraphViewer;
import com.satgraf.graph.UI.NodePanel;
import com.satlib.graph.Edge;
import com.satlib.graph.Node;
import com.satlib.implication.ImplicationClause;
import com.satlib.implication.ImplicationNode;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 *
 * @author zacknewsham
 */
public class ImplicationNodePanel extends NodePanel<ImplicationNode> implements ItemListener{

  private final JLabel connectionsLabel = new JLabel("Intra Edges");
  private EdgeCheckBoxPanel connectionsPanel = new EdgeCheckBoxPanel();
  
  private final JLabel valueLabel = new JLabel("True/False");
  private final JComboBox value = new JComboBox(new String[]{"UNSET","FALSE","TRUE"});
  private final JLabel clausesLabel = new JLabel("Clauses");
  private final ImplicationClausesPanel clausesPanel = new ImplicationClausesPanel();
  private final JScrollPane clausesScroll = new JScrollPane(clausesPanel);
  public ImplicationNodePanel(GraphViewer graph) {
    super(graph);
    value.addItemListener(this);
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 1;
    c.gridy = 3;
    panel.add(valueLabel, c);
    
    c.weightx = 0.0;
    c.gridx = 1;
    c.gridwidth = 1;
    c.gridy = 3;
    panel.add(value, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 5;
    panel.add(connectionsLabel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 6;
    c.weighty = 0.45;
    panel.add(connectionsPanel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 7;
    panel.add(clausesLabel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 8;
    c.weighty = 0.45;
    panel.add(clausesScroll, c);
  }
  
  @Override
  public void itemStateChanged(ItemEvent e) {
    if(value.getSelectedItem().equals("UNSET")){
      node.unset();
    }
    else if(value.getSelectedItem().equals("TRUE")){
      node.setValue(true, ImplicationNode.SET.DECISION);
    }
    else if(value.getSelectedItem().equals("FALSE")){
      node.setValue(false, ImplicationNode.SET.DECISION);
    }
    graph.addUpdatedNode(node, Node.NodeState.SHOW, true);
  }
  @Override
  public void update() {
    super.update();
    node = (ImplicationNode)graph.getSelectedNode();
    int edgesSize = 0;
    int clausesSize = 0;
    String setness = "UNSET";
    HashSet<Edge> edges = new HashSet<>();
    HashSet<ImplicationClause> clauses = new HashSet<>();
    if(node != null){
      clauses = node.getClauses();
      Iterator<Edge> cons = node.getEdges().iterator();
      while(cons.hasNext()){
        Edge con = cons.next();
        edges.add(con);
      }
      edgesSize = edges.size();
      clausesSize = node.getClauses().size();
      setness = node.isSet() == false ? "UNSET" : (node.getValue() ? "TRUE" : "FALSE");
    }
    connectionsLabel.setText(String.format("Edges (%d)", edgesSize));
    clausesLabel.setText(String.format("Clauses (%d)", clausesSize));
    ItemListener cl = value.getItemListeners()[0];
    value.removeItemListener(cl);
    value.setSelectedItem(setness);
    value.addItemListener(cl);
    panel.remove(connectionsPanel);
    connectionsPanel = new EdgeCheckBoxPanel(graph, edges);
    panel.remove(scroll);
    clausesPanel.clear();
    clausesPanel.addAll(clauses);
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 6;
    c.weighty = 0.45;
    panel.add(connectionsPanel, c);
  }
  
}
