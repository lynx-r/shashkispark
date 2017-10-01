package com.workingbit.board.controller.util;

import com.github.rutledgepaulv.prune.Tree;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.MovesList;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.isSubDiagonal;

/**
 * Created by Aleksey Popryaduhin on 19:39 10/08/2017.
 */
public class HighlightMoveUtil {

  private Square selectedSquare;

  private HighlightMoveUtil(Square selectedSquare) throws BoardServiceException {
    if (selectedSquare == null || selectedSquare.getDraught() == null) {
      throw new BoardServiceException("Selected square without placed draught");
    }
    this.selectedSquare = selectedSquare;
    selectedSquare.getDraught().setHighlighted(true);
//    board.setSelectedSquare(selectedSquare);
  }

  /**
   * highlightedAssignedMoves moves for the selected square
   *
   * @param selectedSquare
   * @return
   * @throws BoardServiceException
   */
  public static MovesList highlightedAssignedMoves(Square selectedSquare) {
    if (selectedSquare == null || !selectedSquare.isOccupied()) {
      throw new BoardServiceException("Invalid selected square");
    }
    HighlightMoveUtil highlightMoveUtil = new HighlightMoveUtil(selectedSquare);
    return highlightMoveUtil.highlightAllAssignedMoves();
  }

  /**
   * Entry point for initially selected square
   */
  private MovesList highlightAllAssignedMoves() {
    List<Square> allowedMoves = new ArrayList<>();
    List<Square> beatenMoves = new ArrayList<>();
    Draught draught = selectedSquare.getDraught();
    boolean down = draught.isBlack();
    boolean queen = draught.isQueen();
    findBeatenMovesOnDiagonalsOfSelectedSquare(selectedSquare, down, queen, beatenMoves, allowedMoves);
    if (beatenMoves.isEmpty()) {
      findAllowedMoves(selectedSquare, allowedMoves, down, queen);
    }
    MovesList movesList = new MovesList();
    movesList.setBeaten(beatenMoves);
    movesList.setAllowed(allowedMoves);

    return movesList;
  }

  private void findBeatenMovesOnDiagonalsOfSelectedSquare(Square selectedSquare, boolean black, boolean queen, List<Square> beatenMoves, List<Square> allowedMoves) {
    List<List<Square>> diagonals = selectedSquare.getDiagonals();
    int indexOfSelected;
    for (List<Square> diagonal : diagonals) {
      indexOfSelected = diagonal.indexOf(selectedSquare);
      if (indexOfSelected != -1) {
        walkOnDiagonal(selectedSquare, black, queen, diagonal, beatenMoves, allowedMoves);
      }
    }
  }

  private void walkOnDiagonal(Square selectedSquare, boolean down, boolean queen, List<Square> diagonal, List<Square> beatenMoves, List<Square> allowedMoves) {
    Tree<Square> treeBeaten = Tree.empty();
    findBeatenMovesOnHalfDiagonal(diagonal, selectedSquare, down, queen, 0, true, treeBeaten.asNode(), allowedMoves);
    beatenMoves.addAll(flatTree(treeBeaten));
    treeBeaten = Tree.empty();
    findBeatenMovesOnHalfDiagonal(diagonal, selectedSquare, !down, queen, 0, true, treeBeaten.asNode(), allowedMoves);
    beatenMoves.addAll(flatTree(treeBeaten));
  }

  private void findBeatenMovesOnHalfDiagonal(List<Square> diagonal, Square selectedSquare, boolean down, boolean queen, int deep, boolean cross, Tree.Node<Square> beatenMoves, List<Square> allowedMoves) {
    if (queen) {
      findBeatenMovesForQueen(diagonal, selectedSquare, down, deep, cross, beatenMoves, allowedMoves);
    } else {
      findBeatenMovesForDraught(diagonal, selectedSquare, down, deep, beatenMoves, allowedMoves);
    }
  }

