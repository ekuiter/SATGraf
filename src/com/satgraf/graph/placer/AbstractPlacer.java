package com.satgraf.graph.placer;

import com.satlib.graph.Graph;
import com.satlib.graph.Node;

public abstract class AbstractPlacer<T extends Node, T1 extends Graph> implements Placer<T>{
	
	protected T1 graph;
	
	public AbstractPlacer(T1 graph){
	    this.graph = graph;
    }
}
