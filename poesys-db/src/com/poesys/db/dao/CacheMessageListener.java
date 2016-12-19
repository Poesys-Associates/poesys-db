/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
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
package com.poesys.db.dao;


import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDtoCache;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.MessageKeyFactory;


/**
 * A thread-based class that listens for messages about the Poesys/DB cache.
 * 
 * @author Robert J. Muller
 */
public class CacheMessageListener implements Runnable, MessageListener {

  /**
   * Logger for this class
   */
  private static final Logger logger =
    Logger.getLogger(CacheMessageListener.class);

  private static final String LISTENER_MSG =
    "com.poesys.db.dao.msg.listener_problem";
  private static final String DELETE_MSG =
    "com.poesys.db.dao.msg.delete_problem";
  private static final String INTERRUPT_MSG =
    "com.poesys.db.dao.msg.interrupted";

  /** JMS topic name for the Poesys/DB delete topic */
  public static final String DELETE_TOPIC = "topic/PoesysCacheDelete";
  /** JMS connection factory name */
  public static final String CONNECTION_FACTORY = "ClusteredConnectionFactory";
  /** JMS ObjectMessage property name for cache name property */
  public static final String CACHE_NAME_PROPERTY = "CacheName";

  private Connection connection;
  private Session sessionConsumer;
  private MessageConsumer consumer;
  
  private final String subsystem;

  /**
   * Create a CacheMessageListener object.
   * 
   * @param subsystem the subsystem of the DTO class
   */
  public CacheMessageListener(String subsystem) {
    this.subsystem = subsystem;
  }

  /**
   * Runs the message listener.
   */
  public void run() {
    try {
      // Look up the connection factory using JNDI.
      Context initial = new InitialContext();
      ConnectionFactory cf =
        (ConnectionFactory)initial.lookup(CONNECTION_FACTORY);

      // Set this object to be a message listener for delete requests.
      Destination deleteTopic = (Destination)initial.lookup(DELETE_TOPIC);
      connection = cf.createConnection();
      sessionConsumer =
        connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      logger.debug("Cache message listener session started with auto-acknowledgment");
      consumer = sessionConsumer.createConsumer(deleteTopic);
      consumer.setMessageListener(this);
      connection.start();

      logger.info("Cache message listener started, listening for cache removal requests");

      // Sleep indefinitely until interruption.
      while (!Thread.currentThread().isInterrupted()) {
        // Sleeps for 10 seconds
        Thread.sleep(10 * 1000);
      }
    } catch (InterruptedException e) {
      String message = com.poesys.db.Message.getMessage(INTERRUPT_MSG, null);
      logger.info(message);
    } catch (Exception e) {
      String message = com.poesys.db.Message.getMessage(LISTENER_MSG, null);
      logger.error(message, e);
    } finally {
      if (connection != null) {
        try {
          int connectionId = connection.hashCode();
          connection.close();
          logger.debug("Closed connection " + connectionId);
        } catch (JMSException e) {
          String message = com.poesys.db.Message.getMessage(LISTENER_MSG, null);
          logger.error(message, e);
        }
      }
    }
  }

  @Override
  public void onMessage(Message message) {
    IPrimaryKey key = null;
    String cacheName = null;

    if (message == null) {
      logger.error("Cache message listener received null message");
    } else {
      try {
        logger.debug("Received cache removal request "
                     + message.getJMSMessageID());
        // Get the message and extract the key and the cache name.
        ObjectMessage objectMessage = (ObjectMessage)message;
        if (objectMessage != null) {
          // Message key is the object payload.
          Serializable object = objectMessage.getObject();
          if (object instanceof com.poesys.ms.pk.IPrimaryKey) {
            com.poesys.ms.pk.IPrimaryKey messageKey =
              (com.poesys.ms.pk.IPrimaryKey)objectMessage.getObject();
            // Translate into database primary key.
            key = MessageKeyFactory.getKey(messageKey);
            // Cache name is a property.
            cacheName = objectMessage.getStringProperty(CACHE_NAME_PROPERTY);
            // Make sure the singleton manager is instantiated.

            IDtoCache<? extends IDbDto> cache =
              CacheDaoManager.getInstance(subsystem).getCache(cacheName);
            // Remove the object from the local cache only if it's there; if
            // it's not there, move on since there's nothing to do.
            if (cache != null) {
              logger.debug("Requesting local removal of key "
                           + key.getValueList() + " from cache " + cacheName);
              cache.removeLocally(key);
            } else {
              logger.debug("No cache from which to remove object "
                           + key.getValueList());
            }
          } else {
            logger.error("Cache message listener received message with a payload that was not a primary key: "
                         + object
                         + " with message ID "
                         + message.getJMSMessageID());
          }
        }
      } catch (JMSException e) {
        // log full information and ignore
        Object[] objects = { cacheName, key.getValueList() };
        String errorMsg = com.poesys.db.Message.getMessage(DELETE_MSG, objects);
        logger.error(errorMsg, e);
      } catch (RuntimeException e) {
        // log and ignore
        logger.error("Runtime exception in onMessage: ", e);
      }
    }
  }
}
