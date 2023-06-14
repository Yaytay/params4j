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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.Comment;
import uk.co.spudsoft.params4j.ConfigurationProperty;
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
  
  private static final Set<String> TERMINAL_TYPES = buildTerminalTypes();

  static Set<String> buildTerminalTypes() {
    Set<String> tt = new HashSet<>();
    tt.add("java.time.LocalDateTime");
    tt.add("java.time.LocalDate");
    tt.add("java.time.LocalTime");
    tt.add("java.time.Duration");
    tt.add("java.lang.String");
    tt.add("java.io.File");
    return Collections.unmodifiableSet(tt);
  }
  
  private JavaPropsMapper createPropsMapper(List<com.fasterxml.jackson.databind.Module> customJsonModules, List<MixIn> mixIns) {
    JavaPropsMapper mapper = JavaPropsMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .build();

    return configureObjectMapper(mapper, customJsonModules, mixIns);
  }
  
  private ObjectMapper createYamlMapper(List<com.fasterxml.jackson.databind.Module> customJsonModules, List<MixIn> mixIns) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    return configureObjectMapper(mapper, customJsonModules, mixIns);
  }
  
  private ObjectMapper createJsonMapper(List<com.fasterxml.jackson.databind.Module> customJsonModules, List<MixIn> mixIns) {
    ObjectMapper mapper = new ObjectMapper();
    return configureObjectMapper(mapper, customJsonModules, mixIns);
  }
  
  private <T extends ObjectMapper> T configureObjectMapper(T mapper, List<com.fasterxml.jackson.databind.Module> customJsonModules, List<MixIn> mixIns) {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    if (customJsonModules != null) {
      for (com.fasterxml.jackson.databind.Module module : customJsonModules) {
        mapper.registerModule(module);
      }
    }
    if (mixIns != null) {
      for (MixIn mixIn : mixIns) {
        mapper.addMixIn(mixIn.target, mixIn.source);
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
  
  /**
   * Constructor.
   * 
   * @param constructor Supplier of objects of type P.
   * @param gatherers The gatherers that will be used to set values on the object provided by the Supplier.
   * @param problemHandler The handler that will be used to report any problems parsing files.
   * @param propsMapper The props mapper that is made available to the gatherers via the Params4JSpi.
   * @param jsonMapper The json mapper that is made available to the gatherers via the Params4JSpi.
   * @param customJsonModules The custom JSON modules that are added to the default JSON mapper (if one is not passed in).
   * @param mixIns The custom MixIns that are added to the default JSON mapper (if one is not passed in).
   * @param yamlMapper The yaml mapper that is made available to the gatherers via the Params4JSpi.
   */
  public Params4JImpl(Supplier<P> constructor
          , List<ParameterGatherer<P>> gatherers
          , DeserializationProblemHandler problemHandler
          , JavaPropsMapper propsMapper
          , ObjectMapper jsonMapper
          , List<com.fasterxml.jackson.databind.Module> customJsonModules
          , List<MixIn> mixIns
          , ObjectMapper yamlMapper
  ) {
    Objects.requireNonNull(constructor, "A valid supplier must be set on the factory");
    Objects.requireNonNull(gatherers, "A set of gatherers must be set on the factory");
    this.constructor = constructor;
    this.gatherers = gatherers;
    this.problemHandler = Objects.requireNonNullElseGet(problemHandler, () -> new DefaultParametersErrorHandler());
    this.propsMapper = Objects.requireNonNullElseGet(propsMapper, () -> createPropsMapper(customJsonModules, mixIns));
    this.jsonMapper = Objects.requireNonNullElseGet(jsonMapper, () -> createJsonMapper(customJsonModules, mixIns));
    this.yamlMapper = Objects.requireNonNullElseGet(yamlMapper, () -> createYamlMapper(customJsonModules, mixIns));
    this.fileWatcher = new FileWatcher(this::changeNotificationHandler);
  }

  /**
   * Get the configured problem handler.
   * 
   * This is primarily for test and debug purposes.
   * 
   * @return the configured problem handler.
   */
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
  public <T> byte[] prepareProperties(String name
          , Collection<T> entries
          , Function<T, Object> keyGetter
          , Function<T, Object> valueGetter
          , String propertyPrefix
  ) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
        for (T entry : entries) {
          try {
            String key = getKeyAsPrefixedString(keyGetter, entry, propertyPrefix);
            if (key != null) {
              String value = (String) valueGetter.apply(entry);
              logger.trace("{}: {} = {}", name, key, value);
              writer.append(key)
                  .append(" = ")
                  .append(value)
                  .append("\r\n");
            }
          } catch (ClassCastException ex) {
            logger.warn("{} unable to get key or value as a string from {}", name, entry);
          }
        }
      }
      return baos.toByteArray();
    }
  }

  private <T> String getKeyAsPrefixedString(Function<T, Object> keyGetter, T entry, String propertyPrefix) {
    String castKey = (String) keyGetter.apply(entry);
    String key = null;
    if (propertyPrefix == null || propertyPrefix.isEmpty()) {
      key = castKey;
    } else if (castKey.startsWith(propertyPrefix)) {
      key = castKey.substring(propertyPrefix.length());
    }
    return key;
  }
    
  @Override
  public P gatherParameters() {
    P value = constructor.get();
    for (ParameterGatherer<P> gatherer : gatherers) {
      try {
        value = gatherer.gatherParameters(this, value);
      } catch (Throwable ex) {
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
  
  private static boolean cannotBeEnvVar(String propName) {
    return    propName.contains("[")
              || propName.contains("]")
            ;
  }
  
  private Object getValue(Object object, Field field) {
    if (object == null || field ==  null) {
      return null;
    }
    try {
      if (field.canAccess(object)) {
        return field.get(object);        
      } else {
        field.setAccessible(true);
        Object value = field.get(object);        
        field.setAccessible(false);
        return value;
      }
    } catch (Throwable ex) {
      logger.debug("Failed to get {} from {}: {}", field, object, ex.getMessage());
      return null;
    }
  }
  
  private Object getValue(Object object, String setterName) {
    if (object == null || setterName ==  null || setterName.length() < 4) {
      return null;
    }
    Method method = null;
    try {
      method = object.getClass().getMethod("get" + setterName.substring(3));
    } catch (NoSuchMethodException ex) {
    }
    if (method == null) {
      try {
        method = object.getClass().getMethod("is" + setterName.substring(3));
      } catch (NoSuchMethodException ex) {
      }
    }
    if (method == null) {
      logger.debug("No method {} or {} on {}", "get" + setterName.substring(3), "is" + setterName.substring(3), object.getClass());
      return null;
    }
    try {
      return method.invoke(object);
    } catch (Throwable ex) {
      logger.debug("Failed to execute {}.{} on {}: {}", object.getClass().getSimpleName(), method, object, ex.getMessage());
      return null;
    }
  }

  private Properties loadDocProperties(Map<String, Properties> docProperties, Class<?> type) {
    
    Properties props = docProperties.get(type.getCanonicalName());
    if (props == null) {
      try (InputStream stream = type.getResourceAsStream(type.getSimpleName() + "-doc.properties")) {
        props = new Properties();
        props.load(stream);
        docProperties.put(type.getCanonicalName(), props);
      } catch (Throwable ex) {
      }
    }
    return props;
  }
  
  @Override
  public List<ConfigurationProperty> getDocumentation(P defaultInstance, String prefix, List<Pattern> terminalClasses, List<Pattern> undocumentedClasses) {
    List<ConfigurationProperty> result = new ArrayList<>();

    walkSetters(result, new HashMap<>(), prefix, defaultInstance, defaultInstance.getClass(), new Stack<>(), terminalClasses, undocumentedClasses);
    
    return result;
  }

  private boolean typeIsIn(List<Pattern> list, String typeName) {
    if (list == null || list.isEmpty() || typeName == null) {
      return false;
    }
    for (Pattern pattern : list) {
      if (pattern.matcher(typeName).matches()) {
        return true;
      }
    }
    return false;
  }
  
  private static class PropertyState {
    final String name;
    final String comment;
    final Class<?> clazz;

    PropertyState(String name, String comment, Class<?> clazz) {
      this.name = name;
      this.comment = comment;
      this.clazz = clazz;
    }        
  }
  
  private <T> void walkSetters(
          List<ConfigurationProperty> properties
          , Map<String, Properties> docProperties
          , String prefix
          , T defaultInstance
          , Class<?> clazz
          , Stack<PropertyState> propertyStates
          , List<Pattern> terminalClasses
          , List<Pattern> undocumentedClasses
  ) throws SecurityException {
    Properties classDocProperties = loadDocProperties(docProperties, clazz);
    Set<String> propsDone = new HashSet<>();
    for (Method method : clazz.getMethods()) {
      if (method.getParameters().length == 1 && method.getName().startsWith("set")) {
        Parameter parameter = method.getParameters()[0];
        
        Class<?> fieldType = parameter.getType();
        String fieldName = JavadocCapturer.setterNameToVariableName(method.getName());
        Object defaultValue = getValue(defaultInstance, method.getName());

        propsDone.add(fieldName);
        documentField(properties
                , docProperties
                , classDocProperties
                , prefix
                , propertyStates
                , terminalClasses
                , undocumentedClasses
                , method
                , fieldName
                , fieldType
                , defaultValue
                , () -> (ParameterizedType) parameter.getParameterizedType()
        );
      }
    }
    for (Field field : clazz.getDeclaredFields()) {
      Class<?> fieldType = field.getType();
      String fieldName = field.getName();      
      Object defaultValue = getValue(defaultInstance, field);

      if (!propsDone.contains(fieldName)) {
        documentField(properties
                , docProperties
                , classDocProperties
                , prefix
                , propertyStates
                , terminalClasses
                , undocumentedClasses
                , field
                , fieldName
                , fieldType
                , defaultValue
                , () -> (ParameterizedType) field.getGenericType()
        );
      }
    }
  }

  private void documentField(
          List<ConfigurationProperty> properties
          , Map<String, Properties> docProperties
          , Properties classDocProperties
          , String prefix
          , Stack<PropertyState> propertyStates
          , List<Pattern> terminalClasses
          , List<Pattern> undocumentedClasses
          , AnnotatedElement annotatedElement
          , String fieldName
          , Class<?> fieldType
          , Object defaultValue
          , Supplier<ParameterizedType> parameterizedTypeGetter
    ) throws SecurityException {
    
    String fieldTypeName = fieldType.getCanonicalName();
    boolean undocumented = typeIsIn(undocumentedClasses, fieldTypeName);
    if (fieldType.isPrimitive() || undocumented || TERMINAL_TYPES.contains(fieldTypeName) || typeIsIn(terminalClasses, fieldTypeName)) {
      outputTerminalField(propertyStates, fieldName, annotatedElement, classDocProperties, undocumented, defaultValue, prefix, fieldType, properties);
    } else if (Map.class.isAssignableFrom(fieldType)) {
      
      Type[] actualTypeArguments = parameterizedTypeGetter.get().getActualTypeArguments();
      if (actualTypeArguments.length == 2) {
        PropertyState ps = new PropertyState(fieldName, classDocProperties.getProperty(fieldName), fieldType);
        propertyStates.push(ps);
        documentField(properties, docProperties, classDocProperties, prefix, propertyStates, terminalClasses, undocumentedClasses, annotatedElement, "<xxx>", (Class) actualTypeArguments[1], null, parameterizedTypeGetter);
        propertyStates.pop();
      }
    } else if (List.class.isAssignableFrom(fieldType)) {
      Type[] actualTypeArguments = parameterizedTypeGetter.get().getActualTypeArguments();
      if (actualTypeArguments.length == 1) {
        PropertyState ps = new PropertyState(fieldName, classDocProperties.getProperty(fieldName), fieldType);
        propertyStates.push(ps);
        documentField(properties, docProperties, classDocProperties, prefix, propertyStates, terminalClasses, undocumentedClasses, annotatedElement, "[<n>]", (Class) actualTypeArguments[0], null, parameterizedTypeGetter);
        propertyStates.pop();
      }      
    } else {
      boolean found = false;
      for (PropertyState ps : propertyStates) {
        if (ps.clazz == fieldType) {
          found = true;
        }
      }
      if (!found) {
        PropertyState ps = new PropertyState(fieldName, classDocProperties.getProperty(fieldName), fieldType);
        propertyStates.push(ps);
        walkSetters(properties, docProperties, prefix, defaultValue, fieldType, propertyStates, terminalClasses, undocumentedClasses);
        propertyStates.pop();
      }
    }
  }

  private void outputTerminalField(
          Stack<PropertyState> propertyStates
          , String name
          , AnnotatedElement element
          , Properties classDocProperties
          , boolean undocumented
          , Object defaultValue
          , String prefix
          , Class<?> type
          , List<ConfigurationProperty> properties
  ) {
    String propName = propertyStates.stream().map(ps -> ps.name).collect(Collectors.joining("."));
    if (propName != null && !propName.isEmpty() && !name.startsWith("[")) {
      propName = propName + ".";
    }
    propName = propName + name;
    
    String newComment = null;
    if (element.isAnnotationPresent(Comment.class)) {
      newComment = element.getAnnotation(Comment.class).value();
    }
    if (newComment == null || newComment.isEmpty()) {
      newComment = classDocProperties.getProperty(name);
    }
    String comment = propertyStates.stream().map(ps -> ps.comment).filter(c -> c != null).collect(Collectors.joining(", "));
    if (newComment != null && !newComment.isEmpty()) {
      if (classDocProperties.containsKey(name)) {
        if (comment != null && !comment.isEmpty()) {
          comment = comment + ", " + newComment;
        } else {
          comment = newComment;
        }
      }
    }
    ConfigurationProperty prop = ConfigurationProperty.builder()
            .canBeEnvVar(!cannotBeEnvVar(propName))
            .undocumented(undocumented)
            .comment(comment)
            .defaultValue(defaultValue == null ? null : defaultValue.toString())
            .name(prefix == null ? propName : prefix + propName)
            .type(type)
            .build();
    properties.add(prop);
  }
  
}
