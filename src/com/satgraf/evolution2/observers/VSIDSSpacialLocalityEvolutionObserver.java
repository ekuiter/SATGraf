/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

import com.satlib.community.Community;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.EvolutionGraph;
import com.satlib.graph.Clause;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author zacknewsham
 */
public class VSIDSSpacialLocalityEvolutionObserver  extends JPanel implements EvolutionObserver, ChartMouseListener{
  private final EvolutionGraph graph;
  private SortableCategoryDataset dataset = new SortableCategoryDataset();
  private JFreeChart objChart = ChartFactory.createBarChart("Communities used", "Community ID", "# Decision", dataset);
  private final ChartPanel chartPanel = new ChartPanel(objChart);
  private final JScrollPane chartScroll = new JScrollPane(chartPanel);
  private final String SERIES_1 = "Decisions";
  private final SelectableBarRenderer renderer = new SelectableBarRenderer();
  private final JCheckBox chkDistribution = new JCheckBox();
  private Comparator currentComparator = COMMUNITY_COMPARATOR;
  static{
    EvolutionObserverFactory.getInstance().register("VSIDSS", VSIDSSpacialLocalityEvolutionObserver.class);
  }
  
  public VSIDSSpacialLocalityEvolutionObserver(EvolutionGraph graph){
    this.graph = graph;
    init();
    
    chkDistribution.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ae) {
        if(chkDistribution.isSelected()){
          currentComparator = DISTRIBUTION_COMPARATOR;
        }
        else {
          currentComparator = COMMUNITY_COMPARATOR;
        }
        synchronized(dataset){
          dataset.sort(currentComparator);
        }
      }
    });
  }
  
  public final void init(){
    for(Community c : graph.getCommunities()){
      dataset.addValue(0, SERIES_1, String.valueOf(c.getId()));
    }
    chartPanel.addChartMouseListener(this); 
    objChart.getCategoryPlot().setRenderer(renderer);
    objChart.getCategoryPlot().getDomainAxis().setTickMarksVisible(false);
    objChart.getCategoryPlot().getDomainAxis().setMinorTickMarksVisible(false);
    objChart.getCategoryPlot().getDomainAxis().setVisible(false);
    renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("Community {1} = {2}", NumberFormat.getInstance()));
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
    c.weighty = 0.95;
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
    c.weighty = 0.05;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 3;
    this.add(new JLabel("Distribution"), c);
    
    c.gridx = 3;
    c.gridy = 1;
    c.weighty = 0.05;
    c.weightx = 0.5;
    c.gridheight = 1;
    c.gridwidth = 1;
    this.add(chkDistribution, c);
    
  }

  @Override
  public void clauseAdded(Clause c) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void nodeAssigned(CommunityNode n, boolean isDecision) {
    if(isDecision){
      synchronized(dataset){
        dataset.setValue(dataset.getValue(SERIES_1, String.valueOf(n.getCommunity())).intValue() + 1, SERIES_1, String.valueOf(n.getCommunity()));
        if(currentComparator == DISTRIBUTION_COMPARATOR)
        dataset.sort(currentComparator);
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
      }
      else{
        renderer.selected = null;
      }
  }

  @Override
  public void chartMouseMoved(ChartMouseEvent cme) {
      Plot p = cme.getChart().getPlot();
      if(cme.getEntity() instanceof CategoryItemEntity){
        renderer.hover = (CategoryItemEntity)cme.getEntity();
      }
      else {
        renderer.hover = null;
      }
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
      return t1.key.compareTo(t.key);
    }
  };
    
  
  private final static Comparator<SortableDatasetEntry> DISTRIBUTION_COMPARATOR = new Comparator<SortableDatasetEntry>(){
    @Override
    public int compare(SortableDatasetEntry t, SortableDatasetEntry t1) {
      return t.value.intValue() - t1.value.intValue();
    }
  };
  
  private static class SortableCategoryDataset extends DefaultCategoryDataset{
    public SortableCategoryDataset(){
      
    }
    
    public void sort(Comparator<SortableDatasetEntry> comparator){
      for(int i = 0; i < getRowCount(); i++){
        Comparable key = getRowKey(i);
        List<SortableDatasetEntry> entries = new ArrayList<>();
        for(int a = 0; a < getColumnCount(); a++){
          SortableDatasetEntry e = new SortableDatasetEntry(getValue(i, a), getColumnKey(a));
          removeValue(getRowKey(i), getColumnKey(a));
          a--;
          entries.add(e);
        }
        List<SortableDatasetEntry> entries2 = new ArrayList<>();
        Collections.sort(entries, comparator);
        if(comparator == DISTRIBUTION_COMPARATOR){
          while(!entries.isEmpty()){
            entries2.add(entries2.size() / 2, entries.remove(0));
          }
          entries = entries2;
        }
        for(int a = 0; a < entries.size(); a++){
          addValue(entries.get(a).value, key, entries.get(a).key);
        }
      }
    }
  }
  
  private static class SelectableBarRenderer extends BarRenderer{
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
      else if(hover != null){
        if(hover.getRowKey().equals(getPlot().getDataset().getRowKey(row)) && hover.getColumnKey().equals(getPlot().getDataset().getColumnKey(col))){
          return selectedPaint;
        }
      }
      return super.getItemPaint(row, col);
    }
  }
  
}
