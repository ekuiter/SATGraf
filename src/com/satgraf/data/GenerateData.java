package com.satgraf.data;

import com.satgraf.FormatValidationRule;
import static com.satgraf.data.CalculateEvolution.options;
import static com.satlib.ForceInit.forceInit;
import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.community.DimacsCommunityGraphFactory;
import com.satlib.community.DimacsLiteralCommunityGraphFactory;
import com.satlib.community.JSONCommunityGraphFactory;
import com.satlib.community.LouvianCommunityMetric;
import com.satlib.community.OLCommunityMetric;
import com.satlib.evolution.observers.QEvolutionObserver;
import com.satlib.evolution.observers.VSIDSSpacialLocalityEvolutionObserver;
import com.satlib.evolution.observers.VSIDSTemporalLocalityEvolutionObserver;
import com.validatedcl.validation.CommandLine;
import com.validatedcl.validation.ValidatedCommandLine;
import com.validatedcl.validation.ValidatedOption;
import com.validatedcl.validation.rules.FileValidationRule;
import com.validatedcl.validation.rules.ListValidationRule;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
public class GenerateData {
  static{
    forceInit(VSIDSTemporalLocalityEvolutionObserver.class);
    forceInit(QEvolutionObserver.class);
    forceInit(VSIDSSpacialLocalityEvolutionObserver.class);
    forceInit(DimacsCommunityGraphFactory.class);
    forceInit(DimacsLiteralCommunityGraphFactory.class);
    forceInit(LouvianCommunityMetric.class);
    forceInit(OLCommunityMetric.class);
    forceInit(CNMCommunityMetric.class);
    forceInit(JSONCommunityGraphFactory.class);
  }
    
  
   public static Options options(){
    Options options = new Options();
    ValidatedOption o;
    
    
    o = new ValidatedOption("f","file",true, "The file containing a list of .cnf files");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes, new String[]{"read"}));
    options.addOption(o);
    
    
    o = new ValidatedOption("c", "community", true,"The community detection algorithm");
    o.setDefault("ol");
    o.addRule(new ListValidationRule(CommunityMetricFactory.getInstance().getNames(), CommunityMetricFactory.getInstance().getDescriptions()));
    options.addOption(o);
    
    o = new ValidatedOption("m","format",true, "The format of the file, and desired graph representation");
    o.addRule(new FormatValidationRule());
    o.setDefault("auto");
    options.addOption(o);
    
    o = new ValidatedOption("o", "output", true, "The output CSV");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.neither, new String[]{"write"}));
    options.addOption(o);
    
    return options;
  }
    public static void main(String args[]) throws FileNotFoundException, IOException, InterruptedException, ParseException{
        if(args.length == 0){
            args = new String[]{
                "-f","/home/zacknewsham/obfuscated.files",
                "-o","bmc.csv"
            };
        }
        
    
        CommandLineParser clp = new GnuParser();
        Options o = options();
        CommandLine cl = new CommandLine(clp.parse(o, args),o);

        if(!ValidatedCommandLine.validateCommandLine(o, cl.getCommandLine())){
          System.err.println(ValidatedCommandLine.getError());
          return;
        }
        File f = new File(cl.getOptionValue("o"));
        boolean fexists = f.exists();
        GenerateOne.bw = new BufferedWriter(new FileWriter(f,true));
        if(!fexists){
            GenerateOne.bw.write("file,"
                    + "vars,"
                    + "ol_coms,"
                    + "mincom,"
                    + "meancom,"
                    + "maxcom,"
                    + "sdcom,"
                    + "mininter,"
                    + "maxinter,"
                    + "meaninter,"
                    + "sdinter,"
                    + "minintra,"
                    + "maxintra,"
                    + "meanintra,"
                    + "sdintra,"
                    + "edgeratio,"
                    + "maxedgeratio,"
                    + "minedgeratio,"
                    + "meanedgeratio,"
                    + "sdedgeratio,"
                    + "unique_edges,"
                    + "ol_q,"
                    + "maxvar,"
                    + "meanvar,"
                    + "total_edges,"
                    + "weight,"
                    + "max_community,"
                    + "num_max_community,"
                    + "num_top_10_community,"
                    + "num_top_20_community,"
                    + "max_inter,"
                    + "num_max_inter,"
                    + "num_top_10_inter,"
                    + "num_top_20_inter,"
                    + "max_total,"
                    + "num_max_total,"
                    + "num_top_10_total,"
                    + "num_top_20_total,"
                    
                    + "num_max_total_inter,"
                    + "num_max_total_community,"
                    + "num_max_inter_community,"
                    + "num_max_total_inter_community,"
                    
                    + "num_top_10_total_inter,"
                    + "num_top_10_total_community,"
                    + "num_top_10_inter_community,"
                    + "num_top_10_total_inter_community,"
                    
                    + "num_top_20_total_inter,"
                    + "num_top_20_total_community,"
                    + "num_top_20_inter_community,"
                    + "num_top_20_total_inter_community,"
                    
                    + "real_shortest_mean,"
                    + "real_shortest_min,"
                    + "real_shortest_max,"
                    + "real_shortest_count,"
                    
                    + "approx_shortest_mean,"
                    + "approx_shortest_min,"
                    + "approx_shortest_max,"
                    + "approx_shortest_count,"
                    + "approx_shortest_pairs_mean,"
                    
                    + "com_shortest_mean,"
                    + "com_shortest_min,"
                    + "com_shortest_max,"
                    + "com_shortest_count,"
                    
                    + "inter_shortest_mean,"
                    + "inter_shortest_min,"
                    + "inter_shortest_max,"
                    + "inter_shortest_count,"
                    
                    + "total_clauses,"
                    + "var_unique_clauses,"
                    + "max_clause,"
                    + "mean_clause,"
                    + "max_reused,"
                    + "min_reused,"
                    + "mean_reused"
                    + "\n");
        }
        BufferedReader br = new BufferedReader(new FileReader(new File(cl.getOptionValue("f"))));
        
        String line;
        while((line = br.readLine()) != null){
          System.out.println(line.split(",")[0].trim());
          try{
            GenerateOne.run(line.split(",")[0].trim(), cl.getOptionValue("m"), cl.getOptionValue("c"));
          }
          catch(OutOfMemoryError e){
            
          }
        }
        //bw.close();
    }
    public static String getUsage(){
        return "generate /path/to/minisat files.txt output.csv\n";
    }
}
