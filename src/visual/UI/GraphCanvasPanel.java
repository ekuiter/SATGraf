/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
    canvasPane.setSize(400, 400);
    canvasPane.setPreferredSize(new Dimension(400,400));
    this.setLayout(new BorderLayout());
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
}
