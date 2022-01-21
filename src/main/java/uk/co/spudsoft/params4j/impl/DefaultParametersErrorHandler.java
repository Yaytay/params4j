/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jtalbut
 */
public class DefaultParametersErrorHandler extends DeserializationProblemHandler {

  private static final Logger logger = LoggerFactory.getLogger(DefaultParametersErrorHandler.class);

  @Override
  public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType, JsonToken t, JsonParser p, String failureMsg) throws IOException {
    logger.error("Unexpected token: {} at {}: {}", targetType, buildLocation(ctxt.getParser()), failureMsg);
    return null;
  }

  @Override
  public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
    logger.error("Unknown property: {} at {} with value {}", propertyName, buildLocation(ctxt.getParser()), p.readValueAsTree());
    return true; 
  }

  @Override
  public Object handleWeirdNativeValue(DeserializationContext ctxt, JavaType targetType, Object valueToConvert, JsonParser p) throws IOException {
    logger.error("Unprocessable native value : {} \"{}\": Cannot convert to {}", buildLocation(ctxt.getParser()), targetType, valueToConvert, targetType);
    return null;
  }

  @Override
  public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert, String failureMsg) throws IOException {
    logger.error("Unprocessable number value : {} \"{}\": {}", buildLocation(ctxt.getParser()), targetType, valueToConvert, failureMsg);
    return null;
  }

  @Override
  public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
    logger.error("Unprocessable string value : {} \"{}\": {}", buildLocation(ctxt.getParser()), valueToConvert, failureMsg);
    return null;
  }
  
  private String buildLocation(JsonParser parser) {
    
    List<String> names = new ArrayList<>();
    for (JsonStreamContext streamContext = parser.getParsingContext(); streamContext != null; streamContext = streamContext.getParent()) {
      if (streamContext.getCurrentName() != null) {
        names.add(streamContext.getCurrentName());
      }
    }
    Collections.reverse(names);
    return String.join(".", names);
  }
}
