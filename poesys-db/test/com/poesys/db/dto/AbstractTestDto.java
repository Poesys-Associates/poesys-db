package com.poesys.db.dto;

/**
 * An abstract superclass that contains methods implemented for all the test
 * objects. These are methods that don't require any custom code for testing.
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractTestDto extends AbstractDto {

  /** Serial version UID for the Serialized object */
  private static final long serialVersionUID = 1L;

  /**
   * Create a AbstractTestDto object.
   */
  public AbstractTestDto() {
    super();
  }

  @Override
  public String getSubsystem() {
    return "com.poesys.db.poesystest.mysql";
  }

  @Override
  public java.sql.Connection getConnection() throws java.sql.SQLException {
    java.sql.Connection connection = null;

    /*
     * The resource bundle for the DTO's subsystem contains the suffix that
     * distinguishes multiple versions of the subsystem in the
     * database.properties file, such as "prod" or "test". Most
     * database.properties files have only one implementation and use external
     * facilities to switch between the databases (JNDI, for example, or
     * producing different database.properties files in different setups). Use
     * the subsystem resource bundle to get the suffix, then use the full
     * subsystem name to get a connection factory for the DTO's subsystem, then
     * use that factory to get a JDBC connection.
     */
    java.util.ResourceBundle rb =
      java.util.ResourceBundle.getBundle("com.poesys.db.poesystest.mysql");
    String subsystem =
      "com.poesys.db.poesystest.mysql"
          + (rb.getString("suffix") == null
             || rb.getString("suffix").length() == 0 ? ""
              : "." + rb.getString("suffix"));

    try {
      connection =
        com.poesys.db.connection.ConnectionFactoryFactory.getInstance(subsystem).getConnection();
    } catch (com.poesys.db.InvalidParametersException e) {
      throw new java.sql.SQLException(e.getMessage());
    } catch (java.io.IOException e) {
      throw new java.sql.SQLException(e.getMessage());
    }

    return connection;
  }
}