  private void findBeatenMovesForQueen(List<Square> diagonal, Square selectedSquare, boolean down, int deep, boolean cross, Tree.Node<Square> beatenMoves, List<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    List<Square> walkAllowedMoves = new ArrayList<>();
    Square next, previous = selectedSquare;
    deep++;
    boolean mustBeat;
    do {
      if (hasNotNextMove(down, squareListIterator)) {
        break;
      }
      if (down) {
//        squareListIterator.next();
//        if (!((down && squareListIterator.hasNext()) || (!down && squareListIterator.hasPrevious()))) {
//          break;
//        }
        next = squareListIterator.next();
      } else {
        next = squareListIterator.previous();
      }
      mustBeat = mustBeat(next, previous);
      if (mustBeat) {
        if (treeContains(beatenMoves, previous)) {
          return;
        }
        addBeatenMove(beatenMoves, previous);
        cross = true;
      } else if (isDraughtWithSameColor(next)) {
        return;
      }
      if (!beatenMoves.getChildren().isEmpty() && canMove(next)) {
        if (cross) {
          walkCross(down, deep, beatenMoves, allowedMoves, walkAllowedMoves, next, previous);
        }
      }
      previous = next;
    }
    while (hasNext(down, squareListIterator));

    if (!walkAllowedMoves.isEmpty() && walkAllowedMoves.contains(previous)) {
      allowedMoves.addAll(walkAllowedMoves);
    }
  }

  private void walkCross(boolean down, int deep, Tree.Node<Square> beatenMoves, List<Square> allowedMoves, List<Square> walkAllowedMoves, Square next, Square previous) {
    List<Tree.Node<Square>> children = beatenMoves.getChildren();
    Tree.Node<Square> newBeatenMoves = children.get(children.size() - 1);
    walkCrossDiagonalForBeaten(next, previous, down, deep, true, newBeatenMoves, allowedMoves);
    boolean hasBeatenOnCrossDiagonal = hasBeatenOnCrossDiagonal(next, previous);
    if (hasBeatenOnCrossDiagonal) {
      addAllowedMove(allowedMoves, next);
    } else if (newBeatenMoves.getChildren().isEmpty()) {
      addAllowedMove(walkAllowedMoves, next);
    }
  }

  private void findBeatenMovesForDraught(List<Square> diagonal, Square selectedSquare, boolean down, int deep, Tree.Node<Square> beatenMoves, List<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    deep++;
    int moveCounter = 0;

    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    walkOnDiagonalForDraught(down, deep, selectedSquare, moveCounter, squareListIterator, allowedMoves, beatenMoves);

//    if (!down) {
//      indexOfSelected++;
//    }
//    squareListIterator = diagonal.listIterator(indexOfSelected);
//    walkOnDiagonalForDraught(!down, deep, selectedSquare, moveCounter, squareListIterator, allowedMoves, beatenMoves);
  }

  private void walkOnDiagonalForDraught(boolean down, int deep, Square previous, int moveCounter, ListIterator<Square> squareListIterator, List<Square> allowedMoves, Tree.Node<Square> beatenMoves) {
    Square next;
    boolean first = down;
    boolean mustBeat;
    do {
      if (hasNotNextMove(down, squareListIterator)) {
        break;
      }
      if (down) {
        if (first) {
          squareListIterator.next();
          first = false;
        }
        if (hasNotNextMove(true, squareListIterator)) {
          break;
        }
        next = squareListIterator.next();
      } else {
        next = squareListIterator.previous();
      }
      mustBeat = mustBeat(next, previous);
      if (mustBeat) {
        if (leavesContain(beatenMoves, previous)) {
          return;
        }
        addBeatenMove(beatenMoves, previous);
        if (!allowedMoves.contains(next)) {
          addAllowedMove(allowedMoves, next);
        }
        walkCrossDiagonalForBeaten(next, previous, down, deep, false, beatenMoves, allowedMoves);
      } else if (isDraughtWithSameColor(next)) {
        return;
      }
      if (moveCounter > 0 && !down || moveCounter > 1) {
        return;
      }
      moveCounter++;
      previous = next;
    }
    while (hasNext(down, squareListIterator));
  }

  private boolean hasNext(boolean down, ListIterator<Square> squareListIterator) {
    return (down && squareListIterator.hasNext()) || (!down && squareListIterator.hasPrevious());
  }

  private boolean hasNotNextMove(boolean down, ListIterator<Square> squareListIterator) {
    return !hasNext(down, squareListIterator);
  }

