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
 * ParameterGather that gathers parameters from environment variables.
 * 
 * Note that on Linux (which include docker on Windows) shells usually have constraints on environment variable names.
 * Specifically names can only consist of letters, digits and the underscore.
 * Furthermore lowercase variable names should be used for local variables, not one that are passed between processes.
 * 
 * Given that params4j expects keys like "prefix.list[1]" this can be a problem.
 * Typically docker and kubernetes do not enforce constraints on the environment variable keys, but if your container
 * uses a shell script to bootstrap your service then it probably is affected.
 * 
 * This class takes two steps to workaround these limitations:
 * 1. Any underscores in environment variable key names are replaced with a dot (".").
 * 2. Optionally, all keys can be converted to lower case (using the default locale).
 * 
 * If this not enough to enable an environment variable to be used for a given value then another approach will have to be found.
 * In particular list values ("list[1]") are not going to work as environment variables if they have to processed by a shell.
 * 
 * To avoid trying to process all environment variables as parameters the namePrefix can be used to filter environment variables.
 * If the namePrefix is supplied (is not null or empty) only environment variables that begin with the prefix will be considered.
 * The prefix will be removed before mapping variables to the parameters object.
 * The prefix is always considered to have a trailing dot ("."), this does not have to be specified in the namePrefix argument.
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public class EnvironmentVariablesGatherer<P> implements ParameterGatherer<P> {

  private final Map<String, String> map;
  private final String namePrefix;
  
  /**
   * Constructor using the default environment.
   * 
   * @param namePrefix The prefix that can be used to filter environment variables.
   * @param toLowerCase If true all environment variable names will be converted to lower case before passing to Jackson.
   */
  public EnvironmentVariablesGatherer(String namePrefix, boolean toLowerCase) {
    this(System.getenv(), namePrefix, toLowerCase);
  }

  /**
   * Constructor using a custom environment.
   * 
   * @param map The environment variables.
   * @param namePrefix The prefix that can be used to filter environment variables.
   * @param toLowerCase If true all environment variable names will be converted to lower case before passing to Jackson.
   */
  public EnvironmentVariablesGatherer(Map<String, String> map,  String namePrefix, boolean toLowerCase) {
    this.map = new HashMap<>();
    for (Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey().replaceAll("_", ".");
      if (toLowerCase) {
        key = key.toLowerCase();
      }
      this.map.put(key, entry.getValue());
    }
    this.namePrefix = namePrefix;
  }
  
  @Override
  public P gatherParameters(Params4JSpi spi, P base) throws IOException {
    ObjectReader reader = spi.getPropsMapper().readerForUpdating(base);
    byte[] props = spi.prepareProperties("Environment variables", map.entrySet(), Entry::getKey, Entry::getValue, namePrefix);
    if (props.length > 0) {
      return reader.readValue(props);
    } else {
      return base;
    }
  }

}
