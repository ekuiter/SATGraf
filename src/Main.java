
import java.net.URISyntaxException;
import visual.community.CommunityGrapher;
import visual.evolution.EvolutionGrapher;
import visual.evolution2.Evolution2Grapher;
import visual.implication.ImplicationGrapher;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zacknewsham
 */
public class Main {
  public static void main(String[] args) throws URISyntaxException{
    String[] newargs;
    if(args.length == 0){
      System.out.println("Too few options. Please use:");
      System.out.print(usage());
      return;
    }
    else{
      newargs = new String[args.length - 1];
      for(int i = 1; i < args.length; i++){
        newargs[i - 1] = args[i];
      }
    }
    if(args[0].equals("com")){
      CommunityGrapher.main(newargs);
    }
    else if(args[0].equals("imp")){
      ImplicationGrapher.main(newargs);
    }
    else if(args[0].equals("evo")){
      EvolutionGrapher.main(newargs);
    }
    else if(args[0].equals("evo2")){
      Evolution2Grapher.main(newargs);
    }
  }
  
  public static String usage(){
    StringBuilder builder = new StringBuilder();
    
    builder.append("com ").append(CommunityGrapher.usage()).append("\n").append(CommunityGrapher.help()).append("\n\n");
    builder.append("imp ").append(ImplicationGrapher.usage()).append("\n").append(ImplicationGrapher.help()).append("\n\n");
    builder.append("evo ").append(EvolutionGrapher.usage()).append("\n").append(EvolutionGrapher.help()).append("\n\n");
    builder.append("evo2 ").append(Evolution2Grapher.usage()).append("\n").append(Evolution2Grapher.help()).append("\n\n");
    
    return builder.toString();
  }
}
