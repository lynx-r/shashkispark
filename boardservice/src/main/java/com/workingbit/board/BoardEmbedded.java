package com.workingbit.board;

import com.workingbit.board.config.AppProperties;
import com.workingbit.board.config.Authority;
import com.workingbit.board.controller.BoardBoxController;
import com.workingbit.board.dao.BoardBoxDao;
import com.workingbit.board.dao.BoardDao;
import com.workingbit.board.dao.NotationDao;
import com.workingbit.board.dao.NotationHistoryDao;
import com.workingbit.board.service.*;
import com.workingbit.orchestrate.OrchestrateModule;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.ExceptionHandler;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.Answer;
import com.workingbit.share.util.Filters;
import com.workingbit.share.util.SparkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.CorsConfig.enableCors;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static spark.Spark.*;

public class BoardEmbedded {

  private static final Logger LOG = LoggerFactory.getLogger(BoardEmbedded.class);

  // Declare dependencies
  public static BoardBoxService boardBoxService;
  public static BoardService boardService;
  public static NotationService notationService;
  public static NotationHistoryService notationHistoryService;
  public static NotationParserService notationParserService;
  //  public static BoardBoxStoreService boardBoxStoreService;
//  public static NotationStoreService notationStoreService;
  public static BoardBoxDao boardBoxDao;
  public static BoardDao boardDao;
  public static NotationDao notationDao;
  public static NotationHistoryDao notationHistoryDao;

  public static AppProperties appProperties;

  static {
    OrchestrateModule.loadModule();

    appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

    boardBoxService = new BoardBoxService();
    boardService = new BoardService();
    notationHistoryService = new NotationHistoryService();
    notationService = new NotationService();
    notationParserService = new NotationParserService();
//    boardBoxStoreService = new BoardBoxStoreService();
//    notationStoreService = new NotationStoreService();

    boardBoxDao = new BoardBoxDao(appProperties);
    boardDao = new BoardDao(appProperties);
    notationDao = new NotationDao(appProperties);
    notationHistoryDao = new NotationHistoryDao(appProperties);
  }

  public static void main(String[] args) {
    int port = appProperties.port();
    LOG.info("Listening on port: " + port);
    port(port);

    Logger logger = LoggerFactory.getLogger(BoardApplication.class);
    SparkUtils.createServerWithRequestLog(logger);
    start();
  }

  public static void start() {
    LOG.info("Initializing routes");
    enableCors(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());
    establishRoutes();
  }

  private static void establishRoutes() {
    path("/", () -> get(Authority.HOME.getPath(), BoardBoxController.home));

    path("/api", () ->
        path("/v1", () -> {
          // open api
          post(Authority.BOARD_VIEW_BRANCH.getPath(), BoardBoxController.viewBranch);
          post(Authority.BOARD_BY_ID.getPath(), BoardBoxController.findBoardById);
          post(Authority.BOARDS_BY_ARTICLE.getPath(), BoardBoxController.findBoardByIds);
          post(Authority.BOARD_LOAD_PREVIEW.getPath(), BoardBoxController.loadPreviewBoard);
          post(Authority.BOARD_SWITCH.getPath(), BoardBoxController.switchNotation);

          // protected api
          post(Authority.BOARD_PROTECTED.getPath(), BoardBoxController.createBoard);
          post(Authority.BOARD_MARK_TASK_PROTECTED.getPath(), BoardBoxController.markTaskBoard);
          post(Authority.BOARD_INIT_PROTECTED.getPath(), BoardBoxController.initBoard);
          post(Authority.BOARD_CLEAR_PROTECTED.getPath(), BoardBoxController.clearBoard);
          post(Authority.PARSE_PDN_PROTECTED.getPath(), BoardBoxController.parsePdn);
          put(Authority.BOARD_PUT_PROTECTED.getPath(), BoardBoxController.saveBoard);
          post(Authority.BOARD_DELETE_PROTECTED.getPath(), BoardBoxController.deleteBoard);
          post(Authority.BOARD_DELETE_BY_ARTICLE_PROTECTED.getPath(), BoardBoxController.deleteBoardByArticleId);
          post(Authority.BOARD_ADD_DRAUGHT_PROTECTED.getPath(), BoardBoxController.addDraught);
          post(Authority.BOARD_MOVE_PROTECTED.getPath(), BoardBoxController.move);
          post(Authority.BOARD_HIGHLIGHT_PROTECTED.getPath(), BoardBoxController.highlightBoard);
          post(Authority.BOARD_REDO_PROTECTED.getPath(), BoardBoxController.redo);
          post(Authority.BOARD_UNDO_PROTECTED.getPath(), BoardBoxController.undo);
          post(Authority.BOARD_FORK_PROTECTED.getPath(), BoardBoxController.forkNotation);
          post(Authority.BOARD_REMOVE_VARIANT_PROTECTED.getPath(), BoardBoxController.removeVariant);
          post(Authority.CHANGE_TURN_PROTECTED.getPath(), BoardBoxController.changeTurn);
          post(Authority.CHANGE_COLOR_PROTECTED.getPath(), BoardBoxController.changeBoardColor);

          exception(RequestException.class, ExceptionHandler.handle);
          notFound((req, res) -> dataToJson(Answer.error(HTTP_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND)));
          internalServerError((req, res) -> dataToJson(Answer.error(HTTP_INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR)));

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        })
    );
  }
}
