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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jdk.javadoc.doclet.Doclet.Option;

/**
 *
 * @author njt
 */
public class AsciiDocOptions {
  
  private final List<Option> options = buildOptions();
  
  private String destDirName;
  private final Set<String> includeClasses = new HashSet<>();
  
  private final AsciiDocLinkMaps linkMaps = new AsciiDocLinkMaps();
  
  private List<Option> buildOptions() {
    List<Option> result = new ArrayList<>();

    result.add(new AbstractAsciiDocOption(1, "Destination directory for output files", "-d", "directory") {
      @Override
      public boolean process(String option, List<String> arguments) {
        destDirName = arguments.get(0);
        return true;
      }
    });
    
    result.add(new AbstractAsciiDocOption(1, "If set, only the listed classes will be processed", "--include-classes", "include-classes") {
      @Override
      public boolean process(String option, List<String> arguments) {
        for (String arg : arguments) {
          String parts[] = arg.split(",");
          for (String part : parts) {
            includeClasses.add(part.trim());
          }
        }
        return true;
      }
    });
    
    result.add(new AbstractAsciiDocOption(1, "Map from package names to a URL base to use for links", "-link", "link") {
      @Override
      public boolean process(String option, List<String> arguments) {
        for (String arg : arguments) {
          String parts[] = arg.split(",");
          for (String part : parts) {
            linkMaps.addConfiguredBaseUrl(part);
          }
        }
        return true;
      }
    });
    
    return result;
  }

  public Set<? extends Option> getOptions() {
    return new HashSet<>(options);
  }

  public String getDestDirName() {
    return destDirName;
  }

  public Set<String> getIncludeClasses() {
    return includeClasses;
  }

  public AsciiDocLinkMaps getLinkMaps() {
    return linkMaps;
  }
  
}
