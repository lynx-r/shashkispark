package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.dao.BoardDao;
import com.workingbit.share.util.Utils;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.EnumRules;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByVH;


/**
 * Created by Aleksey Popryaduhin on 21:15 11/08/2017.
 */
public class BaseServiceTest {

//  @Autowired
//  BoardHistoryService boardHistoryService;

  BoardDao boardDao;

  BoardBox getBoard(boolean fillBoard) {
    Board board = BoardUtils.initBoard(fillBoard, false, EnumRules.RUSSIAN);
    Utils.setRandomIdAndCreatedAt(board);
    return new BoardBox(board);
  }

  BoardService boardService() {
    return BoardService.getInstance();
  }

  BoardBoxService boardBoxService() {
    return BoardBoxService.getInstance();
  }

  Draught getDraught(int v, int h) {
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
