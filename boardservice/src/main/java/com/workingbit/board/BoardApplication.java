package com.workingbit.board;

import com.workingbit.board.config.AppProperties;
import com.workingbit.board.controller.BoardBoxController;
import com.workingbit.board.dao.BoardBoxDao;
import com.workingbit.board.dao.BoardDao;
import com.workingbit.board.dao.NotationDao;
import com.workingbit.board.service.BoardBoxService;
import com.workingbit.board.util.Path;
import com.workingbit.share.util.Filters;
import com.workingbit.share.util.SparkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.CorsConfig.enableCors;
import static spark.Spark.*;

public class BoardApplication {

  private static final Logger LOG = LoggerFactory.getLogger(BoardApplication.class);

  // Declare dependencies
  public static BoardBoxService boardBoxService;
  public static BoardBoxDao boardBoxDao;
  public static BoardDao boardDao;
  public static NotationDao notationDao;

  private static AppProperties appProperties;

  static {
    appProperties = configurationProvider().bind("app", AppProperties.class);

    boardBoxService = new BoardBoxService();
    boardBoxDao = new BoardBoxDao(appProperties);
    boardDao = new BoardDao(appProperties);
    notationDao = new NotationDao(appProperties);
  }

  public static void main(String[] args) {
    int port = appProperties.port();
    if (args != null && args.length > 0) {
      port = Integer.parseInt(args[0]);
    }
    System.out.println("Listening on port: " + port);
    port(port);
    start();
  }

  public static void start() {
    Logger logger = LoggerFactory.getLogger(BoardApplication.class);
    SparkUtils.createServerWithRequestLog(logger);

    LOG.info("Initializing routes");
    enableCors(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());
    establishRoutes();
  }

  private static void establishRoutes() {
    path("/api", () ->
        path("/v1", () -> {
          get(Path.BOARD_BY_ID, BoardBoxController.findBoardById);

          post(Path.BOARD_ADD_DRAUGHT, BoardBoxController.addDraught);
          post(Path.BOARD, BoardBoxController.createBoard);
          put(Path.BOARD, BoardBoxController.saveBoard);
          post(Path.BOARD_LOAD_PREVIEW, BoardBoxController.loadPreviewBoard);
          post(Path.BOARD_MOVE, BoardBoxController.move);
          post(Path.BOARD_HIGHLIGHT, BoardBoxController.highlightBoard);
          post(Path.BOARD_REDO, BoardBoxController.redo);
          post(Path.BOARD_UNDO, BoardBoxController.undo);
          post(Path.BOARD_SWITCH, BoardBoxController.switchNotation);
          post(Path.BOARD_FORK, BoardBoxController.forkNotation);
          put(Path.CHANGE_TURN, BoardBoxController.changeTurn);

          notFound((req, res) -> "Not found");
          internalServerError((req, res) -> "Internal server message");

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        }));
  }
}
