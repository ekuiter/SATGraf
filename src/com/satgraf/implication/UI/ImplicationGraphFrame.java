/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import static com.satlib.ForceInit.forceInit;
import com.satgraf.FormatValidationRule;
import com.satgraf.actions.ExportAction;
import com.satgraf.actions.OpenAction;
import com.satgraf.actions.UnExportableAction;
import com.satgraf.actions.UnOpenableAction;
import com.satgraf.community.placer.GridPlacer;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.GraphViewer;
import com.satgraf.graph.UI.SimpleCanvas;
import com.satgraf.graph.placer.FruchPlacer;
import com.satgraf.graph.placer.KKPlacer;
import com.satgraf.graph.placer.Placer;
import com.satgraf.graph.placer.PlacerFactory;
import com.satlib.graph.GraphFactory;
import com.satlib.graph.GraphFactoryFactory;
import com.satlib.implication.ImplicationGraphFactory;
import com.satlib.implication.DimacsImplicationGraphFactory;
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

/**
 *
 * @author zacknewsham
 */
public class ImplicationGraphFrame extends GraphFrame{
  static{
    //forceInit(FruchGPUPlacer.class);
    forceInit(FruchPlacer.class);
    forceInit(KKPlacer.class);
    forceInit(GridPlacer.class);
    forceInit(DimacsImplicationGraphFactory.class);
  }

  HashMap<String,Pattern> patterns;
  public ImplicationGraphFrame(ImplicationGraphViewer graphViewer, HashMap<String,Pattern> patterns) {
    super(graphViewer);
    this.patterns = patterns;
  }
  
  public ImplicationGraphViewer getGraphViewer(){
    return (ImplicationGraphViewer)graphViewer;
  }
  @Override 
  public void show(){
    if(graphViewer != null && graphViewer.graph != null && panel == null){
      canvasPanel = new GraphCanvasPanel(new SimpleCanvas(graphViewer));
      panel = new ImplicationOptionsPanel(this, getGraphViewer(), patterns.keySet());
    }
    super.show();
  }
  
  @Override
  public OpenAction getOpenAction(){
    return new UnOpenableAction(this);
  }
  
  @Override
  public ExportAction getExportAction(){
    return new UnExportableAction(this);
  }
  
  public static Options options(){
    Options options = new Options();
    ValidatedOption o;
    
    o = new ValidatedOption("f","file",true, "The file (either .cnf or .sb)");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes, new String[]{"read"}));
    options.addOption(o);
    
    o = new ValidatedOption("u","url",true, "A file URL (either .cnf or .sb)");
    options.addOption(o);
    
    o = new ValidatedOption("l","layout",true,"The layout algorithm to use");
    o.setDefault("f");
    o.addRule(new ListValidationRule(PlacerFactory.getInstance().getNames(),PlacerFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("p", "pattern",true,"A list of regex expressions to group variables (not yet implemented)");
    options.addOption(o);
    
    o = new ValidatedOption("m","format",true, "The format of the file, and desired graph representation");
    o.addRule(new FormatValidationRule());
    o.setDefault("auto");
    options.addOption(o);
    
    return options;
  }
  
  public static void main(String args[]) throws IOException, ParseException, InstantiationException{
    if(args.length == 0){
      args = new String[]{
        //"formula/satcomp/dimacs/toybox.cnf",
        //"-f","/home/zacknewsham/aes.sb",
        "-f","/home/zacknewsham/Documents/University/visualizationpaper/formula/aes_16_10_keyfind_3.cnf",
        //"/home/zacknewsham/Sites/multisat/formula/27round.cnf",
        //"-f","/media/zacknewsham/SAT/sat2014/sc14-app/005-80-12.cnf",
        "-l","f"
      };
      System.out.print(Help.getHelp(options()));
      return;
    }
    CommandLineParser clp = new GnuParser();
    Options o = options();
    CommandLine cl = new CommandLine(clp.parse(o, args), o);
    
    if(!ValidatedCommandLine.validateCommandLine(o, cl.getCommandLine())){
      System.err.println(ValidatedCommandLine.getError());
      return;
    }
    ImplicationGraphFactory factory = null;
    HashMap<String, String> patterns = new HashMap<>();
    Object in;
    if(cl.getOptionValue("f") == null && cl.getOptionValue("u") == null){
      System.err.println("Must supply either -f or -u");
      return;
    }
    else if(cl.getOptionValue("f") == null){
      URL input = new URL(cl.getOptionValue("u"));
      in = input;
      String extension = cl.getOptionValue("u").substring(cl.getOptionValue("u").lastIndexOf(".")+1);
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, null, new HashMap<String,String>());
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(tmp == null || !(tmp instanceof ImplicationGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
        throw e;
      }
      factory = (ImplicationGraphFactory)tmp;
    }
    else{
      File input = new File(cl.getOptionValue("f"));
      in = input;
      String extension = cl.getOptionValue("f").substring(cl.getOptionValue("f").lastIndexOf(".")+1);
      GraphFactory tmp = GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), extension, null, new HashMap<String,String>());
      if(tmp == null){
        throw new InstantiationException(cl.getOptionValue("m") + " is not available for format " + extension);
      }
      else if(tmp == null || !(tmp instanceof ImplicationGraphFactory)){
        InstantiationException e = new InstantiationException(tmp.getClass().getName() + " is not an instance of CommunityGraph");
        throw e;
      }
      factory = (ImplicationGraphFactory)tmp;
    }
    if(factory == null){
      System.err.println("Factory is null");
      return;
    }
    
    ImplicationGraphViewer graphViewer = new ImplicationGraphViewer(null, factory.getNodeLists(), null);
    ImplicationGraphFrame frmMain = new ImplicationGraphFrame(graphViewer, factory.getPatterns());
    frmMain.setProgressive(factory);
    frmMain.preinit();
    
    frmMain.show();
    
    if(in instanceof File){
      factory.makeGraph((File)in);
    }
    else if(in instanceof URL){
      factory.makeGraph((URL)in);
    }
    graphViewer.graph = factory.getGraph();
    
    Placer p = null;
    frmMain.setPlacerName(cl.getOptionValue("l"));
    if(factory.getGraph() instanceof Placer){
      p = (Placer)factory.getGraph();
    }
    else{
      p = PlacerFactory.getInstance().getByName(frmMain.getPlacerName(), factory.getGraph());
    }
    frmMain.setProgressive(p);
    graphViewer.setPlacer(p);
    frmMain.init();
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.show();
  }
}
