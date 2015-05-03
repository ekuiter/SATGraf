package com.satgraf.graph.UI;

import com.satgraf.UI.Layer;
import com.satlib.graph.Edge;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EdgeLayer extends Layer {
	
  protected final GraphViewer graph;
  private ExecutorService pool;
  private int threadCount;
  private Graphics g;

  public EdgeLayer(Dimension size, GraphViewer graph) {
    super(size);
    this.graph = graph;
    threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    pool = Executors.newFixedThreadPool(threadCount);
  }

  protected Color getColor(Edge e) {
    return Color.WHITE;
  }

  public synchronized void paintComponent(final Graphics g) {
    this.g = g;
    int taskCount = threadCount;
    int edgeCount = graph.getGraph().getEdgesList().size();
    Graphics2D g2d = (Graphics2D) g.create();

    ArrayList<Future> threads = new ArrayList();
    for (int t = taskCount; t > 0; t--) {
        final int from = (int) Math.floor(edgeCount * (t - 1) / taskCount);
        final int to = (int) Math.floor(edgeCount * t / taskCount);

        Future future = pool.submit(new Runnable() {
            @Override
            public void run() {
                drawEdges(from, to);
            }
        });
        threads.add(future);
    }

    for (Future future : threads) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException ex) {
        ex.printStackTrace();
      }
    }
    graph.clearUpdatedEdges();
  }

  private void drawEdges(int from, int to) {
    ArrayList<Edge> edges = new ArrayList<Edge>(graph.getGraph().getEdgesList());
    scale = graph.getScale();
    for (int i = from; i < to; i++) {
      Edge e = edges.get(i);

      if (!e.getStart().isVisible() || !e.getEnd().isVisible()){
        continue;
      }

      drawConnection(e);
    }
  }

  private static final BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  private static final RenderingHints hints = GraphCanvas.getRenderingHints();
  private double scale = 1.0;
  protected void drawConnection(Edge c) {
    if (!graph.shouldShowEdge(c)){
      return;
    }

    if(c.getStart().isVisible() && c.getEnd().isVisible()){
      int startX = (int) (graph.getX(c.getStart()) * scale);
      int startY = (int) (graph.getY(c.getStart()) * scale);
      int endX = (int) (graph.getX(c.getEnd()) * scale);
      int endY = (int) (graph.getY(c.getEnd()) * scale);
      /*Rectangle r = new Rectangle(startX, startY, endX, endY);
      if(r.intersects(this.getVisibleRect())==false){
        return;
      }*/
      
      synchronized (graph) {
        g.setColor(getColor(c));
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHints(hints);
        g2d.setStroke(stroke);
        g2d.drawLine(startX, startY, endX, endY);
      }
    }
  }
}
