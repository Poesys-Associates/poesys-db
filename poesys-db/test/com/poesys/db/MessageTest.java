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


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Test the Message class.
 * 
 * @author Robert J. Muller
 */
public class MessageTest {
  /**
   * Initialize the message bundles.
   * 
   * @throws java.lang.Exception when can't initialize property file
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    List<String> list = new ArrayList<String>();
    // Add a bundle with user-supplied test messages and a message override.
    list.add("com.poesys.db.TestBundle");
    Message.initializePropertiesFiles(list);
  }

  /**
   * Test the standard messsage with arguments. Depends on the message
   * com.poesys.db.connection.msg.invalid_jndi in PoesysDbBundle.
   */
  @Test
  public void testGetMessage() {
    Object[] args = { "java:jdbc/ReadOnlyTairTestJTDS" };
    String message =
      Message.getMessage("com.poesys.db.connection.msg.invalid_jndi", args);
    assertTrue("Couldn't find message", message != null);
  }

  /**
   * Test a non-bundle message (not in the bundle).
   */
  @Test
  public void testGetMessageNonBundleMessage() {
    String input = "message not in bundle";
    String message = null;
    try {
      message = Message.getMessage(input, null);
      assertTrue("Did not pass through non-bundle message" + message,
                 input.equals(message));
    } catch (MissingResourceException e) {
      fail("Threw missing resource exception instead of passing through message");
    }
  }

  /**
   * Test a standard message with no arguments.
   */
  @Test
  public void testGetMessageNoArgs() {
    String message =
      Message.getMessage("com.poesys.db.connection.msg.noConnection", null);
    assertTrue("No connection".equals(message));
  }

  /**
   * Test a standard message with no arguments where arguments are supplied by
   * mistake.
   */
  @Test
  public void testGetMessageTooManyArgs() {
    Object[] args = { "useless string" };
    String message =
      Message.getMessage("com.poesys.db.connection.msg.noConnection", args);
    assertTrue("No connection".equals(message));
  }

  /**
   * Test the standard messsage with arguments. Depends on the message
   * com.poesys.db.connection.msg.invalid_jndi in PoesysDbBundle.
   */
  @Test
  public void testGetUserMessage() {
    Object[] args = { "arg" };
    String message = Message.getMessage("com.poesys.db.test.args", args);
    assertTrue("This is a test message with argument arg", message != null);
  }

  /**
   * Test a standard user-bundle message with no arguments.
   */
  @Test
  public void testGetUserMessageNoArgs() {
    String message = Message.getMessage("com.poesys.db.test.message", null);
    assertTrue("This is a test message".equals(message));
  }

  /**
   * Test the standard messsage with arguments. Depends on the message
   * com.poesys.db.connection.msg.invalid_jndi in PoesysDbBundle.
   */
  @Test
  public void testGetUserOverriddenMessage() {
    String message =
      Message.getMessage("com.poesys.db.connection.msg.rollbackError", null);
    assertTrue("Did not override message " + message,
               message.equals("No rollback"));
  }
}
