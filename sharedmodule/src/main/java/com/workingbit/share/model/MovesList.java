package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Square;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * MovesList
 */
//@JsonRootName(value = "movesList")
@Getter
@Setter
public class MovesList {
  private List<Square> allowed = new ArrayList<>();
  private List<Square> captured = new ArrayList<>();
}

