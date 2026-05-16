package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<String> getAllCategoryNames() {
        List<String> categories = new ArrayList<>();

        String sql = "SELECT category_name FROM Category ORDER BY category_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            categories.add("Home");

            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }

        } catch (Exception e) {
            System.out.println("Failed to load categories from database");
            e.printStackTrace();
        }

        return categories;
    }
    public int getCategoryIdByName(String categoryName) {
    String sql = "SELECT category_id FROM Category WHERE category_name = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, categoryName);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        }

    } catch (Exception e) {
        System.out.println("Failed to get category id");
        e.printStackTrace();
    }

    return -1;
}
    
}