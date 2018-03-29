package com.workingbit.board.grammar;

import net.percederberg.grammatica.parser.ParserCreationException;

import java.io.Reader;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationParser extends PdnReadingParser {
  public NotationParser(Reader in) throws ParserCreationException {
    super(in);
  }
}
