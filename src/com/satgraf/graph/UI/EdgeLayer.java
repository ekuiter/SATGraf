package com.satgraf.graph.UI;

import com.satgraf.UI.Layer;
import com.satlib.Progressive;
import com.satlib.graph.Edge;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EdgeLayer extends Layer implements Progressive{
	
  protected final GraphViewer graph;
  private final ExecutorCompletionService pool;
  private int threadCount;
  private Graphics g;
  private int drawn = 0;
  private int total = 0;

  public EdgeLayer(Dimension size, GraphViewer graph) {
    super(size);
    this.graph = graph;
    threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    pool = new ExecutorCompletionService(Executors.newFixedThreadPool(threadCount));
  }

  protected Color getColor(Edge e) {
    return graph.getColor(e);
  }

  public synchronized void paintComponent(final Graphics g) {
    synchronized(g){
      this.g = g;
      int taskCount = threadCount;
      int edgeCount = graph.getGraph().getEdges().size();
      Graphics2D g2d = (Graphics2D) g.create();
      drawn = 0;
      total = edgeCount;
      ArrayList<Future> threads = new ArrayList();
      for (int t = taskCount; t > 0; t--) {
          final int from = (int) Math.floor(edgeCount * (t - 1) / taskCount);
          final int to = (int) Math.floor(edgeCount * t / taskCount);

          Future future = pool.submit(new Callable<Object>() {
              @Override
              public Object call() {
                  drawEdges(from, to);
                  return null;
              }
          });
          threads.add(future);
      }
      for (Future future : threads) {
        try {
          pool.take();
        } catch (InterruptedException ex) {
          Logger.getLogger(EdgeLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      graph.clearUpdatedEdges();
    }
  }

  double oldScale;
  private void drawEdges(int from, int to) {
    ArrayList<Edge> edges = new ArrayList<Edge>(graph.getGraph().getEdges());
    scale = graph.getScale();
    
    if(scale != oldScale){
      startPoints.clear();
      endPoints.clear();
    }
    oldScale = scale;
    for (int i = from; i < to; i++) {
      Edge e = edges.get(i);

      if (graph.hideAllEdges() || !e.getStart().isVisible() || !e.getEnd().isVisible()){
        continue;
      }

      drawConnection(e);
      synchronized(pool){
        drawn++;
      }
    }
  }

  private static final BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  private static final RenderingHints hints = GraphCanvas.getRenderingHints();
  private double scale = 1.0;
  final Map<Edge, Point2D> startPoints = new HashMap<>();
  final Map<Edge, Point2D> endPoints = new HashMap<>();
  protected void drawConnection(Edge c) {
    if (!graph.shouldShowEdge(c)){
      return;
    }
    if(c.getStart().isVisible() && c.getEnd().isVisible()){
      int startX, startY, endX, endY;
      if(startPoints.containsKey(c)){
        startX = (int)startPoints.get(c).getX();
        startY = (int)startPoints.get(c).getY();
        endX = (int)endPoints.get(c).getX();
        endY = (int)endPoints.get(c).getY();
      }
      else{
        startX = (int) (graph.getX(c.getStart()) * scale);
        startY = (int) (graph.getY(c.getStart()) * scale);
        endX = (int) (graph.getX(c.getEnd()) * scale);
        endY = (int) (graph.getY(c.getEnd()) * scale);
        synchronized(startPoints){
          startPoints.put(c, new Point(startX, startY));
        }
        synchronized(endPoints){
          endPoints.put(c, new Point(endX, endY));
        }
      }
      Rectangle r = new Rectangle(Math.min(startX, endX), Math.min(startY, endY), Math.abs(startX-endX), Math.abs(startY-endY));
      if(!r.intersects(g.getClipBounds()) && !g.getClipBounds().intersects(r)){
        return;
      }
      
      synchronized (graph) {
        g.setColor(graph.getColor(c));
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHints(hints);
        g2d.setStroke(stroke);
        g2d.drawLine(startX, startY, endX, endY);
      }
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
