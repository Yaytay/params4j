/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 * Gather parameters from files in a directory.
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class DirGatherer<P> implements ParameterGatherer<P> {
  
  private static final Logger logger = LoggerFactory.getLogger(DirGatherer.class);
  
  private final File dir;
  private final List<FileType> types;

  public DirGatherer(File dir, FileType... types) {
    this.dir = dir;
    this.types = Arrays.asList(types);    
  }

  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      Arrays.sort(files);  // To ensure consistent behaviour across systems
      for (FileType type : types) {
        for (File file : files) {
          for (String extension : type.getGatherer().extensions()) {
            if (file.exists() && file.getName().endsWith(extension)) {
              try {
                base = type.getGatherer().gatherParameters(spi, file, base);
              } catch(Throwable ex) {
                logger.error("Failed to process file {}: ", file, ex);
              }
            }
          }
        }
      }
    }
    spi.watch(dir.toPath());
    
    return base;    
  }

}
