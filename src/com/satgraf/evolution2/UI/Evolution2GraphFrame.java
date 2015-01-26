package com.satgraf.evolution2.UI;

import static com.satgraf.ForceInit.forceInit;
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.community.placer.FruchGPUPlacer;
import com.satgraf.community.placer.FruchPlacer;
import com.satgraf.community.placer.GridPlacer;
import com.satgraf.community.placer.KKPlacer;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.community.OLCommunityMetric;
import com.satlib.community.placer.CommunityPlacer;
import com.satlib.community.placer.CommunityPlacerFactory;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import com.validatedcl.validation.Help;
import com.validatedcl.validation.RequiredOption;
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

public class Evolution2GraphFrame extends CommunityGraphFrame implements EvolutionGraphFactoryObserver{
  static{
    forceInit(FruchGPUPlacer.class);
    forceInit(FruchPlacer.class);
    forceInit(KKPlacer.class);
    forceInit(GridPlacer.class);
    forceInit(OLCommunityMetric.class);
    forceInit(CNMCommunityMetric.class);
  }
  private EvolutionGraphFactory factory;
  public static String minisat;
  public Evolution2GraphFrame(EvolutionGraphFactory factory, Evolution2GraphViewer viewer, HashMap<String, Pattern> patterns, CommunityMetric metric) {
    super(viewer, patterns, metric);
    this.factory = factory;
    factory.addObserver(this);
  }

  public void setFactory(EvolutionGraphFactory factory){
    this.factory = factory;
  }
  
  public String toJson() {
    StringBuilder json = new StringBuilder(super.toJson());

    return json.toString();
  }
  
  public Evolution2GraphViewer getGraphViewer(){
	 return (Evolution2GraphViewer)graphViewer;
  }

  public void show() {
    if (graphViewer != null && graphViewer.graph != null && panel == null) {
      canvasPanel = new GraphCanvasPanel(new Evolution2GraphCanvas((Evolution2GraphViewer)graphViewer));
      panel = new Evolution2OptionsPanel(this, getGraphViewer(), patterns.keySet());
      factory.buildEvolutionFile();
      super.show();
    } else {
      super.show();
    }
  }

  @Override
  public com.satgraf.actions.OpenAction getOpenAction() {
    return new OpenAction(this);
  }

  @Override
  public com.satgraf.actions.ExportAction getExportAction() {
	if(graphViewer != null)
		graphViewer.selectNode(null);
    return new ExportAction(this);
  }
  

  @Override
  public void init() {
    super.init();
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
    
    o = new ValidatedOption("s","solver",true,"The location of the modified solver");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes,new String[]{"execute"}));
    o.setDefault(System.getProperty("user.dir") + "/solvers/minisat/minisat");
    options.addOption(o);
    
    return options;
  }
  
  public static void main(String[] args) throws IOException, ParseException {
    if (args.length == 0) {
      args = new String[]{
        "-f","formula/satcomp/dimacs/toybox.cnf",
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
    
    String comName = cl.getOptionValue("c", o.getOption("c").getValue());
    HashMap<String, String> patterns = new HashMap<String, String>();
    minisat = cl.getOptionValue("s",o.getOption("s").getValue());
    Evolution2GraphFactoryFactory factoryfactory = new Evolution2GraphFactoryFactory(comName, minisat);
    EvolutionGraphFactory factory;
    Object in;
    if(cl.getOptionValue("f") == null && cl.getOptionValue("u") == null){
      System.err.println("Must supply either -f or -u");
      return;
    }
    else if(cl.getOptionValue("f") == null){
      URL input = new URL(cl.getOptionValue("u"));
      in = input;
      factory = factoryfactory.getFactory(input, patterns);
    
    }
    else{
      File input = new File(cl.getOptionValue("f"));
      in = input;
      factory = factoryfactory.getFactory(input, patterns);
    }
    
    
    Evolution2GraphViewer graphViewer = new Evolution2GraphViewer(null, factory.getNodeLists(), null);
    Evolution2GraphFrame frmMain = new Evolution2GraphFrame(factory, graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.setPlacerName(cl.getOptionValue("l", o.getOption("l").getValue()));
    frmMain.setCommunityName(comName);
    frmMain.setProgressive(factory);
    frmMain.preinit();
    
    frmMain.setVisible(true);
    if(in instanceof File){
      factory.makeGraph((File)in);
    }
    else if(in instanceof URL){
      factory.makeGraph((URL)in);
    }
    CommunityPlacer p = CommunityPlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
    frmMain.setProgressive(p);
    graphViewer.graph = factory.getGraph();
    graphViewer.setPlacer(p);
    frmMain.init();
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.show();
  }

  public static String usage() {
    return "[formula/path.cnf | saved/path.sb] [ol | cnm] [f | grid | kk] [dumpfreq] [/path/to/solver]";
  }

  public static String help() {
    return "\"formula\" \"community algorithm\" \"layout algorithm\" \"dump frequency\" \"path to modified solver + options\"\n"
            + "View the evolution of the community VIG of a SAT formula while being solved, dumping after every [dumpfreq] variable assignments";
  }

  @Override
  public void notifyObserver(EvolutionGraphFactory factory, Action action) {
    if(action == Action.newline){
      
    ((Evolution2OptionsPanel)panel).newFileReady(factory.getLineNumber());
    }
    else if(action == Action.process){
      show();
    }
  }
}
