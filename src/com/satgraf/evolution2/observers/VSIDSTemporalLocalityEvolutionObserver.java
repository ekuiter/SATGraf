/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.EvolutionGraph;
import com.satlib.evolution.observers.EvolutionObserver;
import com.satlib.evolution.observers.EvolutionObserverFactory;
import com.satlib.graph.Clause;
import com.satlib.graph.Node;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author zacknewsham
 */
public class VSIDSTemporalLocalityEvolutionObserver extends JPanel implements EvolutionObserver{
  private final EvolutionGraph graph;
  private final JTextField txtWindowSize = new JTextField("10");
  private final JLabel lblWorstCase = new JLabel("0");
  private static final int VARS_PER_REDRAW = 10;
  private final List<CommunityNode> decisions = new ArrayList<>();
  private HashMap<CommunityNode, List<CommunityNode>> propogations = new HashMap<>();
  private final XYSeriesCollection dataset = new XYSeriesCollection();
  private final XYSeries decisionSeries = new XYSeries("Decisions");
  private final XYSeries propogationSeries = new XYSeries("Propogations");
  private final JFreeChart objChart = ChartFactory.createXYLineChart("Communities used", "# Decision", "# Communities", dataset);
  private final ChartPanel chartPanel = new ChartPanel(objChart);
  private final JScrollPane chartScroll = new JScrollPane(chartPanel);
  private CommunityMetric metric;
  static{
    EvolutionObserverFactory.getInstance().register("VSIDST", VSIDSTemporalLocalityEvolutionObserver.class);
  }
  
  public VSIDSTemporalLocalityEvolutionObserver(EvolutionGraph graph){
    this.graph = graph;
    init();
    decisionSeries.add(0, 0);
    propogationSeries.add(0, 0);
    propogations.put(null, new ArrayList<CommunityNode>());
    txtWindowSize.addActionListener(new ActionListener(){

      @Override
      public void actionPerformed(ActionEvent ae) {
        fullRedraw();
      }
      
    });
  }

  public final void init(){
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = getInsets();
    c.insets.left = 10;
    c.insets.right = 10;
    c.anchor = GridBagConstraints.PAGE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weighty = 0.0;
    
    c.gridx = 0;
    c.gridy = 0;
    c.weighty = 0.8;
    c.weightx = 1.0;
    c.gridheight = 1;
    c.fill = GridBagConstraints.BOTH;
    c.gridwidth = 4;
    dataset.addSeries(decisionSeries);
    dataset.addSeries(propogationSeries);
    chartPanel.setPreferredSize(new Dimension(100,300));
    chartPanel.setMinimumSize(chartPanel.getPreferredSize());
    this.add(chartScroll, c);
    c.ipady = 0;
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 1;
    c.weighty = 0.0;
    c.weightx = 0.5;
    c.gridwidth = 3;
    c.gridheight = 1;
    this.add(new JLabel("Window Size"), c);
    
    c.gridx = 3;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    this.add(txtWindowSize, c);
    
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 3;
    c.gridheight = 1;
    this.add(new JLabel("Worst case #coms"), c);
    
    c.gridx = 3;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    this.add(lblWorstCase, c);
  }
  
  
  @Override
  public void addEdge(CommunityEdge e){
    
  }
  
  @Override
  public void removeEdge(CommunityEdge e){
    
  }
  
  private synchronized void fullRedraw(){
    decisionSeries.clear();
    decisionSeries.add(0,0);
    propogationSeries.clear();
    propogationSeries.add(0,0);
    Set<Integer> decisionComs = new HashSet<>();
    Set<Integer> propogationComs = new HashSet<>();
    int worstCase = Integer.parseInt(lblWorstCase.getText());
    int windowSize = Integer.parseInt(txtWindowSize.getText());
    for(CommunityNode p : propogations.get(null)){
      propogationComs.add(p.getCommunity());
    }
    propogationSeries.update(new Integer(0), new Integer(propogationComs.size()));
    propogationComs.clear();
    for(int i = 0; i < decisions.size(); i++){
      if(i > 0 && i % windowSize == 0){
        propogationSeries.add(i, propogationComs.size());
        decisionSeries.add(i, decisionComs.size());
        if(decisionComs.size() > worstCase){
          worstCase = decisionComs.size();
        }
        decisionComs.clear();
        propogationComs.clear();
      }
      CommunityNode d = decisions.get(i);
      for(CommunityNode p : propogations.get(d)){
        propogationComs.add(p.getCommunity());
      }
      decisionComs.add(d.getCommunity());
    }
    lblWorstCase.setText(String.valueOf(worstCase));
  }

  
  @Override
  public void setCommunityMetric(CommunityMetric metric){
    this.metric = metric;
  }
  
  @Override
  public synchronized void nodeAssigned(CommunityNode n, Node.NodeAssignmentState state, boolean isDecision) {
    if(state == Node.NodeAssignmentState.UNASSIGNED){
      return;
    }
    int windowSize = Integer.parseInt(txtWindowSize.getText());
    if(isDecision){
      decisions.add(n);
      propogations.put(n, new ArrayList<CommunityNode>());
      if(decisions.size() == 1){
        Set<Integer> propogationComs = new HashSet<>();
        for(CommunityNode p : propogations.get(null)){
          propogationComs.add(p.getCommunity());
        }
        propogationSeries.update(new Integer(0), new Integer(propogationComs.size()));
      }
    }
    else{
      CommunityNode d = null;
      if(!decisions.isEmpty()){
        d = decisions.get(decisions.size() - 1);
      }
      propogations.get(d).add(n);
    }
    if(isDecision && !decisions.isEmpty() && (decisions.size() % windowSize) == 0){
      Set<Integer> decisionComs = new HashSet<>();
      Set<Integer> propogationComs = new HashSet<>();
      int worstCase = Integer.parseInt(lblWorstCase.getText());
      for(int i = decisions.size() - 10; i < decisions.size(); i++){
        decisionComs.add(decisions.get(i).getCommunity());
        if(i > 1){
          CommunityNode d = decisions.get(i - 1);
          for(CommunityNode p : propogations.get(d)){
            propogationComs.add(p.getCommunity());
          }
        }
      }
      decisionSeries.add(decisions.size(), decisionComs.size());
      propogationSeries.add(decisions.size(), propogationComs.size());
      if(decisionComs.size() > worstCase){
        worstCase = decisionComs.size();
      }
      decisionComs.clear();
      propogationComs.clear();
      lblWorstCase.setText(String.valueOf(worstCase));
    }
  }
  

  @Override
  public void newFileReady() {
    
  }
  
  public String getName(){
    return "VSIDS T";
  }

  @Override
  public void updateGraph() {
  }

  
}
