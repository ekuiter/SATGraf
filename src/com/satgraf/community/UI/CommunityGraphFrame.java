/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import static com.satgraf.ForceInit.forceInit;
import com.satgraf.community.placer.FruchGPUPlacer;
import com.satgraf.community.placer.FruchPlacer;
import com.satgraf.community.placer.GridPlacer;
import com.satgraf.community.placer.KKPlacer;
import com.satgraf.evolution.UI.EvolutionGraphFrame;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityGraphFactoryFactory;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.community.JSONCommunityGraph;
import com.satlib.community.OLCommunityMetric;
import com.satlib.community.placer.CommunityPlacer;
import com.satlib.community.placer.CommunityPlacerFactory;
import com.validatedcl.validation.Help;
import com.validatedcl.validation.ValidatedCommandLine;
import com.validatedcl.validation.ValidatedOption;
import com.validatedcl.validation.rules.FileValidationRule;
import com.validatedcl.validation.rules.ListValidationRule;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;
/**
 *
 * @author zacknewsham
 */
public class CommunityGraphFrame extends GraphFrame{
  static{
    forceInit(FruchGPUPlacer.class);
    forceInit(FruchPlacer.class);
    forceInit(KKPlacer.class);
    forceInit(GridPlacer.class);
    forceInit(OLCommunityMetric.class);
    forceInit(CNMCommunityMetric.class);
  }

  protected HashMap<String, Pattern> patterns;
  private CommunityMetric metric;
  public CommunityGraphFrame(CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns, CommunityMetric metric) {
    super(graphViewer);
    this.patterns = patterns;
    this.metric = metric;
    
  }
  public CommunityGraphViewer getGraphViewer(){
    return (CommunityGraphViewer)graphViewer;
  }
  
  public void setPatterns(HashMap<String, Pattern> patterns){
    this.patterns = patterns;
  }
  
  
  public void show() {
    if(graphViewer != null && graphViewer.graph != null && panel == null){
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(getGraphViewer()));

      panel = new CommunityOptionsPanel(this, getGraphViewer(), patterns.keySet());
    }
    super.show();
  }
  
  public void init(){
    super.init();
    metric.getCommunities((CommunityGraph)graphViewer.getGraph());
  }
  
  public void fromJson(JSONObject json){
    if(!(this instanceof EvolutionGraphFrame)){
      JSONCommunityGraph graph = new JSONCommunityGraph((JSONObject)json.get("graphViewer"));
      graph.init();
      this.graphViewer = new CommunityGraphViewer(graph, graph.getNodeLists(), graph);
      this.patterns = new HashMap<>();
      init();
      show();
      this.graphViewer.fromJson((JSONObject)json.get("graphViewer"));
    }
    super.fromJson(json);
  }
  public String toJson(){
    StringBuilder json = new StringBuilder(super.toJson());
    
    return json.toString();
  }
  
  @Override
  public com.satgraf.actions.OpenAction getOpenAction(){
    return new OpenAction(this);
  }  
  
  @Override
  public com.satgraf.actions.ExportAction getExportAction(){
    return new ExportAction(this);
  }  
  
  
  public static Options options(){
    Options options = new Options();
    ValidatedOption o;
    
    o = new ValidatedOption("f","file",true, "The file (either .cnf or .sb)");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes, new String[]{"read"}));
    options.addOption(o);
    
    o = new ValidatedOption("u","url",true, "A file URL (either .cnf or .sb)");
    options.addOption(o);
    
    o = new ValidatedOption("c", "community", true,"The community detection algorithm");
    o.setDefault("ol");
    o.addRule(new ListValidationRule(CommunityMetricFactory.getInstance().getNames()));
    options.addOption(o);
    
    o = new ValidatedOption("l","layout",true,"The layout algorithm to use");
    o.setDefault("f");
    o.addRule(new ListValidationRule(CommunityPlacerFactory.getInstance().getNames()));
    options.addOption(o);
    
    o = new ValidatedOption("p", "pattern",true,"A list of regex expressions to group variables (not yet implemented)");
    options.addOption(o);
    
    return options;
  }
  
  public static void main(String args[]) throws IOException, ParseException{
    if(args.length == 0){
      args = new String[]{
        //"formula/satcomp/dimacs/toybox.cnf",
        //"-f","/home/zacknewsham/aes.sb",
        "-f","formula/satcomp/dimacs/toybox.cnf",
        //"/home/zacknewsham/Sites/multisat/formula/27round.cnf",
        //"-f","/media/zacknewsham/SAT/sat2014/sc14-app/005-80-12.cnf",
        "-c","ol",
        "-l","f"
      };
      System.out.print(Help.getHelp(options()));
      //return;
    }
    CommandLineParser clp = new GnuParser();
    Options o = options();
    CommandLine cl = clp.parse(o, args);
    
    if(!ValidatedCommandLine.validateCommandLine(o, cl)){
      System.err.println(ValidatedCommandLine.getError());
      return;
    }
    
    
    CommunityGraphFactoryFactory ff = new CommunityGraphFactoryFactory(cl.getOptionValue("c", o.getOption("c").getValue()));
    HashMap<String, String> patterns = new HashMap<>();
    CommunityGraphFactory factory;
    
    Object in;
    if(cl.getOptionValue("f") == null && cl.getOptionValue("u") == null){
      System.err.println("Must supply either -f or -u");
      return;
    }
    else if(cl.getOptionValue("f") == null){
      URL input = new URL(cl.getOptionValue("u"));
      in = input;
      factory = ff.getFactory(input, patterns);
    }
    else{
      File input = new File(cl.getOptionValue("f"));
      in = input;
      factory = ff.getFactory(input, patterns);
    }
    
    /*for (int i = 5; i < args.length; i += 2) {
      patterns.put(args[i], args[i + 1]);
    }*/
    
    CommunityGraphViewer graphViewer = new CommunityGraphViewer(null, factory.getNodeLists(), null);
    CommunityGraphFrame frmMain = new CommunityGraphFrame(graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.setProgressive(factory);
    frmMain.preinit();
    if(in instanceof File){
      factory.makeGraph((File)in);
    }
    else if(in instanceof URL){
      factory.makeGraph((URL)in);
    }
    
    frmMain.setVisible(true);
    CommunityPlacer p = null;
    if(factory.getGraph() instanceof CommunityPlacer){
      p = (CommunityPlacer)factory.getGraph();
    }
    else{
      p = CommunityPlacerFactory.getInstance().getByName(cl.getOptionValue("l", o.getOption("l").getValue()), factory.getGraph());
    }
    frmMain.setProgressive(p);
    graphViewer.graph = factory.getGraph();
    graphViewer.setPlacer(p);
    frmMain.init();
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.show();
  }
}
