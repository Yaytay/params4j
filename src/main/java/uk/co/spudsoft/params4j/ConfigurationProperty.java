/*
 * Copyright (C) 2022 jtalbut
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.spudsoft.params4j;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Locale;
import java.util.Objects;

/**
 * Documentation for a single property found by reflecting the Parameters class.
 * @author jtalbut
 */
@JsonDeserialize(builder = ConfigurationProperty.Builder.class)
public class ConfigurationProperty {
  
  /**
   * The Java class representing the property.
   */
  public final Class<?> type;
  /**
   * The name of the property.
   * If the name contains [&lt;n>] it represents a list of items and may be repeated as many times as desired with &lt;n> being replaced by a monotonically increasing integer starting at zero.
   * If the name contains &lt;xxx> it represents a map of items and may be repeated as many times as desired with &lt;xxx> being replaced by a string.
   */
  public final String name;
  /**
   * If true this property can be represented as an environment variable.
   * 
   * Lists cannot be represented as environment variables because the '[' and ']' characters are not permitted in environment variables in a Linux shell.
   * It may still be possible to specify list properties as an environment variable as long as no Unix-like shell is required to process it.
   * 
   * Terminal classes that cannot be represented as a single string cannot be represented as a single environment variable.
   */
  public final boolean canBeEnvVar;
  /**
   * If true this property cannot be specified as is, but requires additional child values to be specified.
   * The child values are not documented here, the {@link Comment} annotation should be used for undocumented classes to provide instructions on where to find their details.
   */
  public final boolean undocumented;
  /**
   * The calculated documentation for this property.
   * The calculated documentation for a field is found (in order) from:
   * 1. The {@link Comment} annotation.
   * 2. Javadoc comments on the setter.
   * 3. Javadoc comments on the field with the same name as the property.
   * When handling nested properties the documentation of the full hierarchy of fields is concatentated together.
   */
  public final String comment;
  /**
   * The default value for this property.
   * This will only be found if the full path to the field is instantiated by default constructors.
   */
  public final String defaultValue;

  /**
   * Builder.
   */
  @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
  public static class Builder {

    private Class<?> type;
    private String name;
    private boolean canBeEnvVar;
    private boolean undocumented;
    private String comment;
    private String defaultValue;
  
    private Builder() {
    }

    /**
     * Set the Java class representing the property.
     * @param value the Java class representing the property.
     * @return this
     */
    public Builder type(final Class<?> value) {
      this.type = value;
      return this;
    }

    /**
     * Set the name of the property.
     * @param value the name of the property.
     * @return this
     */
    public Builder name(final String value) {
      this.name = value;
      return this;
    }

    /**
     * Set whether the property can be represented by an env var. 
     * @param value whether the property can be represented by an env var.
     * @return this
     */
    public Builder canBeEnvVar(final boolean value) {
      this.canBeEnvVar = value;
      return this;
    }

    /**
     * Set whether the property represent an undocumented class of properties.
     * If the property does represent an undocumented class of properties the comment should link to the documentation for that class.
     * @param value whether the property represent an undocumented class of properties.
     * @return this
     */
    public Builder undocumented(final boolean value) {
      this.undocumented = value;
      return this;
    }

    /**
     * Set the calculated documentation for this property.
     * @param value calculated documentation for this property.
     * @return this
     */
    public Builder comment(final String value) {
      this.comment = value;
      return this;
    }

    /**
     * Set the default value for this property.
     * @param value default value for this property.
     * @return this
     */
    public Builder defaultValue(final String value) {
      this.defaultValue = value;
      return this;
    }

    /**
     * Construct a new {@link ConfigurationProperty} instance.
     * @return a new {@link ConfigurationProperty} instance.
     */
    public ConfigurationProperty build() {
      return new uk.co.spudsoft.params4j.ConfigurationProperty(type, name, canBeEnvVar, undocumented, comment, defaultValue);
    }
  }

  /**
   * Construct a {@link Builder} instance.
   * @return a newly constructed {@link Builder} instance.
   */
  public static ConfigurationProperty.Builder builder() {
    return new ConfigurationProperty.Builder();
  }

