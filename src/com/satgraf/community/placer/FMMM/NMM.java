package com.satgraf.community.placer.FMMM;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javafx.geometry.Point2D;

import com.satgraf.community.placer.FMMM.ParticleInfo.ParticleInfoComparer;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;

// TODO: Make sure that all function definitions that I have changed are obeyed

public class NMM {
	
	private static final double MIN_BOX_LENGTH = 1e-300;

	private FruchtermanReingold ExactMethod; 	// Needed in case that using_NMM == false
	
	private int _tree_construction_way; 		// 1 = pathwise; 2 = subtreewise
	private int _find_small_cell; 				// 0 = iterative; 1 = Aluru
	private int _particles_in_leaves; 			// max. number of particles for leaves of the quadtree
	private int _precision; 					// precision for p-term multipole expansion
	
	private double boxlength; 					// length of the drawing box
	private Point2D down_left_corner; 			// down left corner of drawing box
	
	private int[] power_of_2; 					// holds the powers of 2 (for speed reasons to calculate the maximal boxindex (index is from 0 to max_power_of_2_index))
	private int max_power_of_2_index; 			// holds max. index for power_of_2 (= 30)
	private double[][] BK; 						// holds the binomial coefficients
	private HashMap<Node, Point2D> rep_forces; 			// stores the rep. forces of the last iteration (needed for error calculation)
	
	public NMM() {
	}
	
	public void calculate_repulsive_forces(Graph g, HashMap<Node, NodeAttributes> A, HashMap<Node, Point2D> F_rep) {
		calculate_repulsive_forces_by_NMM(g, A, F_rep);
	}
	
	public void make_initialisations(double bl, Point2D d_l_c, int p_i_l, int p, int t_c_w, int f_s_c) {
		particles_in_leaves(p_i_l);
		precision(p);
		tree_construction_way(t_c_w);
		find_sm_cell(f_s_c);
		down_left_corner = d_l_c;
		boxlength = bl;
		init_binko(2 * precision());
		init_power_of_2_array();
	}
	
	public void update_boxlength_and_cornercoordinate(double b_l, Point2D d_l_c) {
		boxlength = b_l;
		down_left_corner = d_l_c;
	}
	
	private void init_power_of_2_array() {
		int p = 1;
		max_power_of_2_index = 30;
		power_of_2 = new int[max_power_of_2_index + 1];
		
		for (int i = 0; i <= max_power_of_2_index; i++) {
			power_of_2[i] = p;
			p *= 2;
		}
	}
	
	private int power_of_two(int i) {
		if (i <= max_power_of_2_index) {
			return power_of_2[i];
		} else {
			return (int) Math.pow(2, i);
		}
	}
	
	private int maxboxindex(int level) {
		if (level < 0) {
			System.out.println("Failure NMM::maxboxindex : wrong level");
			return -1;
		} else {
			return power_of_two(level) - 1;
		}
	}
	
	private void calculate_repulsive_forces_by_NMM(Graph g, HashMap<Node, NodeAttributes> A, HashMap<Node, Point2D> F_rep) {
		QuadTree T = null;
		HashMap<Node, Point2D> F_direct = new HashMap<Node, Point2D>();
		HashMap<Node, Point2D> F_local_exp = new HashMap<Node, Point2D>();
		HashMap<Node, Point2D> F_multipole_exp = new HashMap<Node, Point2D>();
		List<QuadTreeNode> quad_tree_leaves = new ArrayList<QuadTreeNode>();
		ArrayList<Node> nodes = new ArrayList(g.getNodes());
		
		for (Node v : nodes) {
			F_direct.put(v, new Point2D(0, 0));
			F_local_exp.put(v, new Point2D(0, 0));
			F_multipole_exp.put(v, new Point2D(0, 0));
		}
		
//		if (tree_construction_way() == FMMMLayout.rtcPathByPath) {
//			build_up_red_quad_tree_path_by_path(g, A, T);
//		} else {
			build_up_red_quad_tree_subtree_by_subtree(g, A, T);
//		}
		
		form_multipole_expansions(A, T, quad_tree_leaves);
		calculate_local_expansions_and_WSPRLS(A, T.get_root_ptr());
		transform_local_exp_to_forces(A, quad_tree_leaves, F_local_exp);
		transform_multipole_exp_to_forces(A, quad_tree_leaves, F_direct);
		calculate_neighbourcell_forces(A, quad_tree_leaves, F_direct);
		add_rep_forces(g, F_direct, F_multipole_exp, F_local_exp, F_rep);
	}
	
	private void calculate_exact_repulsive_forces(Graph g, HashMap<Node, NodeAttributes> A, HashMap<Node, Point2D> F_rep) {
		ExactMethod.calculate_exact_repulsive_forces(g, A, F_rep);
	}
	
	private void build_up_red_quad_tree_path_by_path(Graph g, HashMap<Node, NodeAttributes> A, QuadTree T) {
		List<QuadTreeNode> act_leaf_List, new_leaf_List, help_ptr;
		List<ParticleInfo> act_x_List_copy, act_y_List_copy;
		QuadTreeNode act_node_ptr;
		
		build_up_root_node(g, A, T);
		
		act_leaf_List = new ArrayList<QuadTreeNode>();
		new_leaf_List = new ArrayList<QuadTreeNode>();
		act_x_List_copy = new ArrayList<ParticleInfo>();
		act_y_List_copy = new ArrayList<ParticleInfo>();
		act_leaf_List.add(T.get_root_ptr());
		
		while(!act_leaf_List.isEmpty()) {
			while (!act_leaf_List.isEmpty()) {
				act_node_ptr = act_leaf_List.remove(0);
				make_copy_and_init_Lists(act_node_ptr.get_x_List_ptr(), act_x_List_copy, act_node_ptr.get_y_List_ptr(), act_y_List_copy);
				T.set_act_ptr(act_node_ptr);
				decompose_subtreenode(T, act_x_List_copy, act_y_List_copy, new_leaf_List);
			}
			
			help_ptr = act_leaf_List;
			act_leaf_List = new_leaf_List;
			new_leaf_List = help_ptr;
		}
	}
	
	private void make_copy_and_init_Lists(List<ParticleInfo> L_x_orig, List<ParticleInfo> L_x_copy, List<ParticleInfo> L_y_orig, List<ParticleInfo> L_y_copy) {
		Iterator<ParticleInfo> origin_x_item, copy_x_item, origin_y_item, copy_y_item;
		ParticleInfo new_cross_ref_item;
		ParticleInfo P_x_orig, P_y_orig, P_x_copy, P_y_copy;
		
		L_x_copy.clear();
		L_y_copy.clear();
		
		origin_x_item = L_x_orig.iterator();
		while (origin_x_item.hasNext()) {
			// Reset values
			P_x_orig = origin_x_item.next();
			P_x_orig.set_subList_ptr(null);
			P_x_orig.set_copy_item(null);
			P_x_orig.unmark();
			P_x_orig.set_tmp_cross_ref_item(null);
			
			P_x_copy = P_x_orig;
			L_x_copy.add(P_x_copy);
			
			P_x_orig.set_copy_item(L_x_copy.get(L_x_copy.size()-1));
		}
		
		origin_y_item = L_y_orig.iterator();
		while (origin_y_item.hasNext()) {
			// Reset values
			P_y_orig = origin_y_item.next();
			P_y_orig.set_subList_ptr(null);
			P_y_orig.set_copy_item(null);
			P_y_orig.set_tmp_cross_ref_item(null);
			P_y_orig.unmark();
			
			P_y_copy = P_y_orig;
			if (P_y_orig.get_cross_ref_item() != null && P_y_orig.get_cross_ref_item() != null) {
				new_cross_ref_item = P_y_orig.get_cross_ref_item().get_copy_item();
			} else {
				new_cross_ref_item = null;
			}
			P_y_copy.set_cross_ref_item(new_cross_ref_item);
			L_y_copy.add(P_y_copy);
			P_x_copy = new_cross_ref_item != null ? new_cross_ref_item : null;
			P_x_copy.set_cross_ref_item(L_y_copy.get(L_y_copy.size()-1));
			
			P_y_orig.set_copy_item(L_y_copy.get(L_y_copy.size()-1));
		}
	}
	
	private void build_up_root_node(Graph g, HashMap<Node, NodeAttributes> A, QuadTree T) {
		T.init_tree();
		T.get_root_ptr().set_Sm_level(0);
		T.get_root_ptr().set_Sm_downleftcorner(down_left_corner);
		T.get_root_ptr().set_Sm_boxlength(boxlength);
		T.get_root_ptr().set_x_List_ptr(new ArrayList<ParticleInfo>());
		T.get_root_ptr().set_y_List_ptr(new ArrayList<ParticleInfo>());
		create_sorted_coordinate_Lists(g, A, T.get_root_ptr().get_x_List_ptr(), T.get_root_ptr().get_y_List_ptr());
	}
	
	private void create_sorted_coordinate_Lists(Graph g, HashMap<Node, NodeAttributes> A, List<ParticleInfo> L_x, List<ParticleInfo> L_y) {
		ParticleInfo P_x = new ParticleInfo();
		ParticleInfo P_y = new ParticleInfo();
		ArrayList<Node> nodes = new ArrayList<Node>(g.getNodes());
		
		for (Node v : nodes) {
			P_x.set_x_y_coord(A.get(v).get_x());
			P_y.set_x_y_coord(A.get(v).get_y());
			P_x.set_vertex(v);
			P_y.set_vertex(v);
			L_x.add(P_x);
			L_y.add(P_y);
			P_x.set_cross_ref_item(L_y.get(L_y.size()-1));
			P_y.set_cross_ref_item(L_x.get(L_x.size()-1));
		}
		
		ParticleInfoComparer comp = new ParticleInfoComparer();
		L_x.sort(comp);
		
		for (ParticleInfo x_item : L_x) {
			P_y = x_item.get_cross_ref_item();
			P_y.set_cross_ref_item(x_item);
		}
		
		L_y.sort(comp);
		
		for (ParticleInfo y_item : L_y) {
			P_x = y_item.get_cross_ref_item();
			P_x.set_cross_ref_item(y_item);
		}
	}
	
