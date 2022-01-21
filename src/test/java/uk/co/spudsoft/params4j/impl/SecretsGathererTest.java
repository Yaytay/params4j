/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import uk.co.spudsoft.params4j.Params4J;
import uk.co.spudsoft.params4j.Params4JSpi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author jtalbut
 */
public class SecretsGathererTest {
  
  @Test
  public void testGatherParameters() throws Exception {
    Params4JSpi p4j = (Params4JSpi) Params4J.<DummyParameters>factory()
            .withConstructor(() -> new DummyParameters())
            .withCustomJsonModule(new SimpleModule("pointless"))
            .create();
    
    assertThat(p4j.getProblemHandler(), instanceOf(DefaultParametersErrorHandler.class));
    
    SecretsGatherer<DummyParameters> gatherer = new SecretsGatherer<>(new File(Helpers.getResourcePath("/secrets")).toPath(), 100, 100, 4, StandardCharsets.UTF_8);
    DummyParameters dp = gatherer.gatherParameters(p4j, new DummyParameters());
    assertEquals(23, dp.getValue());
    assertEquals("user", dp.getChild().getUsername());
    assertEquals("pass", dp.getChild().getPassword());
  }
  
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
  
}
