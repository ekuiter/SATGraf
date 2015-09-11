/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.color;

import com.satlib.graph.Edge;
import java.awt.Color;

/**
 *
 * @author zacknewsham
 */
public interface EdgeColoring<E extends Edge> {
  Color getColor(E e);
}
