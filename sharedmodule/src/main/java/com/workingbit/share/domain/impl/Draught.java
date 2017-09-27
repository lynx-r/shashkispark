package com.workingbit.share.domain.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.ICoordinates;

import java.util.Date;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 09:28 10/08/2017.
 */
public class Draught implements ICoordinates, BaseDomain {
  /**
   * row
   */
  private int v;
  /**
   * col
   */
  private int h;

  @JsonIgnore
  private int dim;

  private boolean black;
  private boolean queen;
  private boolean beaten;
  private boolean highlighted;

  public Draught() {
  }

  public Draught(int v, int h, int dim) {
    this.v = v;
    this.h = h;
    this.dim = dim;
  }

  public Draught(int v, int h, int dim, boolean black) {
    this(v, h, dim);
    this.black = black;
  }

  public Draught(int v, int h, int dim, boolean black, boolean queen) {
    this(v, h, dim, black);
    this.queen = queen;
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

  public boolean isBlack() {
    return black;
  }

  public void setBlack(boolean black) {
    this.black = black;
  }

  public boolean isQueen() {
    return queen;
  }

  public void setQueen(boolean queen) {
    this.queen = queen;
  }

  public boolean isBeaten() {
    return beaten;
  }

  public void setBeaten(boolean beaten) {
    this.beaten = beaten;
  }

  public boolean isHighlighted() {
    return highlighted;
  }

  public void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Draught draught = (Draught) o;
    return v == draught.v &&
        h == draught.h &&
        black == draught.black &&
        queen == draught.queen &&
        beaten == draught.beaten;
  }

  @Override
  public int hashCode() {
    return Objects.hash(v, h, black, queen, beaten);
  }

  @Override
  public String toString() {
    return "Draught{" +
        "notation=" + getNotation() +
        ", v=" + v +
        ", h=" + h +
        ", black=" + black +
        ", queen=" + queen +
        ", beaten=" + beaten +
        ", highlight=" + highlighted +
        '}';
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public void setId(String id) {

  }

  @Override
  public Date getCreatedAt() {
    return null;
  }

  @Override
  public void setCreatedAt(Date createdAt) {

  }
}
