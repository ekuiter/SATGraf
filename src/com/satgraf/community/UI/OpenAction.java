/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.community.placer.FruchPlacer;
import com.satlib.community.CommunityGraphViewer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import com.satgraf.graph.UI.GraphFrame;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityGraphFactoryFactory;

/**
 *
 * @author zacknewsham
 */
public class OpenAction extends com.satgraf.actions.OpenAction<CommunityGraphFrame>{

  public OpenAction(CommunityGraphFrame frame) {
    super(frame);
  }

  
  
  @Override
  public void open(File file) {
    try {
      String[] parts = file.getAbsolutePath().split("\\.");
      if(parts[parts.length - 1].equals("cnf")){
        CommunityGraphFactory factory = (new CommunityGraphFactoryFactory("ol")).getFactory(file,new HashMap<String, String>());
        factory.makeGraph(file);
        this.frame.setGraphViewer(new CommunityGraphViewer(factory.getGraph(), factory.getNodeLists(), new FruchPlacer(factory.getGraph())));
        this.frame.setPatterns(new HashMap<String, Pattern>());
        this.frame.init();
        this.frame.setPanel(null);
        this.frame.show();
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
      }
    } 
    catch (IOException ex) {
      Logger.getLogger(CommunityGraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}