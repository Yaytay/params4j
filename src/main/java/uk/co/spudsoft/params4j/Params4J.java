/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import uk.co.spudsoft.params4j.impl.Params4JFactoryImpl;

/**
 * Core class for Params4J.
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public interface Params4J<P> {
  
  /**
   * Factory method for creating a Params4J builder.
   * @param <P> The type of the parameters object.
   * @return A newly created Params4JFactory&lt;P>
   */
  static <P> Params4JFactory<P> factory() {
    return new Params4JFactoryImpl<>();
  }
  
  /**
   * Gather parameters from all the configured ParameterGatherers and return a single P object.
   * @return A P object filled with values found by all the configured ParameterGatherers.
   */
  P gatherParameters();
  
  /**
   * Start the monitor for any dynamic changes to parameters whilst the system is running.
   * 
   * Note that this may be called when no actual change has been made to the P object.
   * 
   * @param handler Callback that will be called with a newly created P object when things might have changed.
   * @return True if any of the configured ParameterGatherers support notifications.
   */
  boolean notifyOfChanges(Consumer<P> handler);
  
  /**
   * Return a set of documentation for the properties that can be set in a Parameters class.The documentation values are determined in the following order:
   * <ol>
   * <li>{@link uk.co.spudsoft.params4j.Comment Comment} annotations.
   * <li>First line of the Javadoc as captured by the {@link uk.co.spudsoft.params4j.impl.JavadocCapturer JavadocCapturer}.
   * </ol>
   * 
   * The two lists of regular expressions are both compared against the {@link java.lang.Class#getCanonicalName() canonicalName} of the setter property.
   * 
   * Terminal classes are those, like LocalDateTime, or File that can be set from a single String value and that do not need to be traversed further.
   *
   * Undocumented classes are those, like Vertx Config, that are massive POJOs of properties that this compilation unit does not know about.
   * When presenting undocumented classes to the user they should be listed with the javadoc URL for these classes (which is not known here).
   * 
   * @param defaultInstance An instance of the class to be examined, preloaded with default values.
   * @param prefix The prefix to be prepended to any determined property names to match the prefix specified in (for example) the CommandLineArgumentsGatherer.
   * @param terminalClasses List of regular expressions used to identify properties that are of terminal types - if the class is terminal it should be settable by Jackson from a single string value.
   * @param undocumentedClasses List of regular expressions used to identify properties that are of undocumented types - if the class is undocumented it will not be traversed further despite not being settable by Jackson.
   * @return A list of properties that can be set.
   */
  List<ConfigurationProperty> getDocumentation(P defaultInstance, String prefix, List<Pattern> terminalClasses, List<Pattern> undocumentedClasses);
  
}
