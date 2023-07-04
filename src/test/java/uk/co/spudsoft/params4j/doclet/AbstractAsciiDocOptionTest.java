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
    assertEquals(3, OPTIONS.size());
    
    assertEquals(1, OPTIONS.get(0).getNames().size());
    assertEquals("-d", OPTIONS.get(0).getNames().get(0));
    assertEquals(1, OPTIONS.get(0).getArgumentCount());
    assertEquals(Kind.STANDARD,  OPTIONS.get(0).getKind());
    assertEquals("Destination directory for output files",  OPTIONS.get(0).getDescription());
    assertEquals("directory",  OPTIONS.get(0).getParameters());
    
    assertEquals(1, OPTIONS.get(1).getNames().size());
    assertEquals("--link", OPTIONS.get(1).getNames().get(0));
    assertEquals(1, OPTIONS.get(1).getArgumentCount());
    assertEquals(Kind.STANDARD,  OPTIONS.get(1).getKind());
    assertEquals("Map from package names to a URL base to use for links",  OPTIONS.get(1).getDescription());
    assertEquals("link",  OPTIONS.get(1).getParameters());
    
    assertEquals(1, OPTIONS.get(2).getNames().size());
    assertEquals("--include-classes", OPTIONS.get(2).getNames().get(0));
    assertEquals(1, OPTIONS.get(2).getArgumentCount());
    assertEquals(Kind.STANDARD,  OPTIONS.get(2).getKind());
    assertEquals("If set, only the listed classes will be processed",  OPTIONS.get(2).getDescription());
    assertEquals("include-classes",  OPTIONS.get(2).getParameters());
    
    
  }

}
