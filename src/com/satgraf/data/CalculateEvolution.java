package com.satgraf.data;


import com.satgraf.FormatValidationRule;
import com.satgraf.community.placer.CommunityPlacerFactory;
import static com.satgraf.evolution.UI.EvolutionGraphFrame.options;
import com.satgraf.graph.placer.PlacerFactory;
import com.satlib.CSVModel;
import static com.satlib.ForceInit.forceInit;
import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.community.JSONCommunityGraphFactory;
import com.satlib.community.LouvianCommunityMetric;
import com.satlib.community.OLCommunityMetric;
import com.satlib.evolution.DimacsEvolutionGraphFactory;
import com.satlib.evolution.DimacsLiteralEvolutionGraphFactory;
import com.satlib.evolution.Evolution;
import com.satlib.evolution.EvolutionGraph;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.observers.CSVEvolutionObserver;
import com.satlib.evolution.observers.CSVEvolutionObserverFactory;
import com.satlib.evolution.observers.QEvolutionObserver;
import com.satlib.evolution.observers.VSIDSSpacialLocalityEvolutionObserver;
import com.satlib.evolution.observers.VSIDSTemporalLocalityEvolutionObserver;
import com.satlib.graph.GraphFactoryFactory;
import com.validatedcl.validation.CommandLine;
import com.validatedcl.validation.ValidatedCommandLine;
import com.validatedcl.validation.ValidatedOption;
import com.validatedcl.validation.rules.FileValidationRule;
import com.validatedcl.validation.rules.ListValidationRule;
import com.validatedcl.validation.rules.NumericValidationRule;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zacknewsham
 */
public class CalculateEvolution {
  static{
    forceInit(VSIDSTemporalLocalityEvolutionObserver.class);
    forceInit(QEvolutionObserver.class);
    forceInit(VSIDSSpacialLocalityEvolutionObserver.class);
    forceInit(DimacsEvolutionGraphFactory.class);
    forceInit(DimacsLiteralEvolutionGraphFactory.class);
    forceInit(LouvianCommunityMetric.class);
    forceInit(OLCommunityMetric.class);
    forceInit(CNMCommunityMetric.class);
    forceInit(JSONCommunityGraphFactory.class);
  }
  
