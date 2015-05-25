package com.satgraf.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPanel;

public abstract class Layer extends JPanel {

  protected ExecutorService pool;
  protected int threadCount;
  protected int currentThreadCount;

  public Layer(Dimension size) {
    setLayout(new BorderLayout());
    setOpaque(false);
    setSize(size);
    setPreferredSize(size);
    setDoubleBuffered(true);
    threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    pool = Executors.newFixedThreadPool(threadCount);
    currentThreadCount = threadCount;
  }
}
