/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.graph;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import visual.UI.GraphCanvasPanel;
import visual.UI.GraphOptionsPanel;

/**
 *
 * @author zacknewsham
 */
public abstract class Grapher <T extends Node, T1 extends Edge, T2 extends Clause>{
  protected File dimacsFile;
  private Runtime runtime = Runtime.getRuntime();
  private static final int MB = 1024*1024;
  protected final File mapFile;
  protected Graph<T, T1, T2> graph;
  protected GraphViewer graphViewer;
  private static String s_all = "All";
  private static String s_named = "Named";
  protected HashMap<String, TIntObjectHashMap<String>> node_lists = new HashMap<>();
  protected final HashMap<String, Pattern> patterns = new HashMap<String, Pattern>();
  protected final TIntObjectHashMap<String> all_names = new TIntObjectHashMap<String>();
  protected final TIntObjectHashMap<String> all = new TIntObjectHashMap<String>();
  protected JFrame frmMain;
  protected JMenu menu = new JMenu("File");
  protected JMenuBar menuBar = new JMenuBar();
  
  private JMenuItem open = new JMenuItem("Open");
  private JMenuItem save = new JMenuItem("Save");
  private JMenuItem export = new JMenuItem("Export");
  
  protected final JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  protected GraphCanvasPanel canvasPanel;
  protected GraphOptionsPanel panel;
  static{
    s_all = s_all.intern();
    s_named = s_named.intern();
  }
  public JFrame getFrame(){
    return frmMain;
  }
  public Grapher(String dimacsFile, String mapFile, HashMap<String, String> patterns){
    initPatterns(patterns);
    this.patterns.put(s_named, Pattern.compile(".*"));
    this.node_lists.put(s_named, all_names);
    this.dimacsFile = new File(dimacsFile);
    this.mapFile = new File(mapFile);
  }
  public Grapher(HashMap<String, String> patterns){
    initPatterns(patterns);
    this.dimacsFile = null;
    this.mapFile = null;
  }
  
  public Grapher(String dimacsFile, HashMap<String, String> patterns){
    initPatterns(patterns);
    this.patterns.put(s_named, Pattern.compile(".*"));
    this.node_lists.put(s_named, all_names);
    this.dimacsFile = new File(dimacsFile);
    this.mapFile = null;
    //this.mapFile = new File(mapFile);
  }
  public Grapher(String dimacsFile, String mapFile, String from_name, String to_name){
    patterns.put("From", Pattern.compile(from_name));
    this.node_lists.put("From", new TIntObjectHashMap<String>());
    patterns.put("To", Pattern.compile(to_name));
    this.node_lists.put("To", new TIntObjectHashMap<String>());
    this.patterns.put(s_named, Pattern.compile(".*"));
    this.node_lists.put(s_named, all_names);
    this.node_lists.put(s_all, all);
    this.dimacsFile = new File(dimacsFile);
    this.mapFile = new File(mapFile);
  }
  
  protected void initFrame(){
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.setSize(1000, 700);
    frmMain.setContentPane(mainPane);
    
    menu.add("File");
    menu.add(open);
    menu.add(save);
    menu.add(export);
    menuBar.add(menu);
    frmMain.setJMenuBar(menuBar);
  }
  
