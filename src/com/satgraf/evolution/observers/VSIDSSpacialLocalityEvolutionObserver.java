/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.observers;

import com.satgraf.evolution.UI.EvolutionGraphViewer;
import com.satgraf.supplemental.SupplementalView;
import com.satgraf.supplemental.SupplementalViewFactory;
import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.EvolutionGraph;
import com.satlib.evolution.observers.EvolutionObserver;
import com.satlib.evolution.observers.EvolutionObserverFactory;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;

/**
 *
 * @author zacknewsham
 */
public class VSIDSSpacialLocalityEvolutionObserver extends JPanel implements EvolutionObserver, ChartMouseListener, SupplementalView<CommunityNode, CommunityEdge, EvolutionGraph,EvolutionGraphViewer>{
  private final EvolutionGraph graph;
  private EvolutionGraphViewer graphViewer;
  private final SortableCategoryDataset dataset = new SortableCategoryDataset();
  private final JFreeChart objChart = ChartFactory.createStackedAreaChart("Communities used", "Community ID", "# Decision", dataset);
  private final ChartPanel chartPanel = new ChartPanel(objChart);
  private final JScrollPane chartScroll = new JScrollPane(chartPanel);
  private final String SERIES_1 = "Decisions";
  private final SelectableBarRenderer renderer = new SelectableBarRenderer();
  private final JRadioButton rdoDistributionTotal = new JRadioButton();
  private final JRadioButton rdoDistributionRatio = new JRadioButton();
  private final JRadioButton rdoCommunity = new JRadioButton();
  private Comparator currentComparator = COMMUNITY_COMPARATOR;
  private CommunityMetric metric;
  static{
    SupplementalViewFactory.getInstance().register("VSIDSS", "A graphical representation of the spacial locailty of the VSIDS decision heuristic", VSIDSSpacialLocalityEvolutionObserver.class);
  }
  public VSIDSSpacialLocalityEvolutionObserver(Graph _graph){
    this.graph = (EvolutionGraph)_graph;
    EvolutionObserverFactory.getInstance().addObserver(this);
    
    rdoDistributionTotal.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ae) {
        if(rdoDistributionTotal.isSelected() || rdoDistributionRatio.isSelected()){
          currentComparator = DISTRIBUTION_COMPARATOR;
        }
        else {
          currentComparator = COMMUNITY_COMPARATOR;
        }
        synchronized(dataset){
          dataset.sort(currentComparator, rdoDistributionRatio.isSelected() ? 1 : 0);
        }
      }
    });
    rdoCommunity.addActionListener(rdoDistributionTotal.getActionListeners()[0]);
    rdoDistributionRatio.addActionListener(rdoDistributionTotal.getActionListeners()[0]);
  }
  
  public final void init(){
    synchronized(dataset){
      ButtonGroup group = new ButtonGroup();
      group.add(rdoCommunity);
      group.add(rdoDistributionTotal);
      group.add(rdoDistributionRatio);
      for(Community c : graph.getCommunities()){
        dataset.addValue(0, SERIES_1.concat(" (Total)"), String.valueOf(c.getId()));
        dataset.addValue(0, SERIES_1.concat(" (Ratio)"), String.valueOf(c.getId()));
      }
      KeyToGroupMap map = new KeyToGroupMap("G1");

      Paint p1 = new ChartColor(0x00, 0xff, 0x00);

      Paint p2 = new ChartColor(0xff, 0x00, 0x00);
      map.mapKeyToGroup(SERIES_1.concat(" (Total)"), "G1");
      map.mapKeyToGroup(SERIES_1.concat(" (Ratio)"), "G2");

      renderer.setSeriesPaint(0, p1);
      renderer.setSeriesPaint(1, p2);
      renderer.setSeriesToGroupMap(map); 
      renderer.setItemMargin(-0.75);
      dataset.sort(COMMUNITY_COMPARATOR, 0);
    }
         
    
        
    chartPanel.addChartMouseListener(this); 
    objChart.getCategoryPlot().setRenderer(renderer);
    ((NumberAxis)objChart.getCategoryPlot().getRangeAxis()).setTickUnit(new NumberTickUnit(1));
    renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("Community {1} = {2}", NumberFormat.getInstance()));
    
    //SubCategoryAxis domainAxis = new SubCategoryAxis("Decisions");
    CategoryAxis domainAxis = objChart.getCategoryPlot().getDomainAxis();
    domainAxis.setCategoryMargin(0.15);
    //domainAxis.setTickMarksVisible(false);
    //domainAxis.setMinorTickMarksVisible(false);
    //objChart.getCategoryPlot().setDomainAxis(domainAxis);
    
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
    c.weighty = 0.925;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.gridheight = 1;
    c.gridwidth = 4;
    chartPanel.setPreferredSize(new Dimension(graph.getCommunities().size() * 10,300));
    chartPanel.setMinimumSize(new Dimension(graph.getCommunities().size() * 10,300));
    chartScroll.setPreferredSize(chartPanel.getPreferredSize());
    this.add(chartScroll, c);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    
    c.gridx = 0;
    c.gridy = 1;
    c.weighty = 0.025;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 3;
    this.add(new JLabel("Community"), c);
    
    c.gridx = 3;
    c.gridy = 1;
    c.weighty = 0.025;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 1;
    rdoCommunity.setSelected(true);
    this.add(rdoCommunity, c);
    
    c.gridx = 0;
    c.gridy = 2;
    c.weighty = 0.025;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 3;
    this.add(new JLabel("Distribution (Total)"), c);
    
    c.gridx = 3;
    c.gridy = 2;
    c.weighty = 0.025;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 1;
    this.add(rdoDistributionTotal, c);
    
    c.gridx = 0;
    c.gridy = 3;
    c.weighty = 0.025;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 3;
    this.add(new JLabel("Distribution (Ratio)"), c);
    
    c.gridx = 3;
    c.gridy = 3;
    c.weighty = 0.025;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 1;
    this.add(rdoDistributionRatio, c);
    
  }

  
  @Override
  public void setCommunityMetric(CommunityMetric metric){
    this.metric = metric;
  }
  
  
  @Override
  public void addEdge(CommunityEdge e){
    
  }
  
  @Override
  public void removeEdge(CommunityEdge e){
    
  }

  @Override
  public void nodeAssigned(CommunityNode n, Node.NodeAssignmentState state, boolean isDecision) {
    if(state == Node.NodeAssignmentState.UNASSIGNED){
      return;
    }
    if(isDecision){
      while(dataset.getColumnCount() == 0){}
      synchronized(dataset){
        int decisions = dataset.getValue(SERIES_1.concat(" (Total)"), String.valueOf(n.getCommunity())).intValue();
        dataset.setValue(decisions + 1, SERIES_1.concat(" (Total)"), String.valueOf(n.getCommunity()));
        dataset.setValue(decisions / (double)graph.getCommunitySize(n.getCommunity()), SERIES_1.concat(" (Ratio)"), String.valueOf(n.getCommunity()));
        if(currentComparator == DISTRIBUTION_COMPARATOR)
        dataset.sort(currentComparator, rdoDistributionRatio.isSelected() ? 1 : 0);
      }
    }
  }
  
  
  @Override
  public String getName(){
    return "VSIDS S";
  }

  @Override
  public void chartMouseClicked(ChartMouseEvent cme) {
      Plot p = cme.getChart().getPlot();
      if(cme.getEntity() instanceof CategoryItemEntity){
        renderer.selected = (CategoryItemEntity)cme.getEntity();
        graphViewer.selectCommunity(Integer.valueOf((String)renderer.selected.getColumnKey()));
      }
      else{
        renderer.selected = null;
        graphViewer.selectCommunity(null);
      }
      objChart.getCategoryPlot().datasetChanged(new DatasetChangeEvent(cme, dataset));
      chartPanel.repaint();
  }

  @Override
  public void chartMouseMoved(ChartMouseEvent cme) {
      Plot p = cme.getChart().getPlot();
      if(cme.getEntity() instanceof CategoryItemEntity){
        renderer.hover = (CategoryItemEntity)cme.getEntity();
        graphViewer.selectCommunity(Integer.valueOf((String)renderer.hover.getColumnKey()));
        objChart.getCategoryPlot().datasetChanged(new DatasetChangeEvent(cme, dataset));
        chartPanel.repaint();
      }
      else {
        boolean repaint = false;
        if(renderer.hover != null){
          repaint = true;
        }
        renderer.hover = null;
        if(renderer.selected != null){
          graphViewer.selectCommunity(Integer.valueOf((String)renderer.selected.getColumnKey()));
        }
        else{
          graphViewer.selectCommunity(null);
        }
        if(repaint){
          objChart.getCategoryPlot().datasetChanged(new DatasetChangeEvent(cme, dataset));
          chartPanel.repaint();
        }
      }
  }

  @Override
  public void newFileReady() {
    
  }

  @Override
  public void updateGraph() {
    
  }

  @Override
  public void setGraphViewer(EvolutionGraphViewer v) {
    this.graphViewer = v;
  }
  
  private static final class SortableDatasetEntry{
    Number value;
    Comparable key;
    public SortableDatasetEntry(Number value, Comparable key){
      this.value = value;
      this.key = key;
    }
  }
  
  private final static Comparator<SortableDatasetEntry> COMMUNITY_COMPARATOR = new Comparator<SortableDatasetEntry>(){
    @Override
    public int compare(SortableDatasetEntry t, SortableDatasetEntry t1) {
      return Integer.parseInt(t.key.toString()) - (Integer.parseInt(t1.key.toString()));
    }
  };
    
  
  private final static Comparator<SortableDatasetEntry> DISTRIBUTION_COMPARATOR = new Comparator<SortableDatasetEntry>(){
    @Override
    public int compare(SortableDatasetEntry t, SortableDatasetEntry t1) {
      return new Double((t.value.doubleValue() - t1.value.doubleValue()) * 10000).intValue();
    }
  };
  
  private static class SortableCategoryDataset extends DefaultCategoryDataset{
    public SortableCategoryDataset(){
      
    }
    
    public void sort(Comparator<SortableDatasetEntry> comparator, int rowIndex){
      Map<Comparable, List<SortableDatasetEntry>> rows = new HashMap<>();
      List<Comparable> keys = new ArrayList<>();
      int _i = 0;
      for(int i = 0; i < getRowCount(); i++){
        Comparable key = getRowKey(i);
        keys.add(key);
        List<SortableDatasetEntry> entries = new ArrayList<>();
        for(int a = 0; a < getColumnCount(); a++){
          SortableDatasetEntry e = new SortableDatasetEntry(getValue(i, a), getColumnKey(a));
          //removeValue(key, getColumnKey(a));
          entries.add(e);
        }
        List<SortableDatasetEntry> entries2 = new ArrayList<>();
        Collections.sort(entries, comparator);
        if(comparator == DISTRIBUTION_COMPARATOR && _i == rowIndex){
          while(!entries.isEmpty()){
            entries2.add(entries2.size() / 2, entries.remove(0));
          }
          entries = entries2;
        }
        rows.put(key, entries);
        _i++;
      }
      clear();
        List<SortableDatasetEntry> sortedBy = rows.get(keys.get(rowIndex));
      for(Comparable key : keys){
        List<SortableDatasetEntry> entries = rows.get(key);
        for(int a = 0; a < entries.size(); a++){
          for(int z = 0; z < entries.size(); z++){
            if(sortedBy.get(a).key.equals(entries.get(z).key)){
              addValue(entries.get(z).value, key, entries.get(z).key);
              break;
            }
          }
        }
      }
    }
  }
  
  private static class SelectableBarRenderer extends GroupedStackedBarRenderer{
    CategoryItemEntity selected;
    CategoryItemEntity hover;
    private final Paint selectedPaint = new ChartColor(0, 0, 0);
    
    @Override
    public Paint getItemPaint(int row, int col) {
      if(selected != null){
        if(selected.getRowKey().equals(getPlot().getDataset().getRowKey(row)) && selected.getColumnKey().equals(getPlot().getDataset().getColumnKey(col))){
          return selectedPaint;
        }
      }
      if(hover != null){
        if(hover.getRowKey().equals(getPlot().getDataset().getRowKey(row)) && hover.getColumnKey().equals(getPlot().getDataset().getColumnKey(col))){
          return selectedPaint;
        }
      }
      return super.getItemPaint(row, col);
    }
  }
  
}
