package stockbite.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Connects the Java app to the MySQL database hosted through XAMPP. */
public final class DBConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3307/stockbite_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP default is empty

    private DBConnection() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
