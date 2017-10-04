package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Aleksey Popryaduhin on 21:29 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NotationStroke {

  private Integer count;
  private NotationAtomStroke first;
  private NotationAtomStroke second;
}
