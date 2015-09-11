/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.color;

import com.satlib.graph.Node;
import java.awt.Color;

/**
 *
 * @author zacknewsham
 */
public interface NodeColoring <N extends Node>{
  Color getOutlineColor(N node);
  Color getFillColor(N node);
}
