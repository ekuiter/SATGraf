/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.UI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;

/**
 *
 * @author zacknewsham
 */
public class SortCheckboxesDropDown extends JComboBox<String> implements ActionListener{
  private final Map<String, Comparator> sort;
  private final SortableCheckboxes panel;
  public SortCheckboxesDropDown(SortableCheckboxes panel, Map<String, Comparator>sort){
    this.panel = panel;
    this.sort = sort;
    List<String> sorted = new ArrayList<>();
    sorted.addAll(sort.keySet());
    Collections.sort(sorted);
    for(String item: sorted){
      this.addItem(item);
    }
    this.addActionListener(this);
  }
  
  @Override
  public void actionPerformed(ActionEvent ae){
    String item = (String) this.getSelectedItem();
    panel.sort(sort.get(item));
  }
}
