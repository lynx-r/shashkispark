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
    boolean first = false;
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
                switch (move.getName()) {
                  case "NUMERICMOVE": {
                    String stroke = move.getImage();
                    notationStroke.parseStrokeFromPdn(stroke, first);
                    if (!first) {
                      notationStroke = new NotationStroke();
                    }
                    first = false;
                  }
                }
                break;
            }
          }
        }
      }
      notationStrokes.add(notationStroke);
    }
    notationStrokes.forEach(System.out::println);
//    parse(notationParser.parse(), );
  }
}