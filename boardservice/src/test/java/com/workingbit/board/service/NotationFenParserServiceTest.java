package com.workingbit.board.service;

import com.workingbit.share.model.NotationFen;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 25/05/2018.
 */
public class NotationFenParserServiceTest {

  private NotationFenParserService notationFenParserService;

  @Test
  public void parse() throws ParserLogException, ParserCreationException {
    String fen = "B:W18,24,27,28,K10,K15:B12,16,20,K22,K25,K29";
    notationFenParserService = new NotationFenParserService();
    NotationFen parse = notationFenParserService.parse(fen);
    String toString = "[SetUp \"1\"]\n" +
        "[FEN \"B:W18,24,27,28,K10,K15:B12,16,20,K22,K25,K29\"]";
    assertEquals(toString, parse.toString());
  }
}