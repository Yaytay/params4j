/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.co.spudsoft.params4j.Params4J;
import uk.co.spudsoft.params4j.Params4JSpi;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author jtalbut
 */
public class EnvironmentVariablesGathererTest {
  
  @Test
  public void testGatherParameters() throws Exception {
    Params4JSpi p4j = (Params4JSpi) Params4J.<DummyParameters>factory()
            .withConstructor(() -> new DummyParameters())
            .withCustomJsonModule(new SimpleModule("pointless"))
            .create();
    
    Map<String, String> env = new HashMap<>();
    env.put("PREFIX_VALUE", "17");
    env.put("PREFIX_LIST[0]", "first");
    env.put("PREFIX_LIST[1]", "second");
    env.put("PREFIX_WRONG", "wrong");
    env.put("ignored", "ignored");
    EnvironmentVariablesGatherer<DummyParameters> gatherer = new EnvironmentVariablesGatherer<>(env, "prefix.");
    DummyParameters dp = gatherer.gatherParameters(p4j, new DummyParameters());
    assertEquals(17, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));
    
    gatherer = new EnvironmentVariablesGatherer<>(env, "prefix");
    dp = gatherer.gatherParameters(p4j, new DummyParameters());
    assertEquals(17, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));    
  }
  
}
