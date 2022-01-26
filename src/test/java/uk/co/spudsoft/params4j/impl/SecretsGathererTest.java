/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.Params4J;
import uk.co.spudsoft.params4j.Params4JSpi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author jtalbut
 */
public class SecretsGathererTest {

  private static final Logger logger = LoggerFactory.getLogger(SecretsGathererTest.class);

  @Test
  public void testEmptyDir() throws Exception {
    Params4JSpi p4j = (Params4JSpi) Params4J.<DummyParameters>factory()
            .withConstructor(() -> new DummyParameters())
            .withCustomJsonModule(new SimpleModule("pointless"))
            .create();

    assertThat(p4j.getProblemHandler(), instanceOf(DefaultParametersErrorHandler.class));

    SecretsGatherer<DummyParameters> gatherer = new SecretsGatherer<>(new File(Helpers.getResourcePath("/nosecrets")).toPath(), 100, 100, 4, StandardCharsets.UTF_8);
    DummyParameters dp = gatherer.gatherParameters(p4j, new DummyParameters());
    assertEquals(0, dp.getValue());
    assertNull(dp.getChild());
  }

  private void writeToFile(File file, String contents) throws IOException {
    try ( OutputStream stream = new FileOutputStream(file)) {
      stream.write(contents.getBytes(StandardCharsets.UTF_8));
    }
  }

  private static void copyDir(Path srcDir, String destDir) throws IOException {
    int baseLength = srcDir.toString().length();
    Files.walk(srcDir).forEach(srcFile -> {
      String srcFileString = srcFile.toString();
      Path destFile = Paths.get(destDir, srcFileString.substring(baseLength));
      try {
        if (!srcFile.equals(srcDir)) {
          Files.copy(srcFile, destFile);
        }
      } catch (IOException ex) {
        logger.warn("Failed to copy test file: ", ex);
      }
    });
  }

  @Test
  public void testGatherChangingParameters() throws Exception {

    File tempRoot = new File("target/temp");
    tempRoot.mkdirs();
    Path secretsDir = Files.createTempDirectory(tempRoot.toPath(), "SecretsGathererTest");
    copyDir(new File(Helpers.getResourcePath("/secrets")).toPath(), secretsDir.toString());

    SecretsGatherer<DummyParameters> gatherer = new SecretsGatherer<>(secretsDir, 100, 100, 4, StandardCharsets.UTF_8);

    Params4J<DummyParameters> p4j = new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withGatherer(gatherer)
            .create();

    DummyParameters dp = p4j.gatherParameters();
    assertEquals(23, dp.getValue());
    assertEquals("user", dp.getChild().getUsername());
    assertEquals("pass", dp.getChild().getPassword());

    AtomicBoolean called = new AtomicBoolean();

    // Only one of these two should be called
    assertTrue(p4j.notifyOfChanges(p -> {
      assertFalse(called.get());
      called.set(true);
      assertEquals(23, p.getValue());
      assertEquals("new-user", p.getChild().getUsername());
      assertEquals("new-pass", p.getChild().getPassword());
    }));

    assertTrue(p4j.notifyOfChanges(p -> {
      assertFalse(called.get());
      called.set(true);
      assertEquals(23, p.getValue());
      assertEquals("new-user", p.getChild().getUsername());
      assertEquals("new-pass", p.getChild().getPassword());
    }));

    writeToFile(new File(secretsDir.toFile(), "/child/username"), "new-user");
    writeToFile(new File(secretsDir.toFile(), "/child/password"), "new-pass");
    logger.debug("Updated file written");

    long start = System.currentTimeMillis();
    while (!called.get()) {
      if (System.currentTimeMillis() > start + 70000) {
        throw new TimeoutException();
      }
      Thread.sleep(100);
    }

    dp = p4j.gatherParameters();
    assertEquals(23, dp.getValue());
    assertEquals("new-user", dp.getChild().getUsername());
    assertEquals("new-pass", dp.getChild().getPassword());

  }
}
