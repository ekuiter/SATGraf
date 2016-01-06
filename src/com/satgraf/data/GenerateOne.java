package com.satgraf.data;


import com.satgraf.FormatValidationRule;
import static com.satgraf.data.CalculateEvolution.options;
import static com.satlib.ForceInit.forceInit;
import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.community.CommunityNode;
import com.satlib.community.DimacsCommunityGraphFactory;
import com.satlib.community.DimacsLiteralCommunityGraphFactory;
import com.satlib.community.DisjointGraphs;
import com.satlib.community.JSONCommunityGraphFactory;
import com.satlib.community.LouvianCommunityMetric;
import com.satlib.community.OLCommunityMetric;
import com.satlib.graph.Clause;
import com.satlib.graph.GraphFactoryFactory;
import com.validatedcl.validation.CommandLine;
import com.validatedcl.validation.ValidatedCommandLine;
import com.validatedcl.validation.ValidatedOption;
import com.validatedcl.validation.rules.FileValidationRule;
import com.validatedcl.validation.rules.ListValidationRule;
import com.validatedcl.validation.rules.NumericValidationRule;
import gnu.trove.map.hash.TIntIntHashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
public class GenerateOne {
  private static String minisat;
    private static int timeout = 60 * 60 * 1000;
    static long MAX_LENGTH = 100 * 1024 * 1024;//100MB
    public static BufferedWriter bw;
    public static int getCommunityDegrees(CommunityNode t){
      Set<Integer> t_ = new HashSet<>();
      for(CommunityEdge e : t.getEdges()){
        t_.add((e.getStart() == t ? e.getEnd() : e.getStart()).getCommunity());
      }
      return t_.size();
    }
    public static int getInterDegrees(CommunityNode t){
      Set<Integer> t_ = new HashSet<>();
      for(CommunityEdge e : t.getEdges()){
        if(e.getStart().getCommunity() != e.getEnd().getCommunity()){
          t_.add((e.getStart() == t ? e.getEnd() : e.getStart()).getId());
        }
      }
      return t_.size();
    }
    public static int getTotalDegrees(CommunityNode t){
      Set<Integer> t_ = new HashSet<>();
      for(CommunityEdge e : t.getEdges()){
        t_.add((e.getStart() == t ? e.getEnd() : e.getStart()).getId());
      }
      return t_.size();
    }
    
    static{
      forceInit(DimacsCommunityGraphFactory.class);
      forceInit(LouvianCommunityMetric.class);
    forceInit(DimacsLiteralCommunityGraphFactory.class);
      forceInit(OLCommunityMetric.class);
      forceInit(CNMCommunityMetric.class);
      forceInit(JSONCommunityGraphFactory.class);
    }
    
