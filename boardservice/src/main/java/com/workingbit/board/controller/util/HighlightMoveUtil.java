package com.workingbit.board.controller.util;

import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.domain.impl.TreeSquare;
import com.workingbit.share.model.MovesList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.isSubDiagonal;

/**
 * Created by Aleksey Popryaduhin on 19:39 10/08/2017.
 */
public class HighlightMoveUtil {

  private final Square selectedSquare;

  private HighlightMoveUtil(@Nullable Square selectedSquare) throws BoardServiceException {
    if (selectedSquare == null || selectedSquare.getDraught() == null) {
      throw new BoardServiceException("Selected square without placed draught");
    }
    this.selectedSquare = selectedSquare.deepClone();
    this.selectedSquare.getDraught().setHighlight(true);
  }

  /**
   * getHighlightedAssignedMoves move for the selected square
   */
  @NotNull
  public static MovesList getHighlightedAssignedMoves(@Nullable Square selectedSquare) {
    if (selectedSquare != null && !selectedSquare.isOccupied()) {
      throw new BoardServiceException("Invalid selected square");
    }
    HighlightMoveUtil highlightMoveUtil = new HighlightMoveUtil(selectedSquare);
    return highlightMoveUtil.highlightAllAssignedMoves();
  }

  /**
   * Entry point for initially selected square
   */
  @NotNull
  private MovesList highlightAllAssignedMoves() {
    Set<Square> allowedMoves = new HashSet<>();
    TreeSquare capturedMoves = new TreeSquare();
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
                                                            @NotNull TreeSquare capturedMoves, @NotNull Set<Square> allowedMoves) {
    List<List<Square>> diagonals = selectedSquare.getDiagonals();
    int indexOfSelected;
    for (List<Square> diagonal : diagonals) {
      indexOfSelected = diagonal.indexOf(selectedSquare);
      if (indexOfSelected != -1) {
        walkOnDiagonal(selectedSquare, black, queen, diagonal, capturedMoves, allowedMoves);
      }
    }
  }

  private void walkOnDiagonal(@NotNull Square selectedSquare, boolean down, boolean queen, @NotNull List<Square> diagonal,
                              TreeSquare capturedMoves, @NotNull Set<Square> allowedMoves) {
    TreeSquare capturedMovesEmpty = new TreeSquare();
    findCapturedMovesOnHalfDiagonal(diagonal, selectedSquare, down, queen, 0, true,
        capturedMovesEmpty, allowedMoves);
    capturedMoves.addChildrenNodes(capturedMovesEmpty);
    capturedMovesEmpty = new TreeSquare();
    findCapturedMovesOnHalfDiagonal(diagonal, selectedSquare, !down, queen, 0, true,
        capturedMovesEmpty, allowedMoves);
    capturedMoves.addChildrenNodes(capturedMovesEmpty);
  }

  private void findCapturedMovesOnHalfDiagonal(@NotNull List<Square> diagonal, @NotNull Square selectedSquare, boolean down,
                                               boolean queen, int deep, boolean cross, @NotNull TreeSquare capturedMoves,
                                               @NotNull Set<Square> allowedMoves) {
    if (queen) {
      findCapturedMovesForQueen(diagonal, selectedSquare, down, deep, cross, capturedMoves, allowedMoves);
    } else {
      findCapturedMovesForDraught(diagonal, selectedSquare, down, deep, cross, capturedMoves, allowedMoves);
    }
  }

  private void findCapturedMovesForQueen(List<Square> diagonal, Square selectedSquare, boolean down, int deep,
                                         boolean cross, @NotNull TreeSquare capturedMoves, @NotNull Set<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    Square next;
    Square previous = selectedSquare;
    deep++;
    AtomicBoolean first = new AtomicBoolean();
    first.set(down);
    Set<TailWalk> tail = new HashSet<>();
    boolean mustBeat;
    if (hasNotNextMove(down, squareListIterator)) {
      return;
    }
    next = getNextSquare(down, first, squareListIterator);
    if (isInvalidStep(next, previous)) {
      return;
    }
    mustBeat = mustBeat(next, previous);
    cross = beat(cross, capturedMoves, allowedMoves, next, previous, mustBeat);
    continueCrossSearch(down, deep, cross, capturedMoves, allowedMoves, next, previous, tail);
    previous = next;
    while (hasNext(down, squareListIterator) && !hasNotNextMove(down, squareListIterator)) {
      next = getNextSquare(down, first, squareListIterator);
      if (isInvalidStep(next, previous)) {
        break;
      }
      mustBeat = mustBeat(next, previous);
      cross = beat(cross, capturedMoves, allowedMoves, next, previous, mustBeat);
      continueCrossSearch(down, deep, cross, capturedMoves, allowedMoves, next, previous, tail);
      previous = next;
    }

    boolean hasTail = !tail.isEmpty() && !capturedMoves.isEmpty();
    if (hasTail) {
      boolean hasCross = tail.stream().anyMatch(TailWalk::isCross);
      if (hasCross) {
        Set<Square> squares = tail
            .stream()
            .filter(TailWalk::isCross)
            .map(TailWalk::getSquare)
            .collect(Collectors.toSet());
        allowedMoves.addAll(squares);
      } else {
        allowedMoves.addAll(tail
            .stream()
            .map(TailWalk::getSquare)
            .collect(Collectors.toSet())
        );
      }
    }
  }

