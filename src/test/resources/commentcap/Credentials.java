/*
 * Copyright (C) 2025 jtalbut
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
package commentcap;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.regex.Pattern;
import uk.co.spudsoft.params4j.SecretsSerializer;

/**
 *
 * @author jtalbut
 */
public class Credentials {
  
  /**
   * The username.
   */
  protected String username;
  /**
   * The password (field; setter has no Javadoc).
   */
  protected String password;

  /**
   * A property that can be set, but not got
   */
  protected String group;
  
  // This should not be included in asciidocs.
  private static final Pattern VALID_NAME = Pattern.compile("^[-a-zA-Z0-9!#$%&'*+.^_`|~]+$");
    
  
  /**
   * Constructor.
   */
  public Credentials() {
  }

  /**
   * Constructor.
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
    if (username == null || username.isEmpty()) {
      sb.append("username=").append(username);
    }
    sb.append("}");
    return sb.toString();
  }
  
  /**
   * The username (getter).
   * @return The username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * The username (setter).
   * @param username the username.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  @JsonSerialize(using = SecretsSerializer.class)
  public String getPassword() {
    return password;
  }

  @JsonSerialize(using = SecretsSerializer.class)
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * The group (setter).
   * @param group the group.
   */
  public void setGroup(String group) {
    this.group = group;
  }
  
}
