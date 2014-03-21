/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import visual.graph.GraphViewer;
import visual.graph.Edge;
import visual.graph.Node;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JPanel;

/**
 *
 * @author zacknewsham
 */
public class EdgeCheckBoxPanel<T extends Node, T1 extends Edge> extends JPanel{
  private final GraphViewer graph;
  private int count = 2;
  public EdgeCheckBoxPanel(GraphViewer graph, Node n, Iterator<T> nodes){
    this.graph = graph;
    count = 0;
    while(nodes.hasNext()){
      T next = nodes.next();
      Edge c = graph.getEdge(n, next);
      add(c);
      count++;
    }
    this.setLayout(new GridLayout(count, 1));
  }
  public EdgeCheckBoxPanel(GraphViewer graph, HashSet<T1> connections) {
    this.graph = graph;
    int community = 0;
    
    Iterator<T1> conns = connections.iterator();
    while(conns.hasNext()){
      add(conns.next());
    }
    this.setLayout(new GridLayout(count, 1));
  }
  public EdgeCheckBoxPanel() {
    this.graph = null;
  }
  public EdgeCheckBoxPanel(GraphViewer graph) {
    this.graph = graph;
    addAll(graph.getConnections());
    this.setLayout(new GridLayout(count, 1));
  }
  final void add(Edge conn){
    EdgeCheckBox jc = new EdgeCheckBox(conn);
      jc.setSelected(true);
      jc.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent ie) {
          EdgeCheckBox box = (EdgeCheckBox)ie.getItem();
          if(box.isSelected()){
            graph.showConnection(box.getConnection());
          }
          else{
            graph.hideEdge(box.getConnection());
          }
        }
      });
      this.add(jc);
      count ++;
  }
  final void addAll(Iterator<Edge> conns){
    while(conns.hasNext()){
      Edge conn = conns.next();
      add(conn);
    }
  }
}
