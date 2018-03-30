/*
 * ParserTestCase.java
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the BSD license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * LICENSE.txt file for more details.
 *
 * Copyright (c) 2003-2015 Per Cederberg. All rights reserved.
 */

package com.workingbit.board.grammar;

import junit.framework.TestCase;
import net.percederberg.grammatica.parser.*;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Base class for all the parser test cases.
 *
 * @author   Per Cederberg
 * @version  1.0
 */
abstract class ParserTestCase extends TestCase {

    /**
     * Creates a new test case.
     *
     * @param name           the test case name
     */
    public ParserTestCase(String name) {
        super(name);
    }

    /**
     * Parses with the parser and checks the output. If the parsing
     * failed or if the tree didn't match the specified output, a test
     * failure will be reported.
     *
     * @param parser         the parser to use
     * @param output         the expected parse tree
     */
    protected void parse(Parser parser, String output) {
        try {
            validateTree(parser.parse(), output);
        } catch (ParserCreationException e) {
            fail(e.getMessage());
        } catch (ParserLogException e) {
            fail(e.getError(0).getMessage());
        }
    }

    /**
     * Parses with the parser and checks the parse error. If the
     * parsing succeeded or if the parse exception didn't match the
     * specified values, a test failure will be reported.
     *
     * @param parser         the parser to use
     * @param type           the parse error type
     * @param line           the line number
     * @param column         the column number
     */
    protected void failParse(Parser parser,
                             int type,
                             int line,
                             int column) {

        try {
            parser.parse();
            fail("parsing succeeded");
        } catch (ParserCreationException e) {
            fail(e.getMessage());
        } catch (ParserLogException e) {
            ParseException  p = e.getError(0);

            assertEquals("error notationNumber", 1, e.getErrorCount());
            assertEquals("error type", type, p.getErrorType());
            assertEquals("line number", line, p.getLine());
            assertEquals("column number", column, p.getColumn());
        }
    }

    /**
     * Validates that a parse tree is identical to a string
     * representation. If the two representations mismatch, a test
     * failure will be reported.
     *
     * @param root           the parse tree root node
     * @param str            the string representation
     */
    private void validateTree(Node root, String str) {
        StringWriter output = new StringWriter();

        root.printTo(new PrintWriter(output));
        validateLines(str, output.toString());
    }

    /**
     * Validates that two strings are identical. If the two strings
     * mismatch, a test failure will be reported.
     *
     * @param expected       the expected result
     * @param result         the result obtained
     */
    private void validateLines(String expected, String result) {
        int     line = 1;
        String  expectLine;
        String  resultLine;
        int     pos;

        while (expected.length() > 0 || result.length() > 0) {
            pos = expected.indexOf('\n');
            if (pos >= 0) {
                expectLine = expected.substring(0, pos);
                expected = expected.substring(pos + 1);
            } else {
                expectLine = expected;
                expected = "";
            }
            pos = result.indexOf('\n');
            if (pos >= 0) {
                resultLine = result.substring(0, pos);
                result = result.substring(pos + 1);
            } else {
                resultLine = result;
                result = "";
            }
            validateLine(line, expectLine, resultLine);
            line++;
        }
    }

    /**
     * Validates that two strings are identical. If the two strings
     * mismatch, a test failure will be reported.
     *
     * @param line           the line number to report
     * @param expected       the expected result
     * @param result         the result obtained
     */
    private void validateLine(int line, String expected, String result) {
        if (!expected.trim().equals(result.trim())) {
            fail("on line: " + line + ", expected: '" + expected +
                 "', found: '" + result + "'");
        }
    }
}
