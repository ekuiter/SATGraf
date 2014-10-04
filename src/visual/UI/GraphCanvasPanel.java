/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author zacknewsham
 */
public class GraphCanvasPanel extends JPanel{
  protected GraphCanvas canvas;
  protected JScrollPane canvasPane;
  public GraphCanvasPanel(){
    
  }
  public GraphCanvasPanel(GraphCanvas canvas){
    this.canvas = canvas;
    
    
    canvasPane = new JScrollPane(canvas);
    canvasPane.setBorder(BorderFactory.createEmptyBorder());
    canvasPane.setSize(getCanvasDimensions());
    canvasPane.setPreferredSize(getCanvasDimensions());
    this.setLayout(new BorderLayout());
    this.setBackground(Color.BLACK);
    this.add(canvasPane, BorderLayout.CENTER);
  }
  public int getHorizontalScrollPosition(){
    return canvasPane.getHorizontalScrollBar().getValue();
  }
  public int getVerticalScrollPosition(){
    return canvasPane.getVerticalScrollBar().getValue();
  }
  public void repaint(){
    super.repaint();
    if(canvas != null){
      canvas.repaint();
    }
    if(canvasPane != null){
      canvasPane.repaint();
    }
  }
  
  static public Dimension getCanvasDimensions() {
	  Toolkit tk = Toolkit.getDefaultToolkit();
	  int width = ((int) tk.getScreenSize().getWidth()) - 400;
	  int height = ((int) tk.getScreenSize().getHeight()) - 400;
	  
	  return new Dimension(width, height);
  }
}
