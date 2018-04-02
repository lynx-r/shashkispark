package com.workingbit.board.service;

import com.workingbit.board.grammar.NotationParser;
import com.workingbit.share.model.Notation;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
import com.workingbit.share.model.NotationHistory;
import net.percederberg.grammatica.parser.Node;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import net.percederberg.grammatica.parser.Token;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationParserService {

  private static Logger logger = Logger.getLogger(NotationParserService.class);
  private static final String GAME_MOVE = "GameMove";
  private static final String MOVENUMBER = "MOVENUMBER";
  private static final String MOVE = "Move";
  private static final String MOVE_STRENGTH = "MoveStrength";
  private static final String COMMENT = "COMMENT";
  private static final String VARIATION = "Variation";

  public Notation parse(String notation) throws ParserLogException, ParserCreationException {
    BufferedReader bufferedReader = new BufferedReader(new StringReader(notation));
    return parse(bufferedReader);
  }

  public Notation parse(BufferedReader bufferedReader) throws ParserCreationException, ParserLogException {
    NotationParser notationParser = new NotationParser(bufferedReader);
    notationParser.prepare();
    Node parse = notationParser.parse();
    Node pdnFile = parse.getChildAt(0);
    Node gameHeader = pdnFile.getChildAt(0);

    Notation notation = new Notation();
    ListOrderedMap<String, String> headers = notation.getTags();
    parseHeader(gameHeader, headers);

    Node game = pdnFile.getChildAt(1);
    NotationHistory notationDrives = notation.getNotationHistory();
    try {
      parseGameBody(game, notationDrives.getVariants());
    } catch (Exception e) {
      game.printTo(System.err);
      logger.error("Parse error ", e);
    }

    notation.setTags(headers);
    notation.setNotationHistory(notationDrives);

    return notation;
  }

  private void parseHeader(Node gameHeader, Map<String, String> headers) {
    for (int i = 0; i < gameHeader.getChildCount(); i++) {
      Node header = gameHeader.getChildAt(i);
      String identifier = ((Token) header.getChildAt(1)).getImage();
      String value = ((Token) header.getChildAt(2)).getImage();
      headers.put(identifier, value);
    }
  }

  private void parseGameBody(Node game, NotationDrives notationDrives) {
    boolean firstMove = false;
    NotationDrive notationDrive = new NotationDrive();
    int gameMoveNumber = 0;
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
                gameMoveNumber = 0;

                String moveNumber = ((Token) gameMove).getImage();
                notationDrive.setNotationNumber(moveNumber);
                break;
              case MOVE:
                gameMoveNumber++;
                Token moveToken = (Token) gameMove.getChildAt(0);
                notationDrive.parseNameFromPdn(moveToken.getName());
                String move = moveToken.getImage();
                notationDrive.addMoveFromPdn(move);
                break;
              case MOVE_STRENGTH:
                Token moveStrength = (Token) gameMove.getChildAt(0);
                String strength = moveStrength.getImage();
                notationDrive.getMoves().get(gameMoveNumber - 1).setMoveStrength(strength);
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
          parseGameBody(variants, subVariants);
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
