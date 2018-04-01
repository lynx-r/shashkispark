package com.workingbit.board.controller;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.handler.ModelHandlerFunc;
import com.workingbit.share.handler.ParamsHandlerFunc;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardPayload;
import spark.Route;

import static com.workingbit.board.BoardApplication.boardBoxService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardBoxController {

  public static Route createBoard = (req, res) ->
      ((ModelHandlerFunc<CreateBoardPayload>) boardRequest ->
          boardBoxService
              .createBoardBox(boardRequest)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_BOARD))
      ).handleRequest(req, res, CreateBoardPayload.class);

  public static Route saveBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) boardRequest ->
          boardBoxService
              .saveAndFillBoard(boardRequest)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_BOARD))
      ).handleRequest(req, res, BoardBox.class);

  public static Route loadBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) boardRequest ->
          boardBoxService
              .loadBoard(boardRequest)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_BOARD))
      ).handleRequest(req, res, BoardBox.class);

  public static Route findBoardById = (req, res) ->
      ((ParamsHandlerFunc) params ->
          boardBoxService.findById(params.get(RequestConstants.ID))
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND,
                  ErrorMessages.BOARD_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res);

  public static Route addDraught = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .addDraught((BoardBox) data)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_ADD_DRAUGHT))
      ).handleRequest(req, res, BoardBox.class);

  public static Route highlightBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .highlight((BoardBox) data)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_HIGHLIGHT_BOARD))
      ).handleRequest(req, res, BoardBox.class);

  public static Route move = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .move((BoardBox) data)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_MOVE))
      ).handleRequest(req, res, BoardBox.class);

  public static Route redo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .redo((BoardBox) data)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_REDO))
      ).handleRequest(req, res, BoardBox.class);

  public static Route undo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .undo((BoardBox) data)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_UNDO))
      ).handleRequest(req, res, BoardBox.class);

  public static Route makeWhiteStroke = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) data ->
          boardBoxService
              .makeWhiteStroke((BoardBox) data)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_MAKE_WHITE_STROKE))
      ).handleRequest(req, res, BoardBox.class);
}
