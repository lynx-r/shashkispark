package com.workingbit.share.domain.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.ICoordinates;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 09:26 10/08/2017.
 */
@Getter
@Setter
public class Square extends BaseDomain implements ICoordinates, Comparable {

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
  private boolean highlight;

  private Draught draught;

  @NotNull
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
    if (draught != null) {
      draught.setDim(dim);
    }
  }

  @NotNull
  public Square dim(int dim) {
    this.dim = dim;
    return this;
  }

  @NotNull
  public Square draught(Draught draught) {
    this.draught = draught;
    return this;
  }


  /**
   * WARN equals without comparing with super
   * @param o
   * @return
   */
  @Override
  public boolean equals(@Nullable Object o) {
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

  @NotNull
  @Override
  public String toString() {
    return "Square{" +
        "notation=" + getNotation() +
        ", notationNum=" + getNotationNum() +
        ", dim=" + dim +
        ", main=" + main +
        ", highlight=" + highlight +
        ", draught=" + draught +
        '}';
  }

  public void addDiagonal(List<Square> diagonal) {
    this.diagonals.add(diagonal);
  }

  @NotNull
  public Square highlight(boolean highlight) {
    setHighlight(highlight);
    return this;
  }

  @Override
  public int compareTo(Object o) {
    Square o1 = (Square) o;
    return o1.getNotation().compareTo(getNotation());
  }
}