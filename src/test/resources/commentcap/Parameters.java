/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package commentcap;

import java.io.File;
import java.time.LocalDateTime;
import javax.swing.Box;
import uk.co.spudsoft.params4j.Comment;
import uk.co.spudsoft.params4j.JavadocCapture;

/**
 *
 * @author jtalbut
 */
@JavadocCapture
public class Parameters {

  // Demonstrate an alien member that cannot be set using a single string value
  // and that we do not want to document all the internal members of.
  private Box alien;
  // Demonstrate an alien member that has a specific custom comment.
  private Box documentedAlien;
  private boolean exitOnRun;
  // File has setReadable(boolean) which should not be documented
  private File baseConfigPath;
  // DataSource has nested setters, which should be documented.
  private DataSource auditDataSource;
  // LocalDateTime is terminal, but should have a specific default value
  private LocalDateTime when = LocalDateTime.of(1971, 06, 05, 14, 0);

  /**
   * alien value that cannot be documented further in this codebase
   * @return an alien value that cannot be documented further in this codebase
   */
  public Box getAlien() {
    return alien;
  }

  /**
   * alien value that cannot be documented further in this codebase
   * @param alien alien value that cannot be documented further in this codebase
   */
  public void setAlien(Box alien) {
    this.alien = alien;
  }

  /**
   * alien value that cannot be documented further in this codebase
   * @return an alien value that cannot be documented further in this codebase
   */
  public Box getDocumentedAlien() {
    return documentedAlien;
  }

  /**
   * alien value that cannot be documented further in this codebase
   * @param documentedAlien alien value that cannot be documented further in this codebase
   */
  @Comment("configure the alien properties as documented at https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/swing/Box.html")
  public void setDocumentedAlien(Box documentedAlien) {
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
   * if true the process will end rather than waiting for requests
   * This is expected to be useful for things such as JIT compilers or CDS preparation.
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
  
  
}
