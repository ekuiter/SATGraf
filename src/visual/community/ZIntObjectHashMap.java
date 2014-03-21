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
class ZIntObjectHashMap<T> {
  private Object[] data;
  public ZIntObjectHashMap(int size) {
    data = new Object[size];
  }
  
  public boolean containsKey(int key){
    return data[key] != null;
  }
  
  public void put(int key, T value){
    data[key] = value;
  }
  
  public T get(int key){
    return (T)data[key];
  }
  
  public T remove(int key){
    T ret  = (T)data[key];
    data[key] = null;
    return ret;
  }
}
