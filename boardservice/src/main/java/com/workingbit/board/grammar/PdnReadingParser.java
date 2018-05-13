/*
 * PdnReadingParser.java
 *
 * THIS FILE HAS BEEN GENERATED AUTOMATICALLY. DO NOT EDIT!
 *
 * Distributed under the Boost Software License, Version 1.0.
 * See http://www.boost.org/LICENSE_1_0.txt.
 *
 * Copyright (c) 2009-2012 Wieger Wesselink.
 */

package com.workingbit.board.grammar;

import java.io.Reader;

import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ProductionPattern;
import net.percederberg.grammatica.parser.ProductionPatternAlternative;
import net.percederberg.grammatica.parser.RecursiveDescentParser;
import net.percederberg.grammatica.parser.Tokenizer;

/**
 * A token stream parser.
 *
 * @author   Wieger Wesselink, <wieger at 10x10 dot org>
 * @version  1.5
 */
class PdnReadingParser extends RecursiveDescentParser {

    /**
     * A generated production node identity constant.
     */
    private static final int SUBPRODUCTION_1 = 3001;

    /**
     * A generated production node identity constant.
     */
    private static final int SUBPRODUCTION_2 = 3002;

    /**
     * A generated production node identity constant.
     */
    private static final int SUBPRODUCTION_3 = 3003;

    /**
     * Creates a new parser with a default analyzer.
     *
     * @param in             the input stream to read from
     *
     * @throws ParserCreationException if the parser couldn't be
     *             initialized correctly
     */
    public PdnReadingParser(Reader in) throws ParserCreationException {
        super(in);
        createPatterns();
    }

    /**
     * Creates a new parser.
     *
     * @param in             the input stream to read from
     * @param analyzer       the analyzer to use while parsing
     *
     * @throws ParserCreationException if the parser couldn't be
     *             initialized correctly
     */
    public PdnReadingParser(Reader in, PdnReadingAnalyzer analyzer)
        throws ParserCreationException {

        super(in, analyzer);
        createPatterns();
    }

    /**
     * Creates a new tokenizer for this parser. Can be overridden by a
     * subclass to provide a custom implementation.
     *
     * @param in             the input stream to read from
     *
     * @return the tokenizer created
     *
     * @throws ParserCreationException if the tokenizer couldn't be
     *             initialized correctly
     */
    protected Tokenizer newTokenizer(Reader in)
        throws ParserCreationException {

        return new PdnReadingTokenizer(in);
    }

    /**
     * Initializes the parser by creating all the production patterns.
     *
     * @throws ParserCreationException if the parser couldn't be
     *             initialized correctly
     */
    private void createPatterns() throws ParserCreationException {
        ProductionPattern             pattern;
        ProductionPatternAlternative  alt;

        pattern = new ProductionPattern(PdnReadingConstants.PDN_FILE,
                                        "PdnFile");
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.GAME, 1, 1);
        alt.addProduction(SUBPRODUCTION_1, 0, -1);
        alt.addProduction(PdnReadingConstants.GAME_SEPARATOR, 0, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.GAME_SEPARATOR,
                                        "GameSeparator");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.ASTERISK, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.RESULT1, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.RESULT2, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.GAME,
                                        "Game");
        alt = new ProductionPatternAlternative();
        alt.addProduction(SUBPRODUCTION_2, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.GAME_BODY, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.GAME_HEADER,
                                        "GameHeader");
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.PDN_TAG, 1, -1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.GAME_BODY,
                                        "GameBody");
        alt = new ProductionPatternAlternative();
        alt.addProduction(SUBPRODUCTION_3, 1, -1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.PDN_TAG,
                                        "PdnTag");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.LBRACKET, 1, 1);
        alt.addToken(PdnReadingConstants.IDENTIFIER, 1, 1);
        alt.addToken(PdnReadingConstants.STRING, 1, 1);
        alt.addToken(PdnReadingConstants.RBRACKET, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.GAME_MOVE,
                                        "GameMove");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.MOVENUMBER, 0, 1);
        alt.addProduction(PdnReadingConstants.MOVE, 1, 1);
        alt.addProduction(PdnReadingConstants.MOVE_STRENGTH, 0, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.VARIATION,
                                        "Variation");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.LPAREN, 1, 1);
        alt.addProduction(PdnReadingConstants.GAME_BODY, 1, 1);
        alt.addToken(PdnReadingConstants.RPAREN, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.MOVE,
                                        "Move");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.NUMERICMOVE, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.ALPHANUMERICMOVE, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.ELLIPSES, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.GAME_RESULT,
                                        "GameResult");
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.RESULT1, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.RESULT2, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.RESULT1,
                                        "Result1");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.WIN1, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.DRAW1, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.LOSS1, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.RESULT2,
                                        "Result2");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.WIN2, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.DRAW2, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.LOSS2, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.DOUBLEFORFEIT, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(PdnReadingConstants.MOVE_STRENGTH,
                                        "MoveStrength");
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.MOVESTRENGTH1, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.MOVESTRENGTH2, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_1,
                                        "Subproduction1");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.GAME_SEPARATOR, 1, 1);
        alt.addProduction(PdnReadingConstants.GAME, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_2,
                                        "Subproduction2");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.GAME_HEADER, 1, 1);
        alt.addProduction(PdnReadingConstants.GAME_BODY, 0, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_3,
                                        "Subproduction3");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.GAME_MOVE, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addProduction(PdnReadingConstants.VARIATION, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.COMMENT, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.SETUP, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addToken(PdnReadingConstants.NAG, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);
    }
}
