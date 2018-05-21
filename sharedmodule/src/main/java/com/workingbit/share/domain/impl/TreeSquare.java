package com.workingbit.share.domain.impl;

import com.github.rutledgepaulv.prune.Tree;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryadukhin on 20/05/2018.
 */
@Getter
@Setter
public class TreeSquare {

  private Tree<Square> tree;

  public TreeSquare() {
    this.tree = Tree.empty();
  }

  public boolean isEmpty() {
    return tree.asNode().getChildren().isEmpty();
  }

  public List<Square> flatTree() {
    return tree.breadthFirstStream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
  }

  public boolean contains(Square square) {
    return flatTree().contains(square);
  }

  public void addChildrenNodes(Tree.Node<Square> subTree) {
    if (!subTree.getChildren().isEmpty()) {
      tree.asNode().addChildrenNodes(subTree);
    }
  }

  public void addTree(TreeSquare subTree) {
    addChildrenNodes(subTree.tree.asNode());
  }
}
