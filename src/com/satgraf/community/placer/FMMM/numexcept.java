package com.satgraf.community.placer.FMMM;

import java.util.Random;

import javafx.geometry.Point2D;

public class numexcept {
	
	private static double epsilon = 0.1;
	private static double POS_SMALL_DOUBLE = 1e-300;
	private static double POS_BIG_DOUBLE = 1e+300;
	private static double POS_BIG_LIMIT = POS_BIG_DOUBLE * 1e-190;
	private static double POS_SMALL_LIMIT = POS_SMALL_DOUBLE * 1e190;
	private static int BILLION = 1000000000;

	public static Point2D choose_distinct_random_point_in_disque(Point2D old_point, double xmin, double xmax, double ymin, double ymax) {
		double mindist;
		double mindist_to_xmin, mindist_to_xmax, mindist_to_ymin, mindist_to_ymax;
		double rand_x, rand_y;
		Point2D new_point = null;
		
		mindist_to_xmin = old_point.getX() - xmin;
		mindist_to_xmax = xmax - old_point.getX();
		mindist_to_ymin = old_point.getY() - ymin;
		mindist_to_ymax = ymax - old_point.getY();
		
		mindist = Math.min(Math.min(mindist_to_xmin, mindist_to_xmax), Math.min(mindist_to_ymin, mindist_to_ymax));
		
		if (mindist > 0) {
			do {
				rand_x = 2 * ((double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2) - 0.5);
				rand_y = 2 * ((double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2) - 0.5);
				new_point = new Point2D(old_point.getX() + mindist * rand_x * epsilon, old_point.getY() + mindist * rand_y * epsilon);
			} while (old_point.equals(new_point) || (Basic.norm(old_point.subtract(new_point)) >= mindist * epsilon));
		} else if (mindist == 0) {
			double mindist_x = 0;
			double mindist_y = 0;
			
			if (mindist_to_xmin > 0)
				mindist_x = (-1) * mindist_to_xmin;
			else if (mindist_to_xmax > 0)
				mindist_x = mindist_to_xmax;
			
			if (mindist_to_ymin > 0)
				mindist_y = (-1) * mindist_to_ymin;
			else if (mindist_to_ymax > 0)
				mindist_y = mindist_to_ymax;
			
			if ((mindist_x != 0) || (mindist_y != 0)) {
				do {
					 rand_x = (double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2);
					 rand_y = (double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2);
					 new_point = new Point2D(old_point.getX() + mindist * rand_x * epsilon, old_point.getY() + mindist * rand_y * epsilon);
				} while (old_point.equals(new_point));
			} else {
				System.out.println("Error DIM2:: box is equal to old_pos");
			}
		} else {
			System.out.println("Error DIM2:: choose_distinct_random_point_in_disque: old_point not in box");
		}
		
		return new_point;
	}
	
	public static Point2D choose_distinct_random_point_in_radius_epsilon(Point2D old_pos) {
		double xmin = old_pos.getX() - 1 * epsilon;
		double xmax = old_pos.getX() + 1 * epsilon;
		double ymin = old_pos.getY() - 1 * epsilon;
		double ymax = old_pos.getY() + 1 * epsilon;
		
		return choose_distinct_random_point_in_disque(old_pos, xmin, xmax, ymin, ymax);
	}
	
	public static Point2D f_rep_near_machine_precision(double distance) {
		Point2D new_force = null;
		
		double randx = (double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2);
		double randy = (double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2);
		int rand_sign_x = Basic.randomNumber(0, 1);
		int rand_sign_y = Basic.randomNumber(0, 1);
		
		if (distance > POS_BIG_LIMIT) {
			new_force = new Point2D(POS_SMALL_LIMIT * (1 + randx) * Math.pow(-1.0, rand_sign_x), POS_SMALL_LIMIT * (1 + randy) * Math.pow(-1.0, rand_sign_y));
		} else if (distance < POS_SMALL_LIMIT) {
			new_force = new Point2D(POS_BIG_LIMIT * randx * Math.pow(-1.0, rand_sign_x), POS_BIG_LIMIT * randy * Math.pow(-1.0, rand_sign_y));
		}
		
		return new_force;
	}
	
	public static Point2D f_near_machine_precision(double distance) {
		Point2D new_force = null;
		
		double randx = (double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2);
		double randy = (double)(Basic.randomNumber(1, BILLION) + 1) / (double)(BILLION + 2);
		int rand_sign_x = Basic.randomNumber(0, 1);
		int rand_sign_y = Basic.randomNumber(0, 1);
		
		if (distance < POS_SMALL_LIMIT) {
			new_force = new Point2D(POS_SMALL_LIMIT * (1 + randx) * Math.pow(-1.0, rand_sign_x), POS_SMALL_LIMIT * (1 + randy) * Math.pow(-1.0, rand_sign_y));
		} else if (distance > POS_BIG_LIMIT) {
			new_force = new Point2D(POS_BIG_LIMIT * randx * Math.pow(-1.0, rand_sign_x), POS_BIG_LIMIT * randy * Math.pow(-1.0, rand_sign_y));
		}
		
		return new_force;
	}
	
	public static boolean nearly_equal(double a, double b) {
		double delta = 1e-10;
		double small_b, big_b;
		
		if (b > 0) {
			small_b = b * (1 - delta);
			big_b = b * (1 + delta);
		} else {
			small_b = b * (1 + delta);
			big_b = b * (1 - delta);
		}
		
		if ((small_b <= a) && (a <= big_b))
			return true;
		else
			return false;
	}
}
