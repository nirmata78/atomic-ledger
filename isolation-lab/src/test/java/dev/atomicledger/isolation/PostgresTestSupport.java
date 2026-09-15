package dev.atomicledger.isolation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Shared Testcontainers Postgres + a two-row {@code orders} table. Given complete as plumbing
 * (T-002-equivalent for this module) — the isolation-level exercises in T-005/T-006 are what
 * you DO with two {@link Connection}s from {@link #dataSource()}, not this setup itself.
 */
public final class PostgresTestSupport implements AutoCloseable {

    private final PostgreSQLContainer<?> container =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("atomic_ledger")
                    .withUsername("atomic")
                    .withPassword("atomic");

    private HikariDataSource dataSource;

    public void start() {
        container.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        // Two-connection isolation experiments need at least two live connections at once;
        // the Hikari default of 10 is already enough, kept explicit here as documentation.
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);

        resetOrdersTable();
    }

    public DataSource dataSource() {
        return dataSource;
    }

    /** Drops and recreates {@code orders} with a single seed row (id=1, status='PENDING', amount=100.00). */
    public void resetOrdersTable() {
        try (Connection c = dataSource.getConnection();
             Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS orders");
            s.execute("""
                    CREATE TABLE orders (
                        id      INTEGER PRIMARY KEY,
                        status  TEXT NOT NULL,
                        amount  NUMERIC(10,2) NOT NULL
                    )
                    """);
            s.execute("INSERT INTO orders (id, status, amount) VALUES (1, 'PENDING', 100.00)");
        } catch (SQLException e) {
            throw new IllegalStateException("failed to reset orders table", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
        container.stop();
    }
}
