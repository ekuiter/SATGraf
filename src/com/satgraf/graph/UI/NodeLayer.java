package com.satgraf.graph.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.satgraf.UI.Layer;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

public class NodeLayer extends Layer {
	
	protected GraphViewer graph;
	private ExecutorService pool;
	private int threadCount;
	private Graphics g;
	
	public NodeLayer(Dimension size, GraphViewer graph) {
		super(size);
		this.graph = graph;
		threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		pool = Executors.newFixedThreadPool(threadCount);
	}
	
	public void paintComponent(Graphics g) {
		this.g = g;
		int taskCount = 8 * threadCount;
		int vertexCount = graph.getGraph().getNodes().size();
		
		ArrayList<Future> threads = new ArrayList();
        for (int t = taskCount; t > 0; t--) {
            final int from = (int) Math.floor(vertexCount * (t - 1) / taskCount);
            final int to = (int) Math.floor(vertexCount * t / taskCount);
            
            Future future = pool.submit(new Runnable() {
				@Override
				public void run() {
					drawNodes(from, to);
				}
			});
            threads.add(future);
        }
        
        for (Future future : threads) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        }
		  
		graph.clearUpdatedNodes();
	}
	
	private void drawNodes(int from, int to) {
		ArrayList<Node> nodes = new ArrayList<Node>(graph.getGraph().getNodes());
		
		for (int i = from; i < to; i++) {
			  Node n = nodes.get(i);
			  
			  if(!n.isVisible()){
		        continue;
		      }
			  
			  drawNode(n);
		}
	}
	
	protected Color getColor(Node n) {
		return n.getColor(graph);
	}
	  
	private void drawNode(Node n) {	  
		if (!graph.getShowAssignedVars() && n.isAssigned())
			return;
			
		drawNodeWithColor(n, getColor(n), getColor(n));
	}
		  
	private void drawNodeWithColor(Node n, Color color, Color fillColor) {
		int diameter = (int) (DrawableNode.NODE_DIAMETER * graph.getScale());
		int x = (int) (n.getX(graph) * graph.getScale()) - diameter/2;
		int y = (int) (n.getY(graph) * graph.getScale()) - diameter/2;
			 
		synchronized (graph) {
			g.setColor(color);
			g.drawArc(x, y, diameter, diameter, 0, 360);
			g.setColor(fillColor);
			g.fillArc(x, y, diameter, diameter, 0, 360);
		}
	}
}
