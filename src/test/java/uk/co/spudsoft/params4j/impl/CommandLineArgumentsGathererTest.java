/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import uk.co.spudsoft.params4j.Params4J;
import uk.co.spudsoft.params4j.Params4JSpi;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author jtalbut
 */
public class CommandLineArgumentsGathererTest {
  
  @Test
  public void testGatherParameters() throws Exception {
    Params4JSpi p4j = (Params4JSpi) Params4J.<DummyParameters>factory()
            .withConstructor(() -> new DummyParameters())
            .withCustomJsonModule(new SimpleModule("pointless"))
            .create();
    
    String args[] = new String[]{
      "value=17"
      , "list[0]=first"
      , "list[1]=second"
      , "wrong=wrong"
    };
    
    CommandLineArgumentsGatherer<DummyParameters> gatherer = new CommandLineArgumentsGatherer<>(args, null);
    DummyParameters dp = gatherer.gatherParameters(p4j, new DummyParameters());
    assertEquals(17, dp.getValue());
    assertEquals("first", dp.getList().get(0));
    assertEquals("second", dp.getList().get(1));
  }  
}
