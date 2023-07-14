/*
 * Copyright (C) 2023 jtalbut
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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author njt
 */
public class HtmlEntities {

  
  private static final Map<String, String> ENTITIES = prepareEntities();
  
  private static Map<String, String> prepareEntities() {
    Map<String, String> result = new HashMap<>();
    result.put("lt", "<");
    result.put("gt", ">");
    result.put("amp", "&");
    result.put("quot", "\"");
    result.put("dollar", "$");
    result.put("percnt", "%");
    result.put("apos", "'");
    result.put("lpar", "(");
    result.put("rpar", ")");
    result.put("lcub", "{");
    result.put("rcub", "}");
    result.put("semi", ";");
    return result;
  }

  private HtmlEntities() {
  }
  
  public static String lookupEntity(String ref) {
    return ENTITIES.get(ref);
  }
  
}
