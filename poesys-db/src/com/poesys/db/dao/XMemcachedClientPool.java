package com.poesys.db.dao;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.log4j.Logger;

import com.poesys.db.DbErrorException;


/**
 * A singleton connection pool for xmemcached clients. The pool contains a
 * minimum number of xmemcached clients connected to memcached. You can acquire
 * any number of clients up to the maximum allowed. There is a global timeout
 * after which an inactive object becomes stale and gets closed. You set the
 * minimum and maximum number of clients and the timeout in milliseconds in the
 * com::poesys::db::memcached.properties file.
 * 
 * This class is currently deprecated because it apparently causes the I/O
 * system to go crazy, opening a huge number of polling event pipe sockets!
 * 
 * @author Robert J. Muller
 */
@Deprecated
public class XMemcachedClientPool {
  /** Logger for debugging */
  private static final Logger logger =
    Logger.getLogger(XMemcachedClientPool.class);

  /** Name of the memcached properties resource bundle */
  private static final String BUNDLE = "com.poesys.db.memcached";

  /** The resource bundle containing the memcached properties. */
  private static final ResourceBundle properties =
    ResourceBundle.getBundle(BUNDLE);

  /** Singleton pool object */
  private static XMemcachedClientPool poolManager = null;

  /** Singleton builder for memcached clients that is part of the instance */
  private MemcachedClientBuilder builder =
    new XMemcachedClientBuilder(AddrUtil.getAddresses(properties.getString("servers")));

  /** A blocking queue of xmemcached clients */
  private static final Queue<Client> pool = new ConcurrentLinkedQueue<Client>();

  private static final ConcurrentHashMap<MemcachedClient, Client> clientMap =
    new ConcurrentHashMap<MemcachedClient, Client>();

  /** time in milliseconds after which an inactive client gets closed */
  private Long timeout;
  /** minimum number of clients in the queue */
  private Long minClients;
  /** maximum number of clients in the queue */
  private Long maxClients;
  /** number of clients to add when queue is empty */
  private Long addClients;

  /** Whether the property values are frozen (not read from property file) */
  private boolean propertiesFrozen = false;

  private static final String EXHAUSTED_ERR =
    "com.poesys.db.dao.query.msg.memcached_connections_exhausted";
  private static final String SHUTDOWN_ERR =
    "com.poesys.db.dao.query.msg.memcached_client_shutdown";

  /**
   * A wrapper for xmemcached clients that supports the pool timeout feature
   */
  private class Client {
    /** the xmemcached client */
    private MemcachedClient client = null;
    private Long startTime;
    private boolean active = false;

    /**
     * Create a Client object using the binary protocol.
     * 
     * @throws IOException when there is a problem building the client
     */
    public Client() throws IOException {
      builder.setCommandFactory(new BinaryCommandFactory());
      client = builder.build();
      startTime = System.currentTimeMillis();
    }

    /**
     * Get the xmemcached client.
     * 
     * @return a client
     */
    public MemcachedClient getClient() {
      active = true;
      return client;
    }

    /**
     * Is the client stale? Should the client be closed?
     * 
     * @return true if the client has been inactive
     */
    public boolean isStale() {
      return System.currentTimeMillis() - startTime > timeout && !active;
    }

    /**
     * Set the active flag on the client. This also resets the internal clock
     * for timeout tracking.
     * 
     * @param active true to make the client active, false to make it inactive
     */
    public void setActive(boolean active) {
      this.active = active;
      startTime = System.currentTimeMillis();
    }
  }

  /**
   * Create the XMemcachedClientPool object. This gets called once at virtual
   * machine startup by the statically initialized pool manager.
   * 
   * @throws IOException when there is a problem building a new client
   */
  private XMemcachedClientPool() throws IOException {
    setProperties();
    // Initialize the pool with the minimum number of clients.
    for (int i = 0; i < minClients; i++) {
      Client client = new Client();
      pool.add(client);
      clientMap.put(client.getClient(), client);
    }
  }

  /**
   * Get the singleton instance of the pool manager.
   * 
   * @return the pool manager
   * @throws IOException when there is a problem creating a default client in
   *           the pool
   */
  public static XMemcachedClientPool getInstance() throws IOException {
    if (poolManager == null) {
      poolManager = new XMemcachedClientPool();
    }
    return poolManager;
  }

  /**
   * Set the properties of the connection pool from the properties file. You
   * should call this method as often as necessary so that the values are
   * reasonably fresh. The properties change only if the propertiesFrozen flag
   * is set to false.
   */
  private void setProperties() {
    if (!propertiesFrozen) {
      timeout = new Long(properties.getString("client_timeout"));
      minClients = new Long(properties.getString("min_clients"));
      maxClients = new Long(properties.getString("max_clients"));
      addClients = new Long(properties.getString("add_clients"));
    }
  }

  /**
   * Set the timeout.
   * 
   * @param timeout a timeout
   */
  public void setTimeout(Long timeout) {
    this.timeout = timeout;
  }

