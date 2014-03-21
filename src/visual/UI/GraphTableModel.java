/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.Rectangle;
import javax.swing.table.AbstractTableModel;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class GraphTableModel extends AbstractTableModel{
  private GraphViewer graph;
  private int cols = 0;
  private int rows = 0;
  public GraphTableModel(GraphViewer graph){
    this.graph = graph;
  }
  @Override
  public int getRowCount() {
    if(rows != 0){
      return rows;
    }
    Rectangle r = graph.getBounds();
    while(r.height > 0){
      r.height -= GraphCanvasRenderer.FRAME_HEIGHT;
      rows++;
    }
    return rows;
  }

  @Override
  public int getColumnCount() {
    if(cols != 0){
      return cols;
    }
    Rectangle r = graph.getBounds();
    while(r.width > 0){
      r.width -= GraphCanvasRenderer.FRAME_WIDTH;
      cols++;
    }
    return cols;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return graph;
  }
  public void reset(){
    cols = 0;
    rows = 0;
  }
}