  private void initPatterns(HashMap<String, String> patterns){
    Iterator<String> ps = patterns.keySet().iterator();
    while(ps.hasNext()){
      String next = ps.next();
      this.patterns.put(next, Pattern.compile(patterns.get(next)));
      this.node_lists.put(next, new TIntObjectHashMap<String>());
    }
    this.node_lists.put("All", all);
  }
  protected void show(){
    if(frmMain == null){
      frmMain = new JFrame();
      initFrame();
    }
    mainPane.setLeftComponent(canvasPanel);
    mainPane.setRightComponent(panel);
    frmMain.setVisible(true);
  }
  protected void setLeftComponent(Component c){
    mainPane.setLeftComponent(c);
  }
  protected void setRightComponent(Component c){
    mainPane.setRightComponent(c);
  }
  protected void generateGraph() throws FileNotFoundException, IOException {
    BufferedReader reader;
    String line;
    if(mapFile != null){
      reader = new BufferedReader(new FileReader(mapFile));
      while((line = reader.readLine()) != null){
        String[] details = line.split("=");
        if(details.length != 2){
          continue;
        }
        Iterator<String> ps = patterns.keySet().iterator();
        while(ps.hasNext()){
          String next = ps.next();
          if(patterns.get(next).matcher(details[1]).matches()){
            int id = Integer.parseInt(details[0]);
            synchronized(all_names){
              node_lists.get(next).put(id, details[1]);
            }
            Node n = graph.createNode(id, details[1]);
            if(n != null){
              n.addGroup(next);
              n.setName(details[1]);
            }
          }
        }
      }
    }
    reader = new BufferedReader(new FileReader(dimacsFile));
    int linecount = 0;
    while((line = reader.readLine()) != null){
      linecount++;
      if(line.length() == 0){
        continue;
      }
      if(line.charAt(0) == '$'){
        continue;
      }
      if(line.charAt(0) == 'c'){
        String[] vars = line.split(" ");
        if(vars.length == 3){
          int id = 0;
          try{
            id = Integer.parseInt(vars[1]);
          }
          catch(NumberFormatException e){
            try{
              id = Integer.parseInt(vars[1].replace("$", ""));
            }
            catch(NumberFormatException e1){
              
            }
          }
          if(id != 0){
            
            Iterator<String> ps = patterns.keySet().iterator();
            while(ps.hasNext()){
              String next = ps.next();
              if(patterns.get(next).matcher(vars[2]).matches()){
                synchronized(all_names){
                  node_lists.get(next).put(id, vars[2]);
                }
                Node n = graph.createNode(id, vars[2]);
                if(n != null){
                  n.addGroup(next);
                  n.setName(vars[2]);
                }
              }
            }
          }
        }
        continue;
      }
      if(line.charAt(0) == 'p'){
        continue;
      }
      String[] vars = line.split(" ");
      TObjectCharHashMap<T> nodes = new TObjectCharHashMap<>();
      for(int i = 0; i < vars.length; i++){
    	if (vars[i].compareTo("") == 0){
            continue;
    	}
        int lit1 = Integer.parseInt(vars[i]);
        boolean lit_1value = true;
        if(lit1 == 0){
          continue;
        }
        T n = null;
        if(lit1 < 0){
          lit1 = 0 - lit1;
          lit_1value = false;
        }
        synchronized(all_names){
          if(all_names.containsKey(lit1)){
            n = graph.createNode(lit1, (String)all_names.get(lit1));
            n.addGroup(s_named);
          }
          else{
            n = graph.createNode(lit1, null);
          }
        }
        n.addGroup(s_all);
        nodes.put(n, lit_1value ? '1' : '0');
        for(int a = i + 1; a < vars.length; a++){
          if(a == i || vars[a].compareTo("") == 0){
            continue;
          }
          int lit2 = Integer.parseInt(vars[a]);
          boolean lit_2value = true;
          if(lit2 == 0){
            continue;
          }
          if(lit2 < 0){
            lit2 = 0 - lit2;
            lit_2value = false;
          }
          if(lit1 == lit2){
            continue;
          }
          T n1 = null;
          if(all_names.containsKey(lit2)){
            n1 = graph.createNode(lit2, (String)all_names.get(lit2));
            n1.addGroup(s_named);
          }
          else{
            n1 = graph.createNode(lit2, null);
          }
          
          nodes.put(n1, lit_2value ? '1' : '0');
          n1.addGroup(s_all);
          if(n != n1){
            graph.connect(n, n1, false);
          }
        }
      }
      if(graph.getClausesCount() % 100 == 0){
        System.err.printf("%d\n", graph.getClausesCount());
        if(runtime.freeMemory() / MB < 1){
          return;
        }
      }
      graph.createClause(nodes);
    }
  }
}
