
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.evolution.UI.EvolutionGraphFrame;
import com.satgraf.evolution.UI.RandomQVTime;
import com.satgraf.evolution2.UI.Evolution2GraphFrame;
import com.satgraf.implication.UI.ImplicationGraphFrame;
import com.validatedcl.validation.Help;
import java.io.IOException;
import java.net.URISyntaxException;
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
public class Main {
  public static void main(String[] args) throws URISyntaxException, IOException, ParseException{
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
      CommunityGraphFrame.main(newargs);
    }
    else if(args[0].equals("imp")){
      ImplicationGraphFrame.main(newargs);
    }
    else if(args[0].equals("evo")){
      EvolutionGraphFrame.main(newargs);
    }
    else if(args[0].equals("evo2")){
      Evolution2GraphFrame.main(newargs);
    }
    else if(args[0].equals("random")){
      RandomQVTime.main(newargs);
    }
  }
  
  public static String usage(){
    String dashes = "--------------------------------";
    StringBuilder builder = new StringBuilder();
    builder.append("Usage: java -jar SatGraf.jar [com|imp|evo|evo2] <options>");
    builder.append(dashes).append("\ncom\n").append(Help.getHelp(CommunityGraphFrame.options())).append(dashes);
    builder.append(dashes).append("\nimp\n").append("Not currently supported").append(dashes);
    builder.append(dashes).append("\nevo\n").append(Help.getHelp(EvolutionGraphFrame.options())).append(dashes);
    builder.append(dashes).append("\nevo2\n").append(Help.getHelp(Evolution2GraphFrame.options())).append(dashes);
    /*builder.append("com ").append(CommunityGraphFrame.usage()).append("\n").append(CommunityGraphFrame.help()).append("\n\n");
    builder.append("imp ").append(ImplicationGraphFrame.usage()).append("\n").append(ImplicationGraphFrame.help()).append("\n\n");
    builder.append("evo ").append(EvolutionGraphFrame.usage()).append("\n").append(EvolutionGraphFrame.help()).append("\n\n");
    builder.append("evo2 ").append(Evolution2GraphFrame.usage()).append("\n").append(Evolution2GraphFrame.help()).append("\n\n");
    */
    return builder.toString();
  }
}
