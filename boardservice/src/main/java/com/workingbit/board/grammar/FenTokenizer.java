/*
 * FenTokenizer.java
 *
 * THIS FILE HAS BEEN GENERATED AUTOMATICALLY. DO NOT EDIT!
 *
 * Distributed under the Boost Software License, Version 1.0.
 * See http://www.boost.org/LICENSE_1_0.txt.
 *
 * Copyright (c) 2009-2016 Wieger Wesselink. All rights reserved.
 */

package com.workingbit.board.grammar;

import java.io.Reader;

import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.TokenPattern;
import net.percederberg.grammatica.parser.Tokenizer;

/**
 * A character stream tokenizer.
 *
 * @author   Wieger Wesselink, <wieger at 10x10 dot org>
 * @version  1.2
 */
class FenTokenizer extends Tokenizer {

    /**
     * Creates a new tokenizer for the specified input stream.
     *
     * @param input          the input stream to read
     *
     * @throws ParserCreationException if the tokenizer couldn't be
     *             initialized correctly
     */
    public FenTokenizer(Reader input) throws ParserCreationException {
        super(input, false);
        createPatterns();
    }

    /**
     * Initializes the tokenizer by creating all the token patterns.
     *
     * @throws ParserCreationException if the tokenizer couldn't be
     *             initialized correctly
     */
    private void createPatterns() throws ParserCreationException {
        TokenPattern  pattern;

        pattern = new TokenPattern(FenConstants.COLOR,
                                   "COLOR",
                                   TokenPattern.REGEXP_TYPE,
                                   "[WB?]");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.KING,
                                   "KING",
                                   TokenPattern.STRING_TYPE,
                                   "K");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.ALPHASQUARE,
                                   "ALPHASQUARE",
                                   TokenPattern.REGEXP_TYPE,
                                   "[a-h][1-8]");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.NUMSQUARE,
                                   "NUMSQUARE",
                                   TokenPattern.REGEXP_TYPE,
                                   "([1-9][\\d]*)|(0[1-9][\\d]*)|0");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.HYPHEN,
                                   "HYPHEN",
                                   TokenPattern.STRING_TYPE,
                                   "-");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.COMMA,
                                   "COMMA",
                                   TokenPattern.STRING_TYPE,
                                   ",");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.COLON,
                                   "COLON",
                                   TokenPattern.STRING_TYPE,
                                   ":");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.DOT,
                                   "DOT",
                                   TokenPattern.STRING_TYPE,
                                   ".");
        addPattern(pattern);

        pattern = new TokenPattern(FenConstants.WHITESPACE,
                                   "WHITESPACE",
                                   TokenPattern.REGEXP_TYPE,
                                   "[ \\t\\n\\r]+");
        pattern.setIgnore();
        addPattern(pattern);
    }
}
