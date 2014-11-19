/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.satlib.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class GraphScaler extends JPanel implements ChangeListener{
  private final JSlider scale = new JSlider(1, 100);
  private GraphViewer graph;
  private final JButton fit = new JButton("Fit");
  public void setScale(int pos){
    scale.setValue(pos);
  }
  public int getScale(){
    return scale.getValue();
  }
  
  public GraphScaler(final GraphFrame frame, GraphViewer graph){
    this.graph = graph;
    scale.setValue((int)(graph.getScale() * 100));
    scale.setPreferredSize(new Dimension(225, 20));
    fit.setPreferredSize(new Dimension(50, 40));
    
    this.setLayout(new BorderLayout());
    this.add(scale, BorderLayout.CENTER);
    this.add(fit, BorderLayout.EAST);
    scale.addChangeListener(this);
    
    fit.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Rectangle gR = GraphScaler.this.graph.getBounds();
        Rectangle vR = frame.getGraphCanvas().getVisibleRect();
        
        double widthRatio = (double)vR.width / (double)gR.width;
        double heightRatio = (double)vR.height / (double)gR.height;
        
        double ratioCap = widthRatio < heightRatio ? widthRatio : heightRatio;
        scale.setValue((int) (ratioCap * 100));
      }
    });
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    if(scale.getValueIsAdjusting() == false){
      graph.setScale((double) 1 / ((double)100 / (double)scale.getValue()));
    }
  }
  void setGraph(GraphViewer graph){
    this.graph = graph;
    scale.removeChangeListener(this);
    scale.setValue((int)(graph.getScale() * 100));
    scale.addChangeListener(this);
    scale.revalidate();
    scale.repaint();
  }
}
