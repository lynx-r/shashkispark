package com.workingbit.board;

import com.workingbit.board.config.AppProperties;
import com.workingbit.board.config.Path;
import com.workingbit.board.controller.BoardBoxController;
import com.workingbit.board.dao.BoardBoxDao;
import com.workingbit.board.dao.BoardDao;
import com.workingbit.board.dao.NotationDao;
import com.workingbit.board.service.BoardBoxService;
import com.workingbit.board.service.BoardStoreService;
import com.workingbit.board.service.NotationStoreService;
import com.workingbit.share.common.ErrorMessages;
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
  public static BoardStoreService boardStoreService;
  public static NotationStoreService notationStoreService;
  public static BoardBoxDao boardBoxDao;
  public static BoardDao boardDao;
  public static NotationDao notationDao;

  private static AppProperties appProperties;

  static {
    appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

    boardBoxService = new BoardBoxService();
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
    path("/", () -> get(Path.HOME, BoardBoxController.home));

    path("/api", () ->
        path("/v1", () -> {
          get(Path.BOARD_BY_ID, BoardBoxController.findBoardById);

          post(Path.BOARD_ADD_DRAUGHT, BoardBoxController.addDraught);
          post(Path.BOARD, BoardBoxController.createBoard);
          put(Path.BOARD, BoardBoxController.saveBoard);
          post(Path.BOARD_MOVE, BoardBoxController.move);
          post(Path.BOARD_HIGHLIGHT, BoardBoxController.highlightBoard);
          post(Path.BOARD_REDO, BoardBoxController.redo);
          post(Path.BOARD_UNDO, BoardBoxController.undo);
          post(Path.BOARD_LOAD_PREVIEW, BoardBoxController.loadPreviewBoard);
          post(Path.BOARD_SWITCH, BoardBoxController.switchNotation);
          post(Path.BOARD_FORK, BoardBoxController.forkNotation);
          post(Path.BOARD_VIEW_BRANCH, BoardBoxController.viewBranch);
          post(Path.CHANGE_TURN, BoardBoxController.changeTurn);

          notFound((req, res) -> dataToJson(Answer.error(HTTP_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND)));
          internalServerError((req, res) -> dataToJson(Answer.error(HTTP_INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR)));

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        })
    );
  }
}
