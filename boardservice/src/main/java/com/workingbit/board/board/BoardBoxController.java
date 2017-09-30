package com.workingbit.board.board;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.BoardBoxIds;
import com.workingbit.share.model.CreateBoardRequest;
import spark.Route;

import static com.workingbit.board.board.util.BoardUtils.getBoardServiceExceptionSupplier;
import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.JsonUtil.jsonToData;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardBoxController {

  public static Route findBoardByIds = (req, res) ->
      dataToJson(BoardBoxService.getInstance().findByIds(jsonToData(req.body(), BoardBoxIds.class)));

  public static Route addDraught = (req, res) ->
      ((BoardBoxHandlerFunc) data ->
          BoardBoxService.getInstance()
              .addDraught(data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to add a draught: " + data))
      ).handleRequest(req, res);

  public static Route createBoard = (req, res) ->
      dataToJson(BoardBoxService.getInstance().createBoard(jsonToData(req.body(), CreateBoardRequest.class))
          .map(Answer::okBoardBox)
          .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to create board with request: " + req.body()))
      );

  public static Route findBoardById = (req, res) ->
      dataToJson(BoardBoxService.getInstance().findById(req.params(":id"))
          .map(Answer::okBoardBox)
          .orElse(Answer.error(HTTP_BAD_REQUEST, String.format("Board with id %s not found", req.params(":id"))))
      );

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
