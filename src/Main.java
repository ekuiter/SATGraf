
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.evolution.UI.EvolutionGraphFrame;
import com.satgraf.implication.UI.ImplicationGraphFrame;
import com.satgraf.test.QAgainstTimeRandomBucketing;
import com.satgraf.test.QAgainstTimeRandomBucketingFixedCVR;
import com.satgraf.test.QAgainstTimeRandomBucketingNoQ;
import com.satgraf.test.QAgainstTimeRandomBucketingVarLength;
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
  public static void main(String[] args) throws URISyntaxException, IOException, ParseException, InstantiationException{
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
    else if(args[0].equals("q")){
      QAgainstTimeRandomBucketing.main(newargs);
    }
    else if(args[0].equals("qcvr")){
      QAgainstTimeRandomBucketingFixedCVR.main(newargs);
    }
    else if(args[0].equals("noq")){
      QAgainstTimeRandomBucketingNoQ.main(newargs);
    }
    else if(args[0].equals("rlen")){
      QAgainstTimeRandomBucketingVarLength.main(newargs);
    }
  }
  
  public static String usage(){
    String dashes = "--------------------------------";
    StringBuilder builder = new StringBuilder();
    builder.append("Usage: java -jar SatGraf.jar [com|imp|evo] <options>\n");
    builder.append(dashes).append("\ncom - View the static community representation of the formula.\n").append("\t".concat(Help.getHelp(CommunityGraphFrame.options()).replace("\n","\n\t"))).append("\n").append(dashes).append("\n");
    builder.append(dashes).append("\nimp - View the graph and manually set the values of nodes to see how they propagate.\n").append("\t".concat(Help.getHelp(ImplicationGraphFrame.options()).replace("\n","\n\t"))).append("\n").append(dashes).append("\n");
    builder.append(dashes).append("\nevo - View the evolution of the structure of the graph, with other evolution properties presented.\n").append("\t".concat(Help.getHelp(EvolutionGraphFrame.options()).replace("\n","\n\t"))).append("\n").append(dashes).append("\n");
    /*builder.append("com ").append(CommunityGraphFrame.usage()).append("\n").append(CommunityGraphFrame.help()).append("\n\n");
    builder.append("imp ").append(ImplicationGraphFrame.usage()).append("\n").append(ImplicationGraphFrame.help()).append("\n\n");
    builder.append("evo ").append(EvolutionGraphFrame.usage()).append("\n").append(EvolutionGraphFrame.help()).append("\n\n");
    builder.append("evo2 ").append(Evolution2GraphFrame.usage()).append("\n").append(Evolution2GraphFrame.help()).append("\n\n");
    */
    return builder.toString();
  }
}
