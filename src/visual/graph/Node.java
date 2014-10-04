/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author zacknewsham
 * @param <T>
 */
public class Node<T extends Edge> implements HasGraphPosition{
  public enum NodeAssignmentState { ASSIGNED_FALSE, ASSIGNED_TRUE, UNASSIGNED }
  public enum NodeState { SHOW, HIDE }
	
  private final HashSet<String> groups = new HashSet<String>();
  //private final ArrayList<Node> conIndexes = new ArrayList<>();
  private final ArrayList<T> connections = new ArrayList<T>();
  public static final NameComparator NAME_COMPARATOR =new NameComparator();
  private int id;
  private String name;
  private int set = 0; //1 = decision, 2 = implication
  private NodeAssignmentState assignmentState;
  private NodeState state;
  private List<NodeAssignmentState> assignmentStateHistory = null;
  
  public Node(int id, String name){
    this(id, name, false, false);
  }
  public Node(int id, String name, boolean is_head, boolean is_tail){
    this.id = id;
    this.name = name;
    state = NodeState.SHOW;
    assignmentState = NodeAssignmentState.UNASSIGNED;
    assignmentStateHistory = new ArrayList<NodeAssignmentState>();
    assignmentStateHistory.add(assignmentState);
    /*if(this.name == null){
      return;
    }*/
  }

  public String toJson(){
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"id\":");
    json.append(this.getId());
    json.append(",\"name\":\"");
    json.append(this.getName());
    json.append("\",\"groups\":[");
    for(String group : groups){
      json.append("{\"group\":\"");
      json.append(group);
      json.append("\"}");
    }
    json.append("]}");
    return json.toString();
  }
  public int hashCode(){
    return this.id;
  }
  public boolean equals(Object o1) {
    Node o = (Node) o1;
    return this.id == o.id;
  }
  
  public String getName(){
    return this.name == null ? String.valueOf(id) : this.name;
  }
  
  @Override
  public String toString(){
    return this.name == null ? String.format("noname (%d)", id) : this.name;
  }
  public Iterator<T> getEdges(){
    return this.connections.iterator();
  }
  public T getEdge(Node n){
    Iterator<T> edges = getEdges();
    while(edges.hasNext()){
      T next = edges.next();
      if((next.getStart() == this && next.getEnd() == n) || (next.getStart() == n && next.getEnd() == this)){
        return next;
      }
    }
    return null;
    /*int index = conIndexes.indexOf(n);
    if (index == -1){
      return null;
    }
    return connections.get(index);*/
  }
  public void addEdge(T e){
    //if(!connections.containsValue(e)){
      //conIndexes.add(e.getStart() == this ? e.getEnd() : e.getStart());
      connections.add(e);
    //}
  }
  
  public Iterator<T> getConnections(){
    return this.connections.iterator();
  }
  
  public int getId(){
    return this.id;
  }

  @Override
  public int getX(GraphViewer graph) {
    return graph.getX(this);
  }

  @Override
  public int getY(GraphViewer graph) {
    return graph.getY(this);
  }
  public Color getFillColor(GraphViewer graph){
    return graph.getFillColor(this);
  }
  public Color getColor(GraphViewer graph){
    return graph.getColor(this);
  }
  public void addGroup(String group){
    groups.add(group);
  }
  public Iterator<String> getGroups(){
    return groups.iterator();
  }

  public boolean inGroup(String set) {
    return groups.contains(set);
  }

  public void setName(String name) {
    this.name = name;
  }
  public boolean getValue(){
    return false;
  }
  public boolean isSet(){
    return false;
  }

  public Collection<T> getEdgesList() {
    return connections;
  }
  public void removeEdge(T e){
    connections.remove(e);
    //conIndexes.remove(e.getStart() == this ? e.getEnd() : e.getStart());
  }
  public static class XComparator implements Comparator <Node>{
    private GraphViewer graph;
    public XComparator(GraphViewer graph){
      this.graph = graph;
    }
    @Override
    public int compare(Node t, Node t1) {
      return graph.getX(t) - graph.getX(t1);
    }
  }
  
  public static class YComparator implements Comparator <Node>{
    private GraphViewer graph;
    public YComparator(GraphViewer graph){
      this.graph = graph;
    }
    @Override
    public int compare(Node t, Node t1) {
      return graph.getY(t) - graph.getY(t1);
    }
  }
  private static class NameComparator implements Comparator<Node>{
    @Override
    public int compare(Node o1, Node o2) {
      AlphanumComparator comp = new AlphanumComparator();
      return comp.compare(o1.getName(), o2.getName());
    }
  }
  
  public void setState(NodeState state) {
	  this.state = state;
  }
  
  public NodeState getState() {
	  return this.state;
  }
  
  public boolean isVisible() {
	  return this.state == NodeState.SHOW;
  }
  
  public NodeAssignmentState getAssignmentState() {
	  return this.assignmentState;
  }
  
  public boolean isAssigned() {
	  return this.assignmentState != NodeAssignmentState.UNASSIGNED;
  }
  
  public void setAssignmentState(NodeAssignmentState state) {
	  this.assignmentState = state;
	  assignmentStateHistory.add(state);
  }
  
  public void revertToPreviousAssignmentState() {
	  int lastElement = assignmentStateHistory.size()-1;
	  assignmentStateHistory.remove(lastElement);
	  this.assignmentState = assignmentStateHistory.get(lastElement-1);
  }
}
