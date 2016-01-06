package com.satgraf.graph.placer;


import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zacknewsham
 */
public class IntegerPair {
  Integer x;
  Integer y;
  public IntegerPair(Integer x, Integer y){
    this.y = y;
    this.x = x;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IntegerPair other = (IntegerPair) obj;
    if (!Objects.equals(this.x, other.x)) {
      return false;
    }
    if (!Objects.equals(this.y, other.y)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + Objects.hashCode(this.x);
    hash = 79 * hash + Objects.hashCode(this.y);
    return hash;
  }
}
