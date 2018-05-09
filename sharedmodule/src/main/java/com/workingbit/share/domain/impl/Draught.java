package com.workingbit.share.domain.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.ICoordinates;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 09:28 10/08/2017.
 */
@Getter
@Setter
public class Draught extends BaseDomain implements ICoordinates{
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
  private boolean captured;
  private boolean markCaptured;
  private boolean highlight;

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

  public Draught setMarkCaptured(boolean markCaptured) {
    this.markCaptured = markCaptured;
    return this;
  }

  /**
   * WARN equals without comparing with super
   * @param o
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Draught draught = (Draught) o;
    return v == draught.v &&
        h == draught.h &&
        black == draught.black &&
        queen == draught.queen &&
        captured == draught.captured;
  }

  @Override
  public int hashCode() {
    return Objects.hash(v, h, black, queen, captured);
  }

  @Override
  public String toString() {
    return "Draught{" +
        "notation=" + getNotation() +
        ", v=" + v +
        ", h=" + h +
        ", black=" + black +
        ", queen=" + queen +
        ", captured=" + captured +
        ", highlight=" + highlight +
        '}';
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
  public LocalDateTime getUpdatedAt() {
    return null;
  }

  @Override
  public void setUpdatedAt(LocalDateTime createdAt) {

  }

  @JsonIgnore
  @Override
  public boolean isReadonly() {
    return false;
  }

  @Override
  public void setReadonly(boolean readonly) {

  }
}
