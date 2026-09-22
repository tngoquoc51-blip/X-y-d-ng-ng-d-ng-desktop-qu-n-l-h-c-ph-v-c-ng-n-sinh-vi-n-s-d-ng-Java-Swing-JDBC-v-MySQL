package vn.edu.eaut.qlhocphi.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection pool nho — tai su dung socket, tranh loi Windows
 * "inability to establish the client portion of a socket".
 * Van dung try-with-resources; close() tra ve pool (khong dong TCP lien tuc).
 */
public class DBConnection {

    private static final int POOL_SIZE = 12;
    private static final int WAIT_MS = 8000;
    private static final BlockingQueue<Connection> POOL = new LinkedBlockingQueue<>(POOL_SIZE);
    private static final AtomicInteger CREATED = new AtomicInteger(0);
    private static volatile boolean driverLoaded = false;

    private static void ensureDriver() throws SQLException {
        if (driverLoaded) return;
        synchronized (DBConnection.class) {
            if (driverLoaded) return;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("Khong tim thay MySQL JDBC Driver", e);
            }
        }
    }

    private static String buildUrl() {
        String url = AppConfig.get("db.url");
        if (url == null || url.isBlank()) {
            url = "jdbc:mysql://localhost:3310/qlhocphi";
        }
        if (!url.contains("connectTimeout=")) {
            String extra = "connectTimeout=5000&socketTimeout=60000&tcpKeepAlive=true&autoReconnect=true&maxReconnects=2";
            url = url + (url.contains("?") ? "&" : "?") + extra;
        }
        return url;
    }

    private static Connection openPhysical() throws SQLException {
        ensureDriver();
        DriverManager.setLoginTimeout(8);
        return DriverManager.getConnection(
                buildUrl(),
                AppConfig.get("db.username"),
                AppConfig.get("db.password")
        );
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection raw = POOL.poll(200, TimeUnit.MILLISECONDS);
            if (raw != null) {
                if (raw.isClosed() || !raw.isValid(2)) {
                    silentClose(raw);
                    CREATED.decrementAndGet();
                } else {
                    return wrap(raw);
                }
            }
            if (CREATED.get() >= POOL_SIZE) {
                raw = POOL.poll(WAIT_MS, TimeUnit.MILLISECONDS);
                if (raw != null && !raw.isClosed() && raw.isValid(2)) {
                    return wrap(raw);
                }
                if (raw != null) {
                    silentClose(raw);
                    CREATED.decrementAndGet();
                }
            }
            Connection neu = openPhysical();
            CREATED.incrementAndGet();
            return wrap(neu);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new SQLException("Bi gian doan khi cho connection", ie);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Loi lay ket noi CSDL: " + e.getMessage(), e);
        }
    }

    private static Connection wrap(Connection physical) {
        InvocationHandler handler = new InvocationHandler() {
            private boolean closed = false;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("close".equals(name)) {
                    if (!closed) {
                        closed = true;
                        traVePool(physical);
                    }
                    return null;
                }
                if ("isClosed".equals(name)) {
                    return closed || physical.isClosed();
                }
                if (closed) {
                    throw new SQLException("Connection da close (tra ve pool)");
                }
                return method.invoke(physical, args);
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler);
    }

    private static void traVePool(Connection physical) {
        try {
            if (physical == null || physical.isClosed()) {
                CREATED.decrementAndGet();
                return;
            }
            try {
                if (!physical.getAutoCommit()) {
                    physical.rollback();
                    physical.setAutoCommit(true);
                }
            } catch (SQLException ignore) { }
            if (!POOL.offer(physical)) {
                silentClose(physical);
                CREATED.decrementAndGet();
            }
        } catch (Exception e) {
            silentClose(physical);
            CREATED.decrementAndGet();
        }
    }

    private static void silentClose(Connection c) {
        try {
            if (c != null) c.close();
        } catch (Exception ignore) { }
    }

    public static void shutdown() {
        Connection c;
        while ((c = POOL.poll()) != null) {
            silentClose(c);
            CREATED.decrementAndGet();
        }
    }
}