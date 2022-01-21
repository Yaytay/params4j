/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j;

import java.util.function.Consumer;
import uk.co.spudsoft.params4j.impl.Params4JFactoryImpl;

/**
 *
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public interface Params4J<P> {
  
  static <P> Params4JFactory<P> factory() {
    return new Params4JFactoryImpl<>();
  }
  
  P gatherParameters();
  
  boolean notifyOfChanges(Consumer<P> handler);
    
}
