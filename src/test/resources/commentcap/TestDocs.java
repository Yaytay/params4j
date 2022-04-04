package commentcap;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.ConfigurationProperty;
import uk.co.spudsoft.params4j.Params4J;

/*
 * Copyright (C) 2022 jtalbut
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

/**
 *
 * @author jtalbut
 */
public class TestDocs {
  
  @SuppressWarnings("constantname")
  private static final Logger logger = LoggerFactory.getLogger(TestDocs.class);

  private List<ConfigurationProperty> docs;
  
  public TestDocs() {
    Params4J<Parameters> params4j = Params4J.<Parameters>factory().withConstructor(() -> new Parameters()).create();
    docs = params4j.getDocumentation(new Parameters(), "--", null, Arrays.asList(Pattern.compile(".*\\.Box")));
  }

  public List<ConfigurationProperty> getDocs() {
    return docs;
  }
  
}
