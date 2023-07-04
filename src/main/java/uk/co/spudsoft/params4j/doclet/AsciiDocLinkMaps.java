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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author njt
 */
public class AsciiDocLinkMaps {
  
  private static final Logger logger = LoggerFactory.getLogger(AsciiDocLinkMaps.class);

  private static final String MODULE_PREFIX = "module:";
  
  private final List<String> configuredBaseUrls = new ArrayList<>();
  private final Map<String, String> baseUrlFromPackage = new HashMap<>();
  private boolean packageListsRead = false;

  public void addConfiguredBaseUrl(String baseUrl) {
    configuredBaseUrls.add(baseUrl);
  }

  public String getUrlForPackage(String packageName) {
    if (!configuredBaseUrls.isEmpty() && !packageListsRead) {
      packageListsRead = true;
      for (String cofiguredBaseUrl : configuredBaseUrls) {
        readPackageList(baseUrlFromPackage, cofiguredBaseUrl);
        readElementList(baseUrlFromPackage, cofiguredBaseUrl);
      }
    }
    String baseUrl = baseUrlFromPackage.get(packageName);
    if (baseUrl == null) {
      return null;
    } else {
      return baseUrl + packageName.replace('.', '/') + "/";
    }
  }

  public String getLinkForReference(Reference ref) {
    StringBuilder builder = new StringBuilder();
    String packageUrl = getUrlForPackage(ref.getPackageName());
    if (packageUrl == null) {
      builder.append(ref.getClassName());
    } else {
      builder.append("link:");
      builder.append(packageUrl);
      builder.append(ref.getClassName());
      builder.append(".html");
      if (ref.getMethodSignature() != null) {
        builder.append("#");
        builder.append(ref.getMethodSignature().replaceAll("\\[", "%5B").replaceAll("\\]", "%5D").replaceAll("\\s+", ""));
      }
      builder.append("[");
      builder.append(ref.getClassName());
      if (ref.getSimpleMethodSignature() != null) {
        builder.append("#");
        builder.append(ref.getSimpleMethodSignatureEscaped());
      }
      builder.append("] ");
    }
    return builder.toString();
  }
  
  static void readPackageList(Map<String, String> baseUrlFromPackage, String baseUrl) {
    try {
      URL link = new URL(baseUrl.endsWith("/") ? baseUrl + "package-list" : baseUrl + "/package-list");
      try (InputStream in = link.openStream()) {
        readPackageList(baseUrlFromPackage, in, baseUrl);
      }
    } catch (Throwable ex) {
      logger.debug("Unable to read package list from {}: ", baseUrl, ex);
    }

  }

  static void readElementList(Map<String, String> baseUrlFromPackage, String baseUrl) {
    try {
      URL link = new URL(baseUrl.endsWith("/") ? baseUrl + "element-list" : baseUrl + "/element-list");
      try (InputStream in = link.openStream()) {
        readPackageList(baseUrlFromPackage, in, baseUrl);
      }
    } catch (Throwable ex) {
      logger.debug("Unable to read package list from {}: ", baseUrl, ex);
    }
  }

  /**
   * Read the file "element-list" and for each element name found, create Extern object and associate it with the element name in
   * the map.
   *
   * @param baseUrlFromPackage Map from package to base URL for docs.
   * @param input InputStream from the "element-list" file.
   * @param url URL or the directory path to the elements.
   * @throws IOException if there is a problem reading or closing the stream
   */
  static void readPackageList(Map<String, String> baseUrlFromPackage, InputStream input, String url) throws IOException {

    try (BufferedReader in = new BufferedReader(new InputStreamReader(input))) {
      String line;
      String moduleUrl = url;
      if (!url.endsWith("/")) {
        url = url + "/";
      }
      while ((line = in.readLine()) != null) {
        if (!line.isBlank()) {
          if (line.startsWith(MODULE_PREFIX)) {
            String moduleName = line.substring(MODULE_PREFIX.length());
            moduleUrl = url + moduleName + "/";
          } else {
            String packageName = line;
            baseUrlFromPackage.put(packageName, moduleUrl);
          }
        }
      }
    }
  }

}
