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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author jtalbut
 */
public class SecretsSerializerTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public class Credentials {

    /**
     * The username.
     */
    protected String username;
    /**
     * The password.
     */
    protected String password;

    /**
     * Constructor.
     *
     * @param username The username to use, if any.
     * @param password The password to use, if any.
     */
    public Credentials(String username, String password) {
      this.username = username;
      this.password = password;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      if (username != null && !username.isEmpty()) {
        sb.append("username=").append(username);
      }
      sb.append("}");
      return sb.toString();
    }

    /**
     * Get the username to specify when connecting to the resource.
     *
     * @return the username to specify when connecting to the resource.
     */
    public String getUsername() {
      return username;
    }

    /**
     * Set the username to specify when connecting to the resource.
     *
     * @param username the username to specify when connecting to the resource.
     */
    public void setUsername(String username) {
      this.username = username;
    }

    /**
     * Get the password to specify when connecting to the resource.
     *
     * @return the password to specify when connecting to the resource.
     */
    @JsonSerialize(using = SecretsSerializer.class)
    public String getPassword() {
      return password;
    }

    /**
     * Set the password to specify when connecting to the resource.
     *
     * @param password the password to specify when connecting to the resource.
     */
    @JsonSerialize(using = SecretsSerializer.class)
    public void setPassword(String password) {
      this.password = password;
    }
  }

  @Test
  public void testSerialize() throws Exception {
    Credentials creds = new Credentials("username", "password");
    assertEquals("{\"username\":\"username\",\"password\":\"********\"}", objectMapper.writeValueAsString(creds));
    creds = new Credentials("username", "");
    assertEquals("{\"username\":\"username\",\"password\":\"\"}", objectMapper.writeValueAsString(creds));
    creds = new Credentials("username", null);
    assertEquals("{\"username\":\"username\",\"password\":null}", objectMapper.writeValueAsString(creds));
    
    // How to disable to the SecretsSerializer  
    ObjectMapper tempMapper = objectMapper.copy().setAnnotationIntrospector(SecretsSerializer.getDisabler());
    creds = new Credentials("username", "password");
    assertEquals("{\"username\":\"username\",\"password\":\"password\"}", tempMapper.writeValueAsString(creds));
    
    // And prove that it's still enabled on the primary objectMapper
    assertEquals("{\"username\":\"username\",\"password\":\"********\"}", objectMapper.writeValueAsString(creds));    
  }

}
