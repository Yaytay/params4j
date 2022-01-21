/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import org.junit.jupiter.api.Test;
import uk.co.spudsoft.params4j.Params4J;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author jtalbut
 */
public class PropertiesResourceGathererTest {
  
  @Test
  public void testGatherParameters() throws Exception {
    PropertiesResourceGatherer<DummyParameters> gatherer = new PropertiesResourceGatherer<>("/test1.properties");
    Params4J<DummyParameters> p4j = new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withGatherer(gatherer)
            .create();
    DummyParameters dp = p4j.gatherParameters();
    assertEquals(17, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));
    assertEquals("2022-01-10T17:10", dp.getLocalDateTime().toString());
    assertFalse(p4j.notifyOfChanges(() -> {}));
    assertFalse(p4j.notifyOfChanges(() -> {}));
  }

  @Test
  public void testIgnoreBadIntParameter() throws Exception {
    PropertiesResourceGatherer<DummyParameters> gatherer = new PropertiesResourceGatherer<>("/test2.properties");
    Params4J<DummyParameters> p4j = new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withGatherer(gatherer)
            .create();
    DummyParameters dp = p4j.gatherParameters();
    assertEquals(0, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));
    assertEquals("2022-01-10T17:11", dp.getLocalDateTime().toString());
  }

}