    public static void run(String file, String format, String com) throws IOException, InterruptedException, OutOfMemoryError{
      File f = new File(file);
      if(false && f.length() > MAX_LENGTH){
        return;
      }
      try{
        System.out.println("Started");
        CommunityGraphFactory factory = (CommunityGraphFactory)GraphFactoryFactory.getInstance().getByNameAndExtension(format, "cnf", com, new HashMap<String, String>());
        long startTime = System.currentTimeMillis();
        factory.makeGraph(f);
        CommunityGraph c = factory.getGraph();
        
        DisjointGraphs dj = new DisjointGraphs();
        Set<CommunityGraph> graphs = dj.getDisjointGraphs(c);
        /*if(graphs.size() == 1){
            return;
        }*/
        if(bw == null){
            bw = new BufferedWriter(new FileWriter(out));
        }
        System.out.println("Built Graph");
        if(c == null){
          return;
        }
        long endTime = System.currentTimeMillis();
        long generateTime = endTime - startTime;
        
        CommunityMetric metric = factory.getMetric();
        double Q = metric.getCommunities(c);
        System.out.println("Calcd coms");
        long Qtime = System.currentTimeMillis() - endTime;
        TIntIntHashMap map = c.getVariableDistribution();
        int maxVar = 0;
        double meanVar = 0.0;
        for(int count : map.values()){
            if(count > maxVar){
                maxVar = count;
            }
            meanVar += count;
        }
        meanVar = meanVar / map.size();

        Integer maxClause = 0;


        Iterator<Clause> clauses = c.getClauses().iterator();
        while(clauses.hasNext()){
            Clause cl = clauses.next();
            if(maxClause < cl.size()){
                maxClause = cl.size();
            }
        }
        List<CommunityNode> n = new ArrayList<>();
        n.addAll(c.getNodes());
        Collections.sort(n, new Comparator<CommunityNode>(){
          @Override
          public int compare(CommunityNode t, CommunityNode t1) {
            return getCommunityDegrees(t1) - getCommunityDegrees(t);
          }
          
        });
        
        HashSet<CommunityNode> numMaxCommunity = new HashSet<>();
        HashSet<CommunityNode> num10PerCommunity = new HashSet<>();
        HashSet<CommunityNode> num20PerCommunity = new HashSet<>();
        int maxCommunity = 0;
        int above10Community = 0;
        int above20Community = 0;
        
        HashSet<CommunityNode> numMaxInter = new HashSet<>();
        HashSet<CommunityNode> num10PerInter = new HashSet<>();
        HashSet<CommunityNode> num20PerInter = new HashSet<>();
        int maxInter = 0;
        int above10Inter = 0;
        int above20Inter = 0;
        
        HashSet<CommunityNode> numMaxTotal = new HashSet<>();
        HashSet<CommunityNode> num10PerTotal = new HashSet<>();
        HashSet<CommunityNode> num20PerTotal = new HashSet<>();
        int maxTotal = 0;
        int above10Total = 0;
        int above20Total = 0;
        
        HashSet<CommunityNode> numMaxTotalInter = new HashSet<>();
        HashSet<CommunityNode> numMaxTotalCommunity = new HashSet<>();
        HashSet<CommunityNode> numMaxInterCommunity = new HashSet<>();
        HashSet<CommunityNode> numMaxTotalInterCommunity = new HashSet<>();
        
        HashSet<CommunityNode> num10PerTotalInter = new HashSet<>();
        HashSet<CommunityNode> num10PerTotalCommunity = new HashSet<>();
        HashSet<CommunityNode> num10PerInterCommunity = new HashSet<>();
        HashSet<CommunityNode> num10PerTotalInterCommunity = new HashSet<>();
        
        HashSet<CommunityNode> num20PerTotalInter = new HashSet<>();
        HashSet<CommunityNode> num20PerTotalCommunity = new HashSet<>();
        HashSet<CommunityNode> num20PerInterCommunity = new HashSet<>();
        HashSet<CommunityNode> num20PerTotalInterCommunity = new HashSet<>();
        
        
        
        
        HashSet<CommunityNode> numTrivial = new HashSet<>();
        
        
        //unique community edges
        //double trivial = 9.5;
        int trivial = 20;
        int count = 0;
        double aboveTrivialCommunity = 0;
        for(CommunityNode node : n){
          if(maxCommunity == 0){
            maxCommunity = getCommunityDegrees(node);
            above10Community = (int)Math.floor((double)maxCommunity / 100.0 * 90.0);
            above20Community = (int)Math.floor((double)maxCommunity / 100.0 * 80.0);
            //aboveTrivialCommunity = (int)Math.floor((double)maxCommunity / 100.0 * (100.0 - trivial));
          }
          int degs = getCommunityDegrees(node);
          if(degs == maxCommunity){
            numMaxCommunity.add(node);
          }
          if(degs >= above10Community){
            num10PerCommunity.add(node);
          }
          if(degs >= above20Community){
            num20PerCommunity.add(node);
          }
          else if(count >= trivial){
            break;
          }
        }
        n.clear();
        n.addAll(c.getNodes());
        Collections.sort(n, new Comparator<CommunityNode>(){
          @Override
          public int compare(CommunityNode t, CommunityNode t1) {
            return getInterDegrees(t1) - getInterDegrees(t);
          }
          
        });
        for(CommunityNode node : n){
          if(maxInter == 0){
            maxInter = getInterDegrees(node);
            above10Inter = (int)Math.floor((double)maxInter / 100.0 * 90.0);
            above20Inter = (int)Math.floor((double)maxInter / 100.0 * 80.0);
          }
          int degs = getInterDegrees(node);
          if(degs == maxInter){
            numMaxInter.add(node);
          }
          if(degs >= above10Inter){
            num10PerInter.add(node);
          }
          if(degs >= above20Inter){
            num20PerInter.add(node);
          }
          else{
            break;
          }
        }
        
        n.clear();
        n.addAll(c.getNodes());
        Collections.sort(n, new Comparator<CommunityNode>(){
          @Override
          public int compare(CommunityNode t, CommunityNode t1) {
            return getTotalDegrees(t1) - getTotalDegrees(t);
          }
          
        });
        for(CommunityNode node : n){
          if(maxTotal == 0){
            maxTotal = getTotalDegrees(node);
            above10Total = (int)Math.floor((double)maxTotal / 100.0 * 90.0);
            above20Total = (int)Math.floor((double)maxTotal / 100.0 * 80.0);
          }
          int degs = getTotalDegrees(node);
          if(degs == maxTotal){
            numMaxTotal.add(node);
          }
          if(degs >= above10Total){
            num10PerTotal.add(node);
          }
          if(degs >= above20Total){
            num20PerTotal.add(node);
          }
          else{
            break;
          }
          if(count < trivial){
            numTrivial.add(node);
          }
          count = count + 1;
        }
        
        numMaxTotalInter.addAll(numMaxTotal);
        numMaxTotalInter.retainAll(numMaxInter);
        numMaxTotalCommunity.addAll(numMaxTotal);
        numMaxTotalCommunity.retainAll(numMaxCommunity);
        numMaxInterCommunity.addAll(numMaxCommunity);
        numMaxInterCommunity.retainAll(numMaxInter);
        numMaxTotalInterCommunity.addAll(numMaxCommunity);
        numMaxTotalInterCommunity.retainAll(numMaxTotal);
        numMaxTotalInterCommunity.retainAll(numMaxInter);
        
        
        num10PerTotalInter.addAll(num10PerTotal);
        num10PerTotalInter.retainAll(num10PerInter);
        num10PerTotalCommunity.addAll(num10PerTotal);
        num10PerTotalCommunity.retainAll(num10PerCommunity);
        num10PerInterCommunity.addAll(num10PerCommunity);
        num10PerInterCommunity.retainAll(num10PerInter);
        num10PerTotalInterCommunity.addAll(num10PerCommunity);
        num10PerTotalInterCommunity.retainAll(num10PerTotal);
        num10PerTotalInterCommunity.retainAll(num10PerInter);
        
        
        num20PerTotalInter.addAll(num20PerTotal);
        num20PerTotalInter.retainAll(num20PerInter);
        num20PerTotalCommunity.addAll(num20PerTotal);
        num20PerTotalCommunity.retainAll(num20PerCommunity);
        num20PerInterCommunity.addAll(num20PerCommunity);
        num20PerInterCommunity.retainAll(num20PerInter);
        num20PerTotalInterCommunity.addAll(num20PerCommunity);
        num20PerTotalInterCommunity.retainAll(num20PerTotal);
        num20PerTotalInterCommunity.retainAll(num20PerInter);
        
        String[] nodeIds = new String[num20PerTotalCommunity.size()];
        int i = 0;
        for(CommunityNode _n : num20PerTotalCommunity){
          nodeIds[i] = String.valueOf(_n.getId());
          i = i + 1;
        }
        
        /*Partitioner p = new CompletePartitioner();
        Splitter s = new SpecifiedSplitter(nodeIds);
        s.setPartitioner(p);
        List<TObjectCharHashMap<Node>> sets = s.getSets(c);
        for(TObjectCharHashMap<Node> set : sets){
          for(Node _n : set.keySet()){
            bw.write((set.get(_n) == '1' ? "" : "-") + _n.getId() + " ");
          }
          bw.write("\n");
        }*/
        System.out.println("Writing");
        bw.write(String.format("\"%s\",%d,%d,%d,%f,%d,%f,%d,%d,%f,%f,%d,%d,%f,%f,%f,%f,%f,%f,%f,%d,%f,%d,%f,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,",
              file, //file
              c.getNodes().size(), //vars
              c.getCommunities().size(),//ol_coms
              c.getMinCommunitySize(),//mincom
              c.getMeanCommunitySize(),//meancom
              c.getMaxCommunitySize(),//maxcom
              c.getSDCommunitySize(),//sdcom
              c.getMinInterEdges(),//mininter
              c.getMaxInterEdges(),//maxinter
              c.getMeanInterEdges(),//meaninter
              c.getSDInterEdges(),//sdinter
              c.getMinIntraEdges(),//minintra
              c.getMaxIntraEdges(),//maxintra
              c.getMeanIntraEdges(),//meanintra
              c.getSDIntraEdges(),//sdintra
              c.getEdgeRatio(),//edgeratio
              c.getMaxEdgeRatio(),//maxedgeratio
              c.getMinEdgeRatio(),//minegeratio
              c.getMeanEdgeRatio(),//meanedgeratio
              c.getSDEdgeRatio(),//sdedgeratio
              c.getEdges().size(), //unique_edges
              Q, //ol_q
              maxVar, //maxvar
              meanVar, //meanvar
              c.getTotalEdges(), //total_edges
              c.getWeight(),//weight
              maxCommunity,
              numMaxCommunity.size(),
              num10PerCommunity.size(),
              num20PerCommunity.size(),
              maxInter,
              numMaxInter.size(),
              num10PerInter.size(),
              num20PerInter.size(),
              maxTotal,
              numMaxTotal.size(),
              num10PerTotal.size(),
              num20PerTotal.size(),
              
              numMaxTotalInter.size(),
              numMaxTotalCommunity.size(),
              numMaxInterCommunity.size(),
              numMaxTotalInterCommunity.size(),
              
              num10PerTotalInter.size(),
              num10PerTotalCommunity.size(),
              num10PerInterCommunity.size(),
              num10PerTotalInterCommunity.size(),
              
              num20PerTotalInter.size(),
              num20PerTotalCommunity.size(),
              num20PerInterCommunity.size(),
              num20PerTotalInterCommunity.size()
              ));
        
        
              
      CalculateNew.run(graphs, bw);
      
      metric = null;
      factory = null;
      c = null;
      UniqueClauses uc = new UniqueClauses(bw, false);
      uc.run(f);
      bw.flush();
      if(true)  
        return;
      Runtime run = Runtime.getRuntime();
        long startMTime = System.currentTimeMillis();
      Process minipure = run.exec(String.format("%s %s", minisat, file));
      try {
        synchronized(minipure){
          minipure.wait(timeout);
        }
        try{
          minipure.destroy();
        }
        catch(Exception e){
          
        }
        long endMTime = System.currentTimeMillis();
        bw.write(String.format("%d,%d,%d,%f,%d,%d,%d,%d,%d\n", endMTime-startMTime, minipure.exitValue(), maxVar, meanVar, maxClause, c.getTotalEdges(), c.getWeight(), generateTime, Qtime));
      } 
      catch (Exception ex) {
        long endMTime = System.currentTimeMillis();
        bw.write(String.format("%d,%d,%d,%f,%d,%d,%d,%d,%d\n", endMTime-startMTime, 30, maxVar, meanVar, maxClause, c.getTotalEdges(), c.getWeight(), generateTime, Qtime));
      }
      bw.flush();
      }
      catch(OutOfMemoryError e){

      }
    }
    
