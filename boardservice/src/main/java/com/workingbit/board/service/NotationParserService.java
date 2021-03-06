package com.workingbit.board.service;

import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.board.grammar.NotationParser;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
import com.workingbit.share.model.NotationFen;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import net.percederberg.grammatica.parser.Node;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import net.percederberg.grammatica.parser.Token;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationParserService {

  private static Logger logger = LoggerFactory.getLogger(NotationParserService.class);
  private static final String GAME_MOVE = "GameMove";
  private static final String MOVENUMBER = "MOVENUMBER";
  private static final String MOVE = "Move";
  private static final String MOVE_STRENGTH = "MoveStrength";
  private static final String COMMENT = "COMMENT";
  private static final String VARIATION = "Variation";

  private NotationFenParserService notationFenParserService;

  public NotationParserService() {
    this.notationFenParserService = new NotationFenParserService();
  }

  @NotNull
  public Notation parse(@NotNull String notation) throws ParserLogException, ParserCreationException {
    BufferedReader bufferedReader = new BufferedReader(new StringReader(notation));
    return parse(bufferedReader);
  }

  @NotNull
  public Notation parse(BufferedReader bufferedReader) throws ParserCreationException, ParserLogException {
    NotationParser notationParser = new NotationParser(bufferedReader);
    notationParser.prepare();
    Node parse = notationParser.parse();
    Node pdnFile = parse.getChildAt(0);
    Node gameHeader = pdnFile.getChildAt(0);

    Notation notation = new Notation();
    ListOrderedMap<String, String> headers = notation.getTags();
    parseHeader(gameHeader, headers, notation);
    if (EnumNotationFormat.SHORT.equals(notation.getFormat())) {
      throw new BoardServiceException(ErrorMessages.IMPORT_SHORT_UNSUPPORTED);
    }

    Node game = pdnFile.getChildAt(1);
    if (game == null) {
      return notation;
    }
    NotationHistory notationDrives = notation.getNotationHistory();
    try {
      parseGameBody(game, notationDrives.getNotation(), notation.getRules(), notation.getFormat());
    } catch (Exception e) {
      game.printTo(System.err);
      logger.error("Parse error ", e);
    }

    notation.setTags(headers);
    notation.setNotationHistory(notationDrives);

    return notation;
  }

  public Notation parseResource(String resourceName) throws ParserLogException, ParserCreationException, URISyntaxException, IOException {
    URL uri = getClass().getResource(resourceName);
    Path path = Paths.get(uri.toURI());
    BufferedReader bufferedReader = Files.newBufferedReader(path);
    return parse(bufferedReader);
  }

  private void parseHeader(Node gameHeader, @NotNull Map<String, String> headers, @NotNull Notation notation) throws ParserLogException, ParserCreationException {
    for (int i = 0; i < gameHeader.getChildCount(); i++) {
      Node header = gameHeader.getChildAt(i);
      String identifier = ((Token) header.getChildAt(1)).getImage();
      String value = ((Token) header.getChildAt(2)).getImage();
      switch (identifier) {
        case "FEN":
          NotationFen fen = notationFenParserService.parse(value.substring(1, value.length() - 1));
          notation.setNotationFen(fen);
          break;
        case "GameType":
          String substring = value.substring(1, value.length() - 1);
          String[] arr = substring.split(",");
          notation.setRules(EnumRules.fromTypeNumber(Integer.parseInt(arr[0])));
          notation.setFormat(EnumNotationFormat.fromType(arr[4]));
          break;
        default:
          headers.put(identifier, value);
          break;
      }
    }
  }

  private void parseGameBody(Node game, @NotNull NotationDrives notationDrives, EnumRules rules, EnumNotationFormat format) {
    boolean firstMove = false;
    NotationDrive notationDrive = new NotationDrive();
    for (int i = 0; i < game.getChildCount(); i++) {
      Node gameBody = game.getChildAt(i);
      switch (gameBody.getName()) {
        case GAME_MOVE: {
          for (int j = 0; j < gameBody.getChildCount(); j++) {
            Node gameMove = gameBody.getChildAt(j);
            switch (gameMove.getName()) {
              case MOVENUMBER:
                if (firstMove) {
                  notationDrives.add(notationDrive);
                  notationDrive = new NotationDrive();
                }
                firstMove = true;

                String moveNumber = ((Token) gameMove).getImage();
                int notationNumberInt = Integer.parseInt(moveNumber.substring(0, moveNumber.indexOf(".")));
                notationDrive.setNotationNumberInt(notationNumberInt);
                break;
              case MOVE:
                Token moveToken = (Token) gameMove.getChildAt(0);
                notationDrive.parseNameFromPdn(moveToken.getName());
                String move = moveToken.getImage();
                notationDrive.addMoveFromPdn(move, rules, format);
                break;
              case MOVE_STRENGTH:
                Token moveStrength = (Token) gameMove.getChildAt(0);
                String strength = moveStrength.getImage();
                notationDrive.getMoves().getLast().setMoveStrength(strength);
                break;
            }
          }
          break;
        }
        case COMMENT: {
          Token token = (Token) gameBody;
          notationDrive.setComment(token.getImage());
          break;
        }
        case VARIATION: {
          Node variants = gameBody.getChildAt(1);
          NotationDrives subVariants = NotationDrives.create();
          parseGameBody(variants, subVariants, rules, format);
          NotationDrive subDrive = subVariants.getFirst().deepClone();
          subDrive.setVariants(subVariants);
          notationDrive.addVariant(subDrive);
          break;
        }
      }
    }
    notationDrives.add(notationDrive);
  }
}
