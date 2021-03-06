/*
 * Copyright (c) 2008 Poesys Associates. All rights reserved.
 * 
 * This file is part of Poesys-DB.
 * 
 * Poesys-DB is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Poesys-DB is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Poesys-DB. If not, see <http://www.gnu.org/licenses/>.
 */
package com.poesys.db;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;


/**
 * Get the properties from the PoesysDbBundle properties file or optionally from
 * user-specified files. These are I18N properties that the application can
 * customize in their own version of the properties file
 * PoesysDbBundle.properties or a custom properties file. The properties class works as a Singleton pattern
 * class that has a list of property files set up when you first access the
 * class.
 * 
 * @author Robert J. Muller
 */
public abstract class Properties {
  /**
   * Log4j logger for the class
   */
  private static Logger logger = Logger.getLogger(Properties.class);
  /** Name of the properties file; app can use this name to open the file */
  public static final String DEFAULT_FILE_NAME = "com.poesys.db.PoesysDbBundle";
  /** Properties from Poesys DB subsystem */
  private static List<ResourceBundle> properties =
    new ArrayList<ResourceBundle>();

  /**
   * Initialize the list of property bundles from the supplied names. The list
   * then contains property bundles in the same order as the input names with
   * the default Poesys/DB bundle last in the list. The application should call
   * this method once as a static initializer.
   * 
   * @param names the property-file names in package format (for example,
   *          com.poesys.db.PoesysDbBundle)
   */
  public static void initializePropertiesFiles(List<String> names) {
    if (names != null) {
      // Add the user-specified files to the list.
      for (String name : names) {
        ResourceBundle bundle;
        try {
          bundle = ResourceBundle.getBundle(name);
          properties.add(bundle);
        } catch (Exception e) {
          // Log the error and move on to the next bundle.
          logger.error(e.getMessage(), e);
        }
      }
    }
    // Add the Poesys DB bundle as the last (or only) file in the list.
    properties.add(ResourceBundle.getBundle(DEFAULT_FILE_NAME));
  }

  /**
   * Get the property identified by the key string. Expand the property template
   * from the resource bundle with any arguments supplied in an array of object
   * arguments (String, Integer, and so on). This method uses the default
   * locale.
   * 
   * @param key the key in the resource bundle that identifies the property
   * @param args an array of object values to substitute in as arguments
   * @return the completed property value
   */
  public static String getProperty(String key, Object[] args) {
    // Call the locale-based method with no locale.
    return getProperty(key, args, null);
  }

  /**
   * Get the property identified by the key string from the designated locale.
   * Expand the property template from the resource bundles with any arguments
   * supplied in an array of object arguments (String, Integer, and so on).
   * There should be a localized resource bundle for the designated locale; if
   * not, the system uses the default locale. If the message is not a key from
   * the resource bundle, render the property directly as the completed property.
   * 
   * @param key the key in a resource bundle that identifies the property
   * @param args an array of object values to substitute in as arguments
   * @param locale the locale to use to look up the property
   * @return the completed property
   * @throws MissingResourceException when the method does not find the key in
   *           any registered bundle
   */
  public static String getProperty(String key, Object[] args, Locale locale)
      throws MissingResourceException {
    String property = null;
    MissingResourceException exception = null;

    if (properties.size() == 0) {
      // The properties list has not been initialized.
      // Add the Poesys DB bundle as the default.
      properties.add(ResourceBundle.getBundle(DEFAULT_FILE_NAME));
    }

    for (ResourceBundle bundle : properties) {
      String pattern = null;
      try {
        pattern = bundle.getString(key);
      } catch (MissingResourceException e) {
        exception = e;
        // Not in this bundle, move on to next bundle.
        continue;
      }
      if (pattern != null) {
        if (args != null && args.length > 0) {
          MessageFormat formatter = new MessageFormat("");
          formatter.setLocale(locale);
          formatter.applyPattern(pattern);
          property = formatter.format(args);
        } else {
          property = pattern;
        }
        // Take the first instance of the key found.
        break;
      }
    }

    // If there's no property at this point, and the input key is not null, make
    // the key the property; otherwise, re-throw the missing-resource exception.
    if (property == null && key != null) {
      property = key;
    } else if (property != null) {
      // return message below
    } else if (exception != null) {
      throw exception;
    } else {
      throw new RuntimeException("Can't get property for exception, null key");
    }

    return property;
  }
}
