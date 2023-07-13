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

import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * This doclet is specifically for the generation of markdown documentation of parameters.
 *
 * Although additional classes can be provided it will only record setters and/or fields (preferring setters, but falling back to
 * fields).
 * 
 * Runs through the specified elements in the {@link jdk.javadoc.doclet.DocletEnvironment} and 
 * uses an {@link uk.co.spudsoft.params4j.doclet.AsciiDocElementVisitor} to walk through the 
 * Java {@link javax.lang.model.element.Element}.
 * 
 * @author njt
 */
public class AsciiDocDoclet implements Doclet {

  private final AsciiDocOptions options;
  private Locale locale;
  private Reporter reporter;
  
  public AsciiDocDoclet() {
    options = new AsciiDocOptions();
  }
  
  @Override
  public void init(Locale locale, Reporter reporter) {
    this.locale = locale;
    this.reporter = reporter;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public Set<? extends Option> getSupportedOptions() {
    return options.getOptions();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_11;
  }

  private static final boolean OK = true;

  @Override
  public boolean run(DocletEnvironment environment) {
    environment.getSpecifiedElements()
            .forEach(e -> {
              reporter.print(Diagnostic.Kind.NOTE, "AsciiDoclet: " + e);
              e.accept(new AsciiDocElementVisitor(environment, options, reporter), null);
            });
    return OK;
  }

}
