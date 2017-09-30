package com.workingbit.board.handler;

import com.workingbit.share.common.Utils;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.handler.AbstractRequestHandler;
import com.workingbit.share.handler.Answer;
import com.workingbit.share.model.CreateBoardRequest;

import java.util.Map;

import static com.workingbit.board.BoardApplication.boardDao;
import static com.workingbit.board.board.util.BoardUtils.initBoard;

/**
 * Created by Aleksey Popryaduhin on 10:31 29/09/2017.
 */
public class BoardCreateHandler extends AbstractRequestHandler<CreateBoardRequest> {

  public Board domain;

  public BoardCreateHandler(Board domain) {
    super(CreateBoardRequest.class, domain);
    this.domain = domain;
  }

  @Override
  protected Answer processImpl(CreateBoardRequest value, Map<String, String> urlParams) {
    Board board = initBoard(value.getFillBoard(), value.getBlack(),
        value.getRules());
    Utils.setRandomIdAndCreatedAt(board);
    board.setCursor(true);
    save(board);
    return null;
  }

  private void save(Board board) {
    boardDao.save(board);
  }
}
