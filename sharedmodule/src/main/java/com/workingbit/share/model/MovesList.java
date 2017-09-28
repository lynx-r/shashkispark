package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Square;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * MovesList
 */
@Getter
@Setter
public class MovesList {
  private List<Square> allowed = new ArrayList<>();
  private List<Square> beaten = new ArrayList<>();
}

