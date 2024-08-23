package network.multicore.vc.persistence.datasource;

public enum DataSourceType {
    MYSQL("com.mysql.ci.jdbc.Driver", "jdbc:mysql://%s:%d/%s"),
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://%s:%d/%s"),
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:%s"),
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s"),
    H2("org.h2.Driver", "jdbc:h2:%s");

    private final String driver;
    private final String url;

    DataSourceType(String driver, String url) {
        this.driver = driver;
        this.url = url;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }
}
