/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satlib.community.CommunityGraphFactory;
import com.satgraf.community.placer.CommunityPlacer;
import com.satgraf.community.placer.CommunityPlacerFactory;
import com.satlib.graph.GraphFactory;
import com.satlib.graph.GraphFactoryFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;

/**
 *
 * @author zacknewsham
 */
public class CommunityApplet extends JApplet{
  public void init() {
      
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            final HashMap<String, String> patterns = new HashMap<String, String>();
            
            
            final int formula_id;
            if(getParameter("formula_id") != null){
              formula_id = Integer.valueOf(getParameter("formula_id"));
            }
            else{
              formula_id = 3256;
            }
            final URL file;
            URL temp  = new URL(String.format("http://satbench.uwaterloo.ca/json/%d.sb", formula_id));
            String extension = "sb";
            HttpURLConnection huc =  (HttpURLConnection)  temp.openConnection(); 
            if(huc.getResponseCode() == 200){
              file = temp;
            }
            else{
              file = new URL(String.format("http://satbench.uwaterloo.ca/formula/%d.cnf", formula_id));
              extension = "cnf";
            }
            
            GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension("auto", extension, "ol", null);
            if(tmp == null){
              throw new InstantiationException("auto is not available for format " + extension);
            }
            else if(!(tmp instanceof CommunityGraphFactory)){
              InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
              throw e;
            }
            final CommunityGraphFactory factory = (CommunityGraphFactory)tmp;

            final CommunityGraphViewer graphViewer = new CommunityGraphViewer(null, factory.getNodeLists(), null);
            final CommunityGraphFrame frmMain = new CommunityGraphFrame(graphViewer, factory.getPatterns(), factory.getMetric());
            frmMain.setProgressive(factory);
            frmMain.preinit();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  frmMain.setVisible(true);
                }
            });
            factory.makeGraph(file);
            CommunityPlacer p = null;
            if(factory.getGraph() instanceof CommunityPlacer){
              p = (CommunityPlacer)factory.getGraph();
            }
            else{
              p = CommunityPlacerFactory.getInstance().getByName("f", factory.getGraph());
            }
            frmMain.setProgressive(p);
            graphViewer.graph = factory.getGraph();
            graphViewer.setPlacer(p);
            
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  frmMain.init();
                  frmMain.show(); 
                }
            });
        } catch (Exception e) {
          e.printStackTrace();
            System.err.println("createGUI didn't complete successfully");
        }
    }
}
