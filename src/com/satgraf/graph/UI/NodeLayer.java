package com.satgraf.graph.UI;

import com.satgraf.UI.Layer;
import com.satlib.Progressive;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Node;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NodeLayer extends Layer implements Progressive{

  protected GraphViewer graph;
  private final ExecutorService pool;
  private int threadCount;
  private Graphics g;
  private int total = 0;
  private int drawn = 0;

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
    total = vertexCount;
    drawn = 0;
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

      if (!n.isVisible()) {
        continue;
      }

      drawNode(n);
      synchronized(pool){
        drawn++;
      }
    }
  }

  protected Color getColor(Node n) {
    return graph.getColor(n);
  }

  private void drawNode(Node n) {
    if (!graph.getShowAssignedVars() && n.isAssigned()) {
      return;
    }

    drawNodeWithColor(n, getColor(n), getColor(n));
  }

  private void drawNodeWithColor(Node n, Color color, Color fillColor) {
    int diameter = (int) (DrawableNode.NODE_DIAMETER * graph.getScale());
    int x = (int) (graph.getX(n) * graph.getScale()) - diameter / 2;
    int y = (int) (graph.getY(n) * graph.getScale()) - diameter / 2;
    
      if(g.getClipBounds().contains(new Point(x, y))==false){
        return;
      }

    synchronized (graph) {
      g.setColor(color);
      g.drawArc(x, y, diameter, diameter, 0, 360);
      g.setColor(fillColor);
      g.fillArc(x, y, diameter, diameter, 0, 360);
    }
  }

  @Override
  public String getProgressionName() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public double getProgress() {
    return 1 / (double) total * (double) drawn;
  }
}
