/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package uk.co.spudsoft.params4j;

import java.io.IOException;

/**
 * Interface defining a mechanism for finding parameters.
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
 */
public interface ParameterGatherer<P> {
  
  /**
   * Collate the parameters that this gatherer understands and combine them with the base object, then return the updated (or new) base object.
   * 
   * All known implementations modify the instance of P that is passed in, but callers should not rely on that behaviour.
   * 
   * @param spi Instance of Params4JSpi for accessing common functionality.
   * @param base Initial value of the parameters that this method will alter.
   * @return Either a new instance of P or the same instance that was passed in.
   * @throws IOException if something goes wrong.
   */
  P gatherParameters(Params4JSpi spi, P base) throws IOException ;
  
}