  private void continueCrossSearch(boolean down, int deep, boolean cross, @NotNull TreeSquare capturedMoves,
                                   @NotNull Set<Square> allowedMoves, Square next, Square previous,
                                   Set<TailWalk> tail) {
    if (hasCapturedAndCanMove(capturedMoves, next) && cross) {
      Set<Square> walking = new HashSet<>();
      walkCross(down, deep, capturedMoves, walking, next, previous);
      if (!walking.isEmpty() || capturedMoves.getMaxDepth() == 0) {
        allowedMoves.addAll(walking);
        tail.add(new TailWalk(next, !walking.isEmpty()));
      }
    }
  }

  private boolean beat(boolean cross, @NotNull TreeSquare capturedMoves, @NotNull Set<Square> allowedMoves, Square next, Square previous, boolean mustBeat) {
    if (mustBeat) {
      if (capturedMoves.contains(previous)) {
        return false;
      }
      TreeSquare walkCaptured = new TreeSquare();
      addCapturedMove(previous, walkCaptured);
      addAllowedMove(next, allowedMoves);
      capturedMoves.addTree(walkCaptured);
      cross = true;
    } else if (isDraughtWithSameColor(next)) {
      return false;
    }
    return cross;
  }

  private void walkCross(boolean down, int deep, TreeSquare capturedMoves,
                         @NotNull Set<Square> walkAllowedMoves, @NotNull Square next, Square previous) {
    Set<Square> crossAllowed = new HashSet<>();
    walkCrossDiagonalForCaptured(next, previous, down, deep, down, true, capturedMoves, crossAllowed);
    if (hasMarkedToCaptureOnCrossDiagonal(next, previous) || !capturedMoves.isEmpty()) {
      addAllowedMove(next, walkAllowedMoves);
    }
    walkAllowedMoves.addAll(crossAllowed);
  }

  private void findCapturedMovesForDraught(List<Square> diagonal, @NotNull Square selectedSquare, boolean down, int deep,
                                           boolean cross, @NotNull TreeSquare capturedMoves, @NotNull Set<Square> allowedMoves) {
    int indexOfSelected = diagonal.indexOf(selectedSquare);
    deep++;
    int moveCounter = 0;

    ListIterator<Square> squareListIterator = diagonal.listIterator(indexOfSelected);
    walkOnDiagonalForDraught(down, deep, cross, selectedSquare, moveCounter, squareListIterator, allowedMoves, capturedMoves);
  }

  private void walkOnDiagonalForDraught(boolean down, int deep, boolean cross, @NotNull Square previous, int moveCounter,
                                        @NotNull ListIterator<Square> squareListIterator, @NotNull Set<Square> allowedMoves,
                                        @NotNull TreeSquare capturedMoves) {
    Square next;
    AtomicBoolean first = new AtomicBoolean();
    first.set(down);
    do {
      if (hasNotNextMove(down, squareListIterator)) {
        return;
      }
      next = getNextSquare(down, first, squareListIterator);
      if (isCanNotMoveNextAndNextIsCaptured(previous, next)) {
        return;
      }
      if (mustBeat(next, previous)) {
        if (capturedMoves.contains(previous)) {
          return;
        }
        TreeSquare walkCaptured = new TreeSquare();
        addCapturedMove(previous, walkCaptured);
        addAllowedMove(next, allowedMoves);
        capturedMoves.addTree(walkCaptured);
        walkCrossDiagonalForCaptured(next, previous, down, deep, false, false, capturedMoves, allowedMoves);
      } else if (isDraughtWithSameColor(next)) {
        return;
      }
      moveCounter++;
      previous = next;
    }
    while (hasNext(down, squareListIterator));
  }

