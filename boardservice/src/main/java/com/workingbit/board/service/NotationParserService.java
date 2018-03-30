package com.workingbit.board.service;

import com.workingbit.board.grammar.NotationParser;
import com.workingbit.share.model.Notation;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
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
    NotationDrives notationDrives = new NotationDrives();
    parseGame(game, notationDrives);

    Notation notation = new Notation();
    notation.setTags(headers);
    notation.setNotationDrives(notationDrives);

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

  private void parseGame(Node game, NotationDrives notationDrives) {
    boolean firstMove = false;
    NotationDrive notationDrive = new NotationDrive();
    for (int i = 0; i < game.getChildCount(); i++) {
      Node gameBody = game.getChildAt(i);
      switch (gameBody.getName()) {
        case "GameMove": {
          int moveNum = 0;
          for (int j = 0; j < gameBody.getChildCount(); j++) {
            Node gameMove = gameBody.getChildAt(j);
            switch (gameMove.getName()) {
              case "MOVENUMBER":
                if (firstMove) {
                  notationDrives.add(notationDrive);
                  notationDrive = new NotationDrive();
                }
                firstMove = true;

                String moveNumber = ((Token) gameMove).getImage();
                notationDrive.setNotationNumber(moveNumber);
                break;
              case "Move":
                moveNum++;
                Token move = (Token) gameMove.getChildAt(0);
                notationDrive.parseNameFromPdn(move.getName());
                String stroke = move.getImage();
                notationDrive.addAtomStrokeFromPdn(stroke);
                notationDrive.setMoveNumber(moveNum);
                break;
              case "MoveStrength":
                Token moveStrength = (Token) gameMove.getChildAt(0);
                String strength = moveStrength.getImage();
                notationDrive.getMoves().get(moveNum - 1).setMoveStrength(strength);
                break;
            }
          }
          break;
        }
        case "COMMENT": {
          Token token = (Token) gameBody;
          notationDrive.setComment(token.getImage());
          break;
        }
        case "Variation": {
          Node variant = gameBody.getChildAt(1);
          NotationDrives notationDrivesVariant = new NotationDrives();
          parseGame(variant, notationDrivesVariant);
          notationDrive.setVariants(notationDrivesVariant);
          break;
        }
      }
    }
    notationDrives.add(notationDrive);
  }

  private boolean isNextNode(String nodeName, int j, Node gameBody) {
    return j + 1 < gameBody.getChildCount()
        && gameBody.getChildAt(j + 1).getName().equals(nodeName);
  }
}
