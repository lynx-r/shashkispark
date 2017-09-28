package com.workingbit.board;

import com.workingbit.share.util.Filters;
import com.workingbit.share.util.SparkUtils;
import com.workingbit.board.board.BoardBoxDao;
import com.workingbit.board.board.BoardController;
import com.workingbit.board.board.BoardDao;
import com.workingbit.board.config.AppProperties;
import com.workingbit.board.util.Path;
import org.apache.log4j.Logger;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.CorsConfig.enableCors;
import static spark.Spark.*;

public class BoardApplication {

  private static final Logger LOG = Logger.getLogger(BoardApplication.class);

  // Declare dependencies
  public static BoardBoxDao boardBoxDao;
  public static BoardDao boardDao;

  public static AppProperties appProperties;

  static {
    appProperties = configurationProvider().bind("app", AppProperties.class);

    boardBoxDao = new BoardBoxDao(appProperties);
    boardDao = new BoardDao(appProperties);
  }

  public static void main(String[] args) {
    port(appProperties.port());
    start();
  }

  static void start() {
    Logger logger = Logger.getLogger(BoardApplication.class);
    SparkUtils.createServerWithRequestLog(logger);

    LOG.info("Initializing routes");

    enableCors(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());

    path("/api", () ->

        path("/v1", () -> {

          get(Path.BOARD_BY_ID, BoardController.findBoardById);

          post(Path.BOARD_BY_IDS, BoardController.findBoardByIds);

          post(Path.BOARD_ADD_DRAUGHT, BoardController.addDraught);
          post(Path.BOARD, BoardController.createBoard);
          post(Path.BOARD_MOVE, BoardController.move);
          post(Path.BOARD_HIGHLIGHT, BoardController.highlightBoard);
          post(Path.BOARD_REDO, BoardController.redo);
          post(Path.BOARD_UNDO, BoardController.undo);

          notFound((req, res) -> "Not found");
          internalServerError((req, res) -> "Internal server error");

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        }));
  }
}