/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package uk.co.spudsoft.params4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import java.util.function.Supplier;

/**
 *
 * @author jtalbut
 * @param <P> The type of the parameters object.
 */
public interface Params4JFactory<P> {
  
  Params4JFactory<P> withConstructor(Supplier<P> supplier);
  
  Params4JFactory<P> withGatherer(ParameterGatherer<P> gatherer);
  
  Params4JFactory<P> withProblemHandler(DeserializationProblemHandler problemHandler);
  Params4JFactory<P> withPropsMapper(JavaPropsMapper propsMapper);
  Params4JFactory<P> withJsonMapper(ObjectMapper jsonMapper);
  Params4JFactory<P> withCustomJsonModule(com.fasterxml.jackson.databind.Module module);
  Params4JFactory<P> withYamlMapper(ObjectMapper yamlMapper);
  
  Params4JFactory<P> withResourceGatherer(String resource, FileType fileType);
  
  
  Params4J<P> create();
  
}
