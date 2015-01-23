package com.satgraf.graph.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.satgraf.UI.Layer;
import com.satlib.graph.Edge;
import com.satlib.graph.GraphViewer;

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

  public void paintComponent(final Graphics g) {
    this.g = g;
    int taskCount = 8 * threadCount;
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
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      } catch (ExecutionException ex) {
        ex.printStackTrace();
      }
    }
    graph.clearUpdatedEdges();
  }

  private void drawEdges(int from, int to) {
    ArrayList<Edge> edges = new ArrayList<Edge>(graph.getGraph().getEdgesList());

    for (int i = from; i < to; i++) {
      Edge e = edges.get(i);

      if (!e.getStart().isVisible() || !e.getEnd().isVisible()){
        continue;
      }

      drawConnection(e);
    }
  }

  protected void drawConnection(Edge c) {
    if (!graph.shouldShowEdge(c)){
      return;
    }

    if(c.getStart().isVisible() && c.getEnd().isVisible()){
      int startX = (int) (c.getStart().getX(graph) * graph.getScale());
      int startY = (int) (c.getStart().getY(graph) * graph.getScale());
      int endX = (int) (c.getEnd().getX(graph) * graph.getScale());
      int endY = (int) (c.getEnd().getY(graph) * graph.getScale());

      synchronized (graph) {
        g.setColor(getColor(c));
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHints(GraphCanvas.getRenderingHints());
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(startX, startY, endX, endY);
      }
    }
  }
}
