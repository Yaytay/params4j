/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package uk.co.spudsoft.params4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;

/**
 * Service provider interface for Params4J.
 * @author jtalbut
 */
public interface Params4JSpi {
  
  DeserializationProblemHandler getProblemHandler();
  
  JavaPropsMapper getPropsMapper();
  
  ObjectMapper getJsonMapper();
  
  ObjectMapper getYamlMapper();
  
  void watch(Path path) throws IOException;
  
  /**
   * Return a newly created properties file with values taken from the passed in entries.
   * 
   * Only entries with a key that begins with the propertyPrefix will be included 
   * and the propertyPrefix will be stripped from the output properties.
   * 
   * The keyGetter and valueGetter are expected to return String values.
   * Any non-String values will be filtered out.
   * 
   * @param <T> The type of the entries - typically Map::Entry.
   * @param entries The collection of entries to be processed.
   * @param keyGetter Function to extract the key from an entry - any non-string values will be ignored.
   * @param valueGetter Function to extract the value from an entry - any non-string values will be ignored.
   * @param propertyPrefix Prefix to be removed from the leading part of any key values.
   *     If not null any key values that do not begin with that prefix will be ignored.
   * @return A newly created properties file as a byte array.
   * @throws IOException if something goes wrong.
   */
  <T> byte[] prepareProperties(Collection<T> entries,
       Function<T, Object> keyGetter,
       Function<T, Object> valueGetter,
       String propertyPrefix
  ) throws IOException;
  
}
