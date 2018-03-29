package com.workingbit.board.service;

import com.workingbit.board.grammar.NotationParser;
import com.workingbit.share.model.Notation;
import com.workingbit.share.model.NotationStroke;
import com.workingbit.share.model.NotationStrokes;
import net.percederberg.grammatica.parser.Node;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import net.percederberg.grammatica.parser.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationParserService {

  public Notation parse(BufferedReader bufferedReader) throws IOException, ParserCreationException, ParserLogException, URISyntaxException {
    NotationParser notationParser = new NotationParser(bufferedReader);
    notationParser.prepare();
    Node parse = notationParser.parse();
    Node pdnFile = parse.getChildAt(0);
    Node gameHeader = pdnFile.getChildAt(0);
    Node game = pdnFile.getChildAt(1);
    NotationStrokes notationStrokes = new NotationStrokes();
    parseGame(game, notationStrokes);

    Notation notation = new Notation();
    notation.setNotationStrokes(notationStrokes);

    return notation;
  }

  private void parseGame(Node game, NotationStrokes notationStrokes) {
    boolean first = false, addMove = false, nextStrength;
    NotationStroke notationStroke = new NotationStroke();
    for (int i = 0; i < game.getChildCount(); i++) {
      Node gameBody = game.getChildAt(i);
      switch (gameBody.getName()) {
        case "GameMove": {
          for (int j = 0; j < gameBody.getChildCount(); j++) {
            Node gameMove = gameBody.getChildAt(j);
            switch (gameMove.getName()) {
              case "MOVENUMBER":
                String moveNumber = ((Token) gameMove).getImage();
                notationStroke.setMoveNumber(moveNumber);
                first = true;
                break;
              case "Move":
                Token move = (Token) gameMove.getChildAt(0);
                String stroke = move.getImage();
                notationStroke.parseStrokeFromPdn(stroke, first, move.getName());
                nextStrength = isNextNode("MoveStrength", j, gameBody);
                if (!first && !nextStrength) {
                  addMove = isAddMoreWhenParent(gameBody, i);
                } else if (!nextStrength) {
                  first = false;
                  addMove = gameBody.getParent().getChildCount() == 1;
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
                  addMove = isAddMoreWhenParent(gameBody, i);
                }
                break;
            }
          }
          break;
        }
        case "COMMENT": {
          Token token = (Token) gameBody;
          notationStroke.setComment(token.getImage());
          addMove = isAddMoreWhenParent(gameBody, i);
          break;
        }
        case "Variation": {
          Node variant = gameBody.getChildAt(1);
          NotationStrokes notationStrokesVariant = new NotationStrokes();
          parseGame(variant, notationStrokesVariant);
          notationStroke.setVariants(notationStrokesVariant);
          addMove = isAddMoreWhenParent(gameBody, i);
          break;
        }
      }
      if (addMove) {
        notationStrokes.add(notationStroke);
        notationStroke = new NotationStroke();
        addMove = false;
      }
    }
  }

  private boolean isAddMoreWhenParent(Node gameBody, int j) {
    return !isNextNode("COMMENT", j, gameBody.getParent())
        && !isNextNode("Variation", j, gameBody.getParent());
  }

  private boolean isNextNode(String nodeName, int j, Node gameBody) {
    return j + 1 < gameBody.getChildCount()
        && gameBody.getChildAt(j + 1).getName().equals(nodeName);
  }
}
