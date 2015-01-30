package com.satgraf.community.placer.FMMM;

import java.util.List;

import javafx.geometry.Point2D;

public class QuadTree {
	
	QuadTreeNode root_ptr = null;
	QuadTreeNode act_ptr = null;

	public QuadTree() {
	}
	
	public void delete_tree(QuadTreeNode node_ptr) {
		if (node_ptr != null) {
			if (node_ptr.get_child_lt_ptr() != null) {
				delete_tree(node_ptr.get_child_lt_ptr());
			}
			if (node_ptr.get_child_rt_ptr() != null) {
				delete_tree(node_ptr.get_child_rt_ptr());
			}
			if (node_ptr.get_child_lb_ptr() != null) {
				delete_tree(node_ptr.get_child_lb_ptr());
			}
			if (node_ptr.get_child_rb_ptr() != null) {
				delete_tree(node_ptr.get_child_rb_ptr());
			}

			if (node_ptr == root_ptr)
				root_ptr = null;
			
			node_ptr = null;
		}
	}
	
	public int delete_tree_and_count_nodes(QuadTreeNode node_ptr) {
		int nodecounter = 0;
		
		if (node_ptr != null) {
			nodecounter++;
			
			if (node_ptr.get_child_lt_ptr() != null) {
				nodecounter += delete_tree_and_count_nodes(node_ptr.get_child_lt_ptr());
			}
			if (node_ptr.get_child_rt_ptr() != null) {
				nodecounter += delete_tree_and_count_nodes(node_ptr.get_child_rt_ptr());
			}
			if (node_ptr.get_child_lb_ptr() != null) {
				nodecounter += delete_tree_and_count_nodes(node_ptr.get_child_lb_ptr());
			}
			if (node_ptr.get_child_rb_ptr() != null) {
				nodecounter += delete_tree_and_count_nodes(node_ptr.get_child_rb_ptr());
			}
			
			if (node_ptr == root_ptr)
				root_ptr = null;
			
			node_ptr = null;
		}
		
		return nodecounter;
	}
	
	public void init_tree() {
		root_ptr = new QuadTreeNode();
		act_ptr = root_ptr;
	}
	
	public void start_at_root() {
		act_ptr = root_ptr;
	}
	
	public void go_to_father() {
		if (act_ptr.get_father_ptr() != null)
			act_ptr = act_ptr.get_father_ptr();
		else
			System.out.println("Error QuadTree: No father Node exists.");
	}
	
	public void go_to_lt_child() {
		act_ptr = act_ptr.get_child_lt_ptr();
	}
	
	public void go_to_rt_child() {
		act_ptr = act_ptr.get_child_rt_ptr();
	}
	
	public void go_to_lb_child() {
		act_ptr = act_ptr.get_child_lb_ptr();
	}
	
	public void go_to_rb_child() {
		act_ptr = act_ptr.get_child_rb_ptr();
	}
	
	public QuadTreeNode create_new_lt_child() {
		QuadTreeNode new_ptr = new QuadTreeNode();
		
		Point2D old_Sm_dlc = act_ptr.get_Sm_downleftcorner();
		Point2D new_Sm_dlc = new Point2D(old_Sm_dlc.getX(), old_Sm_dlc.getY() + act_ptr.get_Sm_boxlength()/2);
		
		new_ptr.set_Sm_level(act_ptr.get_Sm_level() + 1);
		new_ptr.set_Sm_downleftcorner(new_Sm_dlc);
		new_ptr.set_Sm_boxlength(act_ptr.get_Sm_boxlength()/2);
		new_ptr.set_father_ptr(act_ptr);
		act_ptr.set_child_lt_ptr(new_ptr);
		
		return new_ptr;
	}
	
	public void create_new_lt_child(List<ParticleInfo> L_x_ptr, List<ParticleInfo> L_y_ptr) {
		QuadTreeNode new_ptr = create_new_lt_child();
		new_ptr.set_x_List_ptr(L_x_ptr);
		new_ptr.set_y_List_ptr(L_y_ptr);
	}
	
