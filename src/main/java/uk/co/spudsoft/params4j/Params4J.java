/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j;

import java.util.function.Consumer;
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
    
}
