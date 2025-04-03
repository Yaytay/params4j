/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import uk.co.spudsoft.params4j.FileType;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 * ParameterGather that gathers parameters from files in a directory.
 * 
 * The files loaded may be property files, or JSON or YAML and will be recognised by their extension (see the @link{uk.co.spudsoft.params4j.impl.FileType} enum for details).
 * The constructor takes any number of 
 * 
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class DirGatherer<P> implements ParameterGatherer<P> {
  
  private static final Logger logger = LoggerFactory.getLogger(DirGatherer.class);
  
  private final File dir;
  private final List<FileType> fileTypes;

  /**
   * Constructor.
   * @param dir The directory that is to be scanned and monitored.
   *            Note that sub directories are not scanned.
   * @param fileTypes The file types that are to be looked for in the directory.
   */
  public DirGatherer(File dir, FileType... fileTypes) {
    this.dir = dir;
    this.fileTypes = Arrays.asList(fileTypes).stream().distinct().collect(Collectors.toList());    
  }

  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    
    Set<File> usedFiles = new HashSet<>();
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (files != null) {
        Arrays.sort(files);  // To ensure consistent behaviour across systems
        for (FileType type : fileTypes) {
          for (File file : files) {
            for (String extension : type.getExtensions()) {
              if (file.exists() && file.getName().endsWith(extension)) {
                logger.debug("Reading file {}", file);
                usedFiles.add(file);
                try {
                  ObjectReader reader = type.getObjectMapper(spi).readerForUpdating(base);

                  try (InputStream stream = new FileInputStream(file)) {
                    base = reader.readValue(stream);
                  }
                } catch (Throwable ex) {
                  logger.error("Failed to process file {}: ", file, ex);
                }
              }
            }
          }
        }
        for (File file : files) {
          if (!usedFiles.contains(file)) {
            logger.trace("Skipped the file {}, not of recognised type ({})", file, fileTypes);        
          }
        }
        spi.watch(dir.toPath());
      } else {
        logger.debug("No files in directory \"{}\"", dir);        
      }
    } else {
      logger.debug("Config directory \"{}\" is not a directory", dir);
    }
    
    return base;    
  }

  @Override
  public String toString() {
    return "Dir (" + dir + ")";
  }
  
}
