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
package com.poesys.db.dto;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.poesys.db.dao.CacheMessageListener;
import com.poesys.db.pk.IPrimaryKey;


/**
 * Adds messaging to the methods in the superclass DtoCache.
 * 
 * @author Robert J. Muller
 * @param <T> the type of object stored in the cache
 * @see DtoCache
 */
public class DtoCacheWithMessaging<T extends IDbDto> extends DtoCache<T> {

  /** Log4j logger for this class */
  private static final Logger logger =
    Logger.getLogger(DtoCacheWithMessaging.class);

  private static final String PRODUCER_MSG =
    "com.poesys.db.dto.msg.producer_problem";

  /**
   * Create a DtoCacheWithMessaging object.
   * 
   * @param name the cache name (fully-qualified class name of type T)
   */
  public DtoCacheWithMessaging(String name) {
    super(name);
  }

  @Override
  public void remove(IPrimaryKey key) {
    // Send a message to listeners asking to remove there. This will remove
    // the object from all listening caches with the cache name of this cache,
    // including THIS one.
    Connection connection = null;
    try {
      Context initial = new InitialContext();
      ConnectionFactory cf =
        (ConnectionFactory)initial.lookup(CacheMessageListener.CONNECTION_FACTORY);
      Destination deleteTopic =
        (Destination)initial.lookup(CacheMessageListener.DELETE_TOPIC);
      connection = cf.createConnection();
      Session session =
        connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      logger.debug("Created auto-acknowledged messaging session");
      MessageProducer producer = session.createProducer(deleteTopic);
      connection.start();
      ObjectMessage om = session.createObjectMessage(key.getMessageObject());
      om.setStringProperty(CacheMessageListener.CACHE_NAME_PROPERTY,
                           getCacheName());
      producer.send(om);
      logger.debug("Sent message to remove " + key.getValueList()
                   + " from cache " + getCacheName() + " with message id "
                   + om.getJMSMessageID());
    } catch (Exception e) {
      Object[] objects = { getCacheName() };
      String message = com.poesys.db.Message.getMessage(PRODUCER_MSG, objects);
      logger.error(message, e);
    } finally {
      if (connection != null) {
        try {
          String connectionString = connection.toString();
          connection.close();
          logger.debug("Closed connection " + connectionString);
        } catch (JMSException e) {
          Object[] objects = { getCacheName() };
          String message =
            com.poesys.db.Message.getMessage(PRODUCER_MSG, objects);
          logger.error(message, e);
        }
      }
    }
  }

}
