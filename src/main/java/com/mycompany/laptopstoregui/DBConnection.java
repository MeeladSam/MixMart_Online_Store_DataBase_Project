package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/onlinestore";
    private static final String USER = "root";
    private static final String PASSWORD = "melad520520520"; // حط باسورد MySQL هون إذا عندك

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}



/////////////////////////////////////////////////////////////To conncect to online_shoping_store_database
/// /////////////////////////////////////////////////////////////To conncect to online_shoping_store_database
 /////////////////////////////////////////////////////////////To conncect to online_shoping_store_database
 /////////////////////////////////////////////////////////////To conncect to online_shoping_store_database
 /////////////////////////////////////////////////////////////To conncect to online_shoping_store_database
 /////////////////////////////////////////////////////////////To conncect to online_shoping_store_database

//package com.mycompany.laptopstoregui;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DBConnection {
//
//    private static final String URL =
//            "jdbc:mysql://164.92.253.36:3306/online_shoping_store_database";
//
//    private static final String USER = "12323283_project_db";
//    private static final String PASSWORD = "62792831";
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(URL, USER, PASSWORD);
//    }
//}