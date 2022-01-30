/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class EnvironmentVariablesGatherer<P> implements ParameterGatherer<P> {

  private final Map<String, String> map;
  private final String propertyPrefix;
  
  public EnvironmentVariablesGatherer(String propertyPrefix) {
    this(System.getenv(), propertyPrefix);
  }

  public EnvironmentVariablesGatherer(Map<String, String> map,  String propertyPrefix) {
    this.map = new HashMap<>();
    for (Entry<String, String> entry : map.entrySet()) {
      this.map.put(entry.getKey().replaceAll("_", ".").toLowerCase(), entry.getValue());
    }
    this.propertyPrefix = propertyPrefix;
  }
  
  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    ObjectReader reader = spi.getPropsMapper().readerForUpdating(base);
    return reader.readValue(
            spi.prepareProperties(map.entrySet(), Entry::getKey, Entry::getValue, propertyPrefix)
    );            
  }

}
