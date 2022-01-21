/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class PropertiesResourceGatherer<P> implements ParameterGatherer<P> {
  
  private static final Logger logger = LoggerFactory.getLogger(PropertiesResourceGatherer.class);

  private final String resource;

  public PropertiesResourceGatherer(String resource) {
    this.resource = resource;
  }

  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    
    ObjectReader reader = spi.getPropsMapper().readerForUpdating(base);
    
    try (InputStream stream = this.getClass().getResourceAsStream(resource)) {
      return reader.readValue(stream);
    }
  }

}
