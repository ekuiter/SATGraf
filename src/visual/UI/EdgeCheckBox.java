/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import visual.graph.Edge;
import javax.swing.JCheckBox;

/**
 *
 * @author zacknewsham
 */
class EdgeCheckBox extends JCheckBox{

  private final Edge conn;
  EdgeCheckBox(Edge conn) {
    super(String.format("%s", conn.toString()));
    this.conn = conn;
  }

  Edge getConnection() {
    return conn;
  }
}
