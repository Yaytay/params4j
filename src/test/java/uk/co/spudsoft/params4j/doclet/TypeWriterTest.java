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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author njt
 */
public class TypeWriterTest {
  
  /**
   * Test of simplifySignature method, of class TypeWriter.
   */
  @Test
  public void testSimplifySignature() {
    assertEquals("", TypeWriter.simplifySignature(""));
    assertEquals("Boolean", TypeWriter.simplifySignature("java.lang.Boolean"));
    assertEquals("Map#forEach(BiConsumer)", TypeWriter.simplifySignature("Map<String, Credentials>#forEach(BiConsumer)"));
    assertEquals("String(byte[\\], int, int, java.nio.charset.Charset)", TypeWriter.simplifySignature("java.lang.String#String(byte[], int, int, java.nio.charset.Charset)"));
    assertEquals("Credentials#getPassword()", TypeWriter.simplifySignature("commentcap.Credentials#getPassword()"));
  }

}
