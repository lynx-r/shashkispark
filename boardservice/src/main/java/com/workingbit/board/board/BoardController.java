package com.workingbit.board.board;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.BoardBoxIds;
import com.workingbit.share.model.CreateBoardRequest;
import spark.Route;

import static com.workingbit.board.board.BoardUtils.getBoardServiceExceptionSupplier;
import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.JsonUtil.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardController {

  public static Route findBoardByIds = (req, res) ->
      dataToJson(BoardBoxService.getInstance().findByIds(jsonToData(req.body(), BoardBoxIds.class)));

  public static Route addDraught = (req, res) ->
      dataToJson(BoardBoxService.getInstance().addDraught(jsonToData(req.body(), BoardBox.class))
          .orElseThrow(getBoardServiceExceptionSupplier("Unable to add a draught")));

  public static Route createBoard = (req, res) ->
      dataToJson(BoardBoxService.getInstance().createBoard(jsonToData(req.body(), CreateBoardRequest.class)));

  public static Route findBoardById = (req, res) ->
      dataToJson(BoardBoxService.getInstance().findById(req.params(":id"))
          .orElseThrow(getBoardServiceExceptionSupplier("Board not found")));

  public static Route highlightBoard = (req, res) ->
      dataToJson(BoardBoxService.getInstance().highlight(jsonToData(req.body(), BoardBox.class))
          .orElseThrow(getBoardServiceExceptionSupplier("Unable to highlight")));

  public static Route move = (req, res) ->
      dataToJson(BoardBoxService.getInstance().move(jsonToData(req.body(), BoardBox.class))
          .orElseThrow(getBoardServiceExceptionSupplier("Unable to move")));

  public static Route redo = (req, res) ->
      dataToJson(BoardBoxService.getInstance().redo(jsonToData(req.body(), BoardBox.class))
      .orElseThrow(getBoardServiceExceptionSupplier("Unable to redo")));

  public static Route undo = (req, res) ->
      dataToJson(BoardBoxService.getInstance().undo(jsonToData(req.body(), BoardBox.class))
      .orElseThrow(getBoardServiceExceptionSupplier("Unable to undo")));
}
