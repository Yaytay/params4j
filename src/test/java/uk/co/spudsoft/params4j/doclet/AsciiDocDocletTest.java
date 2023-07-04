/*
 * Copyright (C) 2023 njt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.spudsoft.params4j.doclet;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.DocumentationTool;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 *
 * @author njt
 */
public class AsciiDocDocletTest {
  
  private static final Logger logger = LoggerFactory.getLogger(AsciiDocDocletTest.class);
  
  private static class DiagListener implements DiagnosticListener<JavaFileObject> {
    
    @SuppressWarnings("unchecked")
    EnumMap<Diagnostic.Kind, Integer> counts = new EnumMap<>(Diagnostic.Kind.class);

    public DiagListener() {
      for (Diagnostic.Kind kind : Diagnostic.Kind.values()) {
        counts.put(kind, 0);
      }
    }
    
    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
      Integer previous = counts.get(diagnostic.getKind());
      counts.put(diagnostic.getKind(), previous + 1);
      logger.atLevel(convertKind(diagnostic.getKind())).log(diagnostic.getMessage(Locale.ROOT));
    }
    
    private Level convertKind(Diagnostic.Kind kind) {
      switch(kind) {
        case ERROR:
          return Level.ERROR;
        case WARNING:
          return Level.WARN;
        case MANDATORY_WARNING:
          return Level.WARN;
        case NOTE:
          return Level.INFO;
        case OTHER:
          return Level.DEBUG;
      }
      return Level.ERROR;
    }
    
    public int getCount(Diagnostic.Kind kind) {
      return counts.get(kind);
    }
        
  }

  /**
   * Test of init method, of class MarkdownDoclet.
   */
  @Test
  public void testInit() throws IOException {
    
    DocumentationTool systemDocumentationTool = ToolProvider.getSystemDocumentationTool();
    String[] args = new String[]{
            "--source-path"
            , "src/test/resources"
            , "-d"
            , "target/parameter-docs"
            , "--include-classes"
            , "commentcap.Parameters,commentcap.Credentials"
            , "--include-classes"
            , "commentcap.DataSource"
            , "--link"
            , "https://docs.oracle.com/en/java/javase/20/docs/api/"
            , "commentcap"
    };
    StringWriter writer = new StringWriter();
    DiagListener diagListener = new DiagListener();
    DocumentationTool.DocumentationTask task = systemDocumentationTool.getTask(
            writer
            , systemDocumentationTool.getStandardFileManager(null, null, StandardCharsets.UTF_8)
            , diagListener
            , AsciiDocDoclet.class
            , Arrays.asList(args)
            , null
    );
    task.call();
    
    String output = writer.getBuffer().toString();
    logger.warn(output);
    assertThat(output, not(containsString("--help")));
    assertEquals(0, diagListener.getCount(Diagnostic.Kind.ERROR));
    assertEquals(0, diagListener.getCount(Diagnostic.Kind.MANDATORY_WARNING));
    assertEquals(0, diagListener.getCount(Diagnostic.Kind.WARNING));
  }

}
