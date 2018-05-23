package com.workingbit.board.grammar;

import net.percederberg.grammatica.parser.ParserCreationException;

import java.io.Reader;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationFenParser extends FenParser {
  public NotationFenParser(Reader in) throws ParserCreationException {
    super(in);
  }
}
