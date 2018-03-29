package com.workingbit.board.grammar;

import com.workingbit.share.model.NotationStroke;
import com.workingbit.share.model.NotationStrokes;
import net.percederberg.grammatica.parser.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationParserTest extends ParserTestCase {

  /**
   * Creates a new test case.
   */
  public NotationParserTest() {
    super("NotationParser");
  }

  public void testParseNotation() throws IOException, ParserCreationException, ParserLogException, URISyntaxException {
    URL uri = getClass().getResource("/pdn/example.pdn");
    Path path = Paths.get(uri.toURI());
    BufferedReader bufferedReader = Files.newBufferedReader(path);
    NotationParser notationParser = new NotationParser(bufferedReader);
    notationParser.prepare();
    Node parse = notationParser.parse();
    Node pdnFile = parse.getChildAt(0);
    Node gameHeader = pdnFile.getChildAt(0);
    Node game = pdnFile.getChildAt(1);
    NotationStrokes notationStrokes = new NotationStrokes();
    game.printTo(System.out);
    parseGame(game, notationStrokes);
    notationStrokes.forEach(System.out::println);
//    parse(notationParser.parse(), );
  }

  private void parseGame(Node game, NotationStrokes notationStrokes) {
    boolean first = false, addMove = false, nextStrength;
    NotationStroke notationStroke = new NotationStroke();
    for (int i = 0; i < game.getChildCount(); i++) {
      Node gameBody = game.getChildAt(i);
      gameBody.printTo(System.out);
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

  private boolean isAddMore(Node gameBody, int j) {
    return !isNextNode("COMMENT", j, gameBody)
        && !isNextNode("Variation", j, gameBody);
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