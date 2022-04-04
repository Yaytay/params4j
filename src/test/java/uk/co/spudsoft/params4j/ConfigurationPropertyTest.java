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


import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author jtalbut
 */
public class ConfigurationPropertyTest {
  
  @Test
  public void testHashCode() {
    ConfigurationProperty cp1 = ConfigurationProperty.builder().name("1").build();
    ConfigurationProperty cp2 = ConfigurationProperty.builder().name("2").build();
    assertThat(cp1.hashCode(), not(equalTo(cp2.hashCode())));
  }

  @Test
  public void testEquals() {
    ConfigurationProperty cp1 = ConfigurationProperty.builder().name("1").canBeEnvVar(true).comment("comment").defaultValue("value").undocumented(true).build();
    assertThat(cp1, equalTo(cp1));
    assertThat(cp1, not(equalTo(null)));
    assertThat(cp1, not(equalTo("fred")));
    ConfigurationProperty cp2 = ConfigurationProperty.builder().name("1").build();
    assertThat(cp1, not(equalTo(cp2)));
    cp2 = ConfigurationProperty.builder().name("1").canBeEnvVar(true).build();
    assertThat(cp1, not(equalTo(cp2)));
    cp2 = ConfigurationProperty.builder().name("1").canBeEnvVar(true).comment("comment").build();
    assertThat(cp1, not(equalTo(cp2)));
    cp2 = ConfigurationProperty.builder().name("1").canBeEnvVar(true).comment("comment").defaultValue("value").build();
    assertThat(cp1, not(equalTo(cp2)));
    cp2 = ConfigurationProperty.builder().name("1").canBeEnvVar(true).comment("comment").defaultValue("value").undocumented(true).build();
    assertThat(cp1, equalTo(cp2));
    cp2 = ConfigurationProperty.builder().name("2").canBeEnvVar(true).comment("comment").defaultValue("value").undocumented(true).build();
    assertThat(cp1, not(equalTo(cp2)));
    cp2 = ConfigurationProperty.builder().name("1").canBeEnvVar(true).comment("comment2").defaultValue("value").undocumented(true).build();
    assertThat(cp1, not(equalTo(cp2)));
    cp2 = ConfigurationProperty.builder().name("1").canBeEnvVar(true).comment("comment").defaultValue("value2").undocumented(true).build();
    assertThat(cp1, not(equalTo(cp2)));
  }
  
}
