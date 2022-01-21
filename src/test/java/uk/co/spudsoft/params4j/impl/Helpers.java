/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import java.io.File;

/**
 *
 * @author jtalbut
 */
public class Helpers {
  
  /**
   * Return the filename of a resource.
   * Note that this is only valid in a maven test environment, in any live environment the resource is likely
   * to be embedded in the jar an inaccessible via file APIs.
   * 
   * @param resourceName The path to the resource (i.e. the relative path from srv/test/resources, with a leading slash).
   * @return The absolute path to the resource.
   */
  public static String getResourcePath(String resourceName) {
    // This would probably work in all reasonable circumstances without running it though the File constructor,
    // but this results in a tidier value (a proper path, rather than something that will probably work).
    return new File(Helpers.class.getResource(resourceName).getFile()).getAbsolutePath();
  }
  
}
