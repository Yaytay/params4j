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

/**
 * Documentation for a single property found by reflecting the Parameters class.
 * @author jtalbut
 */
public class ConfigurationProperty {
  
  public final Class<?> type;
  public final String name;
  public final boolean canBeEnvVar;
  public final boolean undocumented;
  public final String comment;
  public final String defaultValue;

  public static class Builder {

    private Class<?> type;
    private String name;
    private boolean canBeEnvVar;
    private boolean undocumented;
    private String comment;
    private String defaultValue;

    private Builder() {
    }

    public Builder type(final Class<?> value) {
      this.type = value;
      return this;
    }

    public Builder name(final String value) {
      this.name = value;
      return this;
    }

    public Builder canBeEnvVar(final boolean value) {
      this.canBeEnvVar = value;
      return this;
    }

    public Builder undocumented(final boolean value) {
      this.undocumented = value;
      return this;
    }

    public Builder comment(final String value) {
      this.comment = value;
      return this;
    }

    public Builder defaultValue(final String value) {
      this.defaultValue = value;
      return this;
    }

    public ConfigurationProperty build() {
      return new uk.co.spudsoft.params4j.ConfigurationProperty(type, name, canBeEnvVar, undocumented, comment, defaultValue);
    }
  }

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

  
}
