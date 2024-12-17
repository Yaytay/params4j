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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Reporter;

/**
 *
 * @author njt
 */
public class AsciiDocLinkMaps {

  private static final String MODULE_PREFIX = "module:";
  
  private final List<String> configuredBaseUrls = new ArrayList<>();
  private final Map<String, String> baseUrlFromPackage = new HashMap<>();
  private boolean packageListsRead = false;

  public void addConfiguredBaseUrl(String baseUrl) {
    configuredBaseUrls.add(baseUrl);
  }

  public String getUrlForPackage(Reporter reporter, String packageName) {
    if (!configuredBaseUrls.isEmpty() && !packageListsRead) {
      packageListsRead = true;
      for (String cofiguredBaseUrl : configuredBaseUrls) {
        readPackageList(reporter, baseUrlFromPackage, cofiguredBaseUrl);
        readElementList(reporter, baseUrlFromPackage, cofiguredBaseUrl);
      }
    }
    String baseUrl = baseUrlFromPackage.get(packageName);
    if (baseUrl == null) {
      return null;
    } else {
      return baseUrl + packageName.replace('.', '/') + "/";
    }
  }

  public String getUrlForType(Reporter reporter, String packageName, String className) {
    String packageUrl = getUrlForPackage(reporter, packageName);
    if (packageUrl == null) {
      return null;
    } else {
      return packageUrl + className + ".html";
    }
  }
  
  @SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "Attempting to read package list from related documentation")
  static void readPackageList(Reporter reporter, Map<String, String> baseUrlFromPackage, String baseUrl) {
    String url = baseUrl.endsWith("/") ? baseUrl + "package-list" : baseUrl + "/package-list";
    try {
      URL link = new URI(url).toURL();
      URLConnection con = link.openConnection();
      con.setConnectTimeout(2000);
      con.setReadTimeout(2000);
      try (InputStream in = con.getInputStream()) {
        readPackageList(baseUrlFromPackage, in, baseUrl);
      }
    } catch (Throwable ex) {
      reporter.print(Diagnostic.Kind.NOTE, "Failed to download from URL " + url + " (" + ex.toString() + ")");
    }

  }

  @SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "Attempting to read element list from related documentation")
  static void readElementList(Reporter reporter, Map<String, String> baseUrlFromPackage, String baseUrl) {
    String url = baseUrl.endsWith("/") ? baseUrl + "element-list" : baseUrl + "/element-list";
    try {
      URL link = new URI(url).toURL();
      URLConnection con = link.openConnection();
      con.setConnectTimeout(2000);
      con.setReadTimeout(2000);
      try (InputStream in = con.getInputStream()) {
        readPackageList(baseUrlFromPackage, in, baseUrl);
      }
    } catch (Throwable ex) {
      reporter.print(Diagnostic.Kind.NOTE, "Failed to download from URL " + url + " (" + ex.toString() + ")");
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

    try (BufferedReader in = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
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
