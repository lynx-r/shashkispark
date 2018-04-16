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
      ((ModelHandlerFunc<CreateBoardPayload>) (data, token) ->
          boardBoxService
              .createBoardBox(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_BOARD))
      ).handleRequest(req, res, true, CreateBoardPayload.class);

  public static Route saveBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .save(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_BOARD))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route loadPreviewBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .loadPreviewBoard(data, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_LOAD_BOARD))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route findBoardById = (req, res) ->
      ((ParamsHandlerFunc) params ->
          boardBoxService.findById(params.get(RequestConstants.ID))
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND,
                  ErrorMessages.BOARD_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res);

  public static Route addDraught = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .addDraught((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_ADD_DRAUGHT))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route highlightBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .highlight((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_HIGHLIGHT_BOARD))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route move = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .move((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_MOVE))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route redo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .redo((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_REDO))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route undo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .undo((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_UNDO))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route switchNotation = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .switchNotation((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SWITCH))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route forkNotation = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .forkNotation((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_FORK))
      ).handleRequest(req, res, true, BoardBox.class);

  public static Route changeTurn = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .changeTurn((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CHANGE_TURN))
      ).handleRequest(req, res, true, BoardBox.class);
}
