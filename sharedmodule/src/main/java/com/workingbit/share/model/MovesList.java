package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.domain.impl.TreeSquare;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * MovesList
 */
@Getter
@Setter
public class MovesList {
  private Set<Square> allowed = new HashSet<>();
  private TreeSquare captured = new TreeSquare();
}