	private void decompose_subtreenode(QuadTree T, List<ParticleInfo> act_x_List_copy, List<ParticleInfo> act_y_List_copy, List<QuadTreeNode> new_leaf_List) {
		QuadTreeNode act_ptr = T.get_act_ptr();
		int act_particle_number = act_ptr.get_x_List_ptr().size();
		DoubleObject x_min = new DoubleObject(0), x_max = new DoubleObject(0), y_min = new DoubleObject(0), y_max = new DoubleObject(0);
		List<ParticleInfo> L_x_l_ptr = new ArrayList<ParticleInfo>(), L_x_r_ptr = new ArrayList<ParticleInfo>(), L_x_lb_ptr = new ArrayList<ParticleInfo>(), 
				L_x_rb_ptr = new ArrayList<ParticleInfo>(), L_x_lt_ptr = new ArrayList<ParticleInfo>(), L_x_rt_ptr = new ArrayList<ParticleInfo>();
		List<ParticleInfo> L_y_l_ptr = new ArrayList<ParticleInfo>(), L_y_r_ptr = new ArrayList<ParticleInfo>(), L_y_lb_ptr = new ArrayList<ParticleInfo>(), 
				L_y_rb_ptr = new ArrayList<ParticleInfo>(), L_y_lt_ptr = new ArrayList<ParticleInfo>(), L_y_rt_ptr = new ArrayList<ParticleInfo>();
		
		calculate_boundaries_of_act_node(T.get_act_ptr(), x_min, x_max, y_min, y_max);
		find_small_cell_iteratively(T.get_act_ptr(), x_min.getVal(), x_max.getVal(), y_min.getVal(), y_max.getVal());
		
		if ((act_particle_number > particles_in_leaves()) && ((x_max.getVal() - x_min.getVal() >= MIN_BOX_LENGTH) || (y_max.getVal() - y_min.getVal() >= MIN_BOX_LENGTH))) {
			split_in_x_direction(act_ptr, L_x_l_ptr, L_y_l_ptr, L_x_r_ptr, L_y_r_ptr);
			if ((L_x_r_ptr.isEmpty()) || (!L_x_l_ptr.isEmpty() && L_x_l_ptr.size() > L_x_r_ptr.size())) {
				split_in_y_direction(act_ptr, L_x_lb_ptr, L_y_lb_ptr, L_x_lt_ptr, L_y_lt_ptr);
				
				if ((L_x_lt_ptr.isEmpty()) || (!L_x_lb_ptr.isEmpty() && L_x_lb_ptr.size() > L_x_lt_ptr.size())) {
					T.create_new_lb_child(L_x_lb_ptr, L_y_lb_ptr);
					T.go_to_lb_child();
					decompose_subtreenode(T, act_x_List_copy, act_y_List_copy, new_leaf_List);
					T.go_to_father();
				} else {
					T.create_new_lt_child(L_x_lt_ptr, L_y_lt_ptr);
					T.go_to_lt_child();
					decompose_subtreenode(T, act_x_List_copy, act_y_List_copy, new_leaf_List);
					T.go_to_father();
				}
			} else {
				split_in_y_direction(act_ptr, L_x_rb_ptr, L_y_rb_ptr, L_x_rt_ptr, L_y_rt_ptr);
				
				if ((L_x_rt_ptr.isEmpty()) || (!L_x_rb_ptr.isEmpty() && L_x_rb_ptr.size() > L_x_rt_ptr.size())) {
					T.create_new_rb_child(L_x_rb_ptr, L_y_rb_ptr);
					T.go_to_rb_child();
					decompose_subtreenode(T, act_x_List_copy, act_y_List_copy, new_leaf_List);
					T.go_to_father();
				} else {
					T.create_new_rt_child(L_x_rt_ptr, L_y_rt_ptr);
					T.go_to_rt_child();
					decompose_subtreenode(T, act_x_List_copy, act_y_List_copy, new_leaf_List);
					T.go_to_father();
				}
			}
			
			if (!L_x_l_ptr.isEmpty() && L_x_lb_ptr.isEmpty() && L_x_lt_ptr.isEmpty() && !act_ptr.child_lb_exists() && !act_ptr.child_lt_exists()) {
				split_in_y_direction(act_ptr, L_x_l_ptr, L_x_lb_ptr, L_x_lt_ptr, L_y_l_ptr, L_y_lb_ptr, L_y_lt_ptr);
			} else if (!L_x_r_ptr.isEmpty() && L_x_rb_ptr.isEmpty() && L_x_rt_ptr.isEmpty() && !act_ptr.child_rb_exists() && !act_ptr.child_rt_exists()) {
				split_in_y_direction(act_ptr, L_x_r_ptr, L_x_rb_ptr, L_x_rt_ptr, L_y_r_ptr, L_y_rb_ptr, L_y_rt_ptr);
			}
			
			if ((!act_ptr.child_lb_exists()) && (!L_x_lb_ptr.isEmpty())) {
				T.create_new_lb_child(L_x_lb_ptr, L_y_lb_ptr);
				T.go_to_lb_child();
				new_leaf_List.add(T.get_act_ptr());
				T.go_to_father();
			}
			
			if ((!act_ptr.child_lt_exists()) && (!L_x_lt_ptr.isEmpty())) {
				T.create_new_lt_child(L_x_lt_ptr, L_y_lt_ptr);
				T.go_to_lt_child();
				new_leaf_List.add(T.get_act_ptr());
				T.go_to_father();
			}
			
			if ((!act_ptr.child_rb_exists()) && (!L_x_rb_ptr.isEmpty())) {
				T.create_new_rb_child(L_x_rb_ptr, L_y_rb_ptr);
				T.go_to_rb_child();
				new_leaf_List.add(T.get_act_ptr());
				T.go_to_father();
			}
			
			if ((!act_ptr.child_rt_exists()) && (!L_x_rt_ptr.isEmpty())) {
				T.create_new_rt_child(L_x_rt_ptr, L_y_rt_ptr);
				T.go_to_rt_child();
				new_leaf_List.add(T.get_act_ptr());
				T.go_to_father();
			}
			
			act_ptr.set_x_List_ptr(null);
			act_ptr.set_y_List_ptr(null);
		} else {
			List<Node> L = new ArrayList<Node>();
			
			for (ParticleInfo info : act_ptr.get_x_List_ptr()) {
				L.add(info.get_vertex());
			}
			T.get_act_ptr().set_contained_nodes(L);
			
			build_up_sorted_subLists(act_x_List_copy, act_y_List_copy);
			
			act_ptr.get_x_List_ptr().clear();
			act_ptr.get_y_List_ptr().clear();
		}
	}
	
	private void calculate_boundaries_of_act_node(QuadTreeNode act_ptr, DoubleObject x_min, DoubleObject x_max, DoubleObject y_min, DoubleObject y_max) {
		List<ParticleInfo> L_x_ptr = act_ptr.get_x_List_ptr();
		List<ParticleInfo> L_y_ptr = act_ptr.get_y_List_ptr();
		
		x_min.setVal(L_x_ptr.get(0).get_x_y_coord());
		x_max.setVal(L_x_ptr.get(L_x_ptr.size()-1).get_x_y_coord());
		y_min.setVal(L_y_ptr.get(0).get_x_y_coord());
		y_max.setVal(L_y_ptr.get(L_y_ptr.size()-1).get_x_y_coord());
	}
	