  private void walkCrossDiagonalForCaptured(Square next, Square previous, boolean down, int deep, boolean cross, boolean queen,
                                            @NotNull TreeSquare capturedMoves, @NotNull Set<Square> allowedMoves) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        findCapturedMovesOnHalfDiagonal(diagonal, next, down, queen, deep, false, capturedMoves, allowedMoves);
        findCapturedMovesOnHalfDiagonal(diagonal, next, !down, queen, deep, false, capturedMoves, allowedMoves);
      }
    }
  }

  private void findAllowedMoves(Square selectedSquare, @NotNull Set<Square> allowedMoves, boolean down, boolean queen) {
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
                                   @NotNull Set<Square> allowedMoves) {
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

  private void findAllowed(List<Square> diagonal, Square selectedSquare, boolean down, @NotNull Set<Square> allowedMoves) {
    int index = diagonal.indexOf(selectedSquare);
    if (down) {
      index++;
    }
    ListIterator<Square> squareListIterator = diagonal.listIterator(index);
    findAllowedUsingIterator(down, allowedMoves, squareListIterator);
  }

  private void findAllowedUsingIterator(boolean down, @NotNull Set<Square> allowedMoves,
                                        @NotNull ListIterator<Square> squareListIterator) {
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

  private void addCapturedMove(Square previous, TreeSquare capturedMoves) {
    Draught draught = previous.getDraught();
    if (draught.getMarkCaptured() == 0 && !draught.isCaptured()) {
      draught.setMarkCaptured(capturedMoves.size() + 1);
    }
    capturedMoves.setData(previous);
  }

  private void addAllowedMove(@Nullable Square next, @NotNull Set<Square> allowedMoves) {
    if (next == null) {
      return;
    }
    next.setHighlight(true);
    allowedMoves.add(next);
  }

  private Square getNextSquare(boolean down, @NotNull AtomicBoolean first, @NotNull ListIterator<Square> squareListIterator) {
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
  private boolean mustBeat(Square nextSquare, @Nullable Square previousSquare) {
    return previousSquare != null && previousSquare.isOccupied()
        && !isDraughtWithSameColor(previousSquare)
        && isDraughtNotOccupied(nextSquare)
        && !previousSquare.getDraught().isCaptured();
  }

  private boolean hasCapturedAndCanMove(TreeSquare capturedMoves, Square next) {
    return !capturedMoves.isEmpty() && isDraughtNotOccupied(next);
  }

  private boolean hasMarkedToCaptureOnCrossDiagonal(Square next, Square previous) {
    for (List<Square> diagonal : next.getDiagonals()) {
      if (!isSubDiagonal(Arrays.asList(previous, next), diagonal)) {
        return diagonal.stream().anyMatch(square -> square.isOccupied()
            && square.getDraught().getMarkCaptured() > 0
            && !square.getDraught().isCaptured());
      }
    }
    return false;
  }

  private boolean hasNext(boolean down, @NotNull ListIterator<Square> squareListIterator) {
    return (down && squareListIterator.hasNext()) || (!down && squareListIterator.hasPrevious());
  }

  private boolean hasNotNextMove(boolean down, @NotNull ListIterator<Square> squareListIterator) {
    return !hasNext(down, squareListIterator);
  }

  private boolean isInvalidStep(@Nullable Square next, @Nullable Square previous) {
    return next == null
        || (next.isOccupied() && next.getDraught().isCaptured())
        || (previous != null && previous.isOccupied()
        && previous.getDraught().isBlack() != this.selectedSquare.getDraught().isBlack()
        && !previous.equals(this.selectedSquare)
        && next.isOccupied());
  }

  private boolean isDraughtWithSameColor(@Nullable Square next) {
    return next != null &&
        next.isOccupied() &&
        next.getDraught().isBlack() == this.selectedSquare.getDraught().isBlack();
  }

  private boolean isMoveAllowed(@Nullable Square previous, Square next) {
    return previous != null && isDraughtNotOccupied(next) && (previous.equals(this.selectedSquare) || isDraughtNotOccupied(previous));
  }

  private boolean isDraughtNotOccupied(@Nullable Square toSquare) {
    return toSquare != null && !toSquare.isOccupied();
  }

  private boolean isCanNotMoveNextAndNextIsCaptured(@NotNull Square previous, @Nullable Square next) {
    return next == null
        || !next.isOccupied() && !previous.isOccupied()
        || previous == selectedSquare && !next.isOccupied()
        || next.isOccupied() && (
        next.getDraught().isCaptured()
            || previous != selectedSquare && previous.isOccupied());
  }

  private boolean isDraughtOnDiagonal(Square selectedSquare, List<Square> diagonal) {
    return diagonal.indexOf(selectedSquare) != -1;
  }

  private static class TailWalk {
    private Square square;
    private boolean cross;

    TailWalk(Square square, boolean cross) {
      this.square = square;
      this.cross = cross;
    }

    public Square getSquare() {
      return square;
    }

    public void setSquare(Square square) {
      this.square = square;
    }

    boolean isCross() {
      return cross;
    }
  }
}
