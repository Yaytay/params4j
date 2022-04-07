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
   * <P>
   * The supplier should return a newly created instance of the parameters object.
   * The instance can have any default values already set.
   * <P>
   * It is permissible to reuse an existing instance, but Params4J will not make any effort to "clear it out", only values set by the gatherers will be altered.
   * This would mean that if a single instance is used and a configuration option is removed that value would retain its previous value.
   * 
   * @param supplier Supplier of the parameters object that Params4J will set.
   * @return this.
   */
  Params4JFactory<P> withConstructor(Supplier<P> supplier);
  
  /**
   * Base method for configuring Params4J to use a gatherer.
   * <P>
   * Gatherers will be processed in the order they are added to the Params4J object.
   * 
   * @param gatherer The gatherer to use.
   * @return this.
   */
  Params4JFactory<P> withGatherer(ParameterGatherer<P> gatherer);
  
  /**
   * Set a custom deserialization problem handler to be used by Jackson.
   * <P>
   * Calling this method is optional, the default implementation ({@link uk.co.spudsoft.params4j.impl.DefaultParametersErrorHandler DefaultParametersErrorHandler}) simply logs any errors (at WARN level) and continues;
   * A single problem handler is used for all gatherers.
   * <P>
   * Note that the problem handler is only used when constructing the default mappers, if the default mappers are not used then
   * the object passed in here will not be used.
   * 
   * @param problemHandler The problem handler to use.
   * @return this.
   */
  Params4JFactory<P> withProblemHandler(DeserializationProblemHandler problemHandler);
  
  /**
   * Set a custom Jackson properties mapper.
   * <P>
   * Calling this method is optional, if not called the default props mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createPropsMapper() Params4JImpl.createPropsMapper}.
   * Note that the props mapper is used when processing system properties and environment variables as well as properties files.
   * 
   * @param propsMapper The props mapper to use.
   * @return this.
   */
  Params4JFactory<P> withPropsMapper(JavaPropsMapper propsMapper);
  
  /**
   * Set a custom Jackson object mapper.
   * <P>
   * Calling this method is optional, if not called the default object mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createJsonMapper(java.util.List) Params4JImpl.createJsonMapper}.
   * 
   * @param jsonMapper The object mapper to use.
   * @return this.
   */
  Params4JFactory<P> withJsonMapper(ObjectMapper jsonMapper);
  
  /**
   * Set a custom Jackson module to use with the default object mapper.
   * <P>
   * Calling this method is optional, if not called the default object mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createJsonMapper(java.util.List) Params4JImpl#createJsonMapper} with no custom modules.
   * Modules will be added in the order that this method is called.
   * <P>
   * If a custom object mapper is used (via {@link #withJsonMapper(com.fasterxml.jackson.databind.ObjectMapper)}) then this method will not achieve anything.
   * 
   * @param module The jackson module to add to the default object mapper.
   * @return this.
   */
  Params4JFactory<P> withCustomJsonModule(com.fasterxml.jackson.databind.Module module);

  /**
   * Set a custom Jackson YAML mapper.
   * <P>
   * Calling this method is optional, if not called the default object mapper is created by {@link uk.co.spudsoft.params4j.impl.Params4JImpl#createYamlMapper() Params4JImpl.createYamlMapper}.
   * <P>
   * Note that a YAML mapper is just an instance of ObjectMapper with a YAMLFactory.
   * If an ordinary JSON mapper is passed in to this method it will not parse YAML files correctly.
   * 
   * @param yamlMapper The object mapper to use for processing YAML files.
   * @return this.
   */
  Params4JFactory<P> withYamlMapper(ObjectMapper yamlMapper);
  
  /**
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.ResourceGatherer ResourceGatherer} to the Params4J instance.
   * <P>
   * This gatherer does no scanning, it loads the single resource file which must be of the specified type.
   * <P>
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
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.DirGatherer DirGatherer} to the Params4J instance.
   * <P>
   * The {@link uk.co.spudsoft.params4j.impl.DirGatherer DirGatherer} scans a directory and gathers values from any files of the specified types.
   * It also configures a monitor for any changes to files in that directory.
   * It does not scan sub directories.
   * <P>
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
   * Convenience method for adding an {@link uk.co.spudsoft.params4j.impl.EnvironmentVariablesGatherer EnvironmentVariablesGatherer} to the Params4J instance.
   * <P>
   * Note that on Linux (which include docker on Windows) shells usually have constraints on environment variable names.
   * Specifically names can only consist of letters, digits and the underscore.
   * Furthermore lowercase variable names should be used for local variables, not one that are passed between processes.
   * <P>
   * Given that params4j expects keys like "prefix.list[1]" this can be a problem.
   * Typically docker and kubernetes do not enforce constraints on the environment variable keys, but if your container
   * uses a shell script to bootstrap your service then it probably is affected.
   * <P>
   * This class takes two steps to workaround these limitations:
   * 1. Any underscores in environment variable key names are replaced with a dot (".").
   * 2. Optionally, all keys can be converted to lower case (using the default locale).
   * <P>
   * If this not enough to enable an environment variable to be used for a given value then another approach will have to be found.
   * In particular list values ("list[1]") are not going to work as environment variables if they have to processed by a shell.
   * <P>
   * To avoid trying to process all environment variables as parameters the namePrefix can be used to filter environment variables.
   * If the namePrefix is supplied (is not null or empty) only environment variables that begin with the prefix will be considered.
   * The prefix will be removed before mapping variables to the parameters object.
   * The prefix is always considered to have a trailing dot ("."), this does not have to be specified in the namePrefix argument.
   * <P>
   * 
   * Equivalent to
   * <pre>
   * return withGatherer(new EnvironmentVariablesGatherer&lt;>(namePrefix, toLowerCase));
   * </pre>
   * 
   * @param namePrefix The prefix that can be used to filter environment variables.
   * @param toLowerCase If true all environment variable names will be converted to lower case before passing to Jackson.
   * @return this.
   */
  Params4JFactory<P> withEnvironmentVariablesGatherer(String namePrefix, boolean toLowerCase);
  

  /**
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.SystemPropertiesGatherer SystemPropertiesGatherer} to the Params4J instance.
   * <P>
   * To avoid trying to process all environment variables as parameters the namePrefix can be used to filter environment variables.
   * If the namePrefix is supplied (is not null or empty) only environment variables that begin with the prefix will be considered.
   * The prefix will be removed before mapping variables to the parameters object.
   * The prefix is always considered to have a trailing dot ("."), this does not have to be specified in the namePrefix argument.
   * <P>
   * 
   * Equivalent to
   * <pre>
   * return withGatherer(new SystemPropertiesGatherer&lt;>(namePrefix));
   * </pre>
   * 
   * @param namePrefix The prefix that can be used to filter environment variables.
   * @return this.
   */
  Params4JFactory<P> withSystemPropertiesGatherer(String namePrefix);
  

  /**
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.CommandLineArgumentsGatherer CommandLineArgumentsGatherer} to the Params4J instance.
   * <P>
   * Arguments are parsed by whatever shell is in use and should be escaped appropriately.
   * Each string in the args[] array should be a single key/value pair separated by "=".
   * Any arguments that do not contain the equals character will be treated as being "=true";
   * <P>
   * To avoid trying to process all arguments as parameters the namePrefix can be used to filter them.
   * If the namePrefix is supplied (is not null or empty) only command line arguments that begin with the prefix will be considered.
   * The prefix will be removed before mapping variables to the parameters object.
   * The prefix is always considered to have a trailing dot ("."), this does not have to be specified in the namePrefix argument.
   * Use of the namePrefix is less useful on command line arguments than on environment variables or system properties, but it sometimes makes sense to include it for consistency. 
   * <P>
   * Equivalent to
   * <pre>
   * return withGatherer(new CommandLineArgumentsGatherer&lt;>(args, namePrefix));
   * </pre>
   * 
   * @param args The arguments passed in to the main() method.
   * @param namePrefix The prefix that can be used to filter environment variables.
   * @return this.
   */
  Params4JFactory<P> withCommandLineArgumentsGatherer(String args[], String namePrefix);
  
  /**
   * Convenience method for adding a {@link uk.co.spudsoft.params4j.impl.SecretsGatherer SecretsGatherer} to the Params4J instance.
   * <P>
   * The {@link uk.co.spudsoft.params4j.impl.SecretsGatherer SecretsGatherer} scans a directory hierarchy and adds any files found to the parameters.
   * Hidden files and directories (those beginning with a '.') are skipped.
   * <P>
   * Note that each entire file is read in as a single parameter value, the files are not parsed.
   * <P>
   * This is aimed at allowing the injection of Kubernetes secrets into the parameters object (hence the name) but can used for other purposes.
   * <P>
   * The various limit parameters are provided to restrict the time and memory wasted by a bad configuration.
   * In a standard situation the limits should be set to values that are larger than any expected values.
   * <P>
   * It also configures a monitor for any changes to files in that directory.
   * <P>
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
   * 
   * @return a new instance of Params4J&lt;P>
   */
  Params4J<P> create();
  
}
