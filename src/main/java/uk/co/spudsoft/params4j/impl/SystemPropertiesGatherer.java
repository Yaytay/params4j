/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class SystemPropertiesGatherer<P> implements ParameterGatherer<P> {

  private final Properties props;
  private final String propertyPrefix;
  
  public SystemPropertiesGatherer(String propertyPrefix) {
    this(System.getProperties(), propertyPrefix);
  }

  public SystemPropertiesGatherer(Properties props,  String propertyPrefix) {
    this.props = props;
    this.propertyPrefix = propertyPrefix;
  }
  
  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    ObjectReader reader = spi.getPropsMapper().readerForUpdating(base);
    return reader.readValue(
            spi.prepareProperties(props.entrySet(), Entry::getKey, Entry::getValue, propertyPrefix)
    );            
  }

}