   public static int largestPowerOf2 (int n)
   {
        int res = 2;        
        while (res < n) {
                res*=2;
        }

        return res;
   }
   public static Options options(){
    Options options = new Options();
    ValidatedOption o;
    
    
    o = new ValidatedOption("f","file",true, "The file (.cnf)");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes, new String[]{"read"}));
    options.addOption(o);
    
    o = new ValidatedOption("c", "community", true,"The community detection algorithm");
    o.setDefault("ol");
    o.addRule(new ListValidationRule(CommunityMetricFactory.getInstance().getNames(), CommunityMetricFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("s","solver",true,"The location of the modified solver");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes,new String[]{"execute"}));
    o.setDefault("solvers/minisat/minisat");
    options.addOption(o);
    
    o = new ValidatedOption("m","format",true, "The format of the file, and desired graph representation");
    o.addRule(new FormatValidationRule());
    o.setDefault("auto");
    options.addOption(o);
    
    o = new ValidatedOption("p", "pipe", true, "The piping file location");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes));
    o.setDefault("solvers/piping/");
    options.addOption(o);
    
    o = new ValidatedOption("d", "directory", true, "The output directory");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes));
    o.addRule(new FileValidationRule(FileValidationRule.IsDirectory.yes, new String[]{"write"}));
    options.addOption(o);
    
    o = new ValidatedOption("w","window",true,"The number of decisions before re-computing Q");
    o.addRule(new NumericValidationRule(true));
    o.setDefault("10");
    options.addOption(o);
    
    o = new ValidatedOption("o", "observer", true, "The name of the observer to use for the evolution");
    o.addRule(new ListValidationRule(CSVEvolutionObserverFactory.getInstance().getNames(), CSVEvolutionObserverFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("e","decisions",true, "The number of decisions to solve until");
    o.addRule(new NumericValidationRule(true));
    o.setDefault("10240");
    options.addOption(o);
    return options;
  }
   
  public static void main(String[] args) throws IOException, InterruptedException, ParseException{
    if(args.length < 1){
      args = new String[]{
        "-f","/home/zacknewsham/obfuscated/lc_test000001.cnf",
        "-d","/home/zacknewsham/",
        "-o","Q",
        "-o","VSIDST",
        "-o","VSIDSS",
        "-e","10240"
      };
    }
    
    CommandLineParser clp = new GnuParser();
    Options o = options();
    CommandLine cl = new CommandLine(clp.parse(o, args),o);
    
    if(!ValidatedCommandLine.validateCommandLine(o, cl.getCommandLine())){
      System.err.println(ValidatedCommandLine.getError());
      return;
    }
    
    EvolutionGraphFactory factory = (EvolutionGraphFactory)GraphFactoryFactory.getInstance().getByNameAndExtension(cl.getOptionValue("m"), "cnf", cl.getOptionValue("c"), new HashMap<String, String>());
    
    File input = new File(cl.getOptionValue("f"));
    
    EvolutionGraph graph = factory.makeGraph(input);
    List<CSVEvolutionObserver> observers = new ArrayList<>();
    boolean shouldSkip = true;
    for(String observer : cl.getCommandLine().getOptionValues("o")){
      CSVEvolutionObserver obs = CSVEvolutionObserverFactory.getInstance().getByName(observer, graph);
      observers.add(obs);
      obs.setCommunityMetric(factory.getMetric());
      if(obs instanceof QEvolutionObserver){
        ((QEvolutionObserver)obs).windowSize = Integer.parseInt(cl.getOptionValue("w"));
      }
      File f = new File(cl.getOptionValue("d") + input.getName() + "." + obs.getName());
      if(!f.exists()){
        shouldSkip = false;
      }
    }
    if(shouldSkip){
      return;
    }
    factory.getMetric().getCommunities(graph);
    factory.setSolver(cl.getOptionValue("s"));
    Evolution.dumpFileDirectory = cl.getOptionValue("p");
    Evolution.pipeFileName = Evolution.dumpFileDirectory + "myPipe.txt";
    Evolution.outputDirectory = Evolution.dumpFileDirectory + "output/";
    Evolution e = factory.getEvolution();
    e.getDecisions();
    factory.process(graph);
    int i = 0;
    int exceptionCount = 0;
    int lastSize = 0;
    int decisions = Integer.parseInt(cl.getOptionValue("e"));
    cl.getOptionValue("o");
    while((factory.solverRunning()) || i < e.getTotalLines() && !e.hasError()){
      while(e.getTotalLines() <= i && factory.solverRunning()){
        Thread.sleep(1000);
        System.err.println("still waiting");
      }
      try{
        e.advanceEvolutionThread(i - 1, i, false);
        exceptionCount=0;
      }
      catch(Exception e1){
        System.err.printf("line not ready %d\n", exceptionCount);
        exceptionCount++;
        Thread.sleep(1000);
        if(exceptionCount == 10){break;}
      }
      if(e.getDecisions() == decisions){
        break;
      }
      else if(e.getDecisions() % 5 == 0 && lastSize != e.getDecisions()){
        System.err.printf("%d\n", e.getDecisions());
      }
      lastSize = e.getDecisions();
      i = i + 1;
    }
    factory.stopSolver();
    if(e.hasError()){
      return;
    }
    for(CSVEvolutionObserver obs : observers){
      CSVModel model = obs.getModel();
      model.toFile(new File(cl.getOptionValue("d") + input.getName() + "." + obs.getName()));
    }
    System.exit(0);
    //System.out.printf("%f,%f,%f\n", q.bestCase, q.worstCase, q.qs.get(10), q.qs.get(25), q.qs.get(50), q.qs.get(100), q.qs.get(250), q.qs.get(500), q.qs.get(750), q.qs.get(1000));
  }
  
}
