package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 21:29 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationStroke implements DeepClone {

  private Integer count;
  private NotationAtomStroke first;
  private NotationAtomStroke second;
  private NotationStrokes alternative = new NotationStrokes();

  public NotationStroke(Integer count, NotationAtomStroke first, NotationAtomStroke second) {
    this.count = count;
    this.first = first;
    this.second = second;
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
    NotationStroke that = (NotationStroke) o;
    return Objects.equals(count, that.count) &&
        Objects.equals(first, that.first) &&
        Objects.equals(second, that.second);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), count, first, second);
  }
}