  private void walkCrossDiagonalForBeaten(Square next, Square previous, boolean down, int deep, boolean queen, Tree.Node<Square> beatenMoves, List<Square> allowedMoves) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        findBeatenMovesOnHalfDiagonal(diagonal, next, down, queen, deep, false, beatenMoves, allowedMoves);
        findBeatenMovesOnHalfDiagonal(diagonal, next, !down, queen, deep, false, beatenMoves, allowedMoves);
      }
    }
  }

  private void findAllowedMoves(Square selectedSquare, List<Square> allowedMoves, boolean down, boolean queen) {
    List<List<Square>> diagonals = selectedSquare.getDiagonals();
    for (List<Square> diagonal : diagonals) {
      int indexOfSelected = diagonal.indexOf(selectedSquare);
      if (indexOfSelected != -1) {
        if (queen) {
          findAllowedForQueen(diagonal, selectedSquare, down, allowedMoves);
          findAllowedForQueen(diagonal, selectedSquare, !down, allowedMoves);
        } else {
          findAllowed(diagonal, selectedSquare, down, allowedMoves);
        }
      }
    }
  }

  private void findAllowedForQueen(List<Square> diagonal, Square selectedSquare, boolean down, List<Square> allowedMoves) {
    ListIterator<Square> squareListIterator = diagonal.listIterator(diagonal.indexOf(selectedSquare));
    while (hasNext(down, squareListIterator)) {
      findAllowedUsingIterator(down, allowedMoves, squareListIterator);
    }
  }

  private void findAllowed(List<Square> diagonal, Square selectedSquare, boolean down, List<Square> allowedMoves) {
    int index = diagonal.indexOf(selectedSquare);
    if (down) {
      index++;
    }
    ListIterator<Square> squareListIterator = diagonal.listIterator(index);
    findAllowedUsingIterator(down, allowedMoves, squareListIterator);
  }

  private void findAllowedUsingIterator(boolean down, List<Square> allowedMoves, ListIterator<Square> squareListIterator) {
    Square next;
    if (down) {
      next = squareListIterator.next();
    } else {
      if (hasNotNextMove(false, squareListIterator)) {
        return;
      }
      next = squareListIterator.previous();
    }
    if (canMove(next)) {
      addAllowedMove(allowedMoves, next);
    }
  }

  private List<Square> flatTree(Tree<Square> treeBeaten) {
    return treeBeaten.breadthFirstStream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
  }

  private void addBeatenMove(Tree.Node<Square> beatenMoves, Square previous) {
    previous.getDraught().setBeaten(true);
    beatenMoves.addChild(previous);
  }

  private boolean addAllowedMove(List<Square> allowedMoves, Square next) {
    next.setHighlighted(true);
    return allowedMoves.add(next);
  }

  private boolean hasBeatenOnCrossDiagonal(Square next, Square previous) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        return diagonal.stream().anyMatch(square -> square.isOccupied() && square.getDraught().isBeaten());
      }
    }
    return false;
  }

  private boolean leavesContain(Tree.Node<Square> beatenMoves, Square search) {
    return beatenMoves.asTree().getLeaves().anyMatch(search::equals);
  }

  private boolean treeContains(Tree.Node<Square> beatenMoves, Square search) {
    Tree.Node<Square> squareNode = beatenMoves;
    while (squareNode.getParent().isPresent()) {
      if (squareNode.getData().equals(search)) {
        return true;
      }
      squareNode = squareNode.getParent().get();
    }
    return false;
  }

  private boolean isDraughtWithSameColor(Square next) {
    return next.isOccupied() && next.getDraught().isBlack() == this.selectedSquare.getDraught().isBlack();
  }

  private boolean canMove(Square nextSquare) {
    return !nextSquare.isOccupied();
  }

  /**
   * Return allowed square for move after beat
   *
   * @param nextSquare square after previous
   * @return must or not beat
   */
  private boolean mustBeat(Square nextSquare, Square previousSquare) {
    return previousSquare.isOccupied()
        && previousSquare.getDraught().isBlack() != this.selectedSquare.getDraught().isBlack()
        && !nextSquare.isOccupied();
  }
}
