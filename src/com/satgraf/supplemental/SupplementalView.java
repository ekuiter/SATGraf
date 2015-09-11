/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.supplemental;

import com.satgraf.graph.UI.GraphViewer;
import com.satlib.community.CommunityMetric;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public interface SupplementalView<N extends Node, E extends Edge, G extends Graph, V extends GraphViewer> {
  void init();
  void setGraphViewer(V v);
  void setCommunityMetric(CommunityMetric metric);
  String getName();
}
