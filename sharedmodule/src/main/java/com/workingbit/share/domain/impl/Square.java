package com.workingbit.share.domain.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.ICoordinates;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 09:26 10/08/2017.
 */
//@JsonRootName(value = "square")
@Data
public class Square implements ICoordinates, BaseDomain, Comparable {

  /**
   * row
   */
  private int v;
  /**
   * col
   */
  private int h;

  /**
   * Board's dimension
   */
  @JsonIgnore
  private int dim;

  /**
   * on the main part where we have the draughts
   */
  private boolean main;

  /**
   * if square highlight for allowing to move
   */
  private boolean highlighted;

  private Draught draught;

  @JsonIgnore
  private List<List<Square>> diagonals = new ArrayList<>();

  public Square() {
  }

  public Square(int v, int h, int dim, boolean main, Draught draught) {
    this.v = v;
    this.h = h;
    this.dim = dim;
    this.main = main;
    this.draught = draught;
  }

  public Square(int v, int h, int dim, boolean main) {
    this(v, h, dim, main, null);
  }

  public boolean isOccupied() {
    return draught != null;
  }

  public void setOccupied(boolean occupied) {
  }

  @Override
  public int getV() {
    return v;
  }

  @Override
  public void setV(int v) {
    this.v = v;
  }

  @Override
  public int getH() {
    return h;
  }

  @Override
  public void setH(int h) {
    this.h = h;
  }

  @Override
  public int getDim() {
    return dim;
  }

  @Override
  public void setDim(int dim) {
    this.dim = dim;
  }

  public Square dim(int dim) {
    this.dim = dim;
    return this;
  }

  public Square draught(Draught draught) {
    this.draught = draught;
    return this;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Square square = (Square) o;
    return v == square.v &&
        h == square.h;
  }

  @Override
  public int hashCode() {
    return Objects.hash(v, h);
  }

  @Override
  public String toString() {
    return "Square{" +
        "notation=" + getNotation() +
        ", highlight=" + highlighted +
        ", draught=" + draught +
        '}';
  }

  public void addDiagonal(List<Square> diagonal) {
    this.diagonals.add(diagonal);
  }

  public Square highlight(boolean highlight) {
    setHighlighted(highlight);
    return this;
  }

  @JsonIgnore
  @Override
  public String getId() {
    return null;
  }

  @Override
  public void setId(String id) {

  }

  @JsonIgnore
  @Override
  public LocalDateTime getCreatedAt() {
    return null;
  }

  @Override
  public void setCreatedAt(LocalDateTime createdAt) {

  }

  @Override
  public int compareTo(Object o) {
    Square o1 = (Square) o;
    return o1.getNotation().compareTo(getNotation());
  }
}