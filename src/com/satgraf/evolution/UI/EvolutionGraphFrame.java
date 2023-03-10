package com.satgraf.evolution.UI;

import com.satgraf.FormatValidationRule;
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.community.placer.CommunityPlacerFactory;
import com.satgraf.evolution.observers.QEvolutionObserver;
import com.satgraf.evolution.observers.VSIDSSpacialLocalityEvolutionObserver;
import com.satgraf.evolution.observers.VSIDSTemporalLocalityEvolutionObserver;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphOptionsPanel;
import com.satgraf.graph.color.EdgeColoringFactory;
import com.satgraf.graph.color.NodeColoringFactory;
import com.satgraf.graph.placer.FruchGPUPlacer;
import com.satgraf.graph.placer.Placer;
import com.satgraf.graph.placer.PlacerFactory;
import com.satgraf.supplemental.SupplementalView;
import com.satgraf.supplemental.SupplementalViewFactory;
import static com.satlib.ForceInit.forceInit;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.evolution.DimacsEvolutionGraphFactory;
import com.satlib.evolution.DimacsLiteralEvolutionGraphFactory;
import com.satlib.evolution.Evolution;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.graph.Clause;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.GraphFactory;
import com.satlib.graph.GraphFactoryFactory;
import com.satlib.graph.MaximumClique;
import com.satlib.graph.Node;
import com.validatedcl.validation.CommandLine;
import com.validatedcl.validation.Help;
import com.validatedcl.validation.ValidatedCommandLine;
import com.validatedcl.validation.ValidatedOption;
import com.validatedcl.validation.rules.FileValidationRule;
import com.validatedcl.validation.rules.ListValidationRule;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class EvolutionGraphFrame extends CommunityGraphFrame {
  static{
    forceInit(VSIDSTemporalLocalityEvolutionObserver.class);
    forceInit(QEvolutionObserver.class);
    forceInit(VSIDSSpacialLocalityEvolutionObserver.class);
    forceInit(DimacsEvolutionGraphFactory.class);
    forceInit(DimacsLiteralEvolutionGraphFactory.class);
    forceInit(EvolutionColoring.class);
    forceInit(EvolutionDecisionTemperatureColoring.class);
    forceInit(EvolutionAssignmentTemperatureColoring.class);
    forceInit(EvolutionCommunityAssignmentTemperatureColoring.class);
    //forceInit(FruchGPUPlacer.class);
  }
  private EvolutionGraphFactory factory;
  public static String minisat;
  public EvolutionGraphFrame(EvolutionGraphFactory factory, final EvolutionGraphViewer viewer, HashMap<String, Pattern> patterns, CommunityMetric metric) {
    super(viewer, patterns, metric);
    this.factory = factory;
    
    this.addWindowListener(new WindowAdapter() {
    	public void windowClosing(WindowEvent e) {
    		viewer.evolution.deleteOutputFolder();
    	}
	});
  }

  public void setFactory(EvolutionGraphFactory factory){
    this.factory = factory;
  }
  
  public String toJson() {
    StringBuilder json = new StringBuilder(super.toJson());

    return json.toString();
  }
  
  public EvolutionGraphViewer getGraphViewer(){
	 return (EvolutionGraphViewer)graphViewer;
  }

  public void show() {
    if (graphViewer != null && graphViewer.graph != null && panel == null) {
      canvasPanel = new GraphCanvasPanel(new EvolutionGraphCanvas((EvolutionGraphViewer)graphViewer));
      panel = new EvolutionOptionsPanel(this, getGraphViewer(), patterns.keySet());
      setProgressive(getGraphCanvas());
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
	if(graphViewer != null){
		graphViewer.selectNode(null);
    }
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
    o.addRule(new ListValidationRule(CommunityMetricFactory.getInstance().getNames(), CommunityMetricFactory.getInstance().getDescriptions()));
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
    
    o = new ValidatedOption("r", "pattern",true,"A list of name:regex expressions to group variables");
    options.addOption(o);
    
    o = new ValidatedOption("p", "pipe", true, "The piping file location");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes));
    o.setDefault("solvers/piping/");
    options.addOption(o);
    
    o = new ValidatedOption("s","solver",true,"The location of the modified solver");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes,new String[]{"execute"}));
    o.setDefault("solvers/minisat/minisat");
    options.addOption(o);
    
    o = new ValidatedOption("o", "supplemental", true, "A named supplemental view");
    o.addRule(new ListValidationRule(SupplementalViewFactory.getInstance().getNames(),SupplementalViewFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("m","format",true, "The format of the file, and desired graph representation");
    o.addRule(new FormatValidationRule());
    o.setDefault("auto");
    options.addOption(o);
    
    o = new ValidatedOption("e", "edge-color", true, "The edge colouring implementation to use");
    o.setDefault("auto");
    o.addRule(new ListValidationRule(EdgeColoringFactory.getInstance().getNames(), EdgeColoringFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("n", "node-color", true, "The node colouring implementation to use");
    o.setDefault("auto");
    o.addRule(new ListValidationRule(NodeColoringFactory.getInstance().getNames(), NodeColoringFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    return options;
  }
  
  public static void main(String[] args) throws IOException, ParseException, InstantiationException {
    if (args.length == 0) {
      System.out.print(Help.getHelp(options()));
      return;
    }
    CommandLineParser clp = new GnuParser();
    Options o = options();
    CommandLine cl = new CommandLine(clp.parse(o, args),o);
    
    if(!ValidatedCommandLine.validateCommandLine(o, cl.getCommandLine())){
      System.err.println(ValidatedCommandLine.getError());
      return;
    }
    
    
    Evolution.dumpFileDirectory = cl.getOptionValue("p");
    Evolution.pipeFileName = Evolution.dumpFileDirectory + "myPipe.txt";
    Evolution.outputDirectory = Evolution.dumpFileDirectory + "output/";
    String comName = cl.getOptionValue("c");
    HashMap<String, String> patterns = new HashMap<>();
    if(cl.getCommandLine().getOptionValues("r") != null){
      for(String pattern : cl.getCommandLine().getOptionValues("r")){
        String[] parts = pattern.split(":",2);
        patterns.put(parts[0], parts[1]);
      }
    }
    minisat = cl.getOptionValue("s");
    EvolutionGraphFactory factory;
    Object in;
    if(cl.getOptionValue("f") == null && cl.getOptionValue("u") == null){
      System.err.println("Must supply either -f or -u");
      return;
    }
    else if(cl.getOptionValue("f") == null){
      URL input = new URL(cl.getOptionValue("u"));
      in = input;
      String extension = cl.getOptionValue("f").substring(cl.getOptionValue("f").lastIndexOf(".")+1);
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, cl.getOptionValue("c"), patterns);
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(tmp == null || !(tmp instanceof EvolutionGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
        throw e;
      }
      factory = (EvolutionGraphFactory)tmp;
    
    }
    else{
      File input = new File(cl.getOptionValue("f"));
      in = input;
      String extension = cl.getOptionValue("f").substring(cl.getOptionValue("f").lastIndexOf(".")+1);
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, cl.getOptionValue("c"), patterns);
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(tmp == null || !(tmp instanceof EvolutionGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of EvolutionGraphFactory");
        throw e;
      }
      factory = (EvolutionGraphFactory)tmp;
    }
    EvolutionGraphViewer graphViewer = new EvolutionGraphViewer(null, factory.getNodeLists(), null);
    
    
    EvolutionGraphFrame frmMain = new EvolutionGraphFrame(factory, graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.setPlacerName(cl.getOptionValue("l"));
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
    graphViewer.setEvolution(new Evolution(factory.getGraph(), (File)in, cl.getOptionValue("s")));
    
    Placer p = null;
    p = CommunityPlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
    if( p == null){
      p = PlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
    }
    frmMain.setProgressive(p);
    graphViewer.graph = factory.getGraph();
    frmMain.init();
    graphViewer.setPlacer(p);
    graphViewer.setNodeColoring(NodeColoringFactory.getInstance().getByName(cl.getOptionValue("n"), graphViewer.graph));
    graphViewer.setEdgeColoring(EdgeColoringFactory.getInstance().getByName(cl.getOptionValue("e"), graphViewer.graph));
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.show();
    GraphOptionsPanel panel = frmMain.panel;
    if(cl.getCommandLine().getOptionValues("o") != null){
      for(String obs : cl.getCommandLine().getOptionValues("o")){

        SupplementalView observer = SupplementalViewFactory.getInstance().getByName(obs, graphViewer.getGraph());
        CommunityMetric metric = CommunityMetricFactory.getInstance().getByName(comName);
        observer.setCommunityMetric(metric);
        observer.setGraphViewer(graphViewer);
        observer.init();

        if(observer instanceof JPanel){
          panel.addPanel((JPanel)observer, observer.getName());
        }
      }
    }
  }

  public static String usage() {
    return "[formula/path.cnf | saved/path.sb] [ol | cnm] [f | grid | kk] [dumpfreq] [/path/to/solver]";
  }

  public static String help() {
    return "\"formula\" \"community algorithm\" \"layout algorithm\" \"dump frequency\" \"path to modified solver + options\"\n"
            + "View the evolution of the community VIG of a SAT formula while being solved, dumping after every [dumpfreq] variable assignments";
  }
}
