/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satlib.graph.Edge;
import com.satgraf.graph.UI.GraphViewer;
import com.satlib.implication.ImplicationNode;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import com.satgraf.graph.UI.EdgeCheckBoxPanel;
import com.satgraf.graph.UI.NodePanel;

/**
 *
 * @author zacknewsham
 */
public class ImplicationNodePanel extends NodePanel<ImplicationNode>{

  private final JLabel connectionsLabel = new JLabel("Intra Edges");
  private EdgeCheckBoxPanel connectionsPanel = new EdgeCheckBoxPanel();
  
  private final JLabel valueLabel = new JLabel("True/False");
  private final JComboBox value = new JComboBox(new String[]{"UNSET","FALSE","TRUE"});
  private final JLabel clausesLabel = new JLabel("Clauses");
  private ImplicationClausesPanel clausesPanel = new ImplicationClausesPanel();
  public ImplicationNodePanel(GraphViewer graph) {
    super(graph);
    value.addItemListener(new ItemListener() {

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
      }
    });
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
    panel.add(clausesPanel, c);
  }

  @Override
  public void update() {
    super.update();
    if(node == null){
      return;
    }
    HashSet<Edge> edges = new HashSet<Edge>();
    Iterator<Edge> cons = node.getEdges();
    while(cons.hasNext()){
      Edge con = cons.next();
      edges.add(con);
    }
    connectionsLabel.setText(String.format("Edges (%d)", edges.size()));
    clausesLabel.setText(String.format("Clauses (%d)", node.getClauses().size()));
    ItemListener cl = value.getItemListeners()[0];
    value.removeItemListener(cl);
    value.setSelectedItem(node.isSet() == false ? "UNSET" : (node.getValue() ? "TRUE" : "FALSE"));
    value.addItemListener(cl);
    panel.remove(connectionsPanel);
    connectionsPanel = new EdgeCheckBoxPanel(graph, edges);
    panel.remove(clausesPanel);
    clausesPanel = new ImplicationClausesPanel(graph, node.getClauses());
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 6;
    c.weighty = 0.45;
    panel.add(connectionsPanel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 8;
    c.weighty = 0.45;
    panel.add(clausesPanel, c);
  }
  
}
