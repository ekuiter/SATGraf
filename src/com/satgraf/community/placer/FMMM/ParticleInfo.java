package com.satgraf.community.placer.FMMM;

import java.util.Comparator;
import java.util.List;

import com.satlib.graph.Node;

public class ParticleInfo {
	
	Node vertex = null; 							// the vertex of Graph that is associated with these attributes
	double x_y_coord = 0; 							// the x (resp. y) coordinate of the actual position of the vertex
	ParticleInfo cross_ref_item = null; 	// the iterator of the ParticleInfo-Element that contains the vertex in the list storing the other coordinates (a cross reference)
	List<ParticleInfo> subList_ptr = null; 	// points to the subList of L_x(L_y) where the actual entry of the ParticleInfo has to be stored
	ParticleInfo copy_item = null; 		// the item of this entry in the copy list
	boolean marked = false; 						// indicates if this ParticleInfo object is marked or not
	ParticleInfo tmp_item = null; 		// a temporary item that is used to construct the cross references for the copy_Lists and the subLists

	public ParticleInfo() {
	}
	
	public void set_vertex(Node v) {
		this.vertex = v;
	}
	
	public void set_x_y_coord(double c) {
		this.x_y_coord = c;
	}
	
	public void set_cross_ref_item(ParticleInfo it) {
		this.cross_ref_item = it;
	}
	
	public void set_subList_ptr(List<ParticleInfo> list) {
		this.subList_ptr = list;
	}
	
	public void set_copy_item(ParticleInfo it) {
		this.copy_item = it;
	}
	
	public void mark() {
		this.marked = true;
	}
	
	public void unmark() {
		this.marked = false;
	}
	
	public void set_tmp_cross_ref_item(ParticleInfo it) {
		this.tmp_item = it;
	}
	
	public Node get_vertex() {
		return this.vertex;
	}
	
	public double get_x_y_coord() {
		return this.x_y_coord;
	}
	
	public ParticleInfo get_cross_ref_item() {
		return this.cross_ref_item;
	}
	
	public List<ParticleInfo> get_subList_ptr() {
		return this.subList_ptr;
	}
	
	public ParticleInfo get_copy_item() {
		return this.copy_item;
	}
	
	public boolean is_marked() {
		return this.marked;
	}
	
	public ParticleInfo get_tmp_cross_ref_item() {
		return this.tmp_item;
	}
	
	public static class ParticleInfoComparer implements Comparator<ParticleInfo> {

		@Override
		public int compare(ParticleInfo a, ParticleInfo b) {
			double p = a.get_x_y_coord();
			double q = b.get_x_y_coord();
			
			if (p < q)
				return -1;
			else if (p > q)
				return 1;
			else
				return 0;
		}
		
	}
}
