/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package uk.co.spudsoft.params4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import java.io.File;
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
  
  /**
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.ResourceGatherer} to the Params4J instance.
   *
   * This gatherer does no scanning, it loads the single resource file which must be of the specified type.
   * 
   * Equivalent to
   * <pre>
   * return withGatherer(new ResourceGatherer&lt;>(resource, fileType));
   * </pre>
   * 
   * @param resource The resource to be gathered.
   * @param fileType The type of file pointed to by the resource.
   * @return this.
   */
  Params4JFactory<P> withResourceGatherer(String resource, FileType fileType);

  /**
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.DirGatherer} to the Params4J instance.
   *
   * The {@link uk.co.spudsoft.params4j.impl.DirGatherer} scans a directory and gathers values from any files of the specified types.
   * It also configures a monitor for any changes to files in that directory.
   * It does not scan sub directories.
   * 
   * Equivalent to
   * <pre>
   * return withGatherer(new DirGatherer$lt;>(dir, fileTypes));
   * </pre>
   * 
   * @param dir The directory containing files to be gathered.
   * @param fileTypes The types of files to be checked in the dir.
   * @return this.
   */
  Params4JFactory<P> withDirGatherer(File dir, FileType... fileTypes);
  
  
  Params4J<P> create();
  
}
