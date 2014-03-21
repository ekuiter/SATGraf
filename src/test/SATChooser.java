/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author zacknewsham
 */
public class SATChooser {
  public static HashMap<String, Double> choose(int timeLimit, int choose, String replace, String csv) throws FileNotFoundException, IOException{
   HashMap<String, Double> available = new HashMap<String, Double>();
    BufferedReader reader = new BufferedReader(new FileReader(csv));
    String line;
    while((line = reader.readLine()) != null){
      String[] lineCols = line.split(",");
      try{
        if(lineCols.length < 2){
          continue;
        }
        double time = Double.parseDouble(lineCols[2]);
        if(time < timeLimit || timeLimit == -1){
          try{
            File f = new File(lineCols[0].replace("SATBench", replace));
            if(f.exists() && f.length() < 20 * 1024 * 1024){
              available.put(f.getAbsolutePath(), time);
            }
          }
          catch(Exception e){
            
          }
        }
      }
      catch(NumberFormatException e){
        
      }
    }
    HashMap<String, Double> chosen = new HashMap<String, Double>();
    List<String> l = new ArrayList<String>(available.keySet());
    Collections.shuffle(l);
    for(int i = 0; i < Math.min(choose, available.size()); i++){
      String s = l.remove(0);
      chosen.put(s, available.get(s));
    }
    return chosen;
  }
  
  public static void main(String[] args) throws FileNotFoundException, IOException{
    if(args.length == 0){
      args = new String[]{
        "900",
        "15",
        "application",
        "/Users/zacknewsham/Downloads/Core_solvers_Sequential_Application_SAT_results.csv"
      };
    }
    HashMap<String, Double> files = choose(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
    Iterator<String> filesI = files.keySet().iterator();
    while(filesI.hasNext()){
      String s = filesI.next();
      System.out.println(s);
    }
  }
}
