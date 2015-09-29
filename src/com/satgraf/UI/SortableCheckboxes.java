/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.UI;

import java.util.Comparator;

/**
 *
 * @author zacknewsham
 */
public interface SortableCheckboxes<T extends Object> {
  void sort(Comparator<T> comp);
}
