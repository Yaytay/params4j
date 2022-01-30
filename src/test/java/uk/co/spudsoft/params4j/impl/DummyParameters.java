/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author jtalbut
 */
public class DummyParameters {
  
  private int value;
  private LocalDateTime localDateTime;
  private List<String> list;
  private DummyChildParameter child;
  private Boolean check;

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public List<String> getList() {
    return list;
  }

  public void setList(List<String> list) {
    this.list = list;
  }

  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  public DummyChildParameter getChild() {
    return child;
  }

  public void setChild(DummyChildParameter child) {
    this.child = child;
  }

  public Boolean getCheck() {
    return check;
  }

  public void setCheck(Boolean check) {
    this.check = check;
  }
    
}
