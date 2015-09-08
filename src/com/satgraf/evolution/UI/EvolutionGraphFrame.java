package com.satgraf.evolution.UI;

import static com.satlib.ForceInit.forceInit;
import com.satgraf.FormatValidationRule;
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.community.placer.CommunityPlacer;
import com.satgraf.community.placer.CommunityPlacerFactory;
import com.satgraf.evolution.observers.EvolutionObserverFactory;
import com.satgraf.evolution.observers.QEvolutionObserver;
import com.satgraf.evolution.observers.VSIDSSpacialLocalityEvolutionObserver;
import com.satgraf.evolution.observers.VSIDSTemporalLocalityEvolutionObserver;
import com.satgraf.evolution.observers.VisualEvolutionObserver;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphOptionsPanel;
import com.satgraf.graph.placer.Placer;
import com.satgraf.graph.placer.PlacerFactory;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.evolution.DimacsEvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.observers.EvolutionObserver;
import com.satlib.graph.GraphFactory;
import com.satlib.graph.GraphFactoryFactory;
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
    forceInit(com.satgraf.evolution.observers.EvolutionObserverFactory.class);
    forceInit(VSIDSTemporalLocalityEvolutionObserver.class);
    forceInit(QEvolutionObserver.class);
    forceInit(VSIDSSpacialLocalityEvolutionObserver.class);
    forceInit(DimacsEvolutionGraphFactory.class);
  }
  private EvolutionGraphFactory factory;
  public static String minisat;
  public EvolutionGraphFrame(EvolutionGraphFactory factory, EvolutionGraphViewer viewer, HashMap<String, Pattern> patterns, CommunityMetric metric) {
    super(viewer, patterns, metric);
    this.factory = factory;
    
    this.addWindowListener(new WindowAdapter() {
    	public void windowClosing(WindowEvent e) {
    		DimacsEvolutionGraphFactory.deleteOutputFolder();
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
    
    o = new ValidatedOption("p", "pattern",true,"A list of regex expressions to group variables (not yet implemented)");
    options.addOption(o);
    
    o = new ValidatedOption("s","solver",true,"The location of the modified solver");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes,new String[]{"execute"}));
    o.setDefault("solvers/minisat/minisat");
    options.addOption(o);
    
    o = new ValidatedOption("o", "observers", true, "A named evolution observer");
    o.addRule(new ListValidationRule(EvolutionObserverFactory.getInstance().getNames(),EvolutionObserverFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("m","format",true, "The format of the file, and desired graph representation");
    o.addRule(new FormatValidationRule());
    o.setDefault("auto");
    options.addOption(o);
    
    return options;
  }
  
  public static void main(String[] args) throws IOException, ParseException, InstantiationException {
    if (args.length == 0) {
      args = new String[]{
       //"-f","/home/zacknewsham/Documents/University/visualizationpaper/formula/unif-k3-r4.267-v421-c1796-S4839562527790587617.cnf",
        "-f","/home/zacknewsham/Sites/satgraf/demo/satgraf/formula/aes_16_10_keyfind_3.cnf",
        //"-f","./formula/27round.cnf",
        "-c","l",
        "-l","c",
        "-o","Q"
      };
      System.out.print(Help.getHelp(options()));
      //return;
    }
    CommandLineParser clp = new GnuParser();
    Options o = options();
    CommandLine cl = new CommandLine(clp.parse(o, args),o);
    
    if(!ValidatedCommandLine.validateCommandLine(o, cl.getCommandLine())){
      System.err.println(ValidatedCommandLine.getError());
      return;
    }
    
    String comName = cl.getOptionValue("c");
    HashMap<String, String> patterns = new HashMap<String, String>();
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
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, cl.getOptionValue("c"), new HashMap<String,String>());
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(tmp == null || !(tmp instanceof EvolutionGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
        throw e;
      }
      factory = (EvolutionGraphFactory)tmp;
      factory.setSolver(cl.getOptionValue("s"));
    
    }
    else{
      File input = new File(cl.getOptionValue("f"));
      in = input;
      String extension = cl.getOptionValue("f").substring(cl.getOptionValue("f").lastIndexOf(".")+1);
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, cl.getOptionValue("c"), new HashMap<String,String>());
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(tmp == null || !(tmp instanceof EvolutionGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
        throw e;
      }
      factory = (EvolutionGraphFactory)tmp;
      factory.setSolver(cl.getOptionValue("s"));
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
    graphViewer.setEvolution(factory.getEvolution());
    
    Placer p = null;
    p = CommunityPlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
    if( p == null){
      p = PlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
    }
    frmMain.setProgressive(p);
    graphViewer.graph = factory.getGraph();
    frmMain.init();
    graphViewer.setPlacer(p);
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    factory.buildEvolutionFile();
    frmMain.show();
    GraphOptionsPanel panel = frmMain.panel;
    if(cl.getOptionValue("o") != null){
      
      EvolutionObserver observer = null;
      if(VisualEvolutionObserver.class.isAssignableFrom(EvolutionObserverFactory.getInstance().getObserverType(cl.getOptionValue("o")))){
        observer = EvolutionObserverFactory.getInstance().getByName(cl.getOptionValue("o"), graphViewer);
      }
      else{
        observer = EvolutionObserverFactory.getInstance().getByName(cl.getOptionValue("o"), factory.getGraph());
      }
      CommunityMetric metric = CommunityMetricFactory.getInstance().getByName(comName);
      observer.setCommunityMetric(metric);
      
      if(observer instanceof JPanel){
        panel.addPanel((JPanel)observer, observer.getName());
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
