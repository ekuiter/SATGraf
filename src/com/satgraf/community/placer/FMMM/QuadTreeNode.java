package com.satgraf.community.placer.FMMM;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

import com.satlib.graph.Node;

public class QuadTreeNode {
	
	int Sm_level = 0;							// level of the small cell
	Point2D Sm_downleftcorner = new Point2D(0, 0);	// coords of the down left corner of the small cell
	double Sm_boxlength = 0;					// length of the small cell
	List<ParticleInfo> L_x_ptr = null;		// points to the lists that contain each Particle of Graph with its x(y)coordinate in increasing order
	List<ParticleInfo> L_y_ptr = null;		// and a cross reference to the list_item in the list with the other coordinate
	int subtreeparticlenumber = 0;				// the number of particles in the subtree rooted at this node
	Complex Sm_center = new Complex(0, 0);		// center of the small cell
	Complex[] ME = null;						// Multipole Expansion terms
	Complex[] LE = null;						// Locale expansion terms
	List<Node> contained_nodes;			// List of nodes of Graph that are contained in this tree
	List<QuadTreeNode> I;					// the list of min. ill sep. nodes in DIM2
	List<QuadTreeNode> D1, D2;				// list of neighbouring(=D1) and not adjacent(=D2) leaves for direct force calculation in DIM2
	List<QuadTreeNode> M;					// list of nodes with multipole force contribution like in DIM2
	QuadTreeNode father_ptr = null;				// points to the father node
	QuadTreeNode child_lt_ptr = null;			// points to the left top child
	QuadTreeNode child_rt_prt = null;			// points to the right top child
	QuadTreeNode child_lb_ptr = null;			// points to the left bottom child
	QuadTreeNode child_rb_ptr = null;			// points to the right bottom child

	public QuadTreeNode() {
	}
	
	public void set_Sm_level(int l) {
		this.Sm_level = l;
	}
	
	public void set_Sm_downleftcorner(Point2D dlc) {
		this.Sm_downleftcorner = dlc;
	}
	
	public void set_Sm_boxlength(double l) {
		this.Sm_boxlength = l;
	}
	
	public void set_x_List_ptr(List<ParticleInfo> x_ptr) {
		this.L_x_ptr = x_ptr;
	}
	
	public void set_y_List_ptr(List<ParticleInfo> y_ptr) {
		this.L_y_ptr = y_ptr;
	}
	
	public void set_particlenumber_in_subtree(int p) {
		this.subtreeparticlenumber = p;
	}
	
	public void set_Sm_center(Complex c) {
		this.Sm_center = c;
	}
	
	public void set_contained_nodes(List<Node> L) {
		this.contained_nodes = L;
	}
	
	public void pushBack_contained_nodes(Node v) {
		this.contained_nodes.add(v);
	}
	
	public Node pop_contained_nodes() {
		return this.contained_nodes.remove(0);
	}
	
	public boolean contained_nodes_empty() {
		return this.contained_nodes.isEmpty();
	}
	
	public void set_I(List<QuadTreeNode> l) {
		this.I = l;
	}
	
	public void set_D1(List<QuadTreeNode> l) {
		this.D1 = l;
	}
	
	public void set_D2(List<QuadTreeNode> l) {
		this.D2 = l;
	}
	
	public void set_M(List<QuadTreeNode> l) {
		this.M = l;
	}
	
	public void set_locale_exp(Complex[] local, int precision) {
		LE = new Complex[precision+1];
		for (int i = 0; i <= precision; i++) {
			LE[i] = local[i];
		}
	}
	
	public void set_multipole_exp(Complex[] multi, int precision) {
		ME = new Complex[precision+1];
		for (int i = 0; i <= precision; i++) {
			ME[i] = multi[i];
		}
	}
	
	public void replace_multipole_exp(Complex[] multi, int precision) {
		for (int i = 0; i <= precision; i++) {
			ME[i] = multi[i];
		}
	}
	
	public void set_father_ptr(QuadTreeNode f) {
		this.father_ptr = f;
	}
	
	public void set_child_lt_ptr(QuadTreeNode c) {
		this.child_lt_ptr = c;
	}
	
	public void set_child_rt_ptr(QuadTreeNode c) {
		this.child_rt_prt = c;
	}
	
	public void set_child_lb_ptr(QuadTreeNode c) {
		this.child_lb_ptr = c;
	}
	
	public void set_child_rb_ptr(QuadTreeNode c) {
		this.child_rb_ptr = c;
	}
	
	public boolean is_root() {
		return this.father_ptr == null;
	}
	
	public boolean is_leaf() {
		return ((this.child_lt_ptr == null) && (this.child_rt_prt == null) && (this.child_lb_ptr == null) && (this.child_rb_ptr == null));
	}
	
	public boolean child_lt_exists() {
		return this.child_lt_ptr == null;
	}
	
	public boolean child_rt_exists() {
		return this.child_rt_prt == null;
	}
	
	public boolean child_lb_exists() {
		return this.child_lb_ptr == null;
	}
	
	public boolean child_rb_exists() {
		return this.child_rb_ptr == null;
	}
	
	public int get_Sm_level() {
		return this.Sm_level;
	}
	
	public Point2D get_Sm_downleftcorner() {
		return this.Sm_downleftcorner;
	}
	
	public double get_Sm_boxlength() {
		return this.Sm_boxlength;
	}
	
	public List<ParticleInfo> get_x_List_ptr() {
		return this.L_x_ptr;
	}
	
	public List<ParticleInfo> get_y_List_ptr() {
		return this.L_y_ptr;
	}
	
	public int get_particlenumber_in_subtree() {
		return this.subtreeparticlenumber;
	}
	
	public Complex get_Sm_center() {
		return this.Sm_center;
	}
	
	public Complex[] get_local_exp() {
		return this.LE;
	}
	
	public Complex[] get_multipole_exp() {
		return this.ME;
	}
	
	public List<Node> get_contained_nodes() {
		return this.contained_nodes;
	}
	
	public List<QuadTreeNode> get_I() {
		return this.I;
	}
	
	public List<QuadTreeNode> get_D1() {
		return this.D1;
	}
	
	public List<QuadTreeNode> get_D2() {
		return this.D2;
	}
	
	public List<QuadTreeNode> get_M() {
		return this.M;
	}
	
	public QuadTreeNode get_father_ptr() {
		return this.father_ptr;
	}
	
	public QuadTreeNode get_child_lt_ptr() {
		return this.child_lt_ptr;
	}
	
	public QuadTreeNode get_child_rt_ptr() {
		return this.child_rt_prt;
	}
	
	public QuadTreeNode get_child_lb_ptr() {
		return this.child_lb_ptr;
	}
	
	public QuadTreeNode get_child_rb_ptr() {
		return this.child_rb_ptr;
	}
}
