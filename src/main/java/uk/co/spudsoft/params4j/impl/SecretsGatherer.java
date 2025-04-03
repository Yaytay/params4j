/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
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
 * Note that each entire file is read in as a single parameter value, the files are not parsed.
 * 
 * This is aimed at allowing the injection of Kubernetes secrets into the parameters object (hence the name) but can used for other purposes.
 * 
 * The various limit parameters are provided to restrict the time and memory wasted by a bad configuration.
 * In a standard situation the limits should be set to values that are larger than any expected values.
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

  /**
   * Constructor.
   * 
   * @param root The base path from which to start searching, typically something like "/etc/[service name]/conf.d"
   * @param fileSizeLimit The maximum size of file to attempt to load.
   * The recommendation is to set this to slightly larger than your standard secret length.
   * @param fileCountLimit The maximum number of files to attempt to load.
   * The recommendation is to set this to one more than the number of secrets in your parameters hierarchy.
   * @param fileDepthLimit The maximum depth of hierarchy to traverse.
   * The recommendation is to set this to one more than the maximum depth of secrets in your parameters hierarchy.
   * @param charset The charset to use when reading the file.
   */
  public SecretsGatherer(Path root, int fileSizeLimit, int fileCountLimit, int fileDepthLimit, Charset charset) {
    this.root = root;
    this.fileSizeLimit = fileSizeLimit;
    this.fileCountLimit = fileCountLimit;
    this.fileDepthLimit = fileDepthLimit;
    this.charset = charset;
  }
    
  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    SecretsWalker visitor = new SecretsWalker(root, spi.getJsonMapper(), fileSizeLimit, fileCountLimit, charset, dir -> {
              try {
                spi.watch(dir);
              } catch (IOException ex) {
                logger.warn("Failed to establish watch on {}: ", dir, ex);
              }
            });
    Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), fileDepthLimit, visitor);
    ObjectNode node = visitor.getObjectNode();
    if (!node.isEmpty()) {
      ObjectReader reader = spi.getJsonMapper().readerForUpdating(base);
      base = reader.readValue(node);
    }
    return base;
  }
  
  @Override
  public String toString() {
    return "Secrets (" + root + ")";
  }
  
}
