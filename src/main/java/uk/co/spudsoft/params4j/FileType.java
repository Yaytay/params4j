/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author jtalbut
 */
public enum FileType {
  
  /**
   * Values are read from a properties file.
   * 
   * The format of the properties file is defined <a href="https://docs.oracle.com/javase/10/docs/api/java/util/Properties.html#load(java.io.Reader)">here</a>.
   * The Jackson handling of properties files is defined <a href="https://fasterxml.github.io/jackson-dataformats-text/javadoc/properties/2.9/com/fasterxml/jackson/dataformat/javaprop/JavaPropsMapper.html">here</a>
   * The specific configuration of the JavaPropsMapper used by Params4J can be found in {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createPropsMapper()}.
   * 
   * @see <a href="https://fasterxml.github.io/jackson-dataformats-text/javadoc/properties/2.9/com/fasterxml/jackson/dataformat/javaprop/JavaPropsMapper.html">JavaPropsMapper</a>
   * @see <a href="https://docs.oracle.com/javase/10/docs/api/java/util/Properties.html#load(java.io.Reader)">Properties::load(java.io.Reader)</a>
   */
  Properties(Arrays.asList(".properties"), Params4JSpi::getPropsMapper)
  , 
  /**
   * Values are read from a yaml file.
   * 
   * The specification for YAML files is <a href="https://yaml.org/spec/">here</a>.
   * The Jackson handling of yaml files is defined <a href="https://github.com/FasterXML/jackson-dataformats-text/tree/2.14/yaml">here</a>.
   * The specific configuration of the ObjectMapper used by Params4J can be found in {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createYamlMapper()}.
   * 
   * @see <a href="https://yaml.org/spec/">YAML spec</a>
   * @see <a href="https://github.com/FasterXML/jackson-dataformats-text/tree/2.14/yaml">jackson-dataformats-test/yaml</a>
   * 
   */
  Yaml(Arrays.asList(".yaml", ".yml"), Params4JSpi::getYamlMapper)
  , 
  /**
   * Values are read from a JSON file.
   * 
   * The specification for JSON files if <a href="https://www.json.org/json-en.html">here</a>.
   * The Jackson handling of json files is defined <a href="https://github.com/FasterXML/jackson">here</a>.
   * The specific configuration of the ObjectMapper used by Params4J can be found in {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createJsonMapper(java.util.List)}.
   * 
   */
  Json(Arrays.asList(".json"), Params4JSpi::getJsonMapper)

  ;
  
  private final List<String> extensions;
  private final Function<Params4JSpi, ObjectMapper> objectMapGetter;

  /**
   * Get the list of ends of file names that indicate this file type.
   * @return the list of filename extensions that indicate this file type.
   */
  public List<String> getExtensions() {
    return extensions;
  }
  
  /**
   * Get the ObjectMapper to use for files of this type.
   * @param spi The service provider interface that contains references to the different ObjectMappers.
   * @return An ObjectMapper able to process this file type.
   */
  public ObjectMapper getObjectMapper(Params4JSpi spi) {
    return objectMapGetter.apply(spi);
  }
  
  /**
   * Constructor.
   * @param extensions the list of filename extensions that indicate this file type.
   * @param objectMapGetter function to object the appropriate ObjectMapper from the service provider interface.
   */
  FileType(List<String> extensions, Function<Params4JSpi, ObjectMapper> objectMapGetter) {
    this.extensions = extensions;
    this.objectMapGetter = objectMapGetter;
  }
  
  
  
}
