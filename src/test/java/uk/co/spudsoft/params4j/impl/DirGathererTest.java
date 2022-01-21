/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.Params4J;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author jtalbut
 */
public class DirGathererTest {
  
  private static final Logger logger = LoggerFactory.getLogger(DirGathererTest.class);
  
  private void writeToFile(File file, String contents) throws IOException {
    try (OutputStream stream = new FileOutputStream(file)) {
      stream.write(contents.getBytes(StandardCharsets.UTF_8));
    }
  }
  
  @Test
  public void testGatherParameters() throws Exception {
    String fileName = Helpers.getResourcePath("/test1.properties");
    File tempRoot = new File("target/temp");
    tempRoot.mkdirs();
    Path tempDir = Files.createTempDirectory(tempRoot.toPath(), "PropertiesFileGathererTest");
    
    String fileContents;
    try (InputStream stream = this.getClass().getResourceAsStream("/test1.properties")) {
      fileContents = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
    
    // Order of file creation doesn't matter
    writeToFile(new File(tempDir.toFile(), "test1.properties"), fileContents);
    writeToFile(new File(tempDir.toFile(), "test4.yml"), "value: 19");
    writeToFile(new File(tempDir.toFile(), "test3.yaml"), "value: 21");
    writeToFile(new File(tempDir.toFile(), "test2.json"), "{\"list\":[\"third\",\"fourth\"]}");
    
    DirGatherer<DummyParameters> gatherer = new DirGatherer<>(tempDir.toFile(), FileType.Properties, FileType.Json, FileType.Yaml);
    Params4J<DummyParameters> p4j = new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withGatherer(gatherer)
            .create();
    DummyParameters dp = p4j.gatherParameters();
    assertEquals(19, dp.getValue());
    // Merging of lists always adds to them, rather than replacing them
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));
    assertEquals("third", dp.getList().get(2));
    assertEquals("fourth", dp.getList().get(3));
    assertEquals("2022-01-10T17:10", dp.getLocalDateTime().toString());
    
    AtomicBoolean called = new AtomicBoolean();
    
    assertTrue(p4j.notifyOfChanges(p -> {
      called.set(true);
    }));
    
    assertTrue(p4j.notifyOfChanges(p -> {
      called.set(true);
    }));
    
    writeToFile(new File(tempDir.toFile(), "test4.yml"), "value: 23\nlocalDateTime: 2022-01-10T18:10");
    logger.debug("Updated file written");
    
    long start = System.currentTimeMillis();
    while(!called.get()) {
      if (System.currentTimeMillis() > start + 70000) {
        throw new TimeoutException();
      }
      Thread.sleep(100);
    }
    
    dp = p4j.gatherParameters();
    assertEquals(23, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));
    assertEquals("third", dp.getList().get(2));
    assertEquals("fourth", dp.getList().get(3));
    assertEquals("2022-01-10T18:10", dp.getLocalDateTime().toString());
    
    
  }
}
