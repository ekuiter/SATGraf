/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

import com.satlib.community.CommunityNode;
import com.satlib.evolution.EvolutionGraph;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import com.satlib.graph.Clause;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
  private XYSeriesCollection dataset = new XYSeriesCollection();
  private XYSeries series = new XYSeries("All");
  private JFreeChart objChart = ChartFactory.createXYLineChart("Communities used", "# Decision", "# Communities", dataset);
  private final ChartPanel chartPanel = new ChartPanel(objChart);
  static{
    EvolutionObserverFactory.getInstance().register("VSIDST", VSIDSTemporalLocalityEvolutionObserver.class);
  }
  
  public VSIDSTemporalLocalityEvolutionObserver(EvolutionGraph graph){
    this.graph = graph;
    init();
    series.add(0, 0);
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
    dataset.addSeries(series);
    chartPanel.setPreferredSize(new Dimension(100,300));
    this.add(chartPanel, c);
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
  public void clauseAdded(Clause c) {
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  private synchronized void fullRedraw(){
    series.clear();
    series.add(0,0);
    Set<Integer> coms = new HashSet<>();
    int worstCase = Integer.parseInt(lblWorstCase.getText());
    int windowSize = Integer.parseInt(txtWindowSize.getText());
    for(int i = 0; i < decisions.size(); i++){
      if(i > 0 && i % windowSize == 0){
        series.add(i, coms.size());
        if(coms.size() > worstCase){
          worstCase = coms.size();
        }
        coms.clear();
      }
      coms.add(decisions.get(i).getCommunity());
    }
    lblWorstCase.setText(String.valueOf(worstCase));
  }

  @Override
  public synchronized void nodeAssigned(CommunityNode n, boolean isDecision) {
    int windowSize = Integer.parseInt(txtWindowSize.getText());
    if(isDecision){
      decisions.add(n);
    }
    if((decisions.size() % windowSize) == 0){
      Set<Integer> coms = new HashSet<>();
      int worstCase = Integer.parseInt(lblWorstCase.getText());
      for(int i = decisions.size() - 10; i < decisions.size(); i++){
        coms.add(decisions.get(i).getCommunity());
      }
      series.add(decisions.size(), coms.size());
      if(coms.size() > worstCase){
        worstCase = coms.size();
      }
      coms.clear();
      lblWorstCase.setText(String.valueOf(worstCase));
    }
  }
  public String getName(){
    return "VSIDS T";
  }

  
}
