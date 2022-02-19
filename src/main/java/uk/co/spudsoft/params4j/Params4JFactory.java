/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package uk.co.spudsoft.params4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 *
 * @author jtalbut
 * @param <P> The type of the parameters object.
 */
public interface Params4JFactory<P> {
  
  /**
   * Configure the constructor to be used by Params4J.
   * 
   * The supplier should return a newly created instance of the parameters object.
   * The instance can have any default values already set.
   * 
   * It is permissible to reuse an existing instance, but Params4J will not make any effort to "clear it out", only values set by the gatherers will be altered.
   * This would mean that if a single instance is used and a configuration option is removed that value would retain its previous value.
   * 
   * @param supplier Supplier of the parameters object that Params4J will set.
   * @return this.
   */
  Params4JFactory<P> withConstructor(Supplier<P> supplier);
  
  /**
   * Base method for configuring Params4J to use a gatherer.
   * 
   * Gatherers will be processed in the order they are added to the Params4J object.
   * 
   * @param gatherer The gatherer to use.
   * @return this.
   */
  Params4JFactory<P> withGatherer(ParameterGatherer<P> gatherer);
  
  /**
   * Set a custom deserialization problem handler to be used by Jackson.
   * 
   * Calling this method is optional, the default implementation ({@link uk.co.spudsoft.params4j.impl.DefaultParametersErrorHandler}) simply logs any errors (at WARN level) and continues;
   * A single problem handler is used for all gatherers.
   * 
   * Note that the problem handler is only used when constructing the default mappers, if the default mappers are not used then
   * the object passed in here will not be used.
   * 
   * @param problemHandler The problem handler to use.
   * @return this.
   */
  Params4JFactory<P> withProblemHandler(DeserializationProblemHandler problemHandler);
  
  /**
   * Set a custom Jackson properties mapper.
   * 
   * Calling this method is optional, if not called the default props mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createPropsMapper()}.
   * Note that the props mapper is used when processing system properties and environment variables as well as properties files.
   * 
   * @param propsMapper The props mapper to use.
   * @return this.
   */
  Params4JFactory<P> withPropsMapper(JavaPropsMapper propsMapper);
  
  /**
   * Set a custom Jackson object mapper.
   * 
   * Calling this method is optional, if not called the default object mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createJsonMapper(java.util.List)}.
   * 
   * @param jsonMapper The object mapper to use.
   * @return this.
   */
  Params4JFactory<P> withJsonMapper(ObjectMapper jsonMapper);
  
  /**
   * Set a custom Jackson module to use with the default object mapper.
   * 
   * Calling this method is optional, if not called the default object mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createJsonMapper(java.util.List)} with no custom modules.
   * Modules will be added in the order that this method is called.
   * 
   * If a custom object mapper is used (via {@link #withJsonMapper(com.fasterxml.jackson.databind.ObjectMapper)}) then this method will not achieve anything.
   * 
   * @param module The jackson module to add to the default object mapper.
   * @return this.
   */
  Params4JFactory<P> withCustomJsonModule(com.fasterxml.jackson.databind.Module module);

  /**
   * Set a custom Jackson YAML mapper.
   * 
   * Calling this method is optional, if not called the default object mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createYamlMapper()}.
   * 
   * Note that a YAML mapper is just an instance of ObjectMapper with a YAMLFactory.
   * If an ordinary JSON mapper is passed in to this method it will not parse YAML files correctly.
   * 
   * @param yamlMapper The object mapper to use for processing YAML files.
   * @return this.
   */
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
  

  /**
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.SecretsGatherer} to the Params4J instance.
   *
   * The {@link uk.co.spudsoft.params4j.impl.SecretsGatherer} scans a directory hierarchy and adds any files found to the parameters.
   * Hidden files and directories (those beginning with a '.') are skipped.
   * 
   * Note that each entire file is read in as a single parameter value, the files are not parsed.
   * 
   * This is aimed at allowing the injection of Kubernetes secrets into the parameters object (hence the name) but can used for other purposes.
   * 
   * The various limit parameters are provided to restrict the time and memory wasted by a bad configuration.
   * In a standard situation the limits should be set to values that are larger than any expected values.
   * 
   * It also configures a monitor for any changes to files in that directory.
   * 
   * Equivalent to
   * <pre>
   * return withGatherer(new SecretsGatherer$lt;>(root, fileSizeLimit, fileCountLimit, fileDepthLimit, charset));
   * </pre>
   * 
   * @param root The base path from which to start searching, typically something like "/etc/[service name]/conf.d"
   * @param fileSizeLimit The maximum size of file to attempt to load.
   * The recommendation is to set this to slightly larger than your standard secret length.
   * @param fileCountLimit The maximum number of files to attempt to load.
   * The recommendation is to set this to one more than the number of secrets in your parameters hierarchy.
   * @param fileDepthLimit The maximum depth of hierarchy to traverse.
   * The recommendation is to set this to one more than the maximum depth of secrets in your parameters hierarchy.
   * @param charset The charset to use when reading the file.
   * @return this.
   */
  Params4JFactory<P> withSecretsGatherer(Path root, int fileSizeLimit, int fileCountLimit, int fileDepthLimit, Charset charset);
  
  /**
   * Factory method for creating a new instance of Params4J&lt;P>.
   * @return a new instance of Params4J&lt;P>
   */
  Params4J<P> create();
  
}