  /**
   * Set the minClients.
   * 
   * @param minClients a minClients
   */
  public void setMinClients(Long minClients) {
    this.minClients = minClients;
  }

  /**
   * Set the maxClients.
   * 
   * @param maxClients a maxClients
   */
  public void setMaxClients(Long maxClients) {
    this.maxClients = maxClients;
  }

  /**
   * Freeze the property settings. This flag tells the pool not to refresh its
   * properties from the properties file, allowing you to control the properties
   * dynamically using the property accessors. Use this flag during testing. The
   * default is not to freeze.
   * 
   * @param freeze true to freeze, false to refresh
   */
  public void freezeProperties(boolean freeze) {
    propertiesFrozen = freeze;
  }

  /**
   * Get an xmemcached client. This method will either return a client or an
   * exception if the pool is full and no clients are available. This method is
   * synchronized because it changes both the pool queue and the client map.
   * 
   * @return a client
   * @throws IOException when there is a problem creating a new client
   * @throws DbErrorException when the pool is exhausted (max size exceeded)
   */
  public synchronized MemcachedClient getClient() throws IOException,
      DbErrorException {
    // Reread configuration file before getting the client.
    setProperties();
    collect();
    int size = clientMap.size();
    if (pool.peek() == null && size < maxClients) {
      // Pool queue is empty and haven't reached max yet, add more clients.
      Long add = computeAddSize(size);
      for (int i = 0; i < add; i++) {
        Client client = poolManager.new Client();
        pool.add(client);
        clientMap.put(client.getClient(), client);
      }
    } else if (pool.peek() == null) {
      // No more clients allowed in pool, give up.
      throw new DbErrorException(EXHAUSTED_ERR);
    }

    Client newClient = pool.poll();
    newClient.setActive(true);
    logger.debug("Got active client for memcached: active pool " + pool.size()
                 + ", total pool " + clientMap.size());
    return newClient.getClient();
  }

  /**
   * Compute the possible number of clients that you can add to the current map
   * based on a pre-computed desired number to add and the maximum size
   * permitted for the map.
   * 
   * @param size the size of the map
   * @return the number of elements you can add to the map given the max-clients
   *         size
   */
  private Long computeAddSize(int size) {
    Long add = addClients;

    if (size + add > maxClients) {
      // Can't add full quota, just add up to max
      add = maxClients - size;
    }
    return add;
  }

  /**
   * Collect garbage on the pool by iterating through the pool and removing any
   * clients that are stale until the pool has the minimum number of clients.
   * This action shrinks the pool down to its minimum size. Note that the pool
   * queue may have fewer clients than the minimum if there are active clients
   * still working. This method is synchronized because it changes the state of
   * both the pool and the map of all clients.
   * 
   * @throws IOException when there is a problem closing the memcached
   *           connection
   */
  public synchronized void collect() throws IOException {
    for (Client client : pool) {
      if (client.isStale() && clientMap.size() > minClients) {
        // Close the xmemcached client and memcached connection.
        client.getClient().shutdown();
        // Remove the client from the map and pool.
        clientMap.remove(client.getClient());
        pool.remove(client);
      }
    }
  }

  /**
   * Release a client, making it inactive. This will also set the clock ticking
   * on the timeout for the client.
   * 
   * @param client the client to de-activate
   */
  public void release(MemcachedClient client) {
    // Get the client.
    Client thisClient = clientMap.get(client);
    // Set the client to be inactive.
    thisClient.setActive(false);
    // Add the client back into the pool.
    pool.add(thisClient);
  }

  /**
   * DEBUGGING ONLY: Get the list of active xmemcached clients. These are the
   * clients that are currently "in use", gotten from the pool but not closed.
   * Declared package access for access within the package only.
   * 
   * @return a list of clients
   */
  List<MemcachedClient> getActiveClients() {
    List<MemcachedClient> clients = new ArrayList<MemcachedClient>();
    for (Client client : clientMap.values()) {
      if (client.active) {
        clients.add(client.getClient());
      }
    }
    return clients;
  }

  /**
   * DEBUGGING ONLY: Get the list of inactive xmemcached clients. These are the
   * clients that are currently not "in use", in the queue. Declared package
   * access for access within the package only.
   * 
   * @return a list of clients
   */
  List<MemcachedClient> getInactiveClients() {
    List<MemcachedClient> clients = new ArrayList<MemcachedClient>();
    for (Client client : pool) {
      if (!client.active) {
        clients.add(client.getClient());
      }
    }
    return clients;
  }

  /**
   * Close all memcached connections and xmemcached clients and destroy the pool
   * manager. This method is synchronized as it clears both the pool queue and
   * the client map.
   */
  public synchronized void close() {
    for (MemcachedClient client : clientMap.keySet()) {
      try {
        client.shutdown();
      } catch (IOException e) {
        logger.warn(SHUTDOWN_ERR, e);
      }
    }
    clientMap.clear();
    pool.clear();
    poolManager = null;
  }
}
