package com.workingbit.board.board;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.func.ModelHandlerFunc;
import com.workingbit.share.func.ParamsHandlerFunc;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardRequest;
import spark.Route;

import static com.workingbit.board.BoardApplication.boardBoxService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardBoxController {

  public static Route createBoard = (req, res) ->
      ((ModelHandlerFunc<CreateBoardRequest>) boardRequest ->
          boardBoxService
              .createBoard(boardRequest)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to create board with request: " + req.body()))
      ).handleRequest(req, res, CreateBoardRequest.class);

  public static Route findBoardById = (req, res) ->
      ((ParamsHandlerFunc) params ->
          boardBoxService.findById(params.get(":id"))
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, String.format("Board with id %s not found", req.params(":id"))))
      );

  public static Route addDraught = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .addDraught((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to add a draught: " + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route highlightBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .highlight((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to highlight board: " + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route move = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .move((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to move: " + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route redo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .redo((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to redo: " + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route undo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .undo((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to undo: " + req.body()))
      ).handleRequest(req, res, BoardBox.class);
}
