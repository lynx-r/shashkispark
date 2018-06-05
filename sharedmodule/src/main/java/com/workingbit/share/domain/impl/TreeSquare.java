package com.workingbit.share.domain.impl;

import com.github.rutledgepaulv.prune.Tree;
import com.workingbit.share.domain.DeepClone;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Aleksey Popryadukhin on 20/05/2018.
 */
@Getter
@Setter
public class TreeSquare implements DeepClone {

  private Tree<Square> tree;

  public TreeSquare() {
    this.tree = Tree.empty();
  }

  public TreeSquare(Tree.Node<Square> squareNode) {
    this.tree = squareNode.asTree();
  }

  public boolean isEmpty() {
    return tree.asNode().getData() == null && tree.asNode().getChildren().isEmpty();
  }

  public List<Square> flatTree() {
    return tree.breadthFirstStream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
  }

  public boolean contains(Square square) {
    return flatTree().contains(square);
  }

  public void addChildrenNodes(@NotNull TreeSquare subTree) {
    if (!subTree.isEmpty()) {
      tree.asNode().addChildrenNodes(subTree.asNode());
    }
  }

  public Tree.Node<Square> asNode() {
    return tree.asNode();
  }

  public void addTree(@NotNull TreeSquare subTree) {
    addChildrenNodes(subTree);
  }

  public void addChild(Square square) {
    tree.asNode().addChild(square);
  }

  public Optional<Tree.Node<Square>> getParent() {
    return tree.asNode().getParent();
  }

  public Square getData() {
    return tree.asNode().getData();
  }

  public List<TreeSquare> getChildren() {
    return tree.asNode().getChildren().stream().map(TreeSquare::new).collect(Collectors.toList());
  }

  public int getMaxDepth() {
    return tree.getMaxDepth();
  }

  public Stream<Square> getDepth(int deep) {
    return tree.getDepth(deep);
  }

  public Stream<Square> getLeaves() {
    return tree.getLeaves();
  }

  public int size() {
    return flatTree().size();
  }

  public void setData(Square square) {
    tree.asNode().setData(square);
  }
}
