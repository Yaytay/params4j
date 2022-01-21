/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * @author jtalbut
 */
public interface FileGatherer {
  
  Set<String> extensions();
  
  <P> P gatherParameters(Params4JSpi spi, File file, P base) throws IOException;
  
}
