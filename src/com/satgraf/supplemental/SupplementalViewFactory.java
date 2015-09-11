/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.supplemental;

import com.satlib.GenericFactory;

/**
 *
 * @author zacknewsham
 */
public class SupplementalViewFactory extends GenericFactory<SupplementalView>{
  private static SupplementalViewFactory singleton = new SupplementalViewFactory();
  public static SupplementalViewFactory getInstance(){
    return singleton;
  }
  private SupplementalViewFactory(){}
}
