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
class HighlightMoveUtil {

  private final Square selectedSquare;

  private HighlightMoveUtil(Square selectedSquare) throws BoardServiceException {
    if (selectedSquare == null || selectedSquare.getDraught() == null) {
      throw new BoardServiceException("Selected square without placed draught");
    }
    this.selectedSquare = (Square) selectedSquare.deepClone();
    this.selectedSquare.getDraught().setHighlighted(true);
  }

  /**
   * highlightedAssignedMoves moves for the selected square
   */
  static MovesList highlightedAssignedMoves(Square selectedSquare) {
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
    List<Square> capturedMoves = new ArrayList<>();
    Draught draught = selectedSquare.getDraught();
    boolean down = draught.isBlack();
    boolean queen = draught.isQueen();
    findCapturedMovesOnDiagonalsOfSelectedSquare(selectedSquare, down, queen, capturedMoves, allowedMoves);
    if (capturedMoves.isEmpty()) {
      findAllowedMoves(selectedSquare, allowedMoves, down, queen);
    } else {
      capturedMoves = capturedMoves
          .stream()
          .distinct()
          .collect(Collectors.toList());
    }
    MovesList movesList = new MovesList();
    movesList.setCaptured(capturedMoves);
    movesList.setAllowed(allowedMoves);

    return movesList;
  }

  private void findCapturedMovesOnDiagonalsOfSelectedSquare(Square selectedSquare, boolean black, boolean queen, List<Square> capturedMoves, List<Square> allowedMoves) {
    List<List<Square>> diagonals = selectedSquare.getDiagonals();
    int indexOfSelected;
    for (List<Square> diagonal : diagonals) {
      indexOfSelected = diagonal.indexOf(selectedSquare);
      if (indexOfSelected != -1) {
        walkOnDiagonal(selectedSquare, black, queen, diagonal, capturedMoves, allowedMoves);
      }
    }
  }

  private void walkOnDiagonal(Square selectedSquare, boolean down, boolean queen, List<Square> diagonal, List<Square> capturedMoves, List<Square> allowedMoves) {
    Tree<Square> treeCaptured = Tree.empty();
    findCapturedMovesOnHalfDiagonal(diagonal, selectedSquare, down, queen, 0, true, treeCaptured.asNode(), allowedMoves);
    capturedMoves.addAll(flatTree(treeCaptured));
    treeCaptured = Tree.empty();
    findCapturedMovesOnHalfDiagonal(diagonal, selectedSquare, !down, queen, 0, true, treeCaptured.asNode(), allowedMoves);
    capturedMoves.addAll(flatTree(treeCaptured));
  }

  private void findCapturedMovesOnHalfDiagonal(List<Square> diagonal, Square selectedSquare, boolean down, boolean queen, int deep, boolean cross, Tree.Node<Square> capturedMoves, List<Square> allowedMoves) {
    if (queen) {
      findCapturedMovesForQueen(diagonal, selectedSquare, down, deep, cross, capturedMoves, allowedMoves);
    } else {
      findCapturedMovesForDraught(diagonal, selectedSquare, down, deep, capturedMoves, allowedMoves);
    }
  }

  private void findCapturedMovesForQueen(List<Square> diagonal, Square selectedSquare, boolean down, int deep, boolean cross, Tree.Node<Square> capturedMoves, List<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    List<Square> walkAllowedMoves = new ArrayList<>();
    Square next, previous = selectedSquare;
    deep++;
    boolean[] first = new boolean[]{down};
    boolean mustBeat;
    do {
      if (hasNotNextMove(down, squareListIterator)) {
        break;
      }
      next = getNextSquare(down, first, squareListIterator);
      if (next == null
          || (next.isOccupied() && next.getDraught().isCaptured())
          || (previous != null && previous.isOccupied()
          && previous.getDraught().isBlack() != this.selectedSquare.getDraught().isBlack()
          && !previous.equals(this.selectedSquare)
          && next.isOccupied())
          ) {
        break;
      }
      mustBeat = mustBeat(next, previous);
      if (mustBeat) {
        if (treeContains(capturedMoves, previous)) {
          return;
        }
        addCapturedMove(previous, capturedMoves);
        cross = true;
      } else if (isDraughtWithSameColor(next)) {
        break;
      }
      if (!capturedMoves.getChildren().isEmpty() && canMove(next)) {
        if (cross) {
          walkCross(down, deep, capturedMoves, allowedMoves, walkAllowedMoves, next, previous);
        }
      }
      previous = next;
    }
    while (hasNext(down, squareListIterator));

    if (!walkAllowedMoves.isEmpty() && walkAllowedMoves.contains(previous)
        // test case `queen_moves_with_beat_and_in_one_square`
        || previous != null && previous.isOccupied()) {
      allowedMoves.addAll(walkAllowedMoves);
    }
  }

