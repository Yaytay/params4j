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

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import uk.co.spudsoft.params4j.Comment;
import uk.co.spudsoft.params4j.JavadocCapture;
import uk.co.spudsoft.params4j.impl.HtmlAnchorElement;

/**
 * Test and demonstration class for the capabilities of Params4J documentation.
 * 
 * In case it is not obvious the documentation on the members of this class are total nonsense.
 * 
 * @author jtalbut
 */
@JavadocCapture
public class Parameters {

  // Demonstrate an alien member that cannot be set using a single string value
  // and that we do not want to document all the internal members of.
  private HtmlAnchorElement alien;
  // Demonstrate an alien member that has a specific custom comment.
  private HtmlAnchorElement documentedAlien;
  private boolean exitOnRun;
  // File has setReadable(boolean) which should not be documented
  private File baseConfigPath;
  // DataSource has nested setters, which should be documented.
  private DataSource auditDataSource = DataSource.builder().build();
  // LocalDateTime is terminal, but should have a specific default value
  private LocalDateTime when = LocalDateTime.of(1971, 06, 05, 14, 0);
  // A key/value pair parameter
  private Map<String, String> translations;
  // A duration
  private Duration howLong = Duration.parse("PT15M");
  
  // An inner class
  private Map.Entry<String, Integer> entry;
  
  // An undocumented value
  private Integer undocumentedValue;
  
  /**
   * The login for a system.
   */
  private Map<String, Credentials> logins;
  // An array parameter
  private List<String> names;

  /**
   * alien value that cannot be documented further in this codebase
   * @return an alien value that cannot be documented further in this codebase
   */
  public HtmlAnchorElement getAlien() {
    return alien;
  }

  /**
   * alien value that cannot be documented further in this codebase
   * @param alien alien value that cannot be documented further in this codebase
   */
  public void setAlien(HtmlAnchorElement alien) {
    this.alien = alien;
  }

  /**
   * alien value that cannot be documented further in this codebase
   * @return an alien value that cannot be documented further in this codebase
   */
  public HtmlAnchorElement getDocumentedAlien() {
    return documentedAlien;
  }

  /**
   * alien value that cannot be documented further in this codebase.
   * <p>
   * Note that the URL in the comment is wrong, can't use an actual HtmlAnchorElement because they are interfaces.
   * So this is really just a {@link uk.co.spudsoft.params4j.impl.HtmlAnchorElement} not an
   * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.xml.dom/org/w3c/dom/html/HTMLAnchorElement.html">HTMLAnchorElement</a>.
   * 
   * @param documentedAlien alien value that cannot be documented further in this codebase
   * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.xml.dom/org/w3c/dom/html/HTMLAnchorElement.html">HTMLAnchorElement.html</a>
   */
  @Comment("configure the alien properties as documented at https://docs.oracle.com/en/java/javase/17/docs/api/jdk.xml.dom/org/w3c/dom/html/HTMLAnchorElement.html")
  public void setDocumentedAlien(HtmlAnchorElement documentedAlien) {
    this.documentedAlien = documentedAlien;
  }
  
  /**
   * if true the process will end rather than waiting for requests
   * This is expected to be useful for things such as JIT compilers or CDS preparation.
   * @return the exitOnRun value.
   */
  public boolean isExitOnRun() {
    return exitOnRun;
  }

  /**
   * if true the process will end rather than waiting for requests.
   * <p>
   * This is expected to be useful for things such as JIT compilers or CDS preparation.
   * <p>
   * Possible values:
   * <uL>
   * <li>true - the process will exit before starting the daemon.
   * <li>false - the process will run as a daemon.
   * </Ul>
   * The method 
   * {@link java.lang.Boolean#parseBoolean(java.lang.String)} 
   * really isn't relevant 
   * (nor are 
   * {@link Map<String, Credentials>#forEach(BiConsumer)}
   * or
   * {@link java.lang.String#String(byte[], int, int, java.nio.charset.Charset)}
   * ).
   * 
   * You could look at {@link commentcap.Credentials#getPassword()} but it wouldn't really help much.
   * 
   * @param exitOnRun the exitOnRun value.
   */
  public void setExitOnRun(boolean exitOnRun) {
    this.exitOnRun = exitOnRun;
  }

  /**
   * The path to the root of the configuration files.
   * @return the path to the root of the configuration files.
   */
  public File getBaseConfigPath() {
    return baseConfigPath;
  }

  /**
   * The path to the root of the configuration files.
   * <p>
   * The value is a {@link java.io.File}.
   * <p>
   * This is an irrelevant ordered list:
   * <OL>
   * <lI>First
   * <Li>Second
   * </oL>
   * 
   * @param baseConfigPath the path to the root of the configuration files.
   */
  public void setBaseConfigPath(File baseConfigPath) {
    this.baseConfigPath = baseConfigPath;
  }

  /**
   * The datasource used for recording activity.
   * @return The datasource used for recording activity.
   */
  public DataSource getAuditDataSource() {
    return auditDataSource;
  }

  /**
   * The datasource used for recording activity.
   * @param auditDataSource The datasource used for recording activity.
   */
  public void setAuditDataSource(DataSource auditDataSource) {
    this.auditDataSource = auditDataSource;
  }

  /**
   * when it happened.
   * @return when it happened.
   */
  public LocalDateTime getWhen() {
    return when;
  }

  /**
   * when it happened.
   * @param when when it happened.
   */
  public void setWhen(LocalDateTime when) {
    this.when = when;
  }

  public Map<String, String> getTranslations() {
    return translations;
  }

  /**
   * translations from one word to another.
   * @param translations 
   */
  public void setTranslations(Map<String, String> translations) {
    this.translations = translations;
  }

  public List<String> getNames() {
    return names;
  }

  /**
   * names that are recognised by the process.
   * @param names 
   */
  public void setNames(List<String> names) {
    this.names = names;
  }

  public Map<String, Credentials> getLogins() {
    return logins;
  }

  /**
   * login for a system.
   * <p>
   * A {@link Map<String, Credentials>} of system names to credentials.
   * <p>
   * See: {@link commentcap.Credentials}
   * @param logins 
   */
  public void setLogins(Map<String, Credentials> logins) {
    this.logins = logins;
  }

  /**
   * how long something should wait.
   * @return the value for how long something should wait.
   */
  public Duration getHowLong() {
    return howLong;
  }

  /**
   * how long something should wait.
   * @param howLong the value for how long something should wait.
   */
  public void setHowLong(Duration howLong) {
    this.howLong = howLong;
  }

  public void setUndocumentedValue(Integer value) {
    undocumentedValue = value;
  }

  /**
   * Set an inner class.
   * @param entry an inner class object.
   */
  public void setEntry(Map.Entry<String, Integer> entry) {
    this.entry = entry;
  }

}
