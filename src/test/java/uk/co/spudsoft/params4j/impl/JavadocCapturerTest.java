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
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.ConfigurationProperty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author jtalbut
 */
public class JavadocCapturerTest {
  
  @SuppressWarnings("constantname")
  private static final Logger logger = LoggerFactory.getLogger(JavadocCapturerTest.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  
  @Test
  public void testGetSupportedAnnotationTypes() {
    JavadocCapturer capturer = new JavadocCapturer();
    assertNotNull(capturer.getSupportedAnnotationTypes());
  }

  @Test
  public void testGetSupportedSourceVersion() {
    JavadocCapturer capturer = new JavadocCapturer();
    assertNotNull(capturer.getSupportedSourceVersion());
  }

  @Test
  public void testProcess() {
    JavadocCapturer capturer = new JavadocCapturer();
    assertTrue(capturer.process(new HashSet<>(), null));
  }
  
  @Test
  public void testCompilation() throws Exception {

    File dest = new File("target/built-classes");
    dest.mkdir();
    
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try ( StandardJavaFileManager mgr = compiler.getStandardFileManager(null, Locale.UK, StandardCharsets.UTF_8)) {
      mgr.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(dest));
      Iterable<? extends JavaFileObject> sources = mgr.getJavaFileObjectsFromFiles(
              Arrays.asList(
                      new File(getClass().getResource("/commentcap/Parameters.java").toURI()),
                      new File(getClass().getResource("/commentcap/DataSource.java").toURI()),
                      new File(getClass().getResource("/commentcap/Credentials.java").toURI()),
                      new File(getClass().getResource("/commentcap/TestDocs.java").toURI())
              )
      );
      JavaCompiler.CompilationTask task = compiler.getTask(null, mgr, null, Arrays.asList("-processor", "uk.co.spudsoft.params4j.impl.JavadocCapturer"), null, sources);
      task.call();
    }

    ClassLoader cl = new URLClassLoader(new URL[]{ dest.toURI().toURL()});
    Class<?> cls = cl.loadClass("commentcap.Parameters");
    
    Properties props = new Properties();
    try ( InputStream stream = cls.getResourceAsStream("/commentcap/Parameters-doc.properties")) {
      props.load(stream);
    }
    assertThat(props.size(), equalTo(11));
    assertThat(props.get("auditDataSource"), equalTo("datasource used for recording activity"));
    assertThat(props.get("baseConfigPath"), equalTo("path to the root of the configuration files"));
    assertThat(props.get("logins"), equalTo("login for a system"));
    for (Object key : props.keySet()) {
      assertThat(key, instanceOf(String.class));
      // No entries for setters in File object
      assertThat((String) key, not(CoreMatchers.containsString(".")));
    }    

    Class<?> testDocsClass = cl.loadClass("commentcap.TestDocs");
    Object object = testDocsClass.getConstructor().newInstance();
    @SuppressWarnings("unchecked")
    List<ConfigurationProperty> docs = (List<ConfigurationProperty>) testDocsClass.getMethod("getDocs").invoke(object);
    assertNotNull(docs);
    for (ConfigurationProperty cp : docs) {
      logger.trace("Configuration property: {}", OBJECT_MAPPER.writeValueAsString(cp));
    }
    assertThat(docs, hasSize(21));
    // Convert from array to map because the order isn't stable with reflection
    Map<String, ConfigurationProperty> docsMap = docs.stream().collect(Collectors.toMap(cp -> cp.name, cp -> cp));
    
    Map<String, ConfigurationProperty> expectedDocsAsJson = loadExpectedDocsAsJson();
    assertEquals(expectedDocsAsJson.size(), docsMap.size());
    for (Entry<String, ConfigurationProperty> entry : expectedDocsAsJson.entrySet()) {
      assertTrue(docsMap.containsKey(entry.getKey()), "Fields does not include " + entry.getKey());
      assertEquals(OBJECT_MAPPER.convertValue(entry.getValue(), ObjectNode.class), OBJECT_MAPPER.convertValue(docsMap.get(entry.getKey()), ObjectNode.class));
    }
    
    Method testProcessArgs = testDocsClass.getMethod("testProcessArgs");
    testProcessArgs.invoke(object);

    Method usageMethod = testDocsClass.getMethod("usage");
    String usage = (String) usageMethod.invoke(object);
    logger.debug("Usage:\n{}", usage);
    
    Method envVarsMethod = testDocsClass.getMethod("envVars");
    String envVars = (String) envVarsMethod.invoke(object);
    logger.debug("Env vars:\n{}", envVars);
    
  }
  
  private Map<String, ConfigurationProperty> loadExpectedDocsAsJson() throws IOException {
    Map<String, ConfigurationProperty> map = new HashMap<>();
    try (InputStream stream = getClass().getResourceAsStream("/commentcap-expected.json")) {      
      ArrayNode array = OBJECT_MAPPER.readValue(stream, ArrayNode.class);
      for (JsonNode node : array) {
        ConfigurationProperty cp = OBJECT_MAPPER.convertValue(node, ConfigurationProperty.class);
        map.put(cp.name, cp);
      }
    }
    return map;
  }
  

}
