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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Demonstrate a Builder based class.
 * Comments have to be on the fields (because there is no setter).
 * @author jtalbut
 */
@JsonDeserialize(builder = DataSource.Builder.class)
public class DataSource {

  /**
   * The URL to use for accessing the datasource.
   **/
  private final String url;
  
  /**
   * The database schema to use when accessing the datasource.
   **/
  private final String schema;
  
  /**
   * The credentials to use for standard actions (DML).
   **/
  private final Credentials user;
  
  /**
   * The credentials to use for preparing the database (DDL).
   * 
   * <p>
   * This could be used in some &quot;Java&quot; like this:<br>
   * <pre data-lang="java">
   * &#64;Test
   * public void testGetAdminUser() {
   *   DataSourceConfig ds = new DataSourceConfig().setAdminUser(new Credentials().setUsername("admin"));
   *   assertThat(ds.getAdminUser().getUsername(), equalTo("admin"));
   * }
   * </pre>
   * or just by calling {@code ds.getAdminUser()}.
   * <p>
   * Note that <em>entity</em> references work in the &lt;pre&gt; as demonstrated by the &quot;&#64;Test&quot;.
   * Inside the &lt;pre&gt; only a limited set of named entity references work, but AsciiDoctor will work with any outside of the &lt;pre&gt; &lpar;see: &Hat;&verbar;&copy;&rpar;.
   * <p>
   * If you want to know more about formatting code in Javadocs please see
   * <a href="https://reflectoring.io/howto-format-code-snippets-in-javadoc/">howto-format-code-snippets-in-javadoc</a>.
   * <p>
   * Note that the above code block is declared with the data-lang attribute on the 'pre' element:
   * <pre data-lang="html"><code>
   * &lt;pre data-lang="java">&lt;code>
   * public void testGetAdminUser() {
   *   DataSourceConfig ds = new DataSourceConfig().setAdminUser(new Credentials().setUsername("admin"));
   *   assertThat(ds.getAdminUser().getUsername(), equalTo("admin"));
   * }
   * &lt;/code>&lt;/pre>
   * </code></pre>
   * 
   * 
   **/
  private final Credentials adminUser;
  
  /**
   * The maximum size of the connection pool.
   **/
  private final int maxPoolSize;

  /**
   * The URL to use for accessing the datasource.
   * @return URL to use for accessing the datasource.
   */
  public String getUrl() {
    return url;
  }

  /**
   * The database schema to use when accessing the datasource.
   * @return database schema to use when accessing the datasource.
   */
  public String getSchema() {
    return schema;
  }

  /**
   * The credentials to use for standard actions (DML).
   * @return The credentials to use for standard actions (DML).
   */
  public Credentials getUser() {
    return user;
  }

  /**
   * The credentials to use for preparing the database (DDL).
   * @return The credentials to use for preparing the database (DDL).
   */
  public Credentials getAdminUser() {
    return adminUser;
  }

  /**
   * The maximum size of the connection pool.
   * @return The maximum size of the connection pool.
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
  public static class Builder {

    private String url;
    private String schema;
    private Credentials user;
    private Credentials adminUser;
    private int maxPoolSize = 10;

    private Builder() {
    }

    public Builder url(final String value) {
      this.url = value;
      return this;
    }

    public Builder schema(final String value) {
      this.schema = value;
      return this;
    }

    public Builder user(final Credentials value) {
      this.user = value;
      return this;
    }

    public Builder adminUser(final Credentials value) {
      this.adminUser = value;
      return this;
    }

    public Builder maxPoolSize(final int value) {
      this.maxPoolSize = value;
      return this;
    }

    public DataSource build() {
      return new commentcap.DataSource(url, schema, user, adminUser, maxPoolSize);
    }
  }

  public static DataSource.Builder builder() {
    return new DataSource.Builder();
  }

  private DataSource(final String url, final String schema, final Credentials user, final Credentials adminUser, final int maxPoolSize) {
    this.url = url;
    this.schema = schema;
    this.user = user;
    this.adminUser = adminUser;
    this.maxPoolSize = maxPoolSize;
  }
  
}
