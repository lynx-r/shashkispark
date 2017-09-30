package com.workingbit.board.board;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardRequest;
import spark.Route;

import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.JsonUtil.jsonToData;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardBoxController {

  public static Route createBoard = (req, res) ->
      dataToJson(
          BoardBoxService.getInstance()
              .createBoard(jsonToData(req.body(), CreateBoardRequest.class))
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to create board with request: " + req.body()))
      );

  public static Route findBoardById = (req, res) ->
      dataToJson(
          BoardBoxService.getInstance()
              .findById(req.params(":id"))
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, String.format("Board with id %s not found", req.params(":id"))))
      );

  public static Route addDraught = (req, res) ->
      ((BoardBoxHandlerFunc) data ->
          BoardBoxService.getInstance()
              .addDraught(data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to add a draught: " + req.body()))
      ).handleRequest(req, res);

  public static Route highlightBoard = (req, res) ->
      ((BoardBoxHandlerFunc) data ->
          BoardBoxService.getInstance()
              .highlight(data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to highlight board: " + req.body()))
      ).handleRequest(req, res);

  public static Route move = (req, res) ->
      ((BoardBoxHandlerFunc) data ->
          BoardBoxService.getInstance()
              .move(data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to move: " + req.body()))
      ).handleRequest(req, res);

  public static Route redo = (req, res) ->
      ((BoardBoxHandlerFunc) data ->
          BoardBoxService.getInstance()
              .redo(data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to redo: " + req.body()))
      ).handleRequest(req, res);

  public static Route undo = (req, res) ->
      ((BoardBoxHandlerFunc) data ->
          BoardBoxService.getInstance()
              .undo(data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to undo: " + req.body()))
      ).handleRequest(req, res);
}
