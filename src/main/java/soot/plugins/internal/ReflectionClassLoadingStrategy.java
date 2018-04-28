/* Soot - a J*va Optimization Framework
 * 
 * Copyright (C) 2018 Bernhard J. Berger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package soot.plugins.internal;

/**
 * Class loading strategy that uses traditional reflection for instantiation.
 *
 * @author Bernhard J. Berger
 */
public class ReflectionClassLoadingStrategy implements ClassLoadingStrategy {

  @Override
  public Object create(final String className) throws ClassNotFoundException, InstantiationException {
    final Class<?> clazz = Class.forName(className);

    try {
      return clazz.newInstance();
    } catch (final IllegalAccessException e) {
      throw new InstantiationException("Failed to create instance of " + className + " due to access restrictions.");
    }
  }
}
