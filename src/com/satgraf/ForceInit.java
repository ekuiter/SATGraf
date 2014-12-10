/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf;

/**
 *
 * @author zacknewsham
 */
public class ForceInit {
  public static void forceInit(Class klass){
    try{
        Class.forName(klass.getName(), true, klass.getClassLoader());
    }
    catch(ClassNotFoundException e){}
  }
  
}
