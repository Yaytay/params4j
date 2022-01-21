/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

/**
 *
 * @author jtalbut
 */
public enum FileType {
  
  Properties(new FileGathererProperties())
  , Yaml(new FileGathererYaml())
  , Json(new FileGathererJson())

  ;
  
  private final FileGatherer gatherer;

  public FileGatherer getGatherer() {
    return gatherer;
  }
  
  FileType(FileGatherer gatherer) {
    this.gatherer = gatherer;
  }
  
}
