package com.workingbit.board.service;

import com.workingbit.board.grammar.NotationParser;
import com.workingbit.share.model.Notation;
import com.workingbit.share.model.NotationStroke;
import com.workingbit.share.model.NotationStrokes;
import net.percederberg.grammatica.parser.*;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationParserService {

  public Notation parse(BufferedReader bufferedReader) throws ParserCreationException, ParserLogException {
    NotationParser notationParser = new NotationParser(bufferedReader);
    notationParser.prepare();
    Node parse = notationParser.parse();
    Node pdnFile = parse.getChildAt(0);
    Node gameHeader = pdnFile.getChildAt(0);

    Map<String, String> headers = new HashMap<>();
    parseHeader(gameHeader, headers);

    Node game = pdnFile.getChildAt(1);
    game.printTo(System.out);
    NotationStrokes notationStrokes = new NotationStrokes();
    parseGame(game, notationStrokes);

    Notation notation = new Notation();
    notation.setTags(headers);
    notation.setNotationStrokes(notationStrokes);

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

  private void parseGame(Node game, NotationStrokes notationStrokes) {
    boolean first = false, addMove = false, nextStrength;
    NotationStroke notationStroke = new NotationStroke();
    int n = 0;
    for (int i = 0; i < game.getChildCount(); i++) {
      Node gameBody = game.getChildAt(i);
      System.out.println(++n + " " + gameBody.getChildCount());
      switch (gameBody.getName()) {
        case "GameMove": {
          for (int j = 0; j < gameBody.getChildCount(); j++) {
            Node gameMove = gameBody.getChildAt(j);
            switch (gameMove.getName()) {
              case "MOVENUMBER":
                if (addMove) {
                  notationStrokes.add(notationStroke);
                  notationStroke = new NotationStroke();
                  addMove = false;
                  n = 0;
                } else {
                  addMove = true;
                }

                String moveNumber = ((Token) gameMove).getImage();
                notationStroke.setMoveNumber(moveNumber);
                first = true;
                break;
              case "Move":
                Token move = (Token) gameMove.getChildAt(0);
                String stroke = move.getImage();
                notationStroke.parseStrokeFromPdn(stroke, first, move.getName());
                nextStrength = isNextNode("MoveStrength", j, gameBody);
                if (!nextStrength) {
                  first = false;
                }
                break;
              case "MoveStrength":
                Token moveStrength = (Token) gameMove.getChildAt(0);
                String strength = moveStrength.getImage();
                if (first) {
                  notationStroke.getFirst().setMoveStrength(strength);
                  first = false;
                } else {
                  notationStroke.getSecond().setMoveStrength(strength);
                }
                break;
            }
          }
          break;
        }
        case "COMMENT": {
          Token token = (Token) gameBody;
          notationStroke.setComment(token.getImage());
          break;
        }
        case "Variation": {
          Node variant = gameBody.getChildAt(1);
          NotationStrokes notationStrokesVariant = new NotationStrokes();
          parseGame(variant, notationStrokesVariant);
          notationStroke.setVariants(notationStrokesVariant);
          break;
        }
      }
    }
  }

  private boolean isNextNode(String nodeName, int j, Node gameBody) {
    return j + 1 < gameBody.getChildCount()
        && gameBody.getChildAt(j + 1).getName().equals(nodeName);
  }
}
