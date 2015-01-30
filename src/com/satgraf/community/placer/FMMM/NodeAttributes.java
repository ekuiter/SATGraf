package com.satgraf.community.placer.FMMM;


import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

import com.satlib.graph.Node;

public class NodeAttributes {

	private Point2D position = new Point2D(0, 0);
	private double width = 0;
	private double height = 0;
	
	private Node v_lower_level = null; 								// the corresponding node in the lower level graph
	private Node v_higher_level = null; 							// the corresponding node in the higher level graph
	
	private int mass = 0; 											//the mass (= number of previously collapsed nodes) of this node
	private int type = 0; 											//1 = sun node (s_node); 2 = planet node (p_node) without a dedicate moon 3 = planet node with dedicated moons (pm_node);4 = moon node (m_node)
	private Node dedicated_sun_node = null; 						//the dedicates s_node of the solar system of this node
	private double dedicated_sun_distance = 0; 						//the distance to the dedicated sun node of the galaxy of this node
	private Node dedicated_pm_node = null; 							//if type == 4 the dedicated_pm_node is saved here
	private List<Double> lambda = new ArrayList<Double>(); 			//the factors lambda for scaling the length of this edge relative to the pass between v's sun and the sun of a neighbour solar system
	private List<Node> neighbour_s_node = new ArrayList<Node>(); 	//this is the list of the neighbour solar systems suns lambda[i] corresponds to neighbour_s_node[i]
	private List<Node> moon_List = new ArrayList<Node>(); 			//the list of all dedicated moon nodes (!= nil if type == 3)
	private boolean placed = false; 								//indicates weather an initial position has been assigned to this node or not
	private double angle_1 = 0; 									//describes the sector where nodes that are not adjacent to other
	private double angle_2 = 6.2831853; 							//solar systems have to be placed
	
	public NodeAttributes() {
	}
	
	public void set_NodeAttributes(double w, double h, Point2D pos, Node v_low, Node v_high) {
		width = w;
		height = h;
		position = pos;
		v_lower_level = v_low;
		v_higher_level = v_high;
	}
	
	public void set_position(Point2D pos) {
		position = pos;
	}
	
	public void set_width(double w) {
		width = w;
	}
	
	public void set_height(double h) {
		height = h;
	}
	
	public void set_x(double x) {
		position = new Point2D(x, position.getY());
	}
	
	public void set_y(double y) {
		position = new Point2D(position.getX(), y);
	}
	
	public Point2D get_position() {
		return position;
	}
	
	public double get_x() {
		return position.getX();
	}
	
	public double get_y() {
		return position.getY();
	}
	
	public double get_width() {
		return width;
	}
	
	public double get_height() {
		return height;
	}
	
	public void set_original_node(Node v) {
		v_lower_level = v;
	}
	
	public void set_copy_node(Node v) {
		v_higher_level = v;
	}
	
	public Node get_original_node() {
		return v_lower_level;
	}
	
	public Node get_copy_node() {
		return v_higher_level;
	}
	
	public void set_lower_level_node(Node v) {
		v_lower_level = v;
	}
	
	public void set_higher_level_node(Node v) {
		v_higher_level = v;
	}
	
	public Node get_lower_level_node() {
		return v_lower_level;
	}
	
	public Node get_higher_level_node() {
		return v_higher_level;
	}
	
	public void set_mass(int m) {
		mass = m;
	}
	
	public void set_type(int t) {
		type = t;
	}
	
	public void set_dedicated_sun_node(Node v) {
		dedicated_sun_node = v;
	}
	
	public void set_dedicated_sun_distance(double d) {
		dedicated_sun_distance = d;
	}
	
	public void set_dedicated_pm_node(Node v) {
		dedicated_pm_node = v;
	}
	
	public void place() {
		placed = true;
	}
	
	public void set_angle_1(double a) {
		angle_1 = a;
	}
	
	public void set_angle_2(double a) {
		angle_2 = a;
	}
	
	public int get_mass() {
		return mass;
	}
	
	public int get_type() {
		return type;
	}
	
	public Node get_dedicated_sun_node() {
		return dedicated_sun_node;
	}
	
	public double get_dedicated_sun_distance() {
		return dedicated_sun_distance;
	}
	
	public Node get_dedicated_pm_node() {
		return dedicated_pm_node;
	}
	
	public boolean is_placed() {
		return placed;
	}
	
	public double get_angle_1() {
		return angle_1;
	}
	
	public double get_angle_2() {
		return angle_2;
	}
	
	public List<Double> get_lambda_list() {
		return lambda;
	}
	
	public List<Node> get_neighbour_sun_node_list() {
		return neighbour_s_node;
	}
	
	public List<Node> get_dedicated_moon_node_list() {
		return moon_List;
	}
	
	public void init_mult_values() {
		type = 0;
		dedicated_sun_node = null;
		dedicated_sun_distance = 0;
		dedicated_pm_node = null;
		lambda.clear();
		neighbour_s_node.clear();
		moon_List.clear();
		placed = false;
		angle_1 = 0;
		angle_2 = 6.2831853;
	}
}
