package com.workingbit.board.controller;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.func.ModelHandlerFunc;
import com.workingbit.share.func.ParamsHandlerFunc;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardPayload;
import spark.Route;

import static com.workingbit.board.BoardApplication.boardBoxService;
import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_CREATED;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardBoxController {

  public static Route createBoard = (req, res) ->
      ((ModelHandlerFunc<CreateBoardPayload>) boardRequest ->
          boardBoxService
              .createBoard(boardRequest)
              .map((boardBox) -> Answer.ok(HTTP_CREATED, boardBox, Answer.Type.BOARD_BOX))
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_CREATE_BOARD + req.body()))
      ).handleRequest(req, res, CreateBoardPayload.class);

  public static Route saveBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) boardRequest ->
          boardBoxService
              .save(boardRequest)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_CREATE_BOARD + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route findBoardById = (req, res) ->
      ((ParamsHandlerFunc) params ->
          boardBoxService.findById(params.get(RequestConstants.ID))
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE,
                  String.format(ErrorMessages.BOARD_WITH_ID_NOT_FOUND, req.params(RequestConstants.ID))))
      ).handleRequest(req, res);

  public static Route addDraught = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .addDraught((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_ADD_DRAUGHT + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route highlightBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .highlight((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_HIGHLIGHT_BOARD + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route move = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .move((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_MOVE + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route redo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .redo((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_REDO + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route undo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .undo((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_UNDO + req.body()))
      ).handleRequest(req, res, BoardBox.class);

  public static Route makeWhiteStroke = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .makeWhiteStroke((BoardBox) data)
              .map(Answer::okBoardBox)
              .orElse(Answer.error(HTTP_GONE, ErrorMessages.UNABLE_TO_MAKE_WHITE_STROKE + req.body()))
      ).handleRequest(req, res, BoardBox.class);
}
