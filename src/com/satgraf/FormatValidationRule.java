/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf;

import com.satlib.graph.GraphFactoryFactory;
import com.validatedcl.validation.ProvidesHelp;
import com.validatedcl.validation.rules.ListValidationRule;
import com.validatedcl.validation.rules.ValidationRule;
import org.apache.commons.cli.Option;

/**
 *
 * @author zacknewsham
 */
public class FormatValidationRule extends ListValidationRule implements ValidationRule, ProvidesHelp{

  public FormatValidationRule() {
    super(GraphFactoryFactory.getInstance().getNames(),new String[0]);
  }

  @Override
  public String getHelp() {
    StringBuilder builder = new StringBuilder();
    builder.append("Must be one of:\n\t");
    for (String name : GraphFactoryFactory.getInstance().getNames()){
      builder.append(name).append(" - accepts: \n\t\t");
      for(String ext : GraphFactoryFactory.getInstance().getExtensions(name)){
        builder.append(".").append(ext).append(" - ").append(GraphFactoryFactory.getInstance().getDescription(name, ext)).append("\n\t\t");
      }
    }
    
    return builder.toString();
  }
  
}
