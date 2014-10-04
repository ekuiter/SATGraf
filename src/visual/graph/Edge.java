/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author zacknewsham
 */
public class Edge <T extends Node> implements HasGraphPosition, Comparable<Edge>{
  public static final int REAL = 0xf0000000;
  public static final int DUMMY = 0x2;
  
  public enum EdgeState { SHOW, HIDE }
  
  protected T a;
  protected T b;
  public int id;
  private EdgeState assignmentState;
  private EdgeState state;
  private List<EdgeState> assignmentStateHistory = null;
  
  private void initState() {
	  this.state = EdgeState.SHOW;
	  this.assignmentState = EdgeState.SHOW;
	  assignmentStateHistory = new ArrayList<EdgeState>();
	  assignmentStateHistory.add(assignmentState);
  }
  
  protected Edge() {
	  initState();
  }
  
  public Edge(T a, T b){
	initState();
	  
    if(a.getId() < b.getId()){
      this.a = a;
      this.b = b;
    }
    else{
      this.b = a;
      this.a = b;
    }
  }
  public T getStart(){
    return a;
  }
  public T getEnd(){
    return b;
  }
  
  
  public String toJson(){
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"id\":").append(this.getId());
    json.append(",\"start\":").append(this.getStart().getId());
    json.append(",\"end\":").append(this.getEnd().getId());
    json.append("}");
    return json.toString();
  }
  
  public int getId(){
    if(id == 0){
      int _id = (this.a.getId() & 0xffff) << 16;
      _id = _id | (this.b.getId() & 0xffff);
      id = new Integer(_id);
    }
    
    return id;
  }
  public int hashCode(){
    if(this.a != null && this.b != null){
      return this.a.hashCode() + this.b.hashCode();
    }
    else{
      return 0;
    }
  }
  public boolean equals(Object o1) {
    Edge o = (Edge) o1;
    return (this.a == o.a && this.b == o.b) || (this.a == o.b && this.b == o.a);
  }
  
  @Override
  public String toString(){
    if(a != null && b != null){
      return String.format("%s -> %s", a.toString(), b.toString());
    }
    else{
      return "";
    }
  }
  public int getType(){
    return REAL;
  }
  
  /*public void setType(int type) {
	  //this.type = type;
  }*/
  
  public T getLeft(GraphViewer graph){
   if(a == null){
     return b;
   } 
   else if(b == null){
     return a;
   }
   else if(graph.getX(a) < graph.getX(b)){
     return a;
   }
   else{
     return b;
   }
  }
  public T getTop(GraphViewer graph){
   if(a == null){
     return b;
   } 
   else if(b == null){
     return a;
   }
   else if(graph.getY(a) < graph.getY(b)){
     return a;
   }
   else{
     return b;
   }
  }

  @Override
  public int getX(GraphViewer graph) {
    return graph.getX(getLeft(graph));
  }

  @Override
  public int getY(GraphViewer graph) {
    return graph.getY(getTop(graph));
  }

  @Override
  public int compareTo(Edge o) {
    return this.getId() - o.getId();
  }
  public Color getColor(GraphViewer graph) {
    return graph.getColor(this);
  }
  
  public T getOpposite(T node) {
	  if (node == a)
		  return b;
	  else if (node == b)
		  return a;
	  return null;
  }
  
  public static class XComparator implements Comparator<Edge>{
    private final GraphViewer graph;
    public XComparator(GraphViewer graph){
      this.graph = graph;
    }
    @Override
    public int compare(Edge o1, Edge o2) {
      if(o1.getLeft(graph) == null){
        return 1;
      }
      else if(o2.getLeft(graph) == null){
        return -1;
      }
      else{
        return graph.getX(o1.getLeft(graph)) - graph.getX(o2.getLeft(graph));
      }
    }
  }
  
  public void setState(EdgeState state) {
	  this.state = state;
  }
  
  public EdgeState getState() {
	  return this.state;
  }
  
  public boolean isVisible() {
	  return this.state == EdgeState.SHOW;
  }
  
  public EdgeState getAssignmentState() {
	  return this.assignmentState;
  }
  
  public boolean isAssignmentVisible() {
	  return this.state == EdgeState.SHOW;
  }
  
  public void setAssignmentState(EdgeState state) {
	  this.assignmentState = state;
	  assignmentStateHistory.add(state);
  }
  
  public void revertToPreviousAssignmentState() {
	  int lastElement = assignmentStateHistory.size()-1;
	  assignmentStateHistory.remove(lastElement);
	  this.assignmentState = assignmentStateHistory.get(lastElement-1);
  }
}