	private boolean in_lt_quad(QuadTreeNode act_ptr, double x_min, double x_max, double y_min, double y_max) {
		double l = act_ptr.get_Sm_downleftcorner().getX();
		double r = act_ptr.get_Sm_downleftcorner().getX() + act_ptr.get_Sm_boxlength() / 2;
		double b = act_ptr.get_Sm_downleftcorner().getY() + act_ptr.get_Sm_boxlength() / 2;
		double t = act_ptr.get_Sm_downleftcorner().getY() + act_ptr.get_Sm_boxlength();
		
		if (l <= x_min && x_max < r && b <= y_min && y_max < t) {
			return true;
		} else if (x_min == x_max && y_min == y_max && l == r && t == b && x_min == r && y_min == b) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean in_rt_quad(QuadTreeNode act_ptr, double x_min, double x_max, double y_min, double y_max) {
		double l = act_ptr.get_Sm_downleftcorner().getX() + act_ptr.get_Sm_boxlength() / 2;
		double r = act_ptr.get_Sm_downleftcorner().getX() + act_ptr.get_Sm_boxlength();
		double b = act_ptr.get_Sm_downleftcorner().getY() + act_ptr.get_Sm_boxlength() / 2;
		double t = act_ptr.get_Sm_downleftcorner().getY() + act_ptr.get_Sm_boxlength();
		
		if (l <= x_min && x_max < r && b <= y_min && y_max < t) {
			return true;
		} else if (x_min == x_max && y_min == y_max && l == r && t == b && x_min == r && y_min == b) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean in_lb_quad(QuadTreeNode act_ptr, double x_min, double x_max, double y_min, double y_max) {
		double l = act_ptr.get_Sm_downleftcorner().getX();
		double r = act_ptr.get_Sm_downleftcorner().getX() + act_ptr.get_Sm_boxlength() / 2;
		double b = act_ptr.get_Sm_downleftcorner().getY();
		double t = act_ptr.get_Sm_downleftcorner().getY() + act_ptr.get_Sm_boxlength() / 2;
		
		if (l <= x_min && x_max < r && b <= y_min && y_max < t) {
			return true;
		} else if (x_min == x_max && y_min == y_max && l == r && t == b && x_min == r && y_min == b) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean in_rb_quad(QuadTreeNode act_ptr, double x_min, double x_max, double y_min, double y_max) {
		double l = act_ptr.get_Sm_downleftcorner().getX() + act_ptr.get_Sm_boxlength() / 2;
		double r = act_ptr.get_Sm_downleftcorner().getX() + act_ptr.get_Sm_boxlength();
		double b = act_ptr.get_Sm_downleftcorner().getY();
		double t = act_ptr.get_Sm_downleftcorner().getY() + act_ptr.get_Sm_boxlength() / 2;
		
		if (l <= x_min && x_max < r && b <= y_min && y_max < t) {
			return true;
		} else if (x_min == x_max && y_min == y_max && l == r && t == b && x_min == r && y_min == b) {
			return true;
		} else {
			return false;
		}
	}
	
	private void split_in_x_direction(QuadTreeNode act_ptr, List<ParticleInfo> L_x_left_ptr, List<ParticleInfo> L_y_left_ptr, List<ParticleInfo> L_x_right_ptr, List<ParticleInfo> L_y_right_ptr) {
		ParticleInfo r_item = act_ptr.get_x_List_ptr().get(act_ptr.get_x_List_ptr().size()-1);
		ParticleInfo last_left_item = null;
		int last_index = -1;
		double act_Sm_boxlength_half = act_ptr.get_Sm_boxlength()/2;
		double x_mid_coord = act_ptr.get_Sm_downleftcorner().getX() + act_Sm_boxlength_half;
		double l_xcoord, r_xcoord;
		boolean left_particleList_empty = false;
		boolean right_particleList_empty = false;
		boolean left_particleList_larger = true;
		
		for (ParticleInfo l_item : act_ptr.get_x_List_ptr()) {
			l_xcoord = l_item.get_x_y_coord();
			r_xcoord = r_item.get_x_y_coord();
			
			if (l_xcoord >= x_mid_coord) {
				left_particleList_larger = false;
				if (last_index >= 0) {
					last_left_item = act_ptr.get_x_List_ptr().get(last_index);
				} else {
					left_particleList_empty = true;
				}
				break;
			} else if (r_xcoord < x_mid_coord) {
				if (last_index >= 0) {
					last_left_item = r_item;
				} else {
					right_particleList_empty = true;
				}
				break;
			}
			
			last_index++;
			r_item = act_ptr.get_x_List_ptr().get(act_ptr.get_x_List_ptr().size() - last_index - 2);
		}
		
		if (left_particleList_empty) {
			L_x_left_ptr.clear();
			L_y_left_ptr.clear();
			L_x_right_ptr.clear();
			L_y_right_ptr.clear();
			
			L_x_right_ptr.addAll(act_ptr.get_x_List_ptr());
			L_y_right_ptr.addAll(act_ptr.get_y_List_ptr());
		} else if (right_particleList_empty) {
			L_x_left_ptr.clear();
			L_y_left_ptr.clear();
			L_x_right_ptr.clear();
			L_y_right_ptr.clear();
			
			L_x_left_ptr.addAll(act_ptr.get_x_List_ptr());
			L_y_left_ptr.addAll(act_ptr.get_y_List_ptr());
		} else if (left_particleList_larger) {
			x_delete_right_subLists(act_ptr, L_x_left_ptr, L_y_left_ptr, L_x_right_ptr, L_y_right_ptr, last_left_item);
		} else {
			x_delete_left_subLists(act_ptr, L_x_left_ptr, L_y_left_ptr, L_x_right_ptr, L_y_right_ptr, last_left_item);
		}
	}
	
	private void split_in_y_direction(QuadTreeNode act_ptr, List<ParticleInfo> L_x_left_ptr, List<ParticleInfo> L_y_left_ptr, List<ParticleInfo> L_x_right_ptr, List<ParticleInfo> L_y_right_ptr) {
		ParticleInfo r_item = act_ptr.get_x_List_ptr().get(act_ptr.get_x_List_ptr().size()-1);
		ParticleInfo last_left_item = null;
		int last_index = -1;
		double act_Sm_boxlength_half = act_ptr.get_Sm_boxlength()/2;
		double y_mid_coord = act_ptr.get_Sm_downleftcorner().getY() + act_Sm_boxlength_half;
		double l_ycoord, r_ycoord;
		boolean left_particleList_empty = false;
		boolean right_particleList_empty = false;
		boolean left_particleList_larger = true;
		
		for (ParticleInfo l_item : act_ptr.get_x_List_ptr()) {
			l_ycoord = l_item.get_x_y_coord();
			r_ycoord = r_item.get_x_y_coord();
			
			if (l_ycoord >= y_mid_coord) {
				left_particleList_larger = false;
				if (last_index >= 0) {
					last_left_item = act_ptr.get_x_List_ptr().get(last_index);
				} else {
					left_particleList_empty = true;
				}
				break;
			} else if (r_ycoord < y_mid_coord) {
				if (last_index >= 0) {
					last_left_item = r_item;
				} else {
					right_particleList_empty = true;
				}
				break;
			}
			
			last_index++;
			r_item = act_ptr.get_x_List_ptr().get(act_ptr.get_x_List_ptr().size() - last_index - 2);
		}
		
		if (left_particleList_empty) {
			L_x_left_ptr.clear();
			L_y_left_ptr.clear();
			L_x_right_ptr.clear();
			L_y_right_ptr.clear();
			L_x_right_ptr.addAll(act_ptr.get_x_List_ptr());
			L_y_right_ptr.addAll(act_ptr.get_y_List_ptr());
		} else if (right_particleList_empty) {
			L_x_left_ptr.clear();
			L_y_left_ptr.clear();
			L_x_right_ptr.clear();
			L_y_right_ptr.clear();
			L_x_left_ptr.addAll(act_ptr.get_x_List_ptr());
			L_y_left_ptr.addAll(act_ptr.get_y_List_ptr());
		} else if (left_particleList_larger) {
			y_delete_right_subLists(act_ptr, L_x_left_ptr, L_y_left_ptr, L_x_right_ptr, L_y_right_ptr, last_left_item);
		} else {
			y_delete_left_subLists(act_ptr, L_x_left_ptr, L_y_left_ptr, L_x_right_ptr, L_y_right_ptr, last_left_item);
		}
	}
	
	private void x_delete_right_subLists(QuadTreeNode act_ptr, List<ParticleInfo> L_x_left_ptr, List<ParticleInfo> L_y_left_ptr, List<ParticleInfo> L_x_right_ptr, List<ParticleInfo> L_y_right_ptr, ParticleInfo last_left_item) {
		ParticleInfo act_item, p_in_L_x_item, p_in_L_y_item;
		
		L_x_left_ptr.clear();
		L_y_left_ptr.clear();
		L_x_right_ptr.clear();
		L_y_right_ptr.clear();
		
		L_x_left_ptr.addAll(act_ptr.get_x_List_ptr());
		L_y_left_ptr.addAll(act_ptr.get_y_List_ptr());
		
		for (int i = L_x_left_ptr.indexOf(last_left_item)+1; i < L_x_left_ptr.size(); i++) {
			act_item = L_x_left_ptr.get(i);
			
			p_in_L_x_item = act_item.get_copy_item();
			p_in_L_x_item.set_subList_ptr(L_x_right_ptr);
			
			p_in_L_y_item = act_item.get_cross_ref_item().get_copy_item();
			p_in_L_y_item.set_subList_ptr(L_y_right_ptr);
			
			if (act_item == last_left_item)
				break;
		}
	}
	
	private void x_delete_left_subLists(QuadTreeNode act_ptr, List<ParticleInfo> L_x_left_ptr, List<ParticleInfo> L_y_left_ptr, List<ParticleInfo> L_x_right_ptr, List<ParticleInfo> L_y_right_ptr, ParticleInfo last_left_item) {
		ParticleInfo act_item, p_in_L_x_item, p_in_L_y_item;
		
		L_x_left_ptr.clear();
		L_y_left_ptr.clear();
		L_x_right_ptr.clear();
		L_y_right_ptr.clear();
		
		L_x_right_ptr.addAll(act_ptr.get_x_List_ptr());
		L_y_right_ptr.addAll(act_ptr.get_y_List_ptr());
		
		for (int i = L_x_right_ptr.indexOf(last_left_item)+1; i < L_x_right_ptr.size(); i++) {
			act_item = L_x_right_ptr.get(i);
			
			p_in_L_x_item = act_item.get_copy_item();
			p_in_L_x_item.set_subList_ptr(L_x_left_ptr);
			
			p_in_L_y_item = act_item.get_cross_ref_item().get_copy_item();
			p_in_L_y_item.set_subList_ptr(L_y_left_ptr);
			
			if (act_item == last_left_item)
				break;
		}
	}
	
	private void y_delete_right_subLists(QuadTreeNode act_ptr, List<ParticleInfo> L_x_left_ptr, List<ParticleInfo> L_y_left_ptr, List<ParticleInfo> L_x_right_ptr, List<ParticleInfo> L_y_right_ptr, ParticleInfo last_left_item) {
		ParticleInfo act_item, p_in_L_x_item, p_in_L_y_item;
		
		L_x_left_ptr.clear();
		L_y_left_ptr.clear();
		L_x_right_ptr.clear();
		L_y_right_ptr.clear();
		
		L_x_left_ptr.addAll(act_ptr.get_x_List_ptr());
		L_y_left_ptr.addAll(act_ptr.get_y_List_ptr());
		
		for (int i = L_y_left_ptr.indexOf(last_left_item)+1; i < L_y_left_ptr.size(); i++) {
			act_item = L_y_left_ptr.get(i);
			
			p_in_L_y_item = act_item.get_copy_item();
			p_in_L_y_item.set_subList_ptr(L_y_right_ptr);
			
			p_in_L_x_item = act_item.get_cross_ref_item().get_copy_item();
			p_in_L_x_item.set_subList_ptr(L_x_right_ptr);
			
			if (act_item == last_left_item)
				break;
		}
	}
	
	private void y_delete_left_subLists(QuadTreeNode act_ptr, List<ParticleInfo> L_x_left_ptr, List<ParticleInfo> L_y_left_ptr, List<ParticleInfo> L_x_right_ptr, List<ParticleInfo> L_y_right_ptr, ParticleInfo last_left_item) {
		ParticleInfo act_item, p_in_L_x_item, p_in_L_y_item;
		
		L_x_left_ptr.clear();
		L_y_left_ptr.clear();
		L_x_right_ptr.clear();
		L_y_right_ptr.clear();
		
		L_x_right_ptr.addAll(act_ptr.get_x_List_ptr());
		L_y_right_ptr.addAll(act_ptr.get_y_List_ptr());
		
		for (int i = L_y_right_ptr.indexOf(last_left_item)+1; i < L_y_right_ptr.size(); i++) {
			act_item = L_y_right_ptr.get(i);
			
			p_in_L_y_item = act_item.get_copy_item();
			p_in_L_y_item.set_subList_ptr(L_y_left_ptr);
			
			p_in_L_x_item = act_item.get_cross_ref_item().get_copy_item();
			p_in_L_x_item.set_subList_ptr(L_x_left_ptr);
			
			if (act_item == last_left_item)
				break;
		}
	}
	
	private void split_in_y_direction(QuadTreeNode act_ptr, List<ParticleInfo> L_x_ptr, List<ParticleInfo> L_x_b_ptr, List<ParticleInfo> L_x_t_ptr, List<ParticleInfo> L_y_ptr, List<ParticleInfo> L_y_b_ptr, List<ParticleInfo> L_y_t_ptr) {
		ParticleInfo r_item = L_y_ptr.get(L_y_ptr.size()-1);
		ParticleInfo last_left_item = null;
		double act_Sm_boxlength_half = act_ptr.get_Sm_boxlength()/2;
		double y_mid_coord = act_ptr.get_Sm_downleftcorner().getY() + act_Sm_boxlength_half;
		double l_ycoord, r_ycoord;
		boolean left_particleList_empty = false;
		boolean right_particleList_empty = false;
		boolean left_particleList_larger = true;
		int last_index = -1;
		
		for (ParticleInfo l_item : L_y_ptr) {
			l_ycoord = l_item.get_x_y_coord();
			r_ycoord = r_item.get_x_y_coord();
			
			if (l_ycoord >= y_mid_coord) {
				left_particleList_larger = false;
				if (last_index >= 0) {
					last_left_item = L_y_ptr.get(last_index);
				} else {
					left_particleList_empty = true;
				}
				
				break;
			} else if (r_ycoord < y_mid_coord) {
				if (last_index >= 0) {
					last_left_item = r_item;
				} else {
					right_particleList_empty = true;
				}
			}
			
			last_index++;
			r_item = L_y_ptr.get(L_y_ptr.size() - last_index - 2);
		}
		
		if (left_particleList_empty) {
			L_x_b_ptr.clear();
			L_y_b_ptr.clear();
			L_x_t_ptr.clear();
			L_y_t_ptr.clear();
			
			L_x_t_ptr.addAll(L_x_ptr);
			L_y_t_ptr.addAll(L_y_ptr);
		} else if (right_particleList_empty) {
			L_x_b_ptr.clear();
			L_y_b_ptr.clear();
			L_x_t_ptr.clear();
			L_y_t_ptr.clear();
			
			L_x_b_ptr.addAll(L_x_ptr);
			L_y_b_ptr.addAll(L_y_ptr);
		} else if (left_particleList_larger) {
			y_move_right_subLists(L_x_ptr, L_x_b_ptr, L_x_t_ptr, L_y_ptr, L_y_b_ptr, L_y_t_ptr, last_left_item);
		} else {
			y_move_left_subLists(L_x_ptr, L_x_b_ptr, L_x_t_ptr, L_y_ptr, L_y_b_ptr, L_y_t_ptr, last_left_item);
		}
	}
	
	private void y_move_left_subLists(List<ParticleInfo> L_x_ptr, List<ParticleInfo> L_x_l_ptr, List<ParticleInfo> L_x_r_ptr, List<ParticleInfo> L_y_ptr, List<ParticleInfo> L_y_l_ptr, List<ParticleInfo> L_y_r_ptr, ParticleInfo last_left_item) {
		L_x_r_ptr.clear();
		L_y_r_ptr.clear();
		L_x_l_ptr.clear();
		L_y_l_ptr.clear();
		
		L_x_r_ptr.addAll(L_x_ptr);
		L_y_r_ptr.addAll(L_y_ptr);
		
		for (ParticleInfo p_in_L_y_item : L_y_r_ptr) {
			L_y_l_ptr.add(p_in_L_y_item);
			
			ParticleInfo p_in_L_x_item = p_in_L_y_item.get_cross_ref_item();
			p_in_L_x_item.set_cross_ref_item(L_y_l_ptr.get(L_y_l_ptr.size()-1));
			p_in_L_x_item.mark();
			
			if (p_in_L_y_item == last_left_item)
				break;
		}
		
		for (ParticleInfo p_in_L_x_item : L_x_r_ptr) {
			if (p_in_L_x_item.is_marked()) {
				p_in_L_x_item.unmark();
				L_x_l_ptr.add(p_in_L_x_item);
				
				ParticleInfo p_in_L_y_item = p_in_L_x_item.get_cross_ref_item();
				p_in_L_y_item.set_cross_ref_item(L_x_l_ptr.get(L_x_l_ptr.size()-1));
			}
		}
	}
	
	private void y_move_right_subLists(List<ParticleInfo> L_x_ptr, List<ParticleInfo> L_x_l_ptr, List<ParticleInfo> L_x_r_ptr, List<ParticleInfo> L_y_ptr, List<ParticleInfo> L_y_l_ptr, List<ParticleInfo> L_y_r_ptr, ParticleInfo last_left_item) {
		L_x_r_ptr.clear();
		L_y_r_ptr.clear();
		L_x_l_ptr.clear();
		L_y_l_ptr.clear();
		
		L_x_l_ptr.addAll(L_x_ptr);
		L_y_l_ptr.addAll(L_y_ptr);
		
		for (int i = L_y_l_ptr.indexOf(last_left_item) + 1; i < L_y_l_ptr.size(); i++) {
			ParticleInfo p_in_L_y_item = L_y_l_ptr.get(i);
			L_y_l_ptr.add(p_in_L_y_item);
			
			ParticleInfo p_in_L_x_item = p_in_L_y_item.get_cross_ref_item();
			p_in_L_x_item.set_cross_ref_item(L_y_r_ptr.get(L_y_r_ptr.size()-1));
			p_in_L_x_item.mark();
		}
		
		for (ParticleInfo p_in_L_x_item : L_x_l_ptr) {
			if (p_in_L_x_item.is_marked()) {
				p_in_L_x_item.unmark();
				L_x_r_ptr.add(p_in_L_x_item);
				
				ParticleInfo p_in_L_y_item = p_in_L_x_item.get_cross_ref_item();
				p_in_L_y_item.set_cross_ref_item(L_x_l_ptr.get(L_x_l_ptr.size()-1));
			}
		}
	}
	
	private void build_up_sorted_subLists(List<ParticleInfo> L_x_copy, List<ParticleInfo> L_y_copy) {
		ParticleInfo P_x, P_y;
		List<ParticleInfo> L_x_ptr, L_y_ptr;
		ParticleInfo new_cross_ref_item;
		
		for (ParticleInfo it : L_x_copy) {
			if (it.get_subList_ptr() != null) {
				P_x = it;
				L_x_ptr = P_x.get_subList_ptr();
				P_x.set_subList_ptr(null);
				P_x.set_copy_item(null);
				P_x.unmark();
				P_x.set_tmp_cross_ref_item(null);
				
				L_x_ptr.add(P_x);
				P_x.set_tmp_cross_ref_item(L_x_ptr.get(L_x_ptr.size()-1));
			}
		}
		
		for (ParticleInfo it : L_y_copy) {
			if (it.get_subList_ptr() != null) {
				P_y = it;
				L_y_ptr = P_y.get_subList_ptr();
				P_y.set_subList_ptr(null);
				P_y.set_copy_item(null);
				P_y.unmark();
				P_y.set_tmp_cross_ref_item(null);
				
				new_cross_ref_item = P_y.get_cross_ref_item().get_tmp_cross_ref_item();
				P_y.set_cross_ref_item(new_cross_ref_item);
				L_y_ptr.add(P_y);
				P_x = new_cross_ref_item;
				P_x.set_cross_ref_item(L_y_ptr.get(L_y_ptr.size()-1));
			}
		}
	}
	
	private void build_up_red_quad_tree_subtree_by_subtree(Graph g, HashMap<Node, NodeAttributes> A, QuadTree T) {
		List<QuadTreeNode> act_subtree_root_List, new_subtree_root_List, help_ptr;
		QuadTreeNode subtree_root_ptr;
		
		build_up_root_vertex(g, T);
		
		act_subtree_root_List = new ArrayList<QuadTreeNode>();
		new_subtree_root_List = new ArrayList<QuadTreeNode>();
		act_subtree_root_List.add(T.get_root_ptr());
		
		while (!act_subtree_root_List.isEmpty()) {
			while (!act_subtree_root_List.isEmpty()) {
				subtree_root_ptr = act_subtree_root_List.remove(0);
				construct_subtree(A, T, subtree_root_ptr, new_subtree_root_List);
			}
			
			help_ptr = act_subtree_root_List;
			act_subtree_root_List = new_subtree_root_List;
			new_subtree_root_List = help_ptr;
		}
	}
	
	private void build_up_root_vertex(Graph g, QuadTree T) {
		T.init_tree();
		T.get_root_ptr().set_Sm_level(0);
		T.get_root_ptr().set_Sm_downleftcorner(down_left_corner);
		T.get_root_ptr().set_Sm_boxlength(boxlength);
		T.get_root_ptr().set_particlenumber_in_subtree(g.getNodeCount());
		
		ArrayList<Node> nodes = new ArrayList<Node>(g.getNodes());
		for (Node v : nodes) {
			T.get_root_ptr().pushBack_contained_nodes(v);
		}
	}
	
	private void construct_subtree(HashMap<Node, NodeAttributes> A, QuadTree T, QuadTreeNode subtree_root_ptr, List<QuadTreeNode> new_subtree_root_List) {
		int n = subtree_root_ptr.get_particlenumber_in_subtree();
		int subtree_depth = (int) Math.max(1, Math.floor(Math.log(n) / Math.log(4)) - 2);
		int maxindex = 1;
		
		for (int i = 1; i < subtree_depth; i++) {
			maxindex *= 2;
		}
		double subtree_min_boxlength = subtree_root_ptr.get_Sm_boxlength() / maxindex;
		
		if (subtree_min_boxlength >= MIN_BOX_LENGTH) {
			QuadTreeNode[][] leaf_ptr = new QuadTreeNode[maxindex-1][maxindex-1];
			T.set_act_ptr(subtree_root_ptr);
			
			if (find_smallest_quad(A, T)) {
				construct_complete_subtree(T, subtree_depth, leaf_ptr, 0, 0, 0);
				set_contained_nodes_for_leaves(A, subtree_root_ptr, leaf_ptr, maxindex);
				T.set_act_ptr(subtree_root_ptr);
				set_particlenumber_in_subtree_entries(T);
				T.set_act_ptr(subtree_root_ptr);
				construct_reduced_subtree(A, T, new_subtree_root_List);
			}
		}
	}
	
	private void construct_complete_subtree(QuadTree T, int subtree_depth, QuadTreeNode[][] leaf_ptr, int act_depth, int act_x_index, int act_y_index) {
		if (act_depth < subtree_depth) {
			T.create_new_lt_child();
			T.create_new_rt_child();
			T.create_new_lb_child();
			T.create_new_rb_child();
			
			T.go_to_lt_child();
			construct_complete_subtree(T, subtree_depth, leaf_ptr, act_depth + 1, 2 * act_x_index, 2 * act_y_index + 1);
			T.go_to_father();
			
			T.go_to_rt_child();
			construct_complete_subtree(T, subtree_depth, leaf_ptr, act_depth + 1, 2 * act_x_index + 1, 2 * act_y_index + 1);
			T.go_to_father();
			
			T.go_to_lb_child();
			construct_complete_subtree(T, subtree_depth, leaf_ptr, act_depth + 1, 2 * act_x_index, 2 * act_y_index);
			T.go_to_father();
			
			T.go_to_rb_child();
			construct_complete_subtree(T, subtree_depth, leaf_ptr, act_depth + 1, 2 * act_x_index + 1, 2 * act_y_index);
			T.go_to_father();
		} else if (act_depth == subtree_depth) {
			leaf_ptr[act_x_index][act_y_index] = T.get_act_ptr();
		} else {
			System.out.println("Error NMM:construct_complete_subtree()");
		}
	}
	
	private void set_contained_nodes_for_leaves(HashMap<Node, NodeAttributes> A, QuadTreeNode subtree_root_ptr, QuadTreeNode[][] leaf_ptr, int maxindex) {
		Node v;
		QuadTreeNode act_ptr;
		double xcoord, ycoord;
		int x_index, y_index;
		double minboxlength = subtree_root_ptr.get_Sm_boxlength()/maxindex;
		
		while (!subtree_root_ptr.contained_nodes_empty()) {
			v = subtree_root_ptr.pop_contained_nodes();
			xcoord = A.get(v).get_x() - subtree_root_ptr.get_Sm_downleftcorner().getX();
			ycoord = A.get(v).get_y() - subtree_root_ptr.get_Sm_downleftcorner().getY();
			x_index = (int)(xcoord / minboxlength);
			y_index = (int)(ycoord / minboxlength);
			
			act_ptr = leaf_ptr[x_index][y_index];
			act_ptr.pushBack_contained_nodes(v);
			act_ptr.set_particlenumber_in_subtree(act_ptr.get_particlenumber_in_subtree() + 1);
		}
	}
	
	private void set_particlenumber_in_subtree_entries(QuadTree T) {
		int child_nr;
		
		if (!T.get_act_ptr().is_leaf()) {
			T.get_act_ptr().set_particlenumber_in_subtree(0);
			
			if (T.get_act_ptr().child_lt_exists()) {
				T.go_to_lt_child();
				set_particlenumber_in_subtree_entries(T);
				T.go_to_father();
				child_nr = T.get_act_ptr().get_child_lt_ptr().get_particlenumber_in_subtree();
				T.get_act_ptr().set_particlenumber_in_subtree(child_nr + T.get_act_ptr().get_particlenumber_in_subtree());
			}
			
			if (T.get_act_ptr().child_rt_exists()) {
				T.go_to_rt_child();
				set_particlenumber_in_subtree_entries(T);
				T.go_to_father();
				child_nr = T.get_act_ptr().get_child_rt_ptr().get_particlenumber_in_subtree();
				T.get_act_ptr().set_particlenumber_in_subtree(child_nr + T.get_act_ptr().get_particlenumber_in_subtree());
			}
			
			if (T.get_act_ptr().child_lb_exists()) {
				T.go_to_lb_child();
				set_particlenumber_in_subtree_entries(T);
				T.go_to_father();
				child_nr = T.get_act_ptr().get_child_lb_ptr().get_particlenumber_in_subtree();
				T.get_act_ptr().set_particlenumber_in_subtree(child_nr + T.get_act_ptr().get_particlenumber_in_subtree());
			}
			
			if (T.get_act_ptr().child_rb_exists()) {
				T.go_to_rb_child();
				set_particlenumber_in_subtree_entries(T);
				T.go_to_father();
				child_nr = T.get_act_ptr().get_child_rb_ptr().get_particlenumber_in_subtree();
				T.get_act_ptr().set_particlenumber_in_subtree(child_nr + T.get_act_ptr().get_particlenumber_in_subtree());
			}
		}
	}
	
	private void construct_reduced_subtree(HashMap<Node, NodeAttributes> A, QuadTree T, List<QuadTreeNode> new_subtree_root_List) {
		do {
			QuadTreeNode act_ptr = T.get_act_ptr();
			delete_empty_subtrees(T);
			T.set_act_ptr(act_ptr);
		} while (check_and_delete_degenerated_node(T));
		
		if (!T.get_act_ptr().is_leaf() && T.get_act_ptr().get_particlenumber_in_subtree() <= particles_in_leaves()) {
			delete_sparse_subtree(T, T.get_act_ptr());
		} else if (T.get_act_ptr().is_leaf() && T.get_act_ptr().get_particlenumber_in_subtree() > particles_in_leaves()) {
			find_smallest_quad(A, T);
		} else if (!T.get_act_ptr().is_leaf()) {
			if (T.get_act_ptr().child_lt_exists()) {
				T.go_to_lt_child();
				construct_reduced_subtree(A, T, new_subtree_root_List);
				T.go_to_father();
			}
			
			if (T.get_act_ptr().child_rt_exists()) {
				T.go_to_rt_child();
				construct_reduced_subtree(A, T, new_subtree_root_List);
				T.go_to_father();
			}
			
			if (T.get_act_ptr().child_lb_exists()) {
				T.go_to_lb_child();
				construct_reduced_subtree(A, T, new_subtree_root_List);
				T.go_to_father();
			}
			
			if (T.get_act_ptr().child_rb_exists()) {
				T.go_to_rb_child();
				construct_reduced_subtree(A, T, new_subtree_root_List);
				T.go_to_father();
			}
		}
	}
	
	private void delete_empty_subtrees(QuadTree T) {
		int child_part_nr;
		QuadTreeNode act_ptr = T.get_act_ptr();
		
		if (act_ptr.child_lt_exists()) {
			child_part_nr = act_ptr.get_child_lt_ptr().get_particlenumber_in_subtree();
			
			if (child_part_nr == 0) {
				T.delete_tree(act_ptr.get_child_lt_ptr());
				act_ptr.set_child_lt_ptr(null);
			}
		}
		
		if (act_ptr.child_rt_exists()) {
			child_part_nr = act_ptr.get_child_rt_ptr().get_particlenumber_in_subtree();
			
			if (child_part_nr == 0) {
				T.delete_tree(act_ptr.get_child_rt_ptr());
				act_ptr.set_child_rt_ptr(null);
			}
		}
		
		if (act_ptr.child_lb_exists()) {
			child_part_nr = act_ptr.get_child_lb_ptr().get_particlenumber_in_subtree();
			
			if (child_part_nr == 0) {
				T.delete_tree(act_ptr.get_child_lb_ptr());
				act_ptr.set_child_lb_ptr(null);
			}
		}
		
		if (act_ptr.child_rb_exists()) {
			child_part_nr = act_ptr.get_child_rb_ptr().get_particlenumber_in_subtree();
			
			if (child_part_nr == 0) {
				T.delete_tree(act_ptr.get_child_rb_ptr());
				act_ptr.set_child_rb_ptr(null);
			}
		}
	}
	
	private boolean check_and_delete_degenerated_node(QuadTree T) {
		QuadTreeNode delete_ptr;
		QuadTreeNode child_ptr;
		
		boolean lt_child = T.get_act_ptr().child_lt_exists();
		boolean rt_child = T.get_act_ptr().child_rt_exists();
		boolean lb_child = T.get_act_ptr().child_lb_exists();
		boolean rb_child = T.get_act_ptr().child_rb_exists();
		boolean is_degenerated = false;
		
		if (lt_child && !rt_child && !lb_child && !rb_child) {
			is_degenerated = true;
			delete_ptr = T.get_act_ptr();
			child_ptr = T.get_act_ptr().get_child_lt_ptr();
			
			eliminateDegeneratedNode(T, child_ptr);
		} else if (!lt_child && rt_child && !lb_child && !rb_child) {
			is_degenerated = true;
			delete_ptr = T.get_act_ptr();
			child_ptr = T.get_act_ptr().get_child_rt_ptr();
			
			eliminateDegeneratedNode(T, child_ptr);
		} else if (!lt_child && !rt_child && lb_child && !rb_child) {
			is_degenerated = true;
			delete_ptr = T.get_act_ptr();
			child_ptr = T.get_act_ptr().get_child_lb_ptr();
			
			eliminateDegeneratedNode(T, child_ptr);
		} else if (!lt_child && !rt_child && !lb_child && rb_child) {
			is_degenerated = true;
			delete_ptr = T.get_act_ptr();
			child_ptr = T.get_act_ptr().get_child_rb_ptr();
			
			eliminateDegeneratedNode(T, child_ptr);
		}
		
		return is_degenerated;
	}

	private void eliminateDegeneratedNode(QuadTree T, QuadTreeNode child_ptr) {
		QuadTreeNode father_ptr;
		if (T.get_act_ptr() == T.get_root_ptr()) {
			T.set_root_ptr(child_ptr);
			T.set_act_ptr(T.get_root_ptr());
		} else {
			father_ptr = T.get_act_ptr().get_father_ptr();
			child_ptr.set_father_ptr(father_ptr);
			
			if (father_ptr.get_child_lt_ptr() == T.get_act_ptr()) {
				father_ptr.set_child_lt_ptr(child_ptr);
			} else if (father_ptr.get_child_rt_ptr() == T.get_act_ptr()) {
				father_ptr.set_child_rt_ptr(child_ptr);
			} else if (father_ptr.get_child_lb_ptr() == T.get_act_ptr()) {
				father_ptr.set_child_lb_ptr(child_ptr);
			} else if (father_ptr.get_child_rb_ptr() == T.get_act_ptr()) {
				father_ptr.set_child_rb_ptr(child_ptr);
			} else {
				System.out.println("Error NMM::delete_degenerated_node");
			}
			
			T.set_act_ptr(child_ptr);
		}
	}
	
	private void delete_sparse_subtree(QuadTree T, QuadTreeNode new_leaf_ptr) {
		collect_contained_nodes(T, new_leaf_ptr);
		
		if (new_leaf_ptr.child_lt_exists()) {
			T.delete_tree(new_leaf_ptr.get_child_lt_ptr());
			new_leaf_ptr.set_child_lt_ptr(null);
		}
		if (new_leaf_ptr.child_rt_exists()) {
			T.delete_tree(new_leaf_ptr.get_child_rt_ptr());
			new_leaf_ptr.set_child_rt_ptr(null);
		}
		if (new_leaf_ptr.child_lb_exists()) {
			T.delete_tree(new_leaf_ptr.get_child_lb_ptr());
			new_leaf_ptr.set_child_lb_ptr(null);
		}
		if (new_leaf_ptr.child_rb_exists()) {
			T.delete_tree(new_leaf_ptr.get_child_rb_ptr());
			new_leaf_ptr.set_child_rb_ptr(null);
		}
	}
	
	private void collect_contained_nodes(QuadTree T, QuadTreeNode new_leaf_ptr) {
		if (T.get_act_ptr().is_leaf()) {
			while (!T.get_act_ptr().contained_nodes_empty()) {
				new_leaf_ptr.pushBack_contained_nodes(T.get_act_ptr().pop_contained_nodes());
			}
		} else if (T.get_act_ptr().child_lt_exists()) {
			T.go_to_lt_child();
			collect_contained_nodes(T, new_leaf_ptr);
			T.go_to_father();
		} else if (T.get_act_ptr().child_rt_exists()) {
			T.go_to_rt_child();
			collect_contained_nodes(T, new_leaf_ptr);
			T.go_to_father();
		} else if (T.get_act_ptr().child_lb_exists()) {
			T.go_to_lb_child();
			collect_contained_nodes(T, new_leaf_ptr);
			T.go_to_father();
		} else if (T.get_act_ptr().child_rb_exists()) {
			T.go_to_rb_child();
			collect_contained_nodes(T, new_leaf_ptr);
			T.go_to_father();
		}
	}
	
	private boolean find_smallest_quad(HashMap<Node, NodeAttributes> A, QuadTree T) {
		assert(!T.get_act_ptr().contained_nodes_empty());
		
		List<Node> L = T.get_act_ptr().get_contained_nodes();
		Node v = L.remove(0);
		double x_min = A.get(v).get_x();
		double x_max = x_min;
		double y_min = A.get(v).get_y();
		double y_max = y_min;
		
		while (!L.isEmpty()) {
			v = L.remove(0);
			if (A.get(v).get_x() < x_min) {
				x_min = A.get(v).get_x();
			}
			if (A.get(v).get_x() > x_max) {
				x_max = A.get(v).get_x();
			}
			if (A.get(v).get_y() < y_min) {
				y_min = A.get(v).get_y();
			}
			if (A.get(v).get_y() > y_max) {
				y_max = A.get(v).get_y();
			}
		}
		
		if (x_min != x_max || y_min != y_max) {
			find_small_cell_iteratively(T.get_act_ptr(), x_min, x_max, y_min, y_max);
			return true;
		} else {
			return false;
		}
	}
	
	private void find_small_cell_iteratively(QuadTreeNode act_ptr, double x_min, double x_max, double y_min, double y_max) {
		int new_level;
		double new_boxlength;
		Point2D new_dlc;
		boolean Sm_cell_found = false;
		
		while (!Sm_cell_found && ((x_max - x_min >= MIN_BOX_LENGTH) || (y_max - y_min >= MIN_BOX_LENGTH))) {
			new_level = act_ptr.get_Sm_level() + 1;
			new_boxlength = act_ptr.get_Sm_boxlength() / 2;
			
			if (in_lt_quad(act_ptr, x_min, x_max, y_min, y_max)) {
				new_dlc = new Point2D(act_ptr.get_Sm_downleftcorner().getX(), act_ptr.get_Sm_downleftcorner().getY() + new_boxlength);
				act_ptr.set_Sm_level(new_level);
				act_ptr.set_Sm_boxlength(new_boxlength);
				act_ptr.set_Sm_downleftcorner(new_dlc);
			} else if (in_rt_quad(act_ptr, x_min, x_max, y_min, y_max)) {
				new_dlc = new Point2D(act_ptr.get_Sm_downleftcorner().getX() + new_boxlength, act_ptr.get_Sm_downleftcorner().getY() + new_boxlength);
				act_ptr.set_Sm_level(new_level);
				act_ptr.set_Sm_boxlength(new_boxlength);
				act_ptr.set_Sm_downleftcorner(new_dlc);
			} else if (in_lb_quad(act_ptr, x_min, x_max, y_min, y_max)) {
				act_ptr.set_Sm_level(new_level);
				act_ptr.set_Sm_boxlength(new_boxlength);
			} else if (in_rb_quad(act_ptr, x_min, x_max, y_min, y_max)) {
				new_dlc = new Point2D(act_ptr.get_Sm_downleftcorner().getX() + new_boxlength, act_ptr.get_Sm_downleftcorner().getY());
				act_ptr.set_Sm_level(new_level);
				act_ptr.set_Sm_boxlength(new_boxlength);
				act_ptr.set_Sm_downleftcorner(new_dlc);
			} else {
				Sm_cell_found = true;
			}
		}
	}
	
	private void find_small_cell_by_formula(QuadTreeNode act_ptr, double x_min, double x_max, double y_min, double y_max) {
		int level_offset = act_ptr.get_Sm_level();
		max_power_of_2_index = 30;
		Point Sm_position;
		double Sm_dlc_x_coord, Sm_dlc_y_coord;
		double Sm_boxlength;
		int Sm_level;
		Point2D Sm_downleftcorner;
		int j_x = max_power_of_2_index+1;
		int j_y = max_power_of_2_index+1;
		boolean rectangle_is_horizontal_line = false;
		boolean rectangle_is_vertical_line = false;
		boolean rectangle_is_point = false;
		
		double x_min_old = x_min;
		double x_max_old = x_max;
		double y_min_old = y_min;
		double y_max_old = y_max;
		
		Sm_boxlength = act_ptr.get_Sm_boxlength();
		Sm_dlc_x_coord = act_ptr.get_Sm_downleftcorner().getX();
		Sm_dlc_y_coord = act_ptr.get_Sm_downleftcorner().getY();
		
		x_min -= Sm_dlc_x_coord;
		x_max -= Sm_dlc_x_coord;
		y_min -= Sm_dlc_y_coord;
		y_max -= Sm_dlc_y_coord;
		
		if (x_min == x_max && y_min == y_max) {
			rectangle_is_point = true;
		} else if (x_min == x_max && y_min != y_max) {
			rectangle_is_vertical_line = true;
		} else {
			j_x = (int) Math.ceil(Math.log(Sm_boxlength / (x_max - x_min))/Math.log(2));
		}
		
		if (x_min != x_max && y_min == y_max) {
			rectangle_is_horizontal_line = true;
		} else {
			j_y = (int) Math.ceil(Math.log(Sm_boxlength / (y_max - y_min))/Math.log(2));
		}
		
		if (rectangle_is_point) {
			// Keep the old values
		} else if (!numexcept.nearly_equal(x_min_old - x_max_old, x_min - x_max) || !numexcept.nearly_equal(y_min_old - y_max_old, y_min - y_max) || 
				x_min / Sm_boxlength < MIN_BOX_LENGTH || x_max / Sm_boxlength < MIN_BOX_LENGTH || y_min / Sm_boxlength < MIN_BOX_LENGTH || 
				y_max / Sm_boxlength < MIN_BOX_LENGTH) {
			find_small_cell_iteratively(act_ptr, x_min_old, x_max_old, y_min_old, y_max_old);
		} else {
			int k, a1, a2, A, j_minus_k;
			double h1, h2;
			int Sm_x_level = 0, Sm_y_level = 0;
			int Sm_x_position = 0, Sm_y_position = 0;
			
			if (x_min != x_max) {
				a1 = (int) Math.ceil((x_min / Sm_boxlength) * power_of_two(j_x));
				a2 = (int) Math.floor((x_max / Sm_boxlength) * power_of_two(j_x));
				h1 = (Sm_boxlength / power_of_two(j_x)) * a1;
				h2 = (Sm_boxlength / power_of_two(j_x)) * a2;
				
				if (((h1 == x_min) && (h2 == x_max)) || ((h1 == x_min) && (h2 != x_max))) {
					A = a2;
				} else if (a1 == a2) {
					A = a1;
				} else {
					if ((a1 % 2) == 0) {
						A = a1;
					} else {
						A = a2;
					}
				}
				
				j_minus_k = (int) (Math.log(1 + (A ^ (A-1))) / Math.log(2) - 1);
				k = j_x - j_minus_k;
				Sm_x_level = k-1;
				Sm_x_position = a1 / power_of_two(j_x - Sm_x_level);
			}
			
			if (y_min != y_max) {
				a1 = (int) Math.ceil((y_min / Sm_boxlength) * power_of_two(j_y));
				a2 = (int) Math.floor((y_max / Sm_boxlength) * power_of_two(j_y));
				h1 = (Sm_boxlength / power_of_two(j_y)) * a1;
				h2 = (Sm_boxlength / power_of_two(j_y)) * a2;
				
				if (((h1 == y_min) && (h2 == y_max)) || ((h1 == y_min) && (h2 != y_max))) {
					A = a2;
				} else if (a1 == a2) {
					A = a1;
				} else {
					if ((a1 & 2) == 0) {
						A = a1;
					} else {
						A = a2;
					}
				}
				
				j_minus_k = (int) (Math.log(1 + (A ^ (A - 1))) / Math.log(2) - 1);
				k = j_y - j_minus_k;
				Sm_y_level = k-1;
				Sm_y_position = a1 / power_of_two(j_y - Sm_y_level);
			}
			
			if ((x_min != x_max) && (y_min != y_max)) {
				if (Sm_x_level == Sm_y_level) {
					Sm_level = Sm_x_level;
					Sm_position = new Point(Sm_x_position, Sm_y_position);
				} else if (Sm_x_level < Sm_y_level) {
					Sm_level = Sm_x_level;
					Sm_position = new Point(Sm_x_position, Sm_y_position / power_of_two(Sm_y_level - Sm_x_level));
				} else {
					Sm_level = Sm_y_level;
					Sm_position = new Point(Sm_x_position / power_of_two(Sm_x_level - Sm_y_level), Sm_y_position);
				}
			} else if (x_min == x_max) {
				Sm_level = Sm_y_level;
				Sm_position = new Point((int) Math.floor((x_min * power_of_two(Sm_level)) / Sm_boxlength), Sm_y_position);
			} else {
				Sm_level = Sm_x_level;
				Sm_position = new Point(Sm_x_position, (int) Math.floor((y_min * power_of_two(Sm_level)) / Sm_boxlength));
			}
			
			Sm_boxlength = Sm_boxlength / power_of_two(Sm_level);
			Sm_downleftcorner = new Point2D(Sm_dlc_x_coord + Sm_boxlength * Sm_position.getX(), Sm_dlc_y_coord + Sm_boxlength * Sm_position.getY());
			act_ptr.set_Sm_level(Sm_level + level_offset);
			act_ptr.set_Sm_boxlength(Sm_boxlength);
			act_ptr.set_Sm_downleftcorner(Sm_downleftcorner);
		}
	}
	
	private void delete_red_quad_tree_and_count_treenodes(QuadTree T) {
		T.delete_tree(T.get_root_ptr());
	}
	
	private void form_multipole_expansions(HashMap<Node, NodeAttributes> A, QuadTree T, List<QuadTreeNode> quad_tree_leaves) {
		T.set_act_ptr(T.get_root_ptr());
		form_multipole_expansions(A, T, quad_tree_leaves);
	}
	
	private void form_multipole_expansion_of_subtree(HashMap<Node, NodeAttributes> A, QuadTree T, List<QuadTreeNode> quad_tree_leaves) {
		init_expansion_Lists(T.get_act_ptr());
		set_center(T.get_act_ptr());
		
		if (T.get_act_ptr().is_leaf()) {
			quad_tree_leaves.add(T.get_act_ptr());
			form_multipole_expansion_of_leaf_node(A, T.get_act_ptr());
		} else {
			if (T.get_act_ptr().child_lt_exists()) {
				T.go_to_lt_child();
				form_multipole_expansion_of_subtree(A, T, quad_tree_leaves);
				add_shifted_expansion_to_father_expansion(T.get_act_ptr());
				T.go_to_father();
			}
			if (T.get_act_ptr().child_rt_exists()) {
				T.go_to_rt_child();
				form_multipole_expansion_of_subtree(A, T, quad_tree_leaves);
				add_shifted_expansion_to_father_expansion(T.get_act_ptr());
				T.go_to_father();
			}
			if (T.get_act_ptr().child_lb_exists()) {
				T.go_to_lb_child();
				form_multipole_expansion_of_subtree(A, T, quad_tree_leaves);
				add_shifted_expansion_to_father_expansion(T.get_act_ptr());
				T.go_to_father();
			}
			if (T.get_act_ptr().child_rb_exists()) {
				T.go_to_rb_child();
				form_multipole_expansion_of_subtree(A, T, quad_tree_leaves);
				add_shifted_expansion_to_father_expansion(T.get_act_ptr());
				T.go_to_father();
			}
		}
	}
	
	private void init_expansion_Lists(QuadTreeNode act_ptr) {
		Complex[] nulList = new Complex[precision()+1];
		
		for (int i = 0; i<= precision(); i++) {
			nulList[i] = new Complex(0, 0);
		}
		
		act_ptr.set_multipole_exp(nulList, precision());
		act_ptr.set_locale_exp(nulList, precision());
	}
	
	private void set_center(QuadTreeNode act_ptr) {
		final int BILLION = 1000000000;
		Point2D Sm_downleftcorner = act_ptr.get_Sm_downleftcorner();
		double Sm_boxlength = act_ptr.get_Sm_boxlength();
		double boxcenter_x_coord, boxcenter_y_coord;
		double rand_y;
		
		boxcenter_x_coord = Sm_downleftcorner.getX() + Sm_boxlength * 0.5;
		boxcenter_y_coord = Sm_downleftcorner.getY() + Sm_boxlength * 0.5;
		
		rand_y = (double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2);
		boxcenter_y_coord += 0.001 * Sm_boxlength * rand_y;
		
		Complex boxcenter = new Complex(boxcenter_x_coord, boxcenter_y_coord);
		act_ptr.set_Sm_center(boxcenter);
	}
	
	private void form_multipole_expansion_of_leaf_node(HashMap<Node, NodeAttributes> A, QuadTreeNode act_ptr) {
		Complex Q = new Complex(0, 0);
		Complex z_0 = act_ptr.get_Sm_center();
		Complex[] coef = new Complex[precision() + 1];
		Complex z_v_minus_z_0_over_k;
		List<Node> nodes_in_box;
		
		nodes_in_box = act_ptr.get_contained_nodes();
		
		for (Node v_it : nodes_in_box) {
			Q = Q.plus(new Complex(1, 0));
		}
		coef[0] = Q;
		
		for (int i = 1; i <= precision(); i++) {
			coef[i] = new Complex(0, 0);
		}
		
		for (Node v_it : nodes_in_box) {
			Complex z_v = new Complex(A.get(v_it).get_x(), A.get(v_it).get_y());
			z_v_minus_z_0_over_k = z_v.minus(z_0);
			
			for (int k = 1; k <= precision(); k++) {
				coef[k] = coef[k].plus(z_v_minus_z_0_over_k.times(-1).times(1/k));
				z_v_minus_z_0_over_k = z_v_minus_z_0_over_k.times(z_v.minus(z_0));
			}
		}
		
		act_ptr.replace_multipole_exp(coef, precision());
	}
	
	private void add_shifted_expansion_to_father_expansion(QuadTreeNode act_ptr) {
		QuadTreeNode father_ptr = act_ptr.get_father_ptr();
		Complex sum;
		Complex z_0, z_1;
		Complex[] z_0_minus_z_1_over = new Complex[precision() + 1];
		
		z_1 = father_ptr.get_Sm_center();
		z_0 = act_ptr.get_Sm_center();
		father_ptr.get_multipole_exp()[0] = father_ptr.get_multipole_exp()[0].plus(act_ptr.get_multipole_exp()[0]);
		
		z_0_minus_z_1_over[0] = new Complex(1, 0);
		for (int i = 1; i <= precision(); i++) {
			z_0_minus_z_1_over[i] = z_0_minus_z_1_over[i-1].times(z_0.minus(z_1));
		}
		
		for (int k = 1; k <= precision(); k++) {
			sum = act_ptr.get_multipole_exp()[0].times(z_0_minus_z_1_over[k].times(-1/k));
			
			for (int s = 1; s <= k; s++) {
				sum = sum.plus(act_ptr.get_multipole_exp()[s].times(z_0_minus_z_1_over[k-s].times(binko(k-1, s-1))));
			}
			
			father_ptr.get_multipole_exp()[k] = father_ptr.get_multipole_exp()[k].plus(sum);
		}
	}
	
	private void calculate_local_expansions_and_WSPRLS(HashMap<Node, NodeAttributes> A, QuadTreeNode act_node_ptr) {
		List<QuadTreeNode> I, L, L2, E, D1, D2, M;
		QuadTreeNode father_ptr = null, selected_node_ptr;
		
		if (!act_node_ptr.is_root()) {
			father_ptr = act_node_ptr.get_father_ptr();
		}
		
		I = new ArrayList<QuadTreeNode>();
		L = new ArrayList<QuadTreeNode>();
		L2 = new ArrayList<QuadTreeNode>();
		E = new ArrayList<QuadTreeNode>();
		D1 = new ArrayList<QuadTreeNode>();
		D2 = new ArrayList<QuadTreeNode>();
		M = new ArrayList<QuadTreeNode>();
		
		if (act_node_ptr.is_root()) {
			if (act_node_ptr.child_lt_exists()) {
				E.add(act_node_ptr.get_child_lt_ptr());
			}
			if (act_node_ptr.child_rt_exists()) {
				E.add(act_node_ptr.get_child_rt_ptr());
			}
			if (act_node_ptr.child_lb_exists()) {
				E.add(act_node_ptr.get_child_lb_ptr());
			}
			if (act_node_ptr.child_rb_exists()) {
				E.add(act_node_ptr.get_child_rb_ptr());
			}
		} else {
			E = father_ptr.get_D1();
			I = father_ptr.get_I();
			
			E.addAll(I);
			I.clear();
		}
		
		while (!E.isEmpty()) {
			selected_node_ptr = E.remove(0);
			if (well_separated(act_node_ptr, selected_node_ptr)) {
				L.add(selected_node_ptr);
			} else if (act_node_ptr.get_Sm_level() < selected_node_ptr.get_Sm_level()) {
				I.add(selected_node_ptr);
			} else if (!selected_node_ptr.is_leaf()) {
				if (selected_node_ptr.child_lt_exists()) {
					E.add(selected_node_ptr.get_child_lt_ptr());
				}
				if (selected_node_ptr.child_rt_exists()) {
					E.add(selected_node_ptr.get_child_rt_ptr());
				}
				if (selected_node_ptr.child_lb_exists()) {
					E.add(selected_node_ptr.get_child_lb_ptr());
				}
				if (selected_node_ptr.child_rb_exists()) {
					E.add(selected_node_ptr.get_child_rb_ptr());
				}
			} else if (bordering(act_node_ptr, selected_node_ptr)) {
				D1.add(selected_node_ptr);
			} else if ((selected_node_ptr != act_node_ptr) && (act_node_ptr.is_leaf())) {
				D2.add(selected_node_ptr);
			} else if ((selected_node_ptr != act_node_ptr) && !(act_node_ptr.is_leaf())) {
				L2.add(selected_node_ptr);
			}
		}
		
		act_node_ptr.set_I(I);
		act_node_ptr.set_D1(D1);
		act_node_ptr.set_D2(D2);
		
		if (!act_node_ptr.is_root()) {
			add_shifted_local_exp_of_parent(act_node_ptr);
		}
		
		for (QuadTreeNode ptr_it : L) {
			add_local_expansion(ptr_it, act_node_ptr);
		}
		
		for (QuadTreeNode ptr_it : L2) {
			add_local_expansion_of_leaf(A, ptr_it, act_node_ptr);
		}
		
		if (!act_node_ptr.is_leaf()) {
			if (act_node_ptr.child_lt_exists()) {
				calculate_local_expansions_and_WSPRLS(A, act_node_ptr.get_child_lt_ptr());
			}
			if (act_node_ptr.child_rt_exists()) {
				calculate_local_expansions_and_WSPRLS(A, act_node_ptr.get_child_rt_ptr());
			}
			if (act_node_ptr.child_lb_exists()) {
				calculate_local_expansions_and_WSPRLS(A, act_node_ptr.get_child_lb_ptr());
			}
			if (act_node_ptr.child_rb_exists()) {
				calculate_local_expansions_and_WSPRLS(A, act_node_ptr.get_child_rb_ptr());
			}
		} else {
			D1 = act_node_ptr.get_D1();
			D2 = act_node_ptr.get_D2();
			
			while (!I.isEmpty()) {
				selected_node_ptr = I.remove(0);
				
				if (selected_node_ptr.is_leaf()) {
					if (bordering(act_node_ptr, selected_node_ptr)) {
						D1.add(selected_node_ptr);
					} else {
						D2.add(selected_node_ptr);
					}
				} else {
					if (bordering(act_node_ptr, selected_node_ptr)) {
						if (selected_node_ptr.child_lt_exists()) {
							I.add(selected_node_ptr.get_child_lt_ptr());
						}
						if (selected_node_ptr.child_rt_exists()) {
							I.add(selected_node_ptr.get_child_rt_ptr());
						}
						if (selected_node_ptr.child_lb_exists()) {
							I.add(selected_node_ptr.get_child_lb_ptr());
						}
						if (selected_node_ptr.child_rb_exists()) {
							I.add(selected_node_ptr.get_child_rb_ptr());
						}
					} else {
						M.add(selected_node_ptr);
					}
				}
			}
			
			act_node_ptr.set_D1(D1);
			act_node_ptr.set_D2(D2);
			act_node_ptr.set_M(M);
		}
	}
	
	private boolean well_separated(QuadTreeNode node_1_ptr, QuadTreeNode node_2_ptr) {
		double boxlength1 = node_1_ptr.get_Sm_boxlength();
		double boxlength2 = node_2_ptr.get_Sm_boxlength();
		double x1_min, x1_max, y1_min, y1_max, x2_min, x2_max, y2_min, y2_max;
		boolean x_overlap, y_overlap;
		
		if (boxlength1 <= boxlength2) {
			x1_min = node_1_ptr.get_Sm_downleftcorner().getX();
			x1_max = node_1_ptr.get_Sm_downleftcorner().getX() + boxlength1;
			y1_min = node_1_ptr.get_Sm_downleftcorner().getY();
			y1_max = node_1_ptr.get_Sm_downleftcorner().getY() + boxlength1;
			
			x2_min = node_2_ptr.get_Sm_downleftcorner().getX() - boxlength2;
			x2_max = node_2_ptr.get_Sm_downleftcorner().getX() + 2*boxlength2;
			y2_min = node_2_ptr.get_Sm_downleftcorner().getY() - boxlength2;
			y2_max = node_2_ptr.get_Sm_downleftcorner().getY() + 2*boxlength2;
		} else {
			x1_min = node_1_ptr.get_Sm_downleftcorner().getX() - boxlength1;
			x1_max = node_1_ptr.get_Sm_downleftcorner().getX() + 2*boxlength1;
			y1_min = node_1_ptr.get_Sm_downleftcorner().getY() - boxlength1;
			y1_max = node_1_ptr.get_Sm_downleftcorner().getY() + 2*boxlength1;
			
			x2_min = node_2_ptr.get_Sm_downleftcorner().getX();
			x2_max = node_2_ptr.get_Sm_downleftcorner().getX() + boxlength2;
			y2_min = node_2_ptr.get_Sm_downleftcorner().getY();
			y2_max = node_2_ptr.get_Sm_downleftcorner().getY() + boxlength2;
		}
		
		if ((x1_max <= x2_min) || numexcept.nearly_equal(x1_max, x2_min) || (x2_max <= x1_min) || (numexcept.nearly_equal(x2_max, x1_min))) {
			x_overlap = false;
		} else {
			x_overlap = true;
		}
		
		if ((y1_max <= y2_min) || numexcept.nearly_equal(y1_max, y2_min) || (y2_max <= y1_min) || numexcept.nearly_equal(y2_max, y1_min)) {
			y_overlap = false;
		} else {
			y_overlap = true;
		}
		
		if (x_overlap && y_overlap) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean bordering(QuadTreeNode node_1_ptr, QuadTreeNode node_2_ptr) {
		double boxlength1 = node_1_ptr.get_Sm_boxlength();
		double boxlength2 = node_2_ptr.get_Sm_boxlength();
		double x1_min, x1_max, y1_min, y1_max, x2_min, x2_max, y2_min, y2_max;
		
		x1_min = node_1_ptr.get_Sm_downleftcorner().getX();
		x1_max = node_1_ptr.get_Sm_downleftcorner().getX() + boxlength1;
		y1_min = node_1_ptr.get_Sm_downleftcorner().getY();
		y1_max = node_1_ptr.get_Sm_downleftcorner().getY() + boxlength1;
		
		x2_min = node_2_ptr.get_Sm_downleftcorner().getX();
		x2_max = node_2_ptr.get_Sm_downleftcorner().getX() + boxlength2;
		y2_min = node_2_ptr.get_Sm_downleftcorner().getY();
		y2_max = node_2_ptr.get_Sm_downleftcorner().getY() + boxlength2;
		
		if (((x2_min <= x1_min || numexcept.nearly_equal(x2_min, x1_min)) && (x1_max <= x2_max || numexcept.nearly_equal(x1_max, x2_max)) &&
				(y2_min <= y1_min || numexcept.nearly_equal(y2_min, y1_min)) && (y1_max <= y2_max || numexcept.nearly_equal(y1_max, y2_max))) ||
				((x1_min <= x2_min || numexcept.nearly_equal(x1_min, x2_min)) && (x2_max <= x1_max || numexcept.nearly_equal(x2_max, x1_max)) && 
				(y1_min <= y2_min || numexcept.nearly_equal(y1_min, y2_min)) && (y2_max <= y1_max || numexcept.nearly_equal(y2_max, y1_max)))) {
			return false;
		} else {
			if (boxlength1 <= boxlength2) {
				if (x1_min < x2_min) {
					x1_min += boxlength1;
					x1_max += boxlength1;
				} else if (x1_max > x2_max) {
					x1_min -= boxlength1;
					x1_max -= boxlength1;
				} 
				
				if (y1_min < y2_min) {
					y1_min += boxlength1;
					y1_max += boxlength1;
				} else if (y1_max > y2_max) {
					y1_min -= boxlength1;
					y1_max -= boxlength1;
				}
			} else {
				if (x1_min < x2_min) {
					x1_min += boxlength2;
					x1_max += boxlength2;
				} else if (x1_max > x2_max) {
					x1_min -= boxlength2;
					x1_max -= boxlength2;
				} 
				
				if (y1_min < y2_min) {
					y1_min += boxlength2;
					y1_max += boxlength2;
				} else if (y1_max > y2_max) {
					y1_min -= boxlength2;
					y1_max -= boxlength2;
				}
			}
			
			if (((x2_min <= x1_min || numexcept.nearly_equal(x2_min, x1_min)) && (x1_max <= x2_max || numexcept.nearly_equal(x1_max, x2_max)) &&
					(y2_min <= y1_min || numexcept.nearly_equal(y2_min, y1_min)) && (y1_max <= y2_max || numexcept.nearly_equal(y1_max, y2_max))) || 
					((x1_min <= x2_min || numexcept.nearly_equal(x1_min, x2_min)) && (x2_max <= x1_min || numexcept.nearly_equal(x2_max, x1_max)) && 
					(y1_min <= y2_min || numexcept.nearly_equal(y1_min, y2_min)) && (y2_max <= y1_max || numexcept.nearly_equal(y2_max, y1_max)))) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	private void add_shifted_local_exp_of_parent(QuadTreeNode node_ptr) {
		QuadTreeNode father_ptr = node_ptr.get_father_ptr();
		
		Complex z_0 = father_ptr.get_Sm_center();
		Complex z_1 = node_ptr.get_Sm_center();
		Complex[] z_1_minus_z_0_over = new Complex[precision()+1];
		
		z_1_minus_z_0_over[0] = new Complex(1, 0);
		for (int i = 1; i <= precision(); i++) {
			z_1_minus_z_0_over[i] = z_1_minus_z_0_over[i-1].times(z_1.minus(z_0));
		}
		
		for (int l = 0; l <= precision(); l++) {
			Complex sum = new Complex(0, 0);
			
			for (int k = l; k <= precision(); k++) {
				sum = sum.plus(father_ptr.get_local_exp()[k].times(z_1_minus_z_0_over[k-l].times(binko(k,l))));
			}
			
			node_ptr.get_local_exp()[l] = node_ptr.get_local_exp()[l].plus(sum);
		}
	}
	
	private void add_local_expansion(QuadTreeNode ptr_0, QuadTreeNode ptr_1) {
		Complex z_0 = ptr_0.get_Sm_center();
		Complex z_1 = ptr_1.get_Sm_center();
		Complex sum, z_error;
		Complex factor;
		Complex z_1_minus_z_0_over_k;
		Complex z_1_minus_z_0_over_s;
		Complex pow_minus_1_s_plus_1;
		Complex pow_minus_1_s;
		
		if ((z_1.minus(z_0).re() <= 0) && (z_1.minus(z_0).im() == 0)) {
			z_error = z_1.minus(z_0).plus(new Complex(0.0000001, 0)).log();
			sum = ptr_0.get_multipole_exp()[0].times(z_error);
		} else {
			sum = ptr_0.get_multipole_exp()[0].times(z_1.minus(z_0).log());
		}
		
		z_1_minus_z_0_over_k = z_1.minus(z_0);
		for (int k = 1; k <= precision(); k++) {
			sum = sum.plus(ptr_0.get_multipole_exp()[k].divides(z_1_minus_z_0_over_k));
			z_1_minus_z_0_over_k = z_1_minus_z_0_over_k.times(z_1.minus(z_0));
		}
		
		ptr_1.get_local_exp()[0] = ptr_1.get_local_exp()[0].plus(sum);
		
		z_1_minus_z_0_over_s = z_1.minus(z_0);
		for (int s = 1; s <= precision(); s++) {
			pow_minus_1_s_plus_1 = new Complex((((s+1) % 2 == 0) ? 1 : -1), 0);
			pow_minus_1_s = new Complex((pow_minus_1_s_plus_1.re() == 1) ? -1 : 1, 0);
			sum = pow_minus_1_s_plus_1.times(ptr_0.get_multipole_exp()[0].divides(z_1_minus_z_0_over_s.times(s)));
			factor = pow_minus_1_s.divides(z_1_minus_z_0_over_s);
			z_1_minus_z_0_over_s = z_1_minus_z_0_over_s.times(z_1.minus(z_0));
			Complex sum_2 = new Complex(0, 0);
			
			z_1_minus_z_0_over_k = z_1.minus(z_0);
			for (int k = 1; k <= precision(); k++) {
				sum_2 = sum_2.plus(ptr_0.get_multipole_exp()[k].divides(z_1_minus_z_0_over_k.times(binko(s+k-1, k-1))));
				z_1_minus_z_0_over_k = z_1_minus_z_0_over_k.times(z_1.minus(z_0));
			}
			
			ptr_1.get_local_exp()[s] = ptr_1.get_local_exp()[s].plus(sum.plus(factor.times(sum_2)));
		}
	}
	
	private void add_local_expansion_of_leaf(HashMap<Node, NodeAttributes> A, QuadTreeNode ptr_0, QuadTreeNode ptr_1) {
		List<Node> contained_nodes = ptr_0.get_contained_nodes();
		double multipole_0_of_v = 1;
		Complex z_1 = ptr_1.get_Sm_center();
		Complex z_error;
		Complex z_1_minus_z_0_over_s;
		Complex pow_minus_1_s_plus_1;
		
		for (Node v_it : contained_nodes) {
			Complex z_0 = new Complex(A.get(v_it).get_x(), A.get(v_it).get_y());
			
			if (z_1.minus(z_0).re() <= 0 && z_1.minus(z_0).im() == 0) {
				z_error = z_1.minus(z_0).plus(new Complex(0.0000001, 0)).log();
				ptr_1.get_local_exp()[0] = ptr_1.get_local_exp()[0].plus(z_error.times(multipole_0_of_v));
			} else {
				ptr_1.get_local_exp()[0] = ptr_1.get_local_exp()[0].plus(z_1.minus(z_0).log().times(multipole_0_of_v));
			}
			
			z_1_minus_z_0_over_s = z_1.minus(z_0);
			
			for (int s = 1; s <= precision(); s++) {
				pow_minus_1_s_plus_1 = new Complex(((s+1) % 2 == 0) ? 1 : -1, 0);
				ptr_1.get_local_exp()[s] = ptr_1.get_local_exp()[s].plus(pow_minus_1_s_plus_1.divides(z_1_minus_z_0_over_s.times(s)).times(multipole_0_of_v));
				z_1_minus_z_0_over_s = z_1_minus_z_0_over_s.times(z_1.minus(z_0));
			}
		}
	}
	
	private void transform_local_exp_to_forces(HashMap<Node, NodeAttributes> A, List<QuadTreeNode> quad_tree_leaves, HashMap<Node, Point2D> F_local_exp) {
		List<Node> contained_nodes;
		Complex sum;
		Complex z_0;
		Complex z_v_minus_z_0_over_k_minus_1;
		Point2D force_vector;
		
		for (QuadTreeNode leaf_ptr : quad_tree_leaves) {
			contained_nodes = leaf_ptr.get_contained_nodes();
			z_0 = leaf_ptr.get_Sm_center();
			
			for (Node v_ptr : contained_nodes) {
				Complex z_v = new Complex(A.get(v_ptr).get_x(), A.get(v_ptr).get_y());
				sum = new Complex(0, 0);
				z_v_minus_z_0_over_k_minus_1 = new Complex(1, 0);
				
				for (int k = 1; k <= precision(); k++) {
					sum = sum.plus(leaf_ptr.get_local_exp()[k].times(z_v_minus_z_0_over_k_minus_1.times(k)));
					z_v_minus_z_0_over_k_minus_1 = z_v_minus_z_0_over_k_minus_1.times(z_v).minus(z_0);
				}
				
				force_vector = new Point2D(sum.re(), sum.im() * -1);
				F_local_exp.put(v_ptr, force_vector);
			}
		}
	}
	
	private void transform_multipole_exp_to_forces(HashMap<Node, NodeAttributes> A, List<QuadTreeNode> quad_tree_leaves, HashMap<Node, Point2D> F_multipole_exp) {
		List<QuadTreeNode> M;
		List<Node> act_contained_nodes;
		Complex sum;
		Complex z_0;
		Complex z_v_minus_z_0_over_minus_k_minus_1;
		Point2D force_vector;
		
		for (QuadTreeNode act_leaf_ptr : quad_tree_leaves) {
			act_contained_nodes = act_leaf_ptr.get_contained_nodes();
			M = act_leaf_ptr.get_M();
			
			for (QuadTreeNode M_node_ptr : M) {
				z_0 = M_node_ptr.get_Sm_center();
				
				for (Node v_ptr : act_contained_nodes) {
					Complex z_v = new Complex(A.get(v_ptr).get_x(), A.get(v_ptr).get_y());
					z_v_minus_z_0_over_minus_k_minus_1 = z_v.minus(z_0).reciprocal();
					sum = M_node_ptr.get_multipole_exp()[0].times(z_v_minus_z_0_over_minus_k_minus_1);
					
					for (int k = 1; k <= precision(); k++) {
						z_v_minus_z_0_over_minus_k_minus_1 = z_v_minus_z_0_over_minus_k_minus_1.divides(z_v.minus(z_0));
						sum = sum.minus(M_node_ptr.get_multipole_exp()[k].times(z_v_minus_z_0_over_minus_k_minus_1).times(k));
					}
					
					force_vector = new Point2D(sum.re(), sum.im() * -1);
					F_multipole_exp.put(v_ptr, F_multipole_exp.get(v_ptr).add(force_vector));
				}
			}
		}
	}
	
	private void calculate_neighbourcell_forces(HashMap<Node, NodeAttributes> A, List<QuadTreeNode> quad_tree_leaves, HashMap<Node, Point2D> F_direct) {
		List<Node> act_contained_nodes, neighbour_contained_nodes;
		List<Node> non_neighbour_contained_nodes;
		List<QuadTreeNode> neighboured_leaves;
		List<QuadTreeNode> non_neighboured_leaves;
		double act_leaf_boxlength, neighbour_leaf_boxlength;
		Point2D act_leaf_dlc, neighbour_leaf_dlc;
		Point2D f_rep_u_on_v;
		Point2D vector_v_minus_u;
		Point2D pos_u, pos_v;
		double norm_v_minus_u, scalar;
		int length;
		Node u, v;
		
		for (QuadTreeNode act_leaf_ptr : quad_tree_leaves) {
			act_contained_nodes = act_leaf_ptr.get_contained_nodes();
			
			if (act_contained_nodes.size() <= particles_in_leaves()) {
				length = act_contained_nodes.size();
				
				for (int k = 1; k < length; k++) {
					u = act_contained_nodes.get(k);
					
					for (int l = k+1; l <= length; l++) {
						v = act_contained_nodes.get(l);
						pos_u = A.get(u).get_position();
						pos_v = A.get(v).get_position();
						
						if (pos_u == pos_v) {
							pos_u = numexcept.choose_distinct_random_point_in_radius_epsilon(pos_u);
						}
						
						vector_v_minus_u = pos_v.subtract(pos_u);
						norm_v_minus_u = Basic.norm(vector_v_minus_u);
						
						f_rep_u_on_v = numexcept.f_rep_near_machine_precision(norm_v_minus_u);
						if (f_rep_u_on_v == null) {
							scalar = f_rep_scalar(norm_v_minus_u) / norm_v_minus_u;
							f_rep_u_on_v = new Point2D(scalar * vector_v_minus_u.getX(), scalar * vector_v_minus_u.getY());
						}
						
						F_direct.put(v, F_direct.get(v).add(f_rep_u_on_v));
						F_direct.put(u, F_direct.get(u).subtract(f_rep_u_on_v));
					}
				}
				
				neighboured_leaves = act_leaf_ptr.get_D1();
				act_leaf_boxlength = act_leaf_ptr.get_Sm_boxlength();
				act_leaf_dlc = act_leaf_ptr.get_Sm_downleftcorner();
				
				for (QuadTreeNode neighbour_leaf_ptr : neighboured_leaves) {
					neighbour_leaf_boxlength = neighbour_leaf_ptr.get_Sm_boxlength();
					neighbour_leaf_dlc = neighbour_leaf_ptr.get_Sm_downleftcorner();
					
					if ((act_leaf_boxlength > neighbour_leaf_boxlength) || (act_leaf_boxlength == neighbour_leaf_boxlength && act_leaf_dlc.getX() < neighbour_leaf_dlc.getX()) || 
							(act_leaf_boxlength == neighbour_leaf_boxlength && act_leaf_dlc.getX() == neighbour_leaf_dlc.getX() && act_leaf_dlc.getY() < neighbour_leaf_dlc.getY())) {
						neighbour_contained_nodes = neighbour_leaf_ptr.get_contained_nodes();
						
						for (Node v_ptr : act_contained_nodes) {
							pos_v = A.get(v_ptr).get_position();
							
							for (Node u_ptr : neighbour_contained_nodes) {
								pos_u = A.get(u_ptr).get_position();
								
								if (pos_u == pos_v) {
									pos_u = numexcept.choose_distinct_random_point_in_radius_epsilon(pos_u);
								}
								
								vector_v_minus_u = pos_v.subtract(pos_u);
								norm_v_minus_u = Basic.norm(vector_v_minus_u);
								
								f_rep_u_on_v = numexcept.f_rep_near_machine_precision(norm_v_minus_u);
								if (f_rep_u_on_v == null) {
									scalar = f_rep_scalar(norm_v_minus_u) / norm_v_minus_u;
									f_rep_u_on_v = new Point2D(scalar * vector_v_minus_u.getX(), scalar * vector_v_minus_u.getY());
								}
								
								F_direct.put(v_ptr, F_direct.get(v_ptr).add(f_rep_u_on_v));
								F_direct.put(u_ptr, F_direct.get(u_ptr).subtract(f_rep_u_on_v));
							}
						}
					}
				}
				
				non_neighboured_leaves = act_leaf_ptr.get_D2();
				for (QuadTreeNode non_neighbour_leaf_ptr : non_neighboured_leaves) {
					non_neighbour_contained_nodes = non_neighbour_leaf_ptr.get_contained_nodes();
					
					for (Node v_ptr : act_contained_nodes) {
						pos_v = A.get(v_ptr).get_position();
						
						for (Node u_ptr : non_neighbour_contained_nodes) {
							pos_u = A.get(u_ptr).get_position();
							
							if (pos_u == pos_v) {
								pos_u = numexcept.choose_distinct_random_point_in_radius_epsilon(pos_u);
							}
							
							vector_v_minus_u = pos_v.subtract(pos_u);
							norm_v_minus_u = Basic.norm(vector_v_minus_u);
							
							f_rep_u_on_v = numexcept.f_rep_near_machine_precision(norm_v_minus_u);
							if (f_rep_u_on_v == null) {
								scalar = f_rep_scalar(norm_v_minus_u) / norm_v_minus_u;
								f_rep_u_on_v = new Point2D(scalar * vector_v_minus_u.getX(), scalar * vector_v_minus_u.getY());
							}
							
							F_direct.put(v_ptr, F_direct.get(v_ptr).add(f_rep_u_on_v));
						}
					}
				}
			} else {
				for (Node v_ptr : act_contained_nodes) {
					pos_v = A.get(v_ptr).get_position();
					pos_u = numexcept.choose_distinct_random_point_in_radius_epsilon(pos_v);
					vector_v_minus_u = pos_v.subtract(pos_u);
					norm_v_minus_u = Basic.norm(vector_v_minus_u);
					f_rep_u_on_v = numexcept.f_rep_near_machine_precision(norm_v_minus_u);
					
					if (f_rep_u_on_v == null) {
						scalar = f_rep_scalar(norm_v_minus_u) / norm_v_minus_u;
						f_rep_u_on_v = new Point2D(scalar * vector_v_minus_u.getX(), scalar * vector_v_minus_u.getY());
					}
					
					F_direct.put(v_ptr, F_direct.get(v_ptr).add(f_rep_u_on_v));
				}
			}
		}
	}
	
	private void add_rep_forces(Graph g, HashMap<Node, Point2D> F_direct, HashMap<Node, Point2D> F_multipole_exp, HashMap<Node, Point2D> F_local_exp, HashMap<Node, Point2D> F_rep) {
		ArrayList<Node> nodes = new ArrayList(g.getNodes());
		
		for (Node v : nodes) {
			F_rep.put(v, F_direct.get(v).add(F_local_exp.get(v)).add(F_multipole_exp.get(v)));
		}
	}
	
	private double f_rep_scalar(double d) {
		if (d > 0) {
			return 1/d;
		} else {
			System.out.println("Error NMM:: f_rep_scalar nodes at same position");
			return 0;
		}
	}
	
	private void init_binko(int t) {
		double double_ptr;
		BK = new double[t+1][];
		
		for (int i = 0; i <= t; i++) {
			BK[i] = new double[i+1];
		}
		
		for (int i = 0; i <= t; i++) {
			BK[i][0] = BK[i][i] = 1;
		}
		
		for (int i = 2; i <= t; i++) {
			for (int j = 1; j < i; j++) {
				BK[i][j] = BK[i-1][j-1] + BK[i-1][j];
			}
		}
	}
	
	private double binko(int n, int k) {
		return BK[n][k];
	}
	
	private int tree_construction_way() {
		return _tree_construction_way;
	}
	
	private void tree_construction_way(int a) {
		_tree_construction_way = (((0 <= a) && (a <= 2)) ? a : 0);
	}
	
	private int find_sm_cell() {
		return _find_small_cell;
	}
	
	private void find_sm_cell(int a) {
		_find_small_cell = (((0 <= a) && (a <= 1)) ? a : 0);
	}
	
	private void particles_in_leaves(int b) {
		_particles_in_leaves = ((b > 1) ? b : 1);
	}
	
	private int particles_in_leaves() {
		return _particles_in_leaves;
	}
	
	private void precision(int p) {
		_precision = ((p >= 1) ? p : 1);
	}
	
	private int precision() {
		return _precision;
	}
}
