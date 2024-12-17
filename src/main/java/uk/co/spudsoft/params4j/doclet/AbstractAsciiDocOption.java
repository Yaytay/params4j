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

import java.util.Collections;
import java.util.List;
import jdk.javadoc.doclet.Doclet;

/**
 * Abstract option for configuring a {@link jdk.javadoc.doclet.Doclet}.
 * 
 * @author njt
 */
public abstract class AbstractAsciiDocOption implements Doclet.Option {
  
  private final int argumentCount;
  private final String description;
  private final List<String> names;
  private final String parameters;

  /**
   * Constructor.
   * 
   * @param argumentCount The number of arguments, this option will consume.
   * @param description Description of the argument.
   * @param name The parameter as it would be specified on the command line (or in addiitonalOptions in the maven plugin).
   * @param parameters The name of the parameter to the option.
   */
  public AbstractAsciiDocOption(int argumentCount, String description, String name, String parameters) {
    this.argumentCount = argumentCount;
    this.description = description;
    this.names = Collections.singletonList(name);
    this.parameters = parameters;
  }

  @Override
  public int getArgumentCount() {
    return argumentCount;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Kind getKind() {
    return Kind.STANDARD;
  }

  @Override
  public List<String> getNames() {
    return names;
  }

  @Override
  public String getParameters() {
    return parameters;
  }
  
}
