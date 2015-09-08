/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import com.satgraf.community.UI.CommunityGraphInfoPanel;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.observers.EvolutionObserver;
import com.satlib.graph.Node;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JLabel;

/**
 *
 * @author zacknewsham
 */
public class EvolutionGraphInfoPanel extends CommunityGraphInfoPanel implements EvolutionObserver{
  private final JLabel lblRatio1 = new JLabel("n/a");
  private final JLabel lblRatio2 = new JLabel("n/a");
  private final JLabel lblRatio3 = new JLabel("n/a");
  public EvolutionGraphInfoPanel(EvolutionGraphViewer graphViewer) {
    super(graphViewer);
  }
  
  @Override
  public void init() {
    super.init();
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = getInsets();
    c.insets.left = 10;
    c.insets.right = 10;
    c.weighty = 1.0;
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("% Inter Decision Vars"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{      
      this.add(lblRatio1, c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("% Majority Inter Decision Vars"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{      
      this.add(lblRatio2, c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("% Decisions in same Community"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{      
      this.add(lblRatio3, c);
    }
    catch(UnsupportedOperationException e){
      
    }
  }

  @Override
  public void addEdge(CommunityEdge e) {
  }

  @Override
  public void removeEdge(CommunityEdge e) {
  }

  private Collection<CommunityNode> decisions = new ArrayList<>();
  @Override
  public void nodeAssigned(CommunityNode node, Node.NodeAssignmentState state, boolean isDecision) {
    if(isDecision){
      decisions.add(node);
      if(decisions.size() % 10 == 0){
        int lastCommunity = -1;
        int interCom = 0;
        int mostInterCom = 0;
        int sameCom = 0;
        for(CommunityNode n : decisions){
          int interComEdges = 0;
          for(CommunityEdge e : n.getEdgesList()){
            if(e.getOpposite(n).getCommunity() != n.getCommunity()){
              interComEdges++;
            }
          }
          if(interComEdges != 0){
            interCom++;
          }
          if(interComEdges > (double)n.getEdgesList().size() / 2.0){
            mostInterCom++;
          }
          if(lastCommunity == n.getCommunity()){
            sameCom++;
          }
          lastCommunity = n.getCommunity();
        }
        Double d1 = (double)interCom/(double)decisions.size();
        Double d2 = (double)mostInterCom/(double)decisions.size();
        Double d3 = (double)sameCom/(double)(decisions.size() - 1);
        lblRatio1.setText(d1.toString().substring(0, Math.min(d1.toString().length(), 5)));
        lblRatio1.revalidate();
        lblRatio2.setText(d2.toString().substring(0, Math.min(d2.toString().length(), 5)));
        lblRatio2.revalidate();
        lblRatio3.setText(d3.toString().substring(0, Math.min(d3.toString().length(), 5)));
        lblRatio3.revalidate();
      }
    }
  }

  @Override
  public void newFileReady() {
  }

  @Override
  public void setCommunityMetric(CommunityMetric metric) {
  }

  @Override
  public void updateGraph() {
  }
}
