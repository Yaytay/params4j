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
package uk.co.spudsoft.params4j.impl;

/**
 * Representation of a MixIn added via the {@link uk.co.spudsoft.params4j.Params4JFactory#withMixIn(java.lang.Class, java.lang.Class)}.
 * 
 * MixIns enable a pre-compiled class to have annotations added to it (by sourcing them from another class).
 * 
 * @author jtalbut
 */
public class MixIn {
  /**
   * Class (or interface) whose annotations to effectively override.
   */
  public final Class<?> target;
  /**
   * Class (or interface) whose annotations are to be "added" to target's annotations, overriding as necessary.
   */
  public final Class<?> source;

  /**
   * Constructor.
   * @param target Class (or interface) whose annotations to effectively override.
   * @param source Class (or interface) whose annotations are to be "added" to target's annotations, overriding as necessary
   */
  public MixIn(Class<?> target, Class<?> source) {
    this.target = target;
    this.source = source;
  }    
}
