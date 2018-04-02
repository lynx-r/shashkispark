//package com.workingbit.share.domain.impl;
//
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import lombok.Data;
//
//import java.util.*;
//
///**
// * Created by Aleksey Popryaduhin on 08:40 14/08/2017.
// */
//@Data
//@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
//public class BoardTreeNode {
//  private BoardSnapshot data;
//  private BoardTreeNode parent;
//  private List<BoardTreeNode> children = new ArrayList<>();
//
//  public BoardTreeNode() {
//  }
//
//  public BoardTreeNode(BoardSnapshot data) {
//    this.data = data;
//  }
//
//  public void addChild(BoardTreeNode child) {
//    child.setParent(this);
//    children.add(child);
//  }
//
//  @JsonIgnore
//  public final BoardTreeNode getRootOfTree() {
//    BoardTreeNode parent = getParent();
//    if (parent.getParent() == null) {
//      return parent;
//    }
//    return parent.getRootOfTree();
//  }
//
//  public Iterator<BoardTreeNode> breadthFirstIter() {
//    final List<BoardTreeNode> queue = new LinkedList<>();
//    BoardTreeNode root = getRootOfTree();
//    queue.add(root);
//
//    return new Iterator<BoardTreeNode>() {
//      @Override
//      public boolean hasNext() {
//        return !queue.isEmpty();
//      }
//
//      @Override
//      public BoardTreeNode next() {
//        BoardTreeNode node = queue.remove(0);
//        queue.addAll(node.getChildren());
//        return node;
//      }
//    };
//  }
//
//
//  @Override
//  public String printVariants() {
//    return "BoardTreeNode{" +
//        "data=" + data +
//        '}';
//  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (this == o) return true;
//    if (o == null || getClass() != o.getClass()) return false;
////    if (!super.equals(o)) return false;
//    BoardTreeNode that = (BoardTreeNode) o;
//    return Objects.equals(data, that.data);
//  }
//
//  @Override
//  public int hashCode() {
//    return Objects.hash(super.hashCode(), data);
//  }
//}
