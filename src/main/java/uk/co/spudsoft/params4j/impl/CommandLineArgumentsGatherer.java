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
 * ParameterGather that gathers parameters from command line arguments.
 * 
 * Arguments are parsed by whatever shell is in use and should be escaped appropriately.
 * Each string in the args[] array should be a single key/value pair separated by "=".
 * Any arguments that do not contain the equals character will be treated as being "=true";
 * 
 * To avoid trying to process all arguments as parameters the namePrefix can be used to filter them.
 * If the namePrefix is supplied (is not null or empty) only environment variables that begin with the prefix will be considered.
 * The prefix will be removed before mapping variables to the parameters object.
 * The prefix is always considered to have a trailing dot ("."), this does not have to be specified in the namePrefix argument.
 * Use of the namePrefix is less useful on command line arguments than on environment variables or system properties, but it often makes sense to include it for consistency. 
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class CommandLineArgumentsGatherer<P> implements ParameterGatherer<P> {

  private final Map<String, String> args;
  private final String namePrefix;
  
  /**
   * Constructor.
   * 
   * @param args The arguments passed in to the main() method.
   * @param namePrefix The prefix that can be used to filter arguments.
   */
  public CommandLineArgumentsGatherer(String args[], String namePrefix) {
    this.args = argsToMap(args);
    this.namePrefix = namePrefix;
  }
  
  final Map<String, String> argsToMap(String args[]) {
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
            spi.prepareProperties("Command line arguments", args.entrySet(), Entry::getKey, Entry::getValue, namePrefix)
    );            
  }

}