	public QuadTreeNode create_new_rt_child() {
		QuadTreeNode new_ptr = new QuadTreeNode();
		
		Point2D old_Sm_dlc = act_ptr.get_Sm_downleftcorner();
		Point2D new_Sm_dlc = new Point2D(old_Sm_dlc.getX() + act_ptr.get_Sm_boxlength()/2, old_Sm_dlc.getY() + act_ptr.get_Sm_boxlength()/2);
		
		new_ptr.set_Sm_level(act_ptr.get_Sm_level() + 1);
		new_ptr.set_Sm_downleftcorner(new_Sm_dlc);
		new_ptr.set_Sm_boxlength(act_ptr.get_Sm_boxlength()/2);
		new_ptr.set_father_ptr(act_ptr);
		act_ptr.set_child_rt_ptr(new_ptr);
		
		return new_ptr;
	}
	
	public void create_new_rt_child(List<ParticleInfo> L_x_ptr, List<ParticleInfo> L_y_ptr) {
		QuadTreeNode new_ptr = create_new_rt_child();
		new_ptr.set_x_List_ptr(L_x_ptr);
		new_ptr.set_y_List_ptr(L_y_ptr);
	}
	
	public QuadTreeNode create_new_lb_child() {
		QuadTreeNode new_ptr = new QuadTreeNode();
		
		Point2D old_Sm_dlc = act_ptr.get_Sm_downleftcorner();
		Point2D new_Sm_dlc = new Point2D(old_Sm_dlc.getX(), old_Sm_dlc.getY());
		
		new_ptr.set_Sm_level(act_ptr.get_Sm_level() + 1);
		new_ptr.set_Sm_downleftcorner(new_Sm_dlc);
		new_ptr.set_Sm_boxlength(act_ptr.get_Sm_boxlength()/2);
		new_ptr.set_father_ptr(act_ptr);
		act_ptr.set_child_lb_ptr(new_ptr);
		
		return new_ptr;
	}
	
	public void create_new_lb_child(List<ParticleInfo> L_x_ptr, List<ParticleInfo> L_y_ptr) {
		QuadTreeNode new_ptr = create_new_lb_child();
		new_ptr.set_x_List_ptr(L_x_ptr);
		new_ptr.set_y_List_ptr(L_y_ptr);
	}
	
	public QuadTreeNode create_new_rb_child() {
		QuadTreeNode new_ptr = new QuadTreeNode();
		
		Point2D old_Sm_dlc = act_ptr.get_Sm_downleftcorner();
		Point2D new_Sm_dlc = new Point2D(old_Sm_dlc.getX() + act_ptr.get_Sm_boxlength()/2, old_Sm_dlc.getY());
		
		new_ptr.set_Sm_level(act_ptr.get_Sm_level() + 1);
		new_ptr.set_Sm_downleftcorner(new_Sm_dlc);
		new_ptr.set_Sm_boxlength(act_ptr.get_Sm_boxlength()/2);
		new_ptr.set_father_ptr(act_ptr);
		act_ptr.set_child_rb_ptr(new_ptr);
		
		return new_ptr;
	}
	
	public void create_new_rb_child(List<ParticleInfo> L_x_ptr, List<ParticleInfo> L_y_ptr) {
		QuadTreeNode new_ptr = create_new_rb_child();
		new_ptr.set_x_List_ptr(L_x_ptr);
		new_ptr.set_y_List_ptr(L_y_ptr);
	}
	
	public QuadTreeNode get_act_ptr() {
		return act_ptr;
	}
	
	public QuadTreeNode get_root_ptr() {
		return root_ptr;
	}
	
	public void set_root_ptr(QuadTreeNode r_ptr) {
		root_ptr = r_ptr;
	}
	
	public void set_act_ptr(QuadTreeNode a_ptr) {
		act_ptr = a_ptr;
	}
	
	public void set_root_node(QuadTreeNode r) {
		set_root_ptr(r);
	}
}
