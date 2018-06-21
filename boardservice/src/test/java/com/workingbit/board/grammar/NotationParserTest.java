package com.workingbit.board.grammar;

import com.workingbit.board.service.NotationParserService;
import com.workingbit.share.domain.impl.Notation;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Aleksey Popryadukhin on 29/03/2018.
 */
public class NotationParserTest {

  @NotNull
  private NotationParserService notationParserService = new NotationParserService();

  @Test
  public void testParseNotation() throws IOException, ParserCreationException, ParserLogException, URISyntaxException {
    String[] testNotations = new String[] {
        "/pdn/example.pdn",
    };

    for (String fileName : testNotations) {
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      BufferedReader bufferedReader = Files.newBufferedReader(path);

      Notation notation = notationParserService.parse(bufferedReader);
      assertNotNull(notation);
      System.out.println(notation.getAsStringAlphaNumeric());
    }
  }
}