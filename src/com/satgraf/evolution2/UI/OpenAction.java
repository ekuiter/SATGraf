/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.UI;

import com.satgraf.community.UI.CommunityGraphFrame;
import com.satlib.community.placer.CommunityPlacer;
import com.satlib.community.placer.CommunityPlacerFactory;
import com.satlib.evolution.EvolutionGraphFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author zacknewsham
 */
public class OpenAction extends com.satgraf.actions.OpenAction<Evolution2GraphFrame>{

  public OpenAction(Evolution2GraphFrame frame) {
    super(frame);
  }

  
  
  
  @Override
  public void open(final File file) {
    try {
      String[] parts = file.getAbsolutePath().split("\\.");
      if(parts[parts.length - 1].equals("cnf")){
        final EvolutionGraphFactory factory = (new Evolution2GraphFactoryFactory(frame.getCommunityName(), Evolution2GraphFrame.minisat)).getFactory(file,new HashMap<String, String>());
        OpenAction.this.frame.setFactory(factory);
        OpenAction.this.frame.setProgressive(factory);
        final SwingWorker worker1 = new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() throws Exception {
            try {
              factory.makeGraph(file);
            } catch (IOException ex) {
              Logger.getLogger(com.satgraf.community.UI.OpenAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
          }
        };


        final SwingWorker worker2 = new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() throws Exception {
            try {
              worker1.get();
            } 
            catch (InterruptedException | ExecutionException ex) {
              Logger.getLogger(com.satgraf.community.UI.OpenAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            factory.getMetric().getCommunities(factory.getGraph());
            return null;
          }
        };
        final SwingWorker worker3 = new SwingWorker<Void, Void>(){
          @Override
          protected Void doInBackground() throws Exception {
            try {
              worker1.get();
            } 
            catch (InterruptedException | ExecutionException ex) {
              Logger.getLogger(com.satgraf.community.UI.OpenAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            CommunityPlacer placer = CommunityPlacerFactory.getInstance().getByName(frame.getPlacerName(),factory.getGraph());
            OpenAction.this.frame.setPatterns(new HashMap<String, Pattern>());
            OpenAction.this.frame.setProgressive(placer);
            try {
              worker2.get();
            } 
            catch (InterruptedException | ExecutionException ex) {
              Logger.getLogger(OpenAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            OpenAction.this.frame.setGraphViewer(new Evolution2GraphViewer(factory.getGraph(), factory.getNodeLists(), placer));

            return null;
          }
        };
        final SwingWorker worker4 = new SwingWorker<Void, Void>(){
          @Override
          protected Void doInBackground() throws Exception {
            try {
              worker3.get();
            } 
            catch (InterruptedException | ExecutionException ex) {
              Logger.getLogger(OpenAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            OpenAction.this.frame.init();
            OpenAction.this.frame.setPanel(null);
            OpenAction.this.frame.show();
            return null;
          }
        };
        
        worker1.execute();
        worker2.execute();
        worker3.execute();
        worker4.execute();
      }
      else{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder contents = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
          contents.append(line).append("\n");
        }
        JSONObject json = (JSONObject)JSONValue.parse(contents.toString());
        this.frame.fromJson(json);
        reader.close();
      }
    } 
    catch (IOException ex) {
      Logger.getLogger(CommunityGraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}
