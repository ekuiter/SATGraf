package com.satgraf.data;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zacknewsham
 */
public class UniqueClauses {
  private boolean printFile = true;
  private static class UniqueClause{
    private HashSet<Integer> literals;
    private UniqueClause(HashSet<Integer> literals){
      this.literals = literals;
    }
    private UniqueClause(String clause){
      String[] vals = clause.split(" ");
      literals = new HashSet<>();
      for(String val : vals){
        if(!val.equals("0") && !val.equals("")){
          literals.add(Integer.valueOf(val));
        }
      }
    }
    private int size(){
      return literals.size();
    }
    
    @Override
    public boolean equals(Object o){
      if(!(o instanceof UniqueClause)){
        return false;
      }
      UniqueClause that = (UniqueClause)o;
      if(this.literals.size() != that.literals.size()){
        return false;
      }
      for(Integer this_literal : this.literals){
        if(!that.literals.contains(-this_literal) && !that.literals.contains(this_literal)){
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      int total = 0;
      for(Integer literal : literals){
        total += Math.abs(literal);
      }
      return total;
    }
  }
  
  private BufferedWriter writer;
  
  public UniqueClauses(BufferedWriter writer, boolean printfile){
    this.writer = writer;
    this.printFile = printfile;
  }
  public UniqueClauses(BufferedWriter writer){
    this(writer, true);
  }
  public UniqueClauses(File out) throws FileNotFoundException, IOException{
    this(out, true);
  }
  public UniqueClauses(File out, boolean printfile) throws FileNotFoundException, IOException{
    this.printFile = printfile;
    writer = new BufferedWriter(new FileWriter(out));
    if(!out.exists()){
      writer.write("file,total_clauses,var_unique_clauses,max_clause,mean_clause,max_reused,min_reused,mean_reused\n");
    }
  }
  
  public void runAll(File list) throws FileNotFoundException, IOException{
    BufferedReader r = new BufferedReader(new FileReader(list));
    boolean first = true;
    String line;
    while((line = r.readLine()) != null){
      if(first){
        first = false;
        continue;
      }
      line=line.split(",")[0];
      line=line.replace("\"", "");
      run(new File(line));
    }
    r.close();
    writer.close();
  }
  
  public void run(File in) throws FileNotFoundException, IOException{
    int total_clauses = 0;
    int total_clause_length = 0;
    int largest_clause = 0;
    
    BufferedReader r = new BufferedReader(new FileReader(in));
    HashMap<UniqueClause, Integer> clauses = new HashMap<>(); 
    String line;
    while((line = r.readLine()) != null){
      if(line.length() == 0 || line.charAt(0) == 'c' || line.charAt(0) == 'p'){
      }
      else{
        UniqueClause c = new UniqueClause(line);
        total_clauses++;
        total_clause_length += c.size();
        if(clauses.containsKey(c)){
          clauses.put(c, clauses.get(c) + 1);
        }
        else{
          clauses.put(c, 0);
        }
        if(c.size() > largest_clause){
          largest_clause = c.size();
        }
      }
    }
    int least_reused = Integer.MAX_VALUE;
    int most_reused = 0;
    int total_reused = 0;
    for(UniqueClause c : clauses.keySet()){
      int c_use = clauses.get(c);
      if(least_reused > c_use){
        least_reused = c_use;
      }
      if(c_use > most_reused){
        most_reused = c_use;
      }
      total_reused += c_use;
    }
    if(printFile){
      writer.write(String.format("%s,%d,%d,%d,%f,%d,%d,%f\n", in.getAbsolutePath(), total_clauses, clauses.size(), largest_clause, (double)total_clause_length/(double)total_clauses, most_reused, least_reused, (double)total_reused/(double)clauses.size()));
    }
    else{
      writer.write(String.format("%d,%d,%d,%f,%d,%d,%f\n", total_clauses, clauses.size(), largest_clause, (double)total_clause_length/(double)total_clauses, most_reused, least_reused, (double)total_reused/(double)clauses.size()));
    }
    writer.flush();
  }
  public static void main(String[] args) throws IOException{
    if(args.length == 0){
      args = new String[]{
        "/home/zacknewsham/Documents/University/Thesis/filelist.txt",
        "/home/zacknewsham/Documents/University/Thesis/simp.clauses.csv"
      };
    }
    
    UniqueClauses c = new UniqueClauses(new File(args[1]));
    c.runAll(new File(args[0]));
    
  }
}
