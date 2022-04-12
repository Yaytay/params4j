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
 * ParameterGather that gathers parameters from Java system properties.
 * 
 * To avoid trying to process all environment variables as parameters the namePrefix can be used to filter environment variables.
 * If the namePrefix is supplied (is not null or empty) only environment variables that begin with the prefix will be considered.
 * The prefix will be removed before mapping variables to the parameters object.
 * The prefix is always considered to have a trailing dot ("."), this does not have to be specified in the namePrefix argument.
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class SystemPropertiesGatherer<P> implements ParameterGatherer<P> {

  private final Properties props;
  private final String propertyPrefix;
  
  /**
   * Constructor.
   * @param namePrefix The prefix to use to filter out system properties that should not be considered.
   */
  public SystemPropertiesGatherer(String namePrefix) {
    this(System.getProperties(), namePrefix);
  }

  /**
   * Constructor.
   * 
   * This constructor is only intended for test purposes.
   * 
   * @param props Properties data to process.
   * @param namePrefix The prefix to use to filter out system properties that should not be considered.
   */
  public SystemPropertiesGatherer(Properties props,  String namePrefix) {
    this.props = props;
    this.propertyPrefix = namePrefix;
  }
  
  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    ObjectReader reader = spi.getPropsMapper().readerForUpdating(base);
    return reader.readValue(
            spi.prepareProperties("System properties", props.entrySet(), Entry::getKey, Entry::getValue, propertyPrefix)
    );            
  }

}
