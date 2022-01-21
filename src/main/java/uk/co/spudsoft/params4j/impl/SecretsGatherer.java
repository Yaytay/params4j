/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 * A ParameterGatherer that collects parameters from a directory hierarchy.
 * 
 * The directory hierarchy translates to the structure of the JSON object and plain files (or soft links to plain files) translate to text objects.
 * Hidden files and directories (those beginning with a '.') are skipped.
 * 
 * 
 * 
 * This is aimed at allowing the injection of Kubernetes secrets into the parameters object (hence the name) but can used for other purposes.
 * 
 * @author jtalbut
 * 
  * @param <P> The type of the parameters object.
*/
public class SecretsGatherer<P> implements ParameterGatherer<P> {

  private static final Logger logger = LoggerFactory.getLogger(SecretsGatherer.class);
  
  private final Path root;
  private final int fileSizeLimit;
  private final int fileCountLimit;
  private final int fileDepthLimit;
  private final Charset charset;

  public SecretsGatherer(Path root, int fileSizeLimit, int fileCountLimit, int fileDepthLimit, Charset charset) {
    this.root = root;
    this.fileSizeLimit = fileSizeLimit;
    this.fileCountLimit = fileCountLimit;
    this.fileDepthLimit = fileDepthLimit;
    this.charset = charset;
  }
    
  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    ObjectNode node = SecretsLoader.gather(spi.getJsonMapper(), root, fileSizeLimit, fileCountLimit, fileDepthLimit, charset,
            dir -> {
              try {
                spi.watch(dir);
              } catch(IOException ex) {
                logger.warn("Failed to establish watch on {}: ", dir, ex);
              }
            });

    if (!node.isEmpty()) {
      ObjectReader reader = spi.getJsonMapper().readerForUpdating(base);
      base = reader.readValue(node);
    }
    return base;
  }
  
}
