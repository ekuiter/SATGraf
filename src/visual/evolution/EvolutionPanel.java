/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import visual.UI.GraphCanvasPanel;
import visual.community.CommunityCanvas;
import visual.community.CommunityGraphViewer;

/**
 *
 * @author zacknewsham
 */
public class EvolutionPanel extends GraphCanvasPanel{
  private ArrayList<CommunityGraphViewer> graphs = new ArrayList<>();
  JTabbedPane pane = new JTabbedPane();
  private int dump_count = 0;
  private int selected_graph = 0;
  private EvolutionOptionsPanel options;
  public EvolutionPanel(CommunityGraphViewer original, EvolutionOptionsPanel options){
    this.setLayout(new BorderLayout());
    this.add(pane, BorderLayout.CENTER);
    this.options = options;
    graphs.add(original);
    GraphCanvasPanel panel = new GraphCanvasPanel(new CommunityCanvas(original));
    pane.add(String.format("Original"), panel);
    pane.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            if(selected_graph != pane.getSelectedIndex()){
              selected_graph = pane.getSelectedIndex();
              
              EvolutionPanel.this.options.setGraph(graphs.get(selected_graph));
            }
        }
    });
    Iterator<CommunityGraphViewer> gs = this.graphs.iterator();
    int i = 0;
    while(gs.hasNext()){
      CommunityGraphViewer g = gs.next();
      i++;
    }
  }
  
  public int getHorizontalScrollPosition(){
    return 0;
  }
  public int getVerticalScrollPosition(){
    return 0;
  }
  public CommunityGraphViewer getGraph(int index){
    return graphs.get(index);
  }
  
  void addGraph(CommunityGraphViewer graph){
    graphs.add(graph);
    GraphCanvasPanel panel = new GraphCanvasPanel(new CommunityCanvas(graph));
    pane.add(String.format("Dump %d", dump_count), panel);
    dump_count++;
    
  }
}
