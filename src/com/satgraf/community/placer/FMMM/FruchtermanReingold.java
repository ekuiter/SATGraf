package com.satgraf.community.placer.FMMM;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.geometry.Point2D;

import com.satlib.graph.Graph;
import com.satlib.graph.Node;

public class FruchtermanReingold {
	
	int _grid_quotient; // for coarsening the FrRe-grid
	int max_gridindex; // maximum index of a grid row/column
	double boxlength; // length of drawing box
	Point2D down_left_corner; // down left corner of drawing box
	
	public FruchtermanReingold() {
		grid_quotient(2);
	}
	
	private void initRepForces(HashMap<Node, Point2D> F_rep, ArrayList<Node> array_of_nodes) {
		F_rep.clear();
		for (int i = 0; i < array_of_nodes.size(); i++) {
			F_rep.put(array_of_nodes.get(i), new Point2D(0, 0));
		}
	}
	
	public void calculate_exact_repulsive_forces(Graph g, HashMap<Node, NodeAttributes> A, HashMap<Node, Point2D> F_rep) {
		Node v, u;
		Point2D f_rep_u_on_v;
		Point2D vector_v_minus_u;
		Point2D pos_u, pos_v;
		double norm_v_minus_u;
		int node_number = g.getNodeCount();
		ArrayList<Node> array_of_nodes = new ArrayList<Node>(g.getNodes());
		double scalar;
		
		initRepForces(F_rep, array_of_nodes);
		
		
		for (int i = 0; i < node_number; i++) {
			u = array_of_nodes.get(i);
			
			for (int j = i+1; j < node_number; j++) {
				v = array_of_nodes.get(j);
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
				
				F_rep.put(v, F_rep.get(v).add(f_rep_u_on_v));
				F_rep.put(u, F_rep.get(u).subtract(f_rep_u_on_v));
			}
		}
	}
	
	public void calculate_approx_repulsive_forces(Graph g, HashMap<Node, NodeAttributes> A, HashMap<Node, Point2D> F_rep) {
		List<Point> neighbour_boxes = new ArrayList<Point>();
		Point neighbour;
		Point2D f_rep_u_on_v;
		Point2D vector_v_minus_u;
		Point2D pos_u, pos_v;
		double norm_v_minus_u;
		double scalar;
		
		int i, j, act_i, act_j, k, l, length;
		Node u, v;
		double x, y, gridboxlength;
		int x_index, y_index;
		
		ArrayList<Node> nodes = new ArrayList<Node>(g.getNodes());
		initRepForces(F_rep, nodes);
		
		max_gridindex = (int) (Math.sqrt((double)g.getNodeCount() / (double)grid_quotient()) - 1);
		max_gridindex = ((max_gridindex > 0) ? max_gridindex : 0);
		ArrayList<Node>[][] contained_nodes = new ArrayList[max_gridindex][max_gridindex];
		
		for (i = 0; i <= max_gridindex; i++) {
			for (j = 0; j <= max_gridindex; j++) {
				contained_nodes[i][j] = new ArrayList<Node>();
			}
		}
		
		gridboxlength = boxlength / (max_gridindex + 1);
		
		for (Node n : nodes) {
			x = A.get(n).get_x() - down_left_corner.getX();
			y = A.get(n).get_y() - down_left_corner.getY();
			x_index = (int) (x / gridboxlength);
			y_index = (int) (y / gridboxlength);
			contained_nodes[x_index][y_index].add(n);
		}
		
		// Force calculation
		for (i = 0; i <= max_gridindex; i++) {
			for (j = 0; j <= max_gridindex; j++) {
				// Step 1: calculate forces inside contained_nodes
				ArrayList<Node> nodearray_i_j = contained_nodes[i][j];
				length = nodearray_i_j.size();
				
				for (k = 1; k < length; k++) {
					u = nodearray_i_j.get(k);
					
					for (l = k+1; l <= length; l++) {
						v = nodearray_i_j.get(l);
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
						
						F_rep.put(v, F_rep.get(v).add(f_rep_u_on_v));
						F_rep.put(u, F_rep.get(u).subtract(f_rep_u_on_v));
					}
				}
				
				// Step 2: calculate forces to nodes in neighbour boxes
				for (k = i - 1; k <= i + 1; k++) {
					for (l = j - 1; l <= j + 1; j++) {
						if ((k >= 0) && (l >= 0) && (k <= max_gridindex) && (l <= max_gridindex)) {
							neighbour = new Point(k, l);
							
							if ((k != i) || (l != j)) {
								neighbour_boxes.add(neighbour);
							}
						}
					}
				}
				
				for (Point act_neighbour_box_it : neighbour_boxes) {
					act_i = (int) act_neighbour_box_it.getX();
					act_j = (int) act_neighbour_box_it.getY();
					
					if ((act_j == j + 1) || ((act_j == j) && (act_i == i + 1))) {
						for (Node v_it : contained_nodes[i][j]) {
							for (Node u_it : contained_nodes[act_i][act_j]) {
								pos_u = A.get(u_it).get_position();
								pos_v = A.get(v_it).get_position();
								
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
								
								F_rep.put(v_it, F_rep.get(v_it).add(f_rep_u_on_v));
								F_rep.put(u_it, F_rep.get(u_it).subtract(f_rep_u_on_v));
							}
						}
					}
				}
			}
		}
	}
	
	public void make_initialisations(double bl, Point2D d_l_c, int grid_quot) {
		grid_quotient(grid_quot);
		down_left_corner = d_l_c;
		boxlength = bl;
	}
	
	public void update_boxlength_and_cornercoordinate(double b_l, Point2D d_l_c) {
		boxlength = b_l;
		down_left_corner = d_l_c;
	}
	
	private double f_rep_scalar(double d) {
		if (d > 0)
			return 1/d;
		else {
			System.out.println("Error FruchtermanReingold:: f_rep_scalar nodes at same position");
			return 0;
		}
	}
	
	private void grid_quotient(int p) {
		_grid_quotient = ((0 <= p) ? p : 2);
	}
	
	private int grid_quotient() {
		return _grid_quotient;
	}
}
