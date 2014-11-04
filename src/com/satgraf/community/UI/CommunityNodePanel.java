/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.graph.UI.NodePanel;
import com.satgraf.graph.UI.EdgeCheckBoxPanel;
import com.satlib.graph.GraphViewer;
import com.satlib.community.CommunityNode;
import com.satlib.community.CommunityEdge;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JLabel;

/**
 *
 * @author zacknewsham
 */
public class CommunityNodePanel extends NodePanel<CommunityNode>{
  private final JLabel communityLabel = new JLabel("Community");
  private final JLabel community = new JLabel("");
  private final JLabel interLabel = new JLabel("Inter Edges");
  private final JLabel intraLabel = new JLabel("Intra Edges");
  
  private EdgeCheckBoxPanel interPanel = new EdgeCheckBoxPanel();
  private EdgeCheckBoxPanel intraPanel = new EdgeCheckBoxPanel();
  public CommunityNodePanel(GraphViewer graph) {
    super(graph);
    GridBagConstraints c = new GridBagConstraints();
    
    c.weightx = 0.1;
    c.gridx = 0;
    c.gridy = 2;
    panel.add(communityLabel, c);
    
    c.weightx = 0.9;
    c.gridx = 1;
    c.gridy = 2;
    panel.add(community, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 3;
    panel.add(interLabel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 4;
    c.weighty = 0.45;
    panel.add(interPanel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 5;
    panel.add(intraLabel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 6;
    c.weighty = 0.45;
    panel.add(intraPanel, c);
    
    this.setLayout(new BorderLayout());
    this.add(scroll, BorderLayout.CENTER);
  }
  
  @Override
  public void update(){
    super.update();
    if(node == null){
    	community.setText("");
    	interLabel.setText("");
    	intraLabel.setText("");
      return;
    }
    community.setText(String.format("%d", node.getCommunity()));
    HashSet<CommunityEdge> inter = new HashSet<CommunityEdge>();
    HashSet<CommunityEdge> intra = new HashSet<CommunityEdge>();
    Iterator<CommunityEdge> cons = node.getEdges();
    while(cons.hasNext()){
      CommunityEdge con = (CommunityEdge)cons.next();
      if(con.getStart().getCommunity() == con.getEnd().getCommunity()){
        intra.add(con);
      }
      else{
        inter.add(con);
      }
    }
    interLabel.setText(String.format("Inter Edges (%d)", inter.size()));
    intraLabel.setText(String.format("Intra Edges (%d)", intra.size()));
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    
    panel.remove(interPanel);
    panel.remove(intraPanel);
    interPanel = new EdgeCheckBoxPanel(graph, inter);
    intraPanel = new EdgeCheckBoxPanel(graph, intra);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 4;
    c.weighty = 0.45;
    panel.add(interPanel, c);
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 6;
    c.weighty = 0.45;
    panel.add(intraPanel, c);
  }
}
