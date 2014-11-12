/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.UI;

import com.satlib.Progressive;
import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 *
 * @author zacknewsham
 */
public class ProgressionViewer extends JPanel{
  private Progressive item;
  private JLabel label;
  private JProgressBar bar;
  private Timer timer;
  public ProgressionViewer(){
    this.setLayout(new BorderLayout());
    label = new JLabel();
    this.add(label, BorderLayout.WEST);
    bar = new JProgressBar();
    bar.setMaximum(100);
    this.add(bar, BorderLayout.CENTER);
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        final int value = ProgressionViewer.this.item == null ? 0 : (int)(100 * ProgressionViewer.this.item.getProgress());
        SwingUtilities.invokeLater ( new Runnable (){
            public void run (){
              bar.setValue(value);
            }
        });
      }
    }, 0, 500);
  }
  public void setProgressive(Progressive item){
    this.item = item;
    label.setText(item.getProgressionName());
  }
}
