package com.workingbit.board.controller;

import com.workingbit.board.config.Authority;
import com.workingbit.orchestrate.function.ModelHandlerFunc;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.ImportPdnPayload;
import spark.Route;

import static com.workingbit.board.BoardEmbedded.boardBoxService;
import static com.workingbit.share.util.SparkUtils.getQueryValue;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class BoardBoxController {

  public static Route home = (req, res) -> "Board. Home, sweet home!";

  public static Route createBoard = (req, res) ->
      ((ModelHandlerFunc<CreateBoardPayload>) (data, token, param) ->
          Answer.created(boardBoxService.createBoardBox(data, token))
      ).handleRequest(req, res,
          Authority.BOARD_PROTECTED,
          CreateBoardPayload.class);

  public static Route parsePdn = (req, res) ->
      ((ModelHandlerFunc<ImportPdnPayload>) (data, token, param) -> {
        try {
          return Answer.created(boardBoxService.parsePdn(data, token));
        } catch (RequestException e) {
          return Answer.error(HTTP_INTERNAL_ERROR, e.getMessages());
        }
      }).handleRequest(req, res,
          Authority.PARSE_PDN_PROTECTED.setAuthorities(Authority.Constants.SECURE_ROLES),
          ImportPdnPayload.class);

  public static Route initBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.initBoard(data, token))
      ).handleRequest(req, res,
          Authority.BOARD_INIT_PROTECTED,
          BoardBox.class);

  public static Route clearBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.clearBoard(data, token))
      ).handleRequest(req, res,
          Authority.BOARD_CLEAR_PROTECTED,
          BoardBox.class);

  public static Route saveBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.save(data, token))
      ).handleRequest(req, res,
          Authority.BOARD_PROTECTED,
          BoardBox.class);

  public static Route loadPreviewBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          boardBoxService
              .loadPreviewBoard(data, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_LOAD_BOARD))
      ).handleRequest(req, res,
          Authority.BOARD_LOAD_PREVIEW,
          BoardBox.class);

  public static Route findBoardById = (req, res) ->
      ((ModelHandlerFunc<DomainId>) (params, token, query) ->
          Answer.ok(boardBoxService.findById(params, token, getQueryValue(query, RequestConstants.PUBLIC_QUERY)))
      ).handleRequest(req, res, Authority.BOARD_BY_ID, DomainId.class);

  public static Route findBoardByIds = (req, res) ->
      ((ModelHandlerFunc<DomainId>) (articleId, token, query) ->
          Answer.ok(boardBoxService.findByArticleId(articleId, token, getQueryValue(query, RequestConstants.PUBLIC_QUERY)))
      ).handleRequest(req, res, Authority.BOARD_BY_ARTICLE, DomainId.class);

  public static Route addDraught = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.addDraught((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_ADD_DRAUGHT_PROTECTED,
          BoardBox.class);

  public static Route highlightBoard = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.highlight((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_HIGHLIGHT_PROTECTED,
          BoardBox.class);

  public static Route move = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.moveSmart((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_MOVE_PROTECTED,
          BoardBox.class);

  public static Route redo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.redo((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_REDO_PROTECTED,
          BoardBox.class);

  public static Route undo = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.undo((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_UNDO_PROTECTED,
          BoardBox.class);

  public static Route switchNotation = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.switchNotation((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_SWITCH,
          BoardBox.class);

  public static Route viewBranch = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.viewBranch((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_VIEW_BRANCH,
          BoardBox.class);

  public static Route forkNotation = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.forkNotation((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.BOARD_FORK_PROTECTED,
          BoardBox.class);

  public static Route changeTurn = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.changeTurn((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.CHANGE_TURN_PROTECTED,
          BoardBox.class);

  public static Route changeBoardColor = (req, res) ->
      ((ModelHandlerFunc<BoardBox>) (data, token, param) ->
          Answer.created(boardBoxService.changeBoardColor((BoardBox) data, token))
      ).handleRequest(req, res,
          Authority.CHANGE_TURN_PROTECTED,
          BoardBox.class);
}
