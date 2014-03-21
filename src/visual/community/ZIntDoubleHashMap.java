/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

/**
 *
 * @author zacknewsham
 */
public class ZIntDoubleHashMap {
  private double[] data;
  public ZIntDoubleHashMap(int size) {
    data = new double[size];
  }
  
  public boolean containsKey(int key){
    return data[key] != 0.0;
  }
  
  public void put(int key, double value){
    data[key] = value;
  }
  
  public double get(int key){
    return data[key];
  }
  
  public double remove(int key){
    double ret  = data[key];
    data[key] = 0.0;
    return ret;
  }
}
