package com.satgraf.community.placer.FMMM;

import java.util.Random;

import javafx.geometry.Point2D;

public class Basic {
	
	public static int randomNumber(int low, int high) {
		Random rand = new Random();
		return low + rand.nextInt(high - low + 1);
	}
	
	public static double norm(Point2D p) {
		return Math.sqrt(p.getX()*p.getX() + p.getY()*p.getY());
	}
}
