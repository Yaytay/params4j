/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.junit.jupiter.api.Test;
import uk.co.spudsoft.params4j.Params4JSpi;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 *
 * @author jtalbut
 */
public class Params4JFactoryImplTest {
  
  @Test
  public void testWithProblemHandler() {
    DeserializationProblemHandler dph = mock(DeserializationProblemHandler.class);
    Params4JSpi p4j = (Params4JSpi) new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withProblemHandler(dph)
            .create();
    assertSame(dph, ((Params4JImpl) p4j).getProblemHandler());
  }

  @Test
  public void testWithPropsMapper() {
    JavaPropsMapper jpm = mock(JavaPropsMapper.class);
    Params4JSpi p4j = (Params4JSpi) new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withPropsMapper(jpm)
            .create();
    assertSame(jpm, p4j.getPropsMapper());
  }

  @Test
  public void testWithJsonMapper() {
    ObjectMapper om = mock(ObjectMapper.class);
    Params4JSpi p4j = (Params4JSpi) new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withJsonMapper(om)
            .create();
    assertSame(om, p4j.getJsonMapper());
  }

  @Test
  public void testWithYamlMapper() {
    ObjectMapper om = mock(ObjectMapper.class);
    Params4JSpi p4j = (Params4JSpi) new Params4JFactoryImpl<DummyParameters>()
            .withConstructor(() -> new DummyParameters())
            .withYamlMapper(om)
            .create();
    assertSame(om, p4j.getYamlMapper());
  }

}
