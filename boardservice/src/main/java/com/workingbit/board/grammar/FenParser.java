/*
 * FenParser.java
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
import net.percederberg.grammatica.parser.ProductionPattern;
import net.percederberg.grammatica.parser.ProductionPatternAlternative;
import net.percederberg.grammatica.parser.RecursiveDescentParser;
import net.percederberg.grammatica.parser.Tokenizer;

/**
 * A token stream parser.
 *
 * @author   Wieger Wesselink, <wieger at 10x10 dot org>
 * @version  1.2
 */
class FenParser extends RecursiveDescentParser {

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
     * A generated production node identity constant.
     */
    private static final int SUBPRODUCTION_4 = 3004;

    /**
     * A generated production node identity constant.
     */
    private static final int SUBPRODUCTION_5 = 3005;

    /**
     * A generated production node identity constant.
     */
    private static final int SUBPRODUCTION_6 = 3006;

    /**
     * Creates a new parser with a default analyzer.
     *
     * @param in             the input stream to read from
     *
     * @throws ParserCreationException if the parser couldn't be
     *             initialized correctly
     */
    public FenParser(Reader in) throws ParserCreationException {
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
    public FenParser(Reader in, FenAnalyzer analyzer)
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

        return new FenTokenizer(in);
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

        pattern = new ProductionPattern(FenConstants.FEN,
                                        "Fen");
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.COLOR, 1, 1);
        alt.addProduction(SUBPRODUCTION_1, 1, 1);
        alt.addToken(FenConstants.DOT, 0, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(FenConstants.NUMERIC_SQUARES,
                                        "NumericSquares");
        alt = new ProductionPatternAlternative();
        alt.addProduction(SUBPRODUCTION_2, 1, -1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(FenConstants.NUMERIC_SQUARE_SEQUENCE,
                                        "NumericSquareSequence");
        alt = new ProductionPatternAlternative();
        alt.addProduction(FenConstants.NUMERIC_SQUARE_RANGE, 1, 1);
        alt.addProduction(SUBPRODUCTION_3, 0, -1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(FenConstants.NUMERIC_SQUARE_RANGE,
                                        "NumericSquareRange");
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.KING, 0, 1);
        alt.addToken(FenConstants.NUMSQUARE, 1, 1);
        alt.addProduction(SUBPRODUCTION_4, 0, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(FenConstants.ALPHA_NUMERIC_SQUARES,
                                        "AlphaNumericSquares");
        alt = new ProductionPatternAlternative();
        alt.addProduction(SUBPRODUCTION_5, 1, -1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(FenConstants.ALPHA_NUMERIC_SQUARE_SEQUENCE,
                                        "AlphaNumericSquareSequence");
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.KING, 0, 1);
        alt.addToken(FenConstants.ALPHASQUARE, 1, 1);
        alt.addProduction(SUBPRODUCTION_6, 0, -1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_1,
                                        "Subproduction1");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addProduction(FenConstants.NUMERIC_SQUARES, 1, 1);
        pattern.addAlternative(alt);
        alt = new ProductionPatternAlternative();
        alt.addProduction(FenConstants.ALPHA_NUMERIC_SQUARES, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_2,
                                        "Subproduction2");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.COLON, 1, 1);
        alt.addToken(FenConstants.COLOR, 1, 1);
        alt.addProduction(FenConstants.NUMERIC_SQUARE_SEQUENCE, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_3,
                                        "Subproduction3");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.COMMA, 1, 1);
        alt.addProduction(FenConstants.NUMERIC_SQUARE_RANGE, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_4,
                                        "Subproduction4");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.HYPHEN, 1, 1);
        alt.addToken(FenConstants.NUMSQUARE, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_5,
                                        "Subproduction5");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.COLON, 1, 1);
        alt.addToken(FenConstants.COLOR, 1, 1);
        alt.addProduction(FenConstants.ALPHA_NUMERIC_SQUARE_SEQUENCE, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);

        pattern = new ProductionPattern(SUBPRODUCTION_6,
                                        "Subproduction6");
        pattern.setSynthetic(true);
        alt = new ProductionPatternAlternative();
        alt.addToken(FenConstants.COMMA, 1, 1);
        alt.addToken(FenConstants.KING, 0, 1);
        alt.addToken(FenConstants.ALPHASQUARE, 1, 1);
        pattern.addAlternative(alt);
        addPattern(pattern);
    }
}
