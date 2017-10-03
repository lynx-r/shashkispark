package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryaduhin on 21:29 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationStroke {

  private Integer count;
  private NotationAtomStroke first;
  private NotationAtomStroke second;
}