    public static String getUsage(){
        return "generateOne pathtofile pathtoresultsdir\n";
    }
    
    
    
    
   public static Options options(){
    Options options = new Options();
    ValidatedOption o;
    
    
    o = new ValidatedOption("f","file",true, "The file (.cnf)");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes, new String[]{"read"}));
    options.addOption(o);
    
    o = new ValidatedOption("u","url",true, "A file URL (.cnf)");
    options.addOption(o);
    
    o = new ValidatedOption("c", "community", true,"The community detection algorithm");
    o.setDefault("ol");
    o.addRule(new ListValidationRule(CommunityMetricFactory.getInstance().getNames(), CommunityMetricFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("m","format",true, "The format of the file, and desired graph representation");
    o.addRule(new FormatValidationRule());
    o.setDefault("auto");
    options.addOption(o);
    
    o = new ValidatedOption("d", "directory", true, "The output directory");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes));
    o.addRule(new FileValidationRule(FileValidationRule.IsDirectory.yes, new String[]{"write"}));
    options.addOption(o);
    
    return options;
  }
   static File out;
    public static void main(String[] args) throws IOException, InterruptedException, ParseException{
      if(args.length == 0){
        args = new String[]{
          "-f","/media/zacknewsham/SAT/sat2013/random/sc13-benchmarks-random/unsat/unsat-unif-k6/unif-k6-r43.37-v71-c3079-S8370820542459007665.cnf",
          "-d","/home/zacknewsham/test"
        };
      }
      if(args.length < 2){
        System.out.println(getUsage());
        return;
      }
      
      CommandLineParser clp = new GnuParser();
      Options o = options();
      CommandLine cl = new CommandLine(clp.parse(o, args),o);

      if(!ValidatedCommandLine.validateCommandLine(o, cl.getCommandLine())){
        System.err.println(ValidatedCommandLine.getError());
        return;
      }
      
      File in = new File(cl.getOptionValue("f"));
      out = new File(cl.getOptionValue("d") + "/" + in.getName());
      
      /*while(out.exists()){
        out = new File(args[1] + "/" + String.valueOf(Math.random())+".cnf");
      }*/
      run(cl.getOptionValue("f"), cl.getOptionValue("m"), cl.getOptionValue("c"));
      System.out.println("closing");
      if(bw != null){
        bw.close();
      }
    }
}
