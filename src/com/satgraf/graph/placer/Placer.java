/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.placer;

import com.satlib.Progressive;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public interface Placer<T extends Node> extends Progressive{
    public abstract T getNodeAtXY(int x, int y, double scale);
	
	public abstract void init();
	public abstract int getX(T node);
	public abstract int getY(T node);
  
}
