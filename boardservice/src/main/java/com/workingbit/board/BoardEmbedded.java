package com.workingbit.board;

import com.workingbit.board.config.AppProperties;
import com.workingbit.board.config.Authority;
import com.workingbit.board.controller.BoardBoxController;
import com.workingbit.board.dao.BoardBoxDao;
import com.workingbit.board.dao.BoardDao;
import com.workingbit.board.dao.NotationDao;
import com.workingbit.board.service.BoardBoxService;
import com.workingbit.board.service.BoardStoreService;
import com.workingbit.board.service.NotationParserService;
import com.workingbit.board.service.NotationStoreService;
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
  public static NotationParserService notationParserService;
  public static BoardStoreService boardStoreService;
  public static NotationStoreService notationStoreService;
  public static BoardBoxDao boardBoxDao;
  public static BoardDao boardDao;
  public static NotationDao notationDao;

  public static AppProperties appProperties;

  static {
    OrchestrateModule.loadModule();

    appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

    boardBoxService = new BoardBoxService();
    notationParserService = new NotationParserService();
    boardStoreService = new BoardStoreService();
    notationStoreService = new NotationStoreService();

    boardBoxDao = new BoardBoxDao(appProperties);
    boardDao = new BoardDao(appProperties);
    notationDao = new NotationDao(appProperties);
  }

  public static void main(String[] args) {
    int port = appProperties.port();
    System.out.println("Listening on port: " + port);
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
          get(Authority.BOARD_BY_ID.getPath(), BoardBoxController.findBoardById);
          post(Authority.BOARD_BY_IDS.getPath(), BoardBoxController.findBoardByIds);
          post(Authority.BOARD_ADD_DRAUGHT.getPath(), BoardBoxController.addDraught);
          post(Authority.BOARD.getPath(), BoardBoxController.createBoard);
          put(Authority.BOARD_PUT.getPath(), BoardBoxController.saveBoard);
          post(Authority.PARSE_PDN.getPath(), BoardBoxController.parsePdn);
          post(Authority.BOARD_MOVE.getPath(), BoardBoxController.move);
          post(Authority.BOARD_HIGHLIGHT.getPath(), BoardBoxController.highlightBoard);
          post(Authority.BOARD_REDO.getPath(), BoardBoxController.redo);
          post(Authority.BOARD_UNDO.getPath(), BoardBoxController.undo);
          post(Authority.BOARD_LOAD_PREVIEW.getPath(), BoardBoxController.loadPreviewBoard);
          post(Authority.BOARD_SWITCH.getPath(), BoardBoxController.switchNotation);
          post(Authority.BOARD_FORK.getPath(), BoardBoxController.forkNotation);
          post(Authority.BOARD_VIEW_BRANCH.getPath(), BoardBoxController.viewBranch);
          post(Authority.CHANGE_TURN.getPath(), BoardBoxController.changeTurn);

          exception(RequestException.class, ExceptionHandler.handle);
          notFound((req, res) -> dataToJson(Answer.error(HTTP_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND)));
          internalServerError((req, res) -> dataToJson(Answer.error(HTTP_INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR)));

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        })
    );
  }
}
