/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

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
import visual.graph.GraphViewer;

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
  
  public GraphScaler(GraphViewer graph){
    this.graph = graph;
    scale.setValue((int)(graph.getScale() * 100));
    scale.setSize(new Dimension(100, 20));
    scale.setPreferredSize(new Dimension(100, 20));
    
    this.setLayout(new BorderLayout());
    this.add(scale, BorderLayout.CENTER);
    this.add(fit, BorderLayout.EAST);
    scale.addChangeListener(this);
    fit.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Rectangle gR = GraphScaler.this.graph.getBounds();
        Rectangle vR = GraphScaler.this.graph.getGraphCanvas().getVisibleRect();
        if(vR.width < vR.height){
          scale.setValue((int)(100.0 / (double)gR.width * (double)vR.width));
        }
        else{
          scale.setValue((int)(100.0 / (double)gR.height * (double)vR.height));
        }
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
