/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityGraphFactoryFactory;
import com.satlib.community.CommunityGraphViewer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author zacknewsham
 */
public class CommunityApplet extends JApplet{
  public void init() {
      
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  try {
                    HashMap<String, String> patterns = new HashMap<String, String>();
                    final int formula_id = Integer.valueOf(getParameter("formula_id"));
                    URL file = new URL(String.format("http://satbench.uwaterloo.ca/formula/%d.cnf", formula_id));
                    URLConnection con = file.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    CommunityGraphFactory factory = (new CommunityGraphFactoryFactory("ol")).getFactory(file, patterns);
                    factory.makeGraph(in);

                    CommunityGraphViewer graphViewer = new CommunityGraphViewer(factory.getGraph(), factory.getNodeLists(), CommunityGraphFrame.getPlacer("f", factory.getGraph()));
                    CommunityGraphFrame frmMain = new CommunityGraphFrame(graphViewer, factory.getPatterns(), factory.getMetric());
                    frmMain.init();
                    //GraphCanvasPanel canvasPanel = new GraphCanvasPanel(new CommunityCanvas(graphViewer));

                    frmMain.show();
                    //add(frmMain.getRootPane());
                  } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                  } catch (IOException ex) {
                    ex.printStackTrace();
                  }
                }
            });
        } catch (Exception e) {
          e.printStackTrace();
            System.err.println("createGUI didn't complete successfully");
        }
    }
}
