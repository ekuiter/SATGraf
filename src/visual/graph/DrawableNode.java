/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import java.awt.Color;

/**
 *
 * @author zacknewsham
 */
public interface DrawableNode {
  public static final int NODE_DIAMETER = 7;
  public static final int NODE_X_SPACING = 20;
  public static final int NODE_Y_SPACING = 20;
  public static final int COMMUNITY_SPACING = 50;
  public static final Color[] NODE_COLORS = new Color[]{
    Color.BLUE,
    Color.YELLOW,
    Color.CYAN,
    Color.MAGENTA,
    Color.GREEN,
    Color.RED
  };
  public static final Color[] COMMUNITY_COLORS = new Color[]{
	  new Color(0xFF00FF),
	    new Color(0x00FF00),
	    new Color(0xCC00FF),
	    new Color(0x33FF00),
	    new Color(0x9900FF),
	    new Color(0x66FF00),
	    new Color(0x6600FF),
	    new Color(0x99FF00),
	    new Color(0x3300FF),
	    new Color(0xCCFF00),
	    new Color(0x0000FF),
	    new Color(0xFFFF00),
	    new Color(0xFF00CC),
	    new Color(0x00FF33),
	    new Color(0xFF33FF),
	    new Color(0x33FF33),
	    new Color(0xCC33FF),
	    new Color(0x66FF33),
	    new Color(0x9933FF),
	    new Color(0x99FF33),
	    new Color(0x6633FF),
	    new Color(0xCCFF33),
	    new Color(0x3333FF),
	    new Color(0xFFFF33),
	    new Color(0x0033FF),
	    new Color(0xFFCC00),
	    new Color(0xFF0099),
	    new Color(0x00FF66),
	    new Color(0xFF33CC),
	    new Color(0x33FF66),
	    new Color(0xFF66FF),
	    new Color(0x66FF66),
	    new Color(0xCC66FF),
	    new Color(0x99FF66),
	    new Color(0x9966FF),
	    new Color(0xCCFF66),
	    new Color(0x6666FF),
	    new Color(0xFFFF66),
	    new Color(0x3366FF),
	    new Color(0xFFCC33),
	    new Color(0x0066FF),
	    new Color(0xFF9900),
	    new Color(0xFF0066),
	    new Color(0x00FF99),
	    new Color(0xFF3399),
	    new Color(0x33FF99),
	    new Color(0xFF66CC),
	    new Color(0x66FF99),
	    new Color(0xFF99FF),
	    new Color(0x99FF99),
	    new Color(0xCC99FF),
	    new Color(0xCCFF99),
	    new Color(0x9999FF),
	    new Color(0xFFFF99),
	    new Color(0x6699FF),
	    new Color(0xFFCC66),
	    new Color(0x3399FF),
	    new Color(0xFF9933),
	    new Color(0x0099FF),
	    new Color(0xFF6600),
	    new Color(0xFF0000),
	    new Color(0x00FFCC),
	    new Color(0xFF3333),
	    new Color(0x33FFCC),
	    new Color(0xFF6666),
	    new Color(0x66FFCC),
	    new Color(0xFF9999),
	    new Color(0x99FFCC),
	    new Color(0xFFCCCC),
	    new Color(0xCCFFCC),
	    new Color(0xCCFFFF),
	    new Color(0xFFFFCC),
	    new Color(0x99FFFF),
	    new Color(0xFFCC99),
	    new Color(0x66FFFF),
	    new Color(0xFF9966),
	    new Color(0x33FFFF),
	    new Color(0xFF6633),
	    new Color(0x00FFFF),
	    new Color(0xFF3300),
  };
  public int getX();
  public int getY();
  public Color getColor();
}