  private ConfigurationProperty(final Class<?> type, final String name, final boolean canBeEnvVar, final boolean undocumented, final String comment, final String defaultValue) {
    this.type = type;
    this.name = name;
    this.canBeEnvVar = canBeEnvVar;
    this.undocumented = undocumented;
    this.comment = comment;
    this.defaultValue = defaultValue;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 71 * hash + Objects.hashCode(this.type);
    hash = 71 * hash + Objects.hashCode(this.name);
    hash = 71 * hash + (this.canBeEnvVar ? 1 : 0);
    hash = 71 * hash + (this.undocumented ? 1 : 0);
    hash = 71 * hash + Objects.hashCode(this.comment);
    hash = 71 * hash + Objects.hashCode(this.defaultValue);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ConfigurationProperty other = (ConfigurationProperty) obj;
    if (this.canBeEnvVar != other.canBeEnvVar) {
      return false;
    }
    if (this.undocumented != other.undocumented) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.comment, other.comment)) {
      return false;
    }
    if (!Objects.equals(this.defaultValue, other.defaultValue)) {
      return false;
    }
    return Objects.equals(this.type, other.type);
  }

  /**
   * Append an output suitable for use in response to a '--help' command.
   * 
   * @param builder The StringBuilder that will have the details appeneded.
   * @param maxNameLen The length of the longest name in the set of ConfigurationProperties being processed.
   * This must be calculated by the called before calling appendUsages for correct alignment.
   * Given a List&lt;ConfigurationProperty> this can be calculated with:
   * <pre>
   * int maxNameLen = docs.stream().map(p -> p.name.length()).max(Integer::compare).get();
   * </pre>
   * @param lineTerminator The string to put at the end of each line, probably "\n", but could be "\n\n" or "\r\n" if you prefer.
   * 
   */
  public void appendUsage(StringBuilder builder, int maxNameLen, String lineTerminator) {
    
    maxNameLen += 6;
    
    builder.append("    ")
            .append(name);
    if (undocumented) {
      builder.append(".<***>");
    }
    builder.append(" ".repeat(maxNameLen + 1 - name.length() - (undocumented ? 6 : 0)))
            .append(comment)            
            .append('\n');

    String typeName = type.getSimpleName();
    builder.append("        ")
            .append(typeName);

    if (undocumented) {
      builder.append(" ".repeat(typeName.length() + 10 > maxNameLen ? 1 : maxNameLen - typeName.length() - 3))
              .append("This parameter is undocumented, consult the source for ")
              .append(type)
              .append(" for details of the values for <***>");
      
    } else if (defaultValue != null) {
      builder.append(" ".repeat(typeName.length() + 10 > maxNameLen ? 1 : maxNameLen - typeName.length() - 3))
              .append("default: ")
              .append(defaultValue);
    }
    builder.append(lineTerminator);
  }

  /**
   * Append environment variable that can be used for this value.Will only change the builder if canBeEnvVar is true.
   * 
   * @param builder The StringBuilder that will have the details appeneded.
   * @param maxNameLen The length of the longest name in the set of ConfigurationProperties being processed.
   * This must be calculated by the called before calling appendUsages for correct alignment.
   * Given a List&lt;ConfigurationProperty> this can be calculated with:
   * <pre>
   * int maxNameLen = docs.stream().map(p -> p.name.length()).max(Integer::compare).get();
   * </pre>
   * @param prefixStrip String to be removed from the beginning of each name (typically '--')
   * @param prefixAdd String to be add to the beginning of each name (the namePrefix passed in to withEnvironmentVariablesGatherer)
   * @param lineTerminator The string to put at the end of each line, probably "\n", but could be "\n\n" or "\r\n" if you prefer.
   * 
   */
  public void appendEnv(StringBuilder builder, int maxNameLen, String prefixStrip, String prefixAdd, String lineTerminator) {
    if (canBeEnvVar) {
      if (prefixStrip == null) {
        prefixStrip = "";
      }
      if (prefixAdd == null) {
        prefixAdd = "";
      }
      
      maxNameLen = maxNameLen - prefixStrip.length() + prefixAdd.length() + 7;
      
      String envVarName = name;
      if (envVarName.startsWith(prefixStrip)) {
        envVarName = envVarName.substring(prefixStrip.length());
      }
      envVarName = envVarName.replaceAll("\\.", "_");
      if (prefixAdd.length() > 0) {
        envVarName = prefixAdd + "_" + envVarName;
      }
      envVarName = envVarName.toUpperCase(Locale.ROOT);
      
      builder.append("    ")
              .append(envVarName);
      if (undocumented) {
        builder.append("_<***>");
      }
      builder.append(" ".repeat(maxNameLen + 1 - envVarName.length() - (undocumented ? 6 : 0)))
              .append(comment)
              .append('\n');

      String typeName = type.getSimpleName();
      builder.append("        ")
              .append(typeName);
            
      if (undocumented) {
        builder.append(" ".repeat(typeName.length() + 10 > maxNameLen ? 1 : maxNameLen - typeName.length() - 3))
                .append("This parameter is undocumented, consult the source for ")
                .append(type)
                .append(" for details of the values for <***>");

      } else if (defaultValue != null) {
        builder.append(" ".repeat(typeName.length() + 10 > maxNameLen ? 1 : maxNameLen - typeName.length() - 3))
                .append("default: ")
                .append(defaultValue);
      }
      
      builder.append(lineTerminator);
    }
  }
  
}
