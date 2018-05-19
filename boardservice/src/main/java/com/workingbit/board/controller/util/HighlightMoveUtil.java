package com.workingbit.board.controller.util;

import com.github.rutledgepaulv.prune.Tree;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.MovesList;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    this.selectedSquare = selectedSquare.deepClone();
    this.selectedSquare.getDraught().setHighlight(true);
  }

  /**
   * getHighlightedAssignedMoves move for the selected square
   */
  static MovesList getHighlightedAssignedMoves(Square selectedSquare) {
    if (selectedSquare != null && !selectedSquare.isOccupied()) {
      throw new BoardServiceException("Invalid selected square");
    }
    HighlightMoveUtil highlightMoveUtil = new HighlightMoveUtil(selectedSquare);
    return highlightMoveUtil.highlightAllAssignedMoves();
  }

  /**
   * Entry point for initially selected square
   */
  private MovesList highlightAllAssignedMoves() {
    Set<Square> allowedMoves = new HashSet<>();
    Set<Square> capturedMoves = new HashSet<>();
    Draught draught = selectedSquare.getDraught();
    boolean down = draught.isBlack();
    boolean queen = draught.isQueen();
    findCapturedMovesOnDiagonalsOfSelectedSquare(selectedSquare, down, queen, capturedMoves, allowedMoves);
    if (capturedMoves.isEmpty()) {
      findAllowedMoves(selectedSquare, allowedMoves, down, queen);
    }
    MovesList movesList = new MovesList();
    movesList.setCaptured(capturedMoves);
    movesList.setAllowed(allowedMoves);

    return movesList;
  }

  private void findCapturedMovesOnDiagonalsOfSelectedSquare(Square selectedSquare, boolean black, boolean queen,
                                                            Set<Square> capturedMoves, Set<Square> allowedMoves) {
    List<List<Square>> diagonals = selectedSquare.getDiagonals();
    int indexOfSelected;
    for (List<Square> diagonal : diagonals) {
      indexOfSelected = diagonal.indexOf(selectedSquare);
      if (indexOfSelected != -1) {
        walkOnDiagonal(selectedSquare, black, queen, diagonal, capturedMoves, allowedMoves);
      }
    }
  }

  private void walkOnDiagonal(Square selectedSquare, boolean down, boolean queen, List<Square> diagonal,
                              Set<Square> capturedMoves, Set<Square> allowedMoves) {
    Tree<Square> treeCaptured = Tree.empty();
    Tree.Node<Square> capturedMovesEmpty = treeCaptured.asNode();
    findCapturedMovesOnHalfDiagonal(diagonal, selectedSquare, down, queen, 0, true,
        capturedMovesEmpty, allowedMoves);
    capturedMoves.addAll(flatTree(treeCaptured));
    treeCaptured = Tree.empty();
    capturedMovesEmpty = treeCaptured.asNode();
    findCapturedMovesOnHalfDiagonal(diagonal, selectedSquare, !down, queen, 0, true,
        capturedMovesEmpty, allowedMoves);
    capturedMoves.addAll(flatTree(treeCaptured));
  }

  private void findCapturedMovesOnHalfDiagonal(List<Square> diagonal, Square selectedSquare, boolean down,
                                               boolean queen, int deep, boolean cross, Tree.Node<Square> capturedMoves,
                                               Set<Square> allowedMoves) {
    if (queen) {
      findCapturedMovesForQueen(diagonal, selectedSquare, down, deep, cross, capturedMoves, allowedMoves);
    } else {
      findCapturedMovesForDraught(diagonal, selectedSquare, down, deep, capturedMoves, allowedMoves);
    }
  }

  private void findCapturedMovesForQueen(List<Square> diagonal, Square selectedSquare, boolean down, int deep,
                                         boolean cross, Tree.Node<Square> capturedMoves, Set<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    Set<Square> walkAllowedMoves = new HashSet<>();
    Square next, previous = selectedSquare;
    deep++;
    AtomicBoolean first = new AtomicBoolean();
    first.set(down);
    Set<Square> tail = new HashSet<>();
    boolean mustBeat;
    do {
      if (hasNotNextMove(down, squareListIterator)) {
        break;
      }
      next = getNextSquare(down, first, squareListIterator);
      if (isInvalidStep(next, previous)) {
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
      if (hasCapturedAndCanMove(capturedMoves, next)) {
        if (cross) {
          walkCross(down, deep, capturedMoves, allowedMoves, walkAllowedMoves, next, previous);
          tail.add(next);
        }
      }
      previous = next;
    }
    while (hasNext(down, squareListIterator));

    if (hasQueenWalkedThereOrMovesWithBeatInOneSquare(walkAllowedMoves, previous)) {
      allowedMoves.addAll(walkAllowedMoves);
    }
    boolean hasTail = !tail.isEmpty() && !capturedMoves.getChildren().isEmpty() && capturedMoves.asTree().getMaxDepth() == 1;
    if (hasTail) {
      allowedMoves.addAll(tail);
    }
    allowTailIfMoreThenOneHighlightedOnItsDepth(deep, capturedMoves, allowedMoves, tail);
  }

  private void walkCross(boolean down, int deep, Tree.Node<Square> capturedMoves, Set<Square> allowedMoves,
                         Set<Square> walkAllowedMoves, Square next, Square previous) {
    List<Tree.Node<Square>> children = capturedMoves.getChildren();
    Tree.Node<Square> newCapturedMoves = children.get(children.size() - 1);
    Set<Square> crossAllowed = new HashSet<>();
    walkCrossDiagonalForCaptured(next, previous, down, deep, true, newCapturedMoves, crossAllowed);
    if (hasMarkedToCaptureOnCrossDiagonal(next, previous)) {
      addAllowedMove(next, allowedMoves);
    } else if (newCapturedMoves.getChildren().isEmpty() && !crossAllowed.isEmpty()) {
      addAllowedMove(next, walkAllowedMoves);
    }
    allowedMoves.addAll(crossAllowed);
  }

  private void findCapturedMovesForDraught(List<Square> diagonal, Square selectedSquare, boolean down, int deep,
                                           Tree.Node<Square> capturedMoves, Set<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    deep++;
    int moveCounter = 0;

    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    walkOnDiagonalForDraught(down, deep, selectedSquare, moveCounter, squareListIterator, allowedMoves, capturedMoves);
  }

  private void walkOnDiagonalForDraught(boolean down, int deep, Square previous, int moveCounter,
                                        ListIterator<Square> squareListIterator, Set<Square> allowedMoves,
                                        Tree.Node<Square> capturedMoves) {
    Square next;
    AtomicBoolean first = new AtomicBoolean();
    first.set(down);
    do {
      if (hasNotNextMove(down, squareListIterator)) {
        break;
      }
      next = getNextSquare(down, first, squareListIterator);
      if (isCanNotMoveNextAndNextIsCaptured(previous, next)) {
        break;
      }
      if (isDraughtStopMoveOrCapturingFinished(down, moveCounter, capturedMoves)) {
        return;
      }
      if (mustBeat(next, previous)) {
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
//      if (isDraughtStopMoveOrCapturingFinished(down, moveCounter, capturedMoves)) {
//        return;
//      }
      moveCounter++;
      previous = next;
    }
    while (hasNext(down, squareListIterator));
  }

  private void walkCrossDiagonalForCaptured(Square next, Square previous, boolean down, int deep, boolean queen,
                                            Tree.Node<Square> capturedMoves, Set<Square> allowedMoves) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        findCapturedMovesOnHalfDiagonal(diagonal, next, down, queen, deep, false, capturedMoves, allowedMoves);
        findCapturedMovesOnHalfDiagonal(diagonal, next, !down, queen, deep, false, capturedMoves, allowedMoves);
      }
    }
  }

  private void findAllowedMoves(Square selectedSquare, Set<Square> allowedMoves, boolean down, boolean queen) {
    List<List<Square>> diagonals = selectedSquare.getDiagonals();
    for (List<Square> diagonal : diagonals) {
      if (isDraughtOnDiagonal(selectedSquare, diagonal)) {
        if (queen) {
          findAllowedForQueen(diagonal, selectedSquare, down, allowedMoves);
          findAllowedForQueen(diagonal, selectedSquare, !down, allowedMoves);
        } else {
          findAllowed(diagonal, selectedSquare, down, allowedMoves);
        }
      }
    }
  }

  private void findAllowedForQueen(List<Square> diagonal, Square selectedSquare, boolean down,
                                   Set<Square> allowedMoves) {
    ListIterator<Square> squareListIterator = diagonal.listIterator(diagonal.indexOf(selectedSquare));
    Square previous = new Square();
    AtomicBoolean first = new AtomicBoolean();
    first.set(down);
    while (hasNext(down, squareListIterator)) {
      Square next = getNextSquare(down, first, squareListIterator);
      if (isMoveAllowed(previous, next)) {
        addAllowedMove(next, allowedMoves);
      } else {
        break;
      }
      previous = next;
    }
  }

  private void findAllowed(List<Square> diagonal, Square selectedSquare, boolean down, Set<Square> allowedMoves) {
    int index = diagonal.indexOf(selectedSquare);
    if (down) {
      index++;
    }
    ListIterator<Square> squareListIterator = diagonal.listIterator(index);
    findAllowedUsingIterator(down, allowedMoves, squareListIterator);
  }

  private void findAllowedUsingIterator(boolean down, Set<Square> allowedMoves,
                                        ListIterator<Square> squareListIterator) {
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
    if (isDraughtNotOccupied(next)) {
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

  private void addAllowedMove(Square next, Set<Square> allowedMoves) {
    if (next == null) {
      return;
    }
    next.setHighlight(true);
    allowedMoves.add(next);
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

  private Square getNextSquare(boolean down, AtomicBoolean first, ListIterator<Square> squareListIterator) {
    if (down) {
      // Trick for the iterator. It returns next on the second call.
      if (first.get()) {
        squareListIterator.next();
        first.set(false);
      }
      if (hasNotNextMove(true, squareListIterator)) {
        return null;
      }
      return squareListIterator.next();
    } else {
      return squareListIterator.previous();
    }
  }

  /**
   * Return allowed square for move after beat
   *
   * @param nextSquare square after previous
   * @return must or not beat
   */
  private boolean mustBeat(Square nextSquare, Square previousSquare) {
    return previousSquare != null && previousSquare.isOccupied()
        && !isDraughtWithSameColor(previousSquare)
        && isDraughtNotOccupied(nextSquare);
  }

  /**
   * test case described in the test `HighlightMoveUtilTest::queen_moves_with_beat_in_one_square`
   *
   * @param walkAllowedMoves move where I was
   * @param previous         my previous move
   * @return move where is was contain my previous this mean
   * that I have deal with a stream that breaks on previous move. E.g. I move from e1 and have two opponent draughts
   * on f2 and f4 then allowed move will be the move that contains g3.
   */
  private boolean hasQueenWalkedThereOrMovesWithBeatInOneSquare(Set<Square> walkAllowedMoves, Square previous) {
    return !walkAllowedMoves.isEmpty() && walkAllowedMoves.contains(previous)
        || previous != null && (previous.isOccupied() || previous.isHighlight());
  }

  private boolean hasCapturedAndCanMove(Tree.Node<Square> capturedMoves, Square next) {
    return !capturedMoves.getChildren().isEmpty() && isDraughtNotOccupied(next);
  }

  private boolean hasMarkedToCaptureOnCrossDiagonal(Square next, Square previous) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        return diagonal.stream().anyMatch(square -> square.isOccupied()
            && square.getDraught().isMarkCaptured());
      }
    }
    return false;
  }

  private boolean hasCapturedOnPrevDiagonal(Square selectedSquare, List<Square> curDiagonal) {
    for (List<Square> d : selectedSquare.getDiagonals()) {
      if (!isSubDiagonal(curDiagonal, d)) {
        var f = d.stream()
            .anyMatch(square -> square.isOccupied() && square.getDraught().isCaptured());
        if (f) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasNext(boolean down, ListIterator<Square> squareListIterator) {
    return (down && squareListIterator.hasNext()) || (!down && squareListIterator.hasPrevious());
  }

  private boolean hasNotNextMove(boolean down, ListIterator<Square> squareListIterator) {
    return !hasNext(down, squareListIterator);
  }

  private boolean isInvalidStep(Square next, Square previous) {
    return next == null
        || (next.isOccupied() && next.getDraught().isCaptured())
        || (previous != null && previous.isOccupied()
        && previous.getDraught().isBlack() != this.selectedSquare.getDraught().isBlack()
        && !previous.equals(this.selectedSquare)
        && next.isOccupied());
  }

  private boolean isDraughtWithSameColor(Square next) {
    return next != null &&
        next.isOccupied() &&
        next.getDraught().isBlack() == this.selectedSquare.getDraught().isBlack();
  }

  private boolean isMoveAllowed(Square previous, Square next) {
    return isDraughtNotOccupied(next) && (previous.equals(this.selectedSquare) || isDraughtNotOccupied(previous));
  }

  private boolean isDraughtStopMoveOrCapturingFinished(boolean down, int moveCounter, Tree.Node<Square> capturedMoves) {
    return (moveCounter > 1 && (!down || capturedMoves.getChildren().isEmpty()))
        || moveCounter >= 2;
  }

  private boolean isDraughtNotOccupied(Square toSquare) {
    return toSquare != null && !toSquare.isOccupied();
  }

  private boolean isCanNotMoveNextAndNextIsCaptured(Square previous, Square next) {
    return next == null || next.isOccupied() &&
        (next.getDraught().isCaptured() ||
            previous != selectedSquare &&
                previous.isOccupied());
  }

  private boolean isDraughtOnDiagonal(Square selectedSquare, List<Square> diagonal) {
    return diagonal.indexOf(selectedSquare) != -1;
  }

  /**
   * Если на глубине квадрата хвоста есть больше одной захваченной шашки,
   * тогда идем по диагонале захваченных шашек (они на одной диагноали)
   * и если там есть клетка хвоста, то добавляем её
   * @param deep глубина погружения в дерево
   * @param capturedMoves сбитые шашки
   * @param allowedMoves разрешенные ходы
   * @param tail ходы которые были разрешены, но не были добавлены в процессе
   *             обхода
   */
  private void allowTailIfMoreThenOneHighlightedOnItsDepth(int deep, Tree.Node<Square> capturedMoves, Set<Square> allowedMoves, Set<Square> tail) {
    List<Square> capturedOnDepth = capturedMoves.asTree().getDepth(deep).collect(Collectors.toList());
    if (capturedOnDepth.size() > 1) {
      for (Square sTail : tail) {
        NEXT_TAIL:
        for (Square square : capturedOnDepth) {
          for (List<Square> squares : square.getDiagonals()) {
            if (isSubDiagonal(capturedOnDepth, squares)) {
              if (squares.contains(sTail)) {
                allowedMoves.add(sTail);
                break NEXT_TAIL;
              }
            }
          }
        }
      }
    }
  }
}
