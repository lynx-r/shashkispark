package com.workingbit.board.controller;

import com.workingbit.board.config.Authority;
import com.workingbit.orchestrate.function.ModelHandlerFunc;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import spark.Route;

import static com.workingbit.board.BoardEmbedded.boardBoxService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardBoxController {

  public static Route home = (req, res) -> "Board. Home, sweet home!";

  public static Route createBoard = (req, res) ->
      ((ModelHandlerFunc<CreateBoardPayload>) (data, token) ->
          boardBoxService
              .createBoardBox(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_BOARD))
      ).handleRequest(req, res,
          Authority.BOARD_PROTECTED,
          CreateBoardPayload.class);

  public static Route parsePdn = (req, res) ->
      ((ModelHandlerFunc<ImportPdnPayload>) (data, token) ->
          boardBoxService
              .parsePdn(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_PARSE_PDN))
      ).handleRequest(req, res,
          Authority.PARSE_PDN_PROTECTED.setAuthorities(Authority.Constants.SECURE_ROLES),
          ImportPdnPayload.class);

  public static Route initBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .initBoard(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_BOARD))
      ).handleRequest(req, res,
          Authority.BOARD_INIT_PROTECTED,
          BoardBox.class);

  public static Route clearBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .clearBoard(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_BOARD))
      ).handleRequest(req, res,
          Authority.BOARD_CLEAR_PROTECTED,
          BoardBox.class);

  public static Route saveBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .save(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_BOARD))
      ).handleRequest(req, res,
          Authority.BOARD_PROTECTED,
          BoardBox.class);

  public static Route loadPreviewBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .loadPreviewBoard(data, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_LOAD_BOARD))
      ).handleRequest(req, res,
          Authority.BOARD_LOAD_PREVIEW,
          BoardBox.class);

  public static Route findBoardById = (req, res) ->
      ((ModelHandlerFunc<DomainId>) (params, token) ->
          boardBoxService.findById(params, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND,
                  ErrorMessages.BOARD_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res, Authority.BOARD_BY_ID, DomainId.class);

  public static Route findBoardByIds = (req, res) ->
      ((ModelHandlerFunc<DomainIds>) (ids, token) ->
          boardBoxService.findByIds(ids, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND,
                  ErrorMessages.BOARD_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res, Authority.BOARD_BY_IDS, DomainIds.class);

  public static Route addDraught = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .addDraught((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_ADD_DRAUGHT))
      ).handleRequest(req, res,
          Authority.BOARD_ADD_DRAUGHT_PROTECTED,
          BoardBox.class);

  public static Route highlightBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .highlight((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_HIGHLIGHT_BOARD))
      ).handleRequest(req, res,
          Authority.BOARD_HIGHLIGHT_PROTECTED,
          BoardBox.class);

  public static Route move = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .move((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_MOVE))
      ).handleRequest(req, res,
          Authority.BOARD_MOVE_PROTECTED,
          BoardBox.class);

  public static Route redo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .redo((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_REDO))
      ).handleRequest(req, res,
          Authority.BOARD_REDO_PROTECTED,
          BoardBox.class);

  public static Route undo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .undo((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_UNDO))
      ).handleRequest(req, res,
          Authority.BOARD_UNDO_PROTECTED,
          BoardBox.class);

  public static Route switchNotation = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .switchNotation((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SWITCH))
      ).handleRequest(req, res,
          Authority.BOARD_SWITCH,
          BoardBox.class);

  public static Route viewBranch = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .viewBranch((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SWITCH))
      ).handleRequest(req, res,
          Authority.BOARD_VIEW_BRANCH,
          BoardBox.class);

  public static Route forkNotation = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .forkNotation((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_FORK))
      ).handleRequest(req, res,
          Authority.BOARD_FORK_PROTECTED,
          BoardBox.class);

  public static Route changeTurn = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token) ->
          boardBoxService
              .changeTurn((BoardBox) data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CHANGE_TURN))
      ).handleRequest(req, res,
          Authority.CHANGE_TURN_PROTECTED,
          BoardBox.class);
}
