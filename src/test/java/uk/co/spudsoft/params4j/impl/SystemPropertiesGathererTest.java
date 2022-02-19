/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import uk.co.spudsoft.params4j.Params4J;
import uk.co.spudsoft.params4j.Params4JSpi;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author jtalbut
 */
public class SystemPropertiesGathererTest {
  
  @Test
  public void testGatherParameters() throws Exception {
    Params4JSpi p4j = (Params4JSpi) Params4J.<DummyParameters>factory()
            .withConstructor(() -> new DummyParameters())
            .withCustomJsonModule(new SimpleModule("pointless"))
            .create();
    
    Properties props = new Properties();
    props.put("prefix.value", "17");
    props.put("prefix.list[0]", "first");
    props.put("prefix.list[1]", "second");
    props.put("prefix.wrong", "wrong");
    props.put("ignored", "ignored");
    SystemPropertiesGatherer<DummyParameters> gatherer = new SystemPropertiesGatherer<>(props, "prefix.");
    DummyParameters dp = gatherer.gatherParameters(p4j, new DummyParameters());
    assertEquals(17, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));
    
    gatherer = new SystemPropertiesGatherer<>(props, "prefix");
    dp = gatherer.gatherParameters(p4j, new DummyParameters());
    assertEquals(17, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));    
  }
  
}
