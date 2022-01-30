/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4J;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class Params4JImpl<P> implements Params4J<P>, Params4JSpi {

  private static final Logger logger = LoggerFactory.getLogger(Params4JImpl.class);
  
  private final Supplier<P> constructor;
  private final List<ParameterGatherer<P>> gatherers;
  private final DeserializationProblemHandler problemHandler;
  private final JavaPropsMapper propsMapper;
  private final ObjectMapper jsonMapper;
  private final ObjectMapper yamlMapper;
  private final FileWatcher fileWatcher;
  private Consumer<P> changeHappenedHandler;
  private final AtomicReference<ObjectNode> lastValue = new AtomicReference<>();
  
  private JavaPropsMapper createPropsMapper() {
    JavaPropsMapper mapper = JavaPropsMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .addModule(new JavaTimeModule())
            .addHandler(problemHandler)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .defaultMergeable(Boolean.TRUE)
            .build();
    return mapper;
  }
  
  private ObjectMapper createYamlMapper() {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.setDefaultMergeable(Boolean.TRUE);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    mapper.addHandler(problemHandler);
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return mapper;
  }
  
  private ObjectMapper createJsonMapper(List<com.fasterxml.jackson.databind.Module> customJsonModules) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    if (customJsonModules != null) {
      for (com.fasterxml.jackson.databind.Module module : customJsonModules) {
        mapper.registerModule(module);
      }
    }
    mapper.registerModule(new JavaTimeModule());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setDefaultMergeable(Boolean.TRUE);
    mapper.addHandler(problemHandler);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    return mapper;
  }
  
  public Params4JImpl(Supplier<P> constructor
          , List<ParameterGatherer<P>> gatherers
          , DeserializationProblemHandler problemHandler
          , JavaPropsMapper propsMapper
          , ObjectMapper jsonMapper
          , List<com.fasterxml.jackson.databind.Module> customJsonModules
          , ObjectMapper yamlMapper
  ) {
    Objects.requireNonNull(constructor, "A valid supplier must be set on the factory");
    Objects.requireNonNull(gatherers, "A set of gatherers must be set on the factory");
    this.constructor = constructor;
    this.gatherers = gatherers;
    this.problemHandler = Objects.requireNonNullElseGet(problemHandler, () -> new DefaultParametersErrorHandler());
    this.propsMapper = Objects.requireNonNullElseGet(propsMapper, () -> createPropsMapper());
    this.jsonMapper = Objects.requireNonNullElseGet(jsonMapper, () -> createJsonMapper(customJsonModules));
    this.yamlMapper = Objects.requireNonNullElseGet(yamlMapper, () -> createYamlMapper());
    this.fileWatcher = new FileWatcher(this::changeNotificationHandler);
  }

  @Override
  public DeserializationProblemHandler getProblemHandler() {
    return problemHandler;
  }

  @Override
  public JavaPropsMapper getPropsMapper() {
    return propsMapper;
  }

  @Override
  public ObjectMapper getJsonMapper() {
    return jsonMapper;
  }

  @Override
  public ObjectMapper getYamlMapper() {
    return yamlMapper;
  }

  @Override
  public void watch(Path path) throws IOException {
    fileWatcher.watch(path);
  }
  
  @Override
  public <T> byte[] prepareProperties(Collection<T> entries
          , Function<T, Object> keyGetter
          , Function<T, Object> valueGetter
          , String propertyPrefix
  ) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
        for (T entry : entries) {
          Object keyObject = keyGetter.apply(entry);
          if (keyObject instanceof String) {
            String castKey = (String) keyObject;
            String key = null;
            if (propertyPrefix == null || propertyPrefix.isEmpty()) {
              key = castKey;
            } else if (castKey.startsWith(propertyPrefix)) {
              key = castKey.substring(propertyPrefix.length());
            }
            Object value = valueGetter.apply(entry);
            if (value instanceof String) {
              writer.append(key)
                  .append(" = ")
                  .append((String) value)
                  .append("\r\n");
            }
          }
        }
      }
      return baos.toByteArray();
    }
  }
    
  @Override
  public P gatherParameters() {
    P value = constructor.get();
    for (ParameterGatherer<P> gatherer : gatherers) {
      try {
        value = gatherer.gatherParameters(this, value);
      } catch(Throwable ex) {
        logger.warn("Failed to process: ", ex);
      }
    }
    lastValue.set(jsonMapper.convertValue(value, ObjectNode.class));
    return value;
  }

  @Override
  public boolean notifyOfChanges(Consumer<P> handler) {
    changeHappenedHandler = handler;
    return fileWatcher.start();
  }
    
  private void changeNotificationHandler() {
    synchronized (lastValue) {
      P newValue = gatherParameters();
      ObjectNode newNode = jsonMapper.convertValue(newValue, ObjectNode.class);
      if (newNode.equals(lastValue.get())) {
        lastValue.set(newNode);
        changeHappenedHandler.accept(newValue);
      }
    }
  }
  
}
