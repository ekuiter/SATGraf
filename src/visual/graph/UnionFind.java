/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 *
 * @author zacknewsham
 */
public class UnionFind{
  private static class Rank{
    int id = -1;
    int rank = 0;
    Rank(){
      id = -1;
    }
    Rank(int id, int rank){
      this.id = id;
      this.rank = rank;
    }
  }
  protected final TIntObjectHashMap<Rank> roots = new TIntObjectHashMap<Rank>();
  public void add(Node n){
    if(!roots.containsKey(n.getId())){
      roots.put(n.getId(), new Rank());
    }
  }
  private int _parent(int n_id){
    return roots.get(n_id).id;
  }
  
  public int find(int n_id){
    int parent = _parent(n_id);
    int set_id = n_id;
    while(parent != -1){
      set_id = parent;
      parent = _parent(parent);
    }
    /*parent = n_id;
    while(parent != -1){
      int tmp = _parent(parent);
      if(tmp != -1){
        roots.get(parent).id = set_id;
      }
      parent = tmp;
    }*/
    return set_id;
  }
  public int find(Node n){
    int n_id = n.getId();
    return find(n_id);
  }
  private int rank(int n_id){
    return roots.get(n_id).rank;
  }
  public void union(int a, int b){
    int parent_a = find(a);
    int parent_b = find(b);
    int dis_a = rank(parent_a);
    int dis_b = rank(parent_b);
    if(dis_a > dis_b){
      roots.get(parent_b).id = parent_a;
    }
    else if(dis_b > dis_a){
      roots.get(parent_a).id = parent_b;
    }
    else if(parent_a != parent_b){
      roots.get(parent_b).id = parent_a;
      roots.get(parent_a).rank++;
    }
  }
  public void union(Node a, Node b){
    union(a.getId(), b.getId());
  }
  
  public boolean connected(Node a, Node b){
    return find(a) == find(b);
  }
}