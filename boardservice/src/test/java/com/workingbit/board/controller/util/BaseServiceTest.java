package com.workingbit.board.controller.util;

import com.workingbit.board.dao.BoardDao;
import com.workingbit.board.service.BoardBoxService;
import com.workingbit.board.service.BoardService;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.EnumRules;
import com.workingbit.share.util.Utils;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByVH;


/**
 * Created by Aleksey Popryaduhin on 21:15 11/08/2017.
 */
public class BaseServiceTest {


  private BoardService boardService = new BoardService();
  private BoardBoxService boardBoxService = new BoardBoxService();

  BoardDao boardDao;

  protected BoardBox getBoard(boolean fillBoard) {
    Board board = BoardUtils.initBoard(fillBoard, false, EnumRules.RUSSIAN);
    Utils.setRandomIdAndCreatedAt(board);
    return new BoardBox(board);
  }

  protected BoardService boardService() {
    return boardService;
  }

  protected BoardBoxService boardBoxService() {
    return boardBoxService;
  }

  protected Draught getDraught(int v, int h) {
    return new Draught(v, h, getRules().getDimension());
  }

  Square getSquare(Draught draught, int v, int h) {
    return new Square(v, h, getRules().getDimension(), true, draught);
  }

  Draught getDraughtBlack(int v, int h) {
    return new Draught(v, h, getRules().getDimension(), true);
  }

  Square getSquareByVH(BoardBox board, int v, int h) {
    return findSquareByVH(board.getBoard(), v, h);
  }

  protected EnumRules getRules() {
    return EnumRules.RUSSIAN;
  }

//  BoardService getBoardServiceMock() {
//    AppProperties appProperties = mock(AppProperties.class);
//    when(appProperties.getRegion()).thenReturn("eu-central-1");
//    BoardDao boardDao = new BoardDao(appProperties);
//    return new BoardService(boardDao, objectMapper, boardHistoryService);
//  }

  protected CreateBoardPayload getCreateBoardRequest() {
    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
    createBoardPayload.setBlack(false);
    createBoardPayload.setFillBoard(false);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    createBoardPayload.setBoardBoxId(Utils.getRandomUUID());
    return createBoardPayload;
  }
}
