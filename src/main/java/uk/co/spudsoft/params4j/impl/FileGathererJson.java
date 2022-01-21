/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * @author jtalbut
 */
public class FileGathererJson implements FileGatherer {

  private static final Set<String> EXTENSIONS = new HashSet<>(Arrays.asList(".json"));
  
  @Override
  public Set<String> extensions() {
    return EXTENSIONS;
  }

  @Override
  public <P> P gatherParameters(Params4JSpi spi, File file, P base) throws IOException {    
    ObjectReader reader = spi.getJsonMapper().readerForUpdating(base);
    
    try (InputStream stream = new FileInputStream(file)) {
      return reader.readValue(stream);
    }
  }
  
}