  private void walkCross(boolean down, int deep, Tree.Node<Square> capturedMoves, List<Square> allowedMoves, List<Square> walkAllowedMoves, Square next, Square previous) {
    List<Tree.Node<Square>> children = capturedMoves.getChildren();
    Tree.Node<Square> newCapturedMoves = children.get(children.size() - 1);
    walkCrossDiagonalForCaptured(next, previous, down, deep, true, newCapturedMoves, allowedMoves);
    boolean hasCapturedOnCrossDiagonal = hasCapturedOnCrossDiagonal(next, previous);
    if (hasCapturedOnCrossDiagonal) {
      addAllowedMove(next, allowedMoves);
    } else if (newCapturedMoves.getChildren().isEmpty()) {
      addAllowedMove(next, walkAllowedMoves);
    }
  }

  private void findCapturedMovesForDraught(List<Square> diagonal, Square selectedSquare, boolean down, int deep, Tree.Node<Square> capturedMoves, List<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    deep++;
    int moveCounter = 0;

    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    walkOnDiagonalForDraught(down, deep, selectedSquare, moveCounter, squareListIterator, allowedMoves, capturedMoves);
  }

  private void walkOnDiagonalForDraught(boolean down, int deep, Square previous, int moveCounter, ListIterator<Square> squareListIterator, List<Square> allowedMoves, Tree.Node<Square> capturedMoves) {
    Square next;
    boolean[] first = new boolean[]{down};
    boolean mustBeat;
    do {
      if (hasNotNextMove(down, squareListIterator)) {
        break;
      }
      next = getNextSquare(down, first, squareListIterator);
      if (next == null || next.isOccupied() && next.getDraught().isCaptured()) {
        break;
      }
      mustBeat = mustBeat(next, previous);
      if (mustBeat) {
        if (leavesContain(capturedMoves, previous)) {
          return;
        }
        addCapturedMove(previous, capturedMoves);
        if (!allowedMoves.contains(next)) {
          addAllowedMove(next, allowedMoves);
        }
        walkCrossDiagonalForCaptured(next, previous, down, deep, false, capturedMoves, allowedMoves);
      } else if (isDraughtWithSameColor(next)) {
        return;
      }
      boolean draughtCanMoveOnOne = moveCounter > 0;
      if (draughtCanMoveOnOne && (!down || capturedMoves.getChildren().isEmpty())) {
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

  private void walkCrossDiagonalForCaptured(Square next, Square previous, boolean down, int deep, boolean queen, Tree.Node<Square> capturedMoves, List<Square> allowedMoves) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        findCapturedMovesOnHalfDiagonal(diagonal, next, down, queen, deep, false, capturedMoves, allowedMoves);
        findCapturedMovesOnHalfDiagonal(diagonal, next, !down, queen, deep, false, capturedMoves, allowedMoves);
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
    Square previous = new Square();
    boolean[] first = new boolean[]{down};
    while (hasNext(down, squareListIterator)) {
      Square next = getNextSquare(down, first, squareListIterator);
      if (next != null && canMove(next) && (previous.equals(this.selectedSquare) || canMove(previous))) {
        addAllowedMove(next, allowedMoves);
      } else {
        break;
      }
      previous = next;
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
      if (hasNotNextMove(true, squareListIterator)) {
        return;
      }
      next = squareListIterator.next();
    } else {
      if (hasNotNextMove(false, squareListIterator)) {
        return;
      }
      next = squareListIterator.previous();
    }
    if (canMove(next)) {
      addAllowedMove(next, allowedMoves);
    }
  }

  private List<Square> flatTree(Tree<Square> treeCaptured) {
    return treeCaptured.breadthFirstStream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
  }

  private void addCapturedMove(Square previous, Tree.Node<Square> capturedMoves) {
    previous.getDraught().setMarkCaptured(true);
    capturedMoves.addChild(previous);
  }

  private void addAllowedMove(Square next, List<Square> allowedMoves) {
    next.setHighlighted(true);
    allowedMoves.add(next);
  }

  private boolean hasCapturedOnCrossDiagonal(Square next, Square previous) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        return diagonal.stream().anyMatch(square -> square.isOccupied()
            && square.getDraught().isMarkCaptured());
      }
    }
    return false;
  }

  private boolean leavesContain(Tree.Node<Square> capturedMoves, Square search) {
    return capturedMoves.asTree().getLeaves().anyMatch(search::equals);
  }

  private boolean treeContains(Tree.Node<Square> capturedMoves, Square search) {
    Tree.Node<Square> squareNode = capturedMoves;
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

  private Square getNextSquare(boolean down, boolean[] first, ListIterator<Square> squareListIterator) {
    if (down) {
      if (first[0]) {
        squareListIterator.next();
        first[0] = false;
      }
      if (hasNotNextMove(true, squareListIterator)) {
        return null;
      }
      return squareListIterator.next();
    } else {
      return squareListIterator.previous();
    }
  }
}
