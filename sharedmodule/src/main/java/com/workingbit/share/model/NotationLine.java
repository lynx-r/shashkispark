package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeName("NotationLine")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotationLine {
  /**
   * notation number
   */
  private Integer currentIndex;
  /**
   * variant id on current notation number
   */
  private Integer variantIndex;
}
