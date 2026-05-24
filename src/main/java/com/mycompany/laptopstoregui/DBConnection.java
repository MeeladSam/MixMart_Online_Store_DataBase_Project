package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Update these values based on your MySQL setup
    private static final String URL =
            "jdbc:mysql://localhost:3306/onlinestore";

    private static final String USER = "your_mysql_username";
    private static final String PASSWORD = "your_mysql_password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
