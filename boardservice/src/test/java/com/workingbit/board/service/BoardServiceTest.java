package com.workingbit.board.service;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateBoardRequest;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 10:08 10/08/2017.
 */
public class BoardServiceTest extends BaseServiceTest {

  @Test
  public void createBoard() throws Exception {
    BoardBox boardBox = boardBoxService().createBoard(getCreateBoardRequest()).get();
    toDelete(boardBox);
    assertNotNull(boardBox.getId());
  }

//  @Test
//  public void findAll() throws Exception {
//    BoardBox board = getNewBoard();
//    toDelete(board);
//    assertNotNull(board.getId());
//    List<BoardBox> all = boardBoxService().findAll(null);
//    assertTrue(all.contains(board));
//  }

  @Test
  public void findById() throws Exception {
    BoardBox board = getNewBoard();
    toDelete(board);
    assertNotNull(board.getId());
    Optional<BoardBox> byId = boardBoxService().findById(board.getId());
    assertNotNull(byId.get());
  }

  @Test
  public void delete() throws Exception {
    BoardBox board = getNewBoard();
    String boardId = board.getId();
    assertNotNull(boardId);
    boardBoxService().delete(boardId);
    Optional<BoardBox> byId = boardBoxService().findById(boardId);
    assertTrue(!byId.isPresent());
  }

  /*
  @Test
  public void should_save_move_history() throws BoardServiceError, ExecutionException, InterruptedException {
    BoardBox board = getNewBoard();
    Draught draught = getDraught(5, 2);
    Square square = getSquareByVH(board, 5, 2);
    square.setDraught(draught);
    Square target = getSquareByVH(board, 4, 3);

    // find allowed and beaten
//    HighlightMoveService highlightMoveUtil = new HighlightMoveService(board.getBoard(), square, getRules());
//    Optional<List<Square>> allowedMovesMap = HighlightMoveService.highlightedAssignedMoves(board,square);
//    List<Square> allowedMoves = (List<Square>) allowedMovesMap.get(allowed.name());
//    List<Draught> beatenMoves = (List<Draught>) allowedMovesMap.get(beaten.name());

    // create moveTo action
    BoardBox finalBoard = board;
    Map<String, Object> moveTo = new HashMap<String, Object>() {{
      put(boardId.name(), finalBoard.getId());
      put(selectedSquare.name(), square);
      put(targetSquare.name(), target);
//      put(allowed.name(), allowedMoves);
//      put(beaten.name(), beatenMoves);
    }};

    // move draught and save
    Map<String, Object> newMoveCoords = boardService.move(moveTo);

    // find saved and check if it's selected square is equals to target
    board = boardService.findById(board.getId()).get();
//    Square newSelectedDraught = board.getSelectedSquare();
//    assertEquals(target, newSelectedDraught);

    assertEquals(newMoveCoords.get(v.name()), -60); // v - up
    assertEquals(newMoveCoords.get(h.name()), 60); // h - right

    // undo and get new board with new board container
    Map<String, Object> undoneBoard = boardHistoryService.undo(board.getId());
//    assertTrue(undoneBoard.isPresent());
//    Square oldSelectedDraught = undoneBoard.get().getBoard().getSelectedSquare();
//    assertEquals(square, oldSelectedDraught);
  }

  @Test
  public void should_undo_move() throws BoardServiceError {
    BoardBox board = getNewBoard();
    Square square = getSquareByVH(board, 5, 2);
    Square target = getSquareByVH(board, 4, 3);

//    Map<String, Object> highlightedAssignedMoves = boardService.highlightedAssignedMoves(boardId, square);
    // find allowed and beaten
//    List<Square> allowedMoves = (List<Square>) highlightedAssignedMoves.get(allowed.name());
//    List<Draught> beatenMoves = (List<Draught>) highlightedAssignedMoves.get(beaten.name());

    // create moveTo action
    Map<String, Object> moveTo = getMoveTo(board, square, target, null, null);
    MapUtils.debugPrint(System.out, "PREP MOVE", moveTo);

    // move draught and save
    Map<String, Object> newMoveCoords = boardService.move(moveTo);
    MapUtils.debugPrint(System.out, "MOVE", newMoveCoords);

    // next move
    Object newSource = newMoveCoords.get(EnumBaseKeys.targetSquare.name());
//    highlightedAssignedMoves = boardService.highlightedAssignedMoves(boardId, newSource);
    // find allowed and beaten
//    allowedMoves = (List<Square>) highlightedAssignedMoves.get(allowed.name());
//    beatenMoves = (List<Draught>) highlightedAssignedMoves.get(beaten.name());

    Square nextTarget = BoardUtils.findSquareByVH(board, 3,4).get();
    // create moveTo action
    moveTo = getMoveTo(board, target, nextTarget, null, null);
    MapUtils.debugPrint(System.out, "PREP MOVE", moveTo);

    // move draught and save
    newMoveCoords = boardService.move(moveTo);
    MapUtils.debugPrint(System.out, "MOVE", newMoveCoords);

    Map<String, Object> undo = boardHistoryService.undo(board.getId());
    MapUtils.debugPrint(System.out, "UNDO", undo);
  }
*/
//  private HashMap<String, Object> getMoveTo(BoardBox board, Square square, Square target, List<Square> allowedMoves, List<Draught> beatenMoves) {
//    return new HashMap<String, Object>() {{
//      put(boardId.name(), board.getId());
//      put(selectedSquare.name(), square);
//      put(targetSquare.name(), target);
//      put(allowed.name(), allowedMoves);
//      put(beaten.name(), beatenMoves);
//    }};
//  }

  @After
  public void tearUp() {
    boards.forEach(board -> boardBoxService().delete(board.getId()));
  }

  private List<BoardBox> boards = new ArrayList<>();

  private void toDelete(BoardBox board) {
    boards.add(board);
  }

  private BoardBox getNewBoard() {
    CreateBoardRequest createBoardRequest = getCreateBoardRequest();
    BoardBox board = boardBoxService().createBoard(createBoardRequest).get();

    // place initial draught on the desk
//    Draught draught = getDraught(5, 2);
//    Optional<Square> sel = BoardUtils.findSquareByVH(board.getBoard(), 5, 2);
//    Square square = sel.get();
//    square.setDraught(draught);
//    board.getBoard().setSelectedSquare(square);
//    boardDao.save(board);
    return board;
  }
}