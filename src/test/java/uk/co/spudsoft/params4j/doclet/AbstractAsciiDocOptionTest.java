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

import java.util.List;
import java.util.stream.Collectors;
import jdk.javadoc.doclet.Doclet.Option;
import jdk.javadoc.doclet.Doclet.Option.Kind;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author njt
 */
public class AbstractAsciiDocOptionTest {
  
  private static final AsciiDocOptions ADO = new AsciiDocOptions();
  private static final List<? extends Option> OPTIONS
          = ADO.getOptions()
                  .stream()
                  .sorted((o1, o2) -> 0 - o1.getNames().get(0).compareTo(o2.getNames().get(0)))
                  .collect(Collectors.toList())
                  ;
  
  @Test
  public void testOptionGetters() {
    assertEquals(4, OPTIONS.size());
    
    Option option = OPTIONS.get(0);
    assertEquals(1, option.getNames().size());
    assertEquals("-link", option.getNames().get(0));
    assertEquals(1, option.getArgumentCount());
    assertEquals(Kind.STANDARD,  option.getKind());
    assertEquals("Map from package names to a URL base to use for links",  option.getDescription());
    assertEquals("link",  option.getParameters());
    
    option = OPTIONS.get(1);
    assertEquals(1, option.getNames().size());
    assertEquals("-d", option.getNames().get(0));
    assertEquals(1, option.getArgumentCount());
    assertEquals(Kind.STANDARD,  option.getKind());
    assertEquals("Destination directory for output files",  option.getDescription());
    assertEquals("directory",  option.getParameters());
    
    option = OPTIONS.get(2);
    assertEquals(1, option.getNames().size());
    assertEquals("--no-fonts", option.getNames().get(0));
    assertEquals(0, option.getArgumentCount());
    assertEquals(Kind.STANDARD,  option.getKind());
    assertEquals("Disable font embedding in output",  option.getDescription());
    assertNull(option.getParameters());
    
    option = OPTIONS.get(3);
    assertEquals(1, option.getNames().size());
    assertEquals("--include-classes", option.getNames().get(0));
    assertEquals(1, option.getArgumentCount());
    assertEquals(Kind.STANDARD,  option.getKind());
    assertEquals("If set, only the listed classes will be processed",  option.getDescription());
    assertEquals("include-classes",  option.getParameters());
    
  }

}
