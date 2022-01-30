/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4JSpi;

/**
 *
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class CommandLineArgumentsGatherer<P> implements ParameterGatherer<P> {

  private final Map<String, String> args;
  private final String prefix;
  
  public CommandLineArgumentsGatherer(String args[], String prefix) {
    this.args = argsToMap(args);
    this.prefix = prefix;
  }
  
  Map<String, String> argsToMap(String args[]) {
    Map<String, String> result = new HashMap<>();
    for (String arg : args) {
      String[] parts = arg.split("=", 2);
      if (parts.length == 1) {
        result.put(arg, "true");
      } else {
        result.put(parts[0], parts[1]);
      }
    }
    return result;
  }
  
  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    ObjectReader reader = spi.getPropsMapper().readerForUpdating(base);
    return reader.readValue(
            spi.prepareProperties(args.entrySet(), Entry::getKey, Entry::getValue, prefix)
    );            
  }

}
