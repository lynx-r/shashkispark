package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.domain.impl.TreeSquare;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * MovesList
 */
@Getter
@Setter
public class MovesList {
  @NotNull
  private Set<Square> allowed = new HashSet<>();
  @NotNull
  private TreeSquare captured = new TreeSquare();
}

