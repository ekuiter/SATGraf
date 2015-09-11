/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.FormatValidationRule;
import com.satgraf.community.placer.CircularCommunityPlacer;
import com.satgraf.community.placer.CommunityPlacerFactory;
import com.satgraf.community.placer.GridKKPlacer;
import com.satgraf.community.placer.GridPlacer;
import com.satgraf.community.placer.JSONCommunityPlacer;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.color.EdgeColoringFactory;
import com.satgraf.graph.color.NodeColoringFactory;
import com.satgraf.graph.placer.FruchPlacer;
import com.satgraf.graph.placer.FruchRandomPlacer;
import com.satgraf.graph.placer.jung.JungWrapper;
import com.satgraf.graph.placer.KKPlacer;
import com.satgraf.graph.placer.Placer;
import com.satgraf.graph.placer.PlacerFactory;
import static com.satlib.ForceInit.forceInit;
import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.community.DimacsCommunityGraphFactory;
import com.satlib.community.DimacsLiteralCommunityGraphFactory;
import com.satlib.community.JSONCommunityGraph;
import com.satlib.community.JSONCommunityGraphFactory;
import com.satlib.community.LouvianCommunityMetric;
import com.satlib.community.OLCommunityMetric;
import com.satlib.graph.GraphFactory;
import com.satlib.graph.GraphFactoryFactory;
import com.validatedcl.validation.CommandLine;
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
    //forceInit(FruchGPUPlacer.class);
    forceInit(FruchPlacer.class);
    forceInit(FruchRandomPlacer.class);
    forceInit(KKPlacer.class);
    forceInit(GridKKPlacer.class);
    forceInit(GridPlacer.class);
    forceInit(CircularCommunityPlacer.class);
    forceInit(LouvianCommunityMetric.class);
    forceInit(OLCommunityMetric.class);
    forceInit(CNMCommunityMetric.class);
    forceInit(JSONCommunityGraphFactory.class);
    forceInit(DimacsCommunityGraphFactory.class);
    forceInit(DimacsLiteralCommunityGraphFactory.class);
    forceInit(CommunityColoring.class);
    forceInit(JungWrapper.class);
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
    metric.getCommunities((CommunityGraph)graphViewer.getGraph());
    super.init();
  }
  
  public void fromJson(JSONObject json){
    JSONCommunityGraph graph = new JSONCommunityGraph((JSONObject)json.get("graphViewer"));
    graph.init();
    this.graphViewer = new CommunityGraphViewer(graph, graph.getNodeLists(), new JSONCommunityPlacer(graph));
    this.patterns = new HashMap<>();
    init();
    show();
    this.graphViewer.fromJson((JSONObject)json.get("graphViewer"));
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
    o.addRule(new ListValidationRule(CommunityMetricFactory.getInstance().getNames(), CommunityMetricFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("m","format",true, "The format of the file, and desired graph representation");
    o.addRule(new FormatValidationRule());
    o.setDefault("auto");
    options.addOption(o);
    
    o = new ValidatedOption("l","layout",true,"The layout algorithm to use");
    o.setDefault("f");
    
    
    String[] names = new String[CommunityPlacerFactory.getInstance().getNames().length + PlacerFactory.getInstance().getNames().length];
    String[] descriptions = new String[CommunityPlacerFactory.getInstance().getDescriptions().length + PlacerFactory.getInstance().getDescriptions().length];
    int i = 0;
    for(String name : PlacerFactory.getInstance().getNames()){
      names[i] = name;
      i++;
    }
    for(String name : CommunityPlacerFactory.getInstance().getNames()){
      names[i] = name;
      i++;
    }
    i = 0;
    for(String description : PlacerFactory.getInstance().getDescriptions()){
      descriptions[i] = description;
      i++;
    }
    for(String description : CommunityPlacerFactory.getInstance().getDescriptions()){
      descriptions[i] = description;
      i++;
    }
    o.addRule(new ListValidationRule(names, descriptions));
    options.addOption(o);
    
    o = new ValidatedOption("p", "pattern",true,"A list of regex expressions to group variables (not yet implemented)");
    options.addOption(o);
    
    o = new ValidatedOption("e", "edge-color", true, "The edge colouring implementation to use");
    o.setDefault("auto");
    o.addRule(new ListValidationRule(EdgeColoringFactory.getInstance().getNames(), EdgeColoringFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("n", "node-color", true, "node edge colouring implementation to use");
    o.setDefault("auto");
    o.addRule(new ListValidationRule(NodeColoringFactory.getInstance().getNames(), NodeColoringFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    return options;
  }
  
  public static void main(String args[]) throws IOException, ParseException, InstantiationException{
    if(args.length == 0){
      args = new String[]{
        //"-f","formula/satcomp/dimacs/toybox.cnf",
        //"-f","/home/zacknewsham/aes.sb",
        "-f","/home/zacknewsham/23/1.cnf",
        //"/home/zacknewsham/Sites/multisat/formula/27round.cnf",
        //"-f","/media/zacknewsham/SAT/sat2014/sc14-app/005-80-12.cnf",
        "-c","ol",
        "-l","jung",
      };
      System.out.print(Help.getHelp(options()));
      //return;
    }
    CommandLineParser clp = new GnuParser();
    Options o = options();
    CommandLine cl = new CommandLine(clp.parse(o, args), o);
    
    if(!ValidatedCommandLine.validateCommandLine(o, cl.getCommandLine())){
      System.err.println(ValidatedCommandLine.getError());
      return;
    }
    
    
    String comName = cl.getOptionValue("c");
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
      String extension = cl.getOptionValue("u").substring(cl.getOptionValue("u").lastIndexOf(".")+1);
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, cl.getOptionValue("c"), new HashMap<String,String>());
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(!(tmp instanceof CommunityGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
        throw e;
      }
      factory = (CommunityGraphFactory)tmp;
    }
    else{
      File input = new File(cl.getOptionValue("f"));
      in = input;
      String extension = cl.getOptionValue("f").substring(cl.getOptionValue("f").lastIndexOf(".")+1);
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, cl.getOptionValue("c"), new HashMap<String,String>());
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(tmp == null || !(tmp instanceof CommunityGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
        throw e;
      }
      factory = (CommunityGraphFactory)tmp;
    }
    if(factory == null){
      System.err.println("Factory is null");
      return;
    }
    
    CommunityGraphViewer graphViewer = new CommunityGraphViewer(null, factory.getNodeLists(), null);
    CommunityGraphFrame frmMain = new CommunityGraphFrame(graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.setCommunityName(comName);
    frmMain.setProgressive(factory);
    frmMain.preinit();
    if(in instanceof File){
      factory.makeGraph((File)in);
    }
    else if(in instanceof URL){
      factory.makeGraph((URL)in);
    }
    
    frmMain.setVisible(true);
    Placer p = null;
    frmMain.setPlacerName(cl.getOptionValue("l"));
    if(factory.getGraph() instanceof Placer){
      p = (Placer)factory.getGraph();
    }
    else{
      p = CommunityPlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
      if( p == null){
        p = PlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
      }
    }
    frmMain.setProgressive(p);
    graphViewer.graph = factory.getGraph();
    frmMain.init();
    graphViewer.setPlacer(p);
    graphViewer.setNodeColoring(NodeColoringFactory.getInstance().getByName(cl.getOptionValue("n"), graphViewer.graph));
    graphViewer.setEdgeColoring(EdgeColoringFactory.getInstance().getByName(cl.getOptionValue("e"), graphViewer.graph));
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.show();
  }
}
