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
import uk.co.spudsoft.params4j.FileType;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * The ResourceGatherer loads a single resource file, which can be properties, JSON or YAML.
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class ResourceGatherer<P> implements ParameterGatherer<P> {
  
  private static final Logger logger = LoggerFactory.getLogger(ResourceGatherer.class);

  private final String resource;
  private final FileType type;

  /**
   * Constructor.
   * 
   * The path is passed directly to this.getClass().getResourceAsStream.
   * There is no option for specifying a custom class loader.
   * 
   * @param resource The path to the resource to load.
   * @param fileType The type of file to process.
   */
  public ResourceGatherer(String resource, FileType fileType) {
    this.resource = resource;
    this.type = fileType;
  }

  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    
    ObjectReader reader = type.getObjectMapper(spi).readerForUpdating(base);
    
    try (InputStream stream = this.getClass().getResourceAsStream(resource)) {
      return reader.readValue(stream);
    } catch (Throwable ex) {
      logger.debug("Unable to read resource: {}", resource);
      return base;
    }
  }

  @Override
  public String toString() {
    return "Resource (" + resource + ")";
  }
}
