/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import uk.co.spudsoft.params4j.FileType;
import uk.co.spudsoft.params4j.ParameterGatherer;
import uk.co.spudsoft.params4j.Params4J;
import uk.co.spudsoft.params4j.Params4JFactory;

/**
 *
 * Implementation of the {@link uk.co.spudsoft.params4j.Params4JFactory}.
 * 
 * @author jtalbut
 * 
 * @param <P> The type of the parameters object.
*/
public class Params4JFactoryImpl<P> implements Params4JFactory<P> {
  
  private Supplier<P> constructor;
  private List<ParameterGatherer<P>> gatherers = new ArrayList<>();
  private DeserializationProblemHandler problemHandler;
  private JavaPropsMapper propsMapper;
  private ObjectMapper jsonMapper;
  private List<com.fasterxml.jackson.databind.Module> customJsonModules = new ArrayList<>();
  private List<MixIn> mixIns = new ArrayList<>();
  private ObjectMapper yamlMapper;
  
  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withConstructor(Supplier<P> supplier) {
    this.constructor = supplier;
    return this;
  }
  
  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withGatherer(ParameterGatherer<P> gatherer) {
    gatherers.add(gatherer);
    return this;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withProblemHandler(DeserializationProblemHandler problemHandler) {
    this.problemHandler = problemHandler;
    return this;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withPropsMapper(JavaPropsMapper propsMapper) {
    this.propsMapper = propsMapper;
    return this;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withJsonMapper(ObjectMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
    return this;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withCustomJsonModule(Module module) {
    this.customJsonModules.add(module);
    return this;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withMixIn(Class<?> target, Class<?> mixinSource) {
    this.mixIns.add(new MixIn(target, mixinSource));
    return this;
  }
  
  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withYamlMapper(ObjectMapper yamlMapper) {
    this.yamlMapper = yamlMapper;
    return this;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withResourceGatherer(String resource, FileType fileType) {
    return withGatherer(new ResourceGatherer<>(resource, fileType));
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withDirGatherer(File dir, FileType... fileTypes) {
    return withGatherer(new DirGatherer<>(dir, fileTypes));
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withEnvironmentVariablesGatherer(String namePrefix, boolean toLowerCase) {
    return withGatherer(new EnvironmentVariablesGatherer<>(namePrefix, toLowerCase));
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withSystemPropertiesGatherer(String namePrefix) {
    return withGatherer(new SystemPropertiesGatherer<>(namePrefix));
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withCommandLineArgumentsGatherer(String[] args, String namePrefix) {
    return withGatherer(new CommandLineArgumentsGatherer<>(args, namePrefix));
  }
  
  
  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Externable object is mutable")
  public Params4JFactory<P> withSecretsGatherer(Path root, int fileSizeLimit, int fileCountLimit, int fileDepthLimit, Charset charset) {
    return withGatherer(new SecretsGatherer<>(root, fileSizeLimit, fileCountLimit, fileDepthLimit, charset));
  }
  
  @Override
  public Params4J<P> create() {
    return new Params4JImpl<>(constructor
            , Collections.unmodifiableList(gatherers)
            , problemHandler
            , propsMapper
            , jsonMapper
            , customJsonModules
            , mixIns
            , yamlMapper
    );
  }
  
}
