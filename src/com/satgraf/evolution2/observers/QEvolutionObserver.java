/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityNode;
import com.satlib.community.ConcreteCommunityGraph;
import com.satlib.evolution.Evolution2GraphViewer;
import com.satlib.evolution.EvolutionGraph;
import com.satlib.graph.Clause;
import com.satlib.graph.Node;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class QEvolutionObserver extends JPanel implements VisualEvolutionObserver{
  private final EvolutionGraph graph;
  private final CommunityGraph tmpGraph;
  private final JTextField txtWindowSize = new JTextField("10");
  private final JLabel lblWorstCase = new JLabel("1.0");
  private final JLabel lblBestCase = new JLabel("0.0");
  private final List<CommunityNode> decisions = new ArrayList<>();
  private final XYSeriesCollection dataset = new XYSeriesCollection();
  private final XYSeries qSeries = new XYSeries("Q");
  private final JFreeChart objChart = ChartFactory.createXYLineChart("Q Evolution", "# Decision", "Q", dataset);
  private final ChartPanel chartPanel = new ChartPanel(objChart);
  private final JScrollPane chartScroll = new JScrollPane(chartPanel);
  private CommunityMetric metric;
  static{
    com.satlib.evolution.observers.EvolutionObserverFactory.getInstance().register("Q", QEvolutionObserver.class);
  }

  public QEvolutionObserver(Evolution2GraphViewer graphViewer){
    this.graph = graphViewer.getGraph();
    tmpGraph = new ConcreteCommunityGraph(){
      
      public Iterator<CommunityNode> getNodesIterator(){
        return getNodes().iterator();
      }
      
      @Override
      public Iterator<CommunityEdge> getEdges(){
        return getEdgesList().iterator();
      }
      
      @Override
      public Collection<CommunityNode> getNodes(){
        Collection<CommunityNode> nodes = new ArrayList<>();
        for(CommunityNode n : super.getNodes()){
          if(n.getAssignmentState() == null || n.getAssignmentState() == Node.NodeAssignmentState.UNASSIGNED){
            nodes.add(n);
          }
        }
        return nodes;
      }
      
      @Override
      public Collection<CommunityEdge> getEdgesList(){
        Collection<CommunityEdge> edges = new ArrayList<>();
        for(CommunityEdge e : super.getEdgesList()){
          if((e.getStart().getAssignmentState() == null || e.getStart().getAssignmentState() == Node.NodeAssignmentState.UNASSIGNED) &&
                  (e.getEnd().getAssignmentState() == null || e.getEnd().getAssignmentState() == Node.NodeAssignmentState.UNASSIGNED)){
            edges.add(e);
          }
        }
        return edges;
      }
    };
    for(CommunityNode n : graph.getNodes()){
      tmpGraph.createNode(n.getId(), n.getName());
    }
    for(CommunityEdge e : graph.getEdgesList()){
     tmpGraph.createEdge(tmpGraph.getNode(e.getStart().getId()), tmpGraph.getNode(e.getEnd().getId()), false);
    }
    
    txtWindowSize.addActionListener(new ActionListener(){

      @Override
      public void actionPerformed(ActionEvent ae) {
        //fullRedraw();
      }
      
    });
    init();
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
    dataset.addSeries(qSeries);
    chartPanel.setPreferredSize(new Dimension(100,300));
    chartPanel.setMinimumSize(chartPanel.getPreferredSize());
    this.add(chartScroll, c);
    c.ipady = 0;
    qSeries.add(0, graph.getQ());
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
    this.add(new JLabel("Worst case Q"), c);
    
    c.gridx = 3;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    lblWorstCase.setText(String.format("%.03f", graph.getQ()));
    this.add(lblWorstCase, c);
    
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 3;
    c.gridheight = 1;
    this.add(new JLabel("Best case Q"), c);
    
    c.gridx = 3;
    c.gridy = 3;
    c.gridwidth = 1;
    c.gridheight = 1;
    lblBestCase.setText(String.format("%.03f", graph.getQ()));
    this.add(lblBestCase, c);
  }
  
  @Override
  public void setCommunityMetric(CommunityMetric metric){
    this.metric = metric;
  }
  
  @Override
  public void addEdge(CommunityEdge e){
    tmpGraph.createEdge(tmpGraph.getNode(e.getStart().getId()), tmpGraph.getNode(e.getEnd().getId()), false);
  }
  
  @Override
  public void removeEdge(CommunityEdge e){
    CommunityEdge _e = tmpGraph.createEdge(tmpGraph.getNode(e.getStart().getId()), tmpGraph.getNode(e.getEnd().getId()), false);
    tmpGraph.removeEdge(_e);
  }

  @Override
  public void nodeAssigned(CommunityNode n, Node.NodeAssignmentState state, boolean isDecision) {
    int windowSize = Integer.parseInt(txtWindowSize.getText());
    CommunityNode np = tmpGraph.getNode(n.getId());
    np.setAssignmentState(state);
    if(isDecision){
      decisions.add(n);
      if(decisions.size() % windowSize == 0 && decisions.size() != 1){
        double Q = metric.getCommunities(tmpGraph);
        double worstCase = Double.parseDouble(lblWorstCase.getText());
        double bestCase = Double.parseDouble(lblBestCase.getText());
        if(Q < worstCase){
          lblWorstCase.setText(String.format("%.03f", Q));
        }
        if(Q > bestCase){
          lblBestCase.setText(String.format("%.03f", Q));
        }
        qSeries.add(decisions.size(), Q);
      }
    }
  }

  @Override
  public void newFileReady() {
    
  }

  @Override
  public String getName() {
    return "Q Progression";
  }

  @Override
  public void updateGraph() {
    
  }
  
}
