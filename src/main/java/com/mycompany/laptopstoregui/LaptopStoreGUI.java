package com.mycompany.laptopstoregui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LaptopStoreGUI {
    private JFrame frame;
    private JTextField searchField;
    private JPanel productsGrid;
    private JPanel cartPanel;
    private JLabel statusLabel;
    private JLabel cartCountLabel;
    private JButton websiteReviewButton;
    private JButton seeWebsiteReviewsButton;
    private JButton adminPanelButton;
    private JButton wishlistButton;
    private JButton ordersButton;
    private JButton cartButton;
    private JPanel cartSidebarWrapper;
    private JPanel sellingsCardWrapper;

    // Filters
    private JComboBox<String> filterCategoryCombo;
    private JComboBox<String> sortPriceCombo;
    private JCheckBox discountedOnlyCheck;
    private JTextField minPriceField;
    private JTextField maxPriceField;

    // Login state
    private boolean loggedIn = false;
    private String currentUser = "";
    private boolean isAdmin = false;
    private String selectedCategory = "Home";
    
    

    // temp holder for selected image path in sell form
    private String selectedItemImagePath = "";

    // simple id generator for user-submitted products
    private int nextProductId = 300;

    private final List<Product> allProducts = new ArrayList<>();
    private final List<CartItem> cartItems = new ArrayList<>();
    private final Set<Integer> firstConfirmPopupShownOrders = new HashSet<>();
    private final Set<Integer> deliveredReviewPopupShownOrders = new HashSet<>();

    public static void main(String[] args) {
        try {
    DBConnection.getConnection();
    System.out.println("Database connected successfully");
    
//    ProductDAO productDAO = new ProductDAO();
//    productDAO.showAllProducts();
    
} catch (Exception e) {
    System.out.println("Database connection failed");
    e.printStackTrace();
}
        
        SwingUtilities.invokeLater(() -> new LaptopStoreGUI().createAndShowGUI());
    }
private void openProductReviewForm(Product product) {
    if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first to review a product.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    ReviewDAO reviewDAO = new ReviewDAO();
    ReviewInfo oldReview = reviewDAO.getProductReview(currentUser, product.id);

    if (!reviewDAO.canReviewProduct(currentUser, product.id)) {
        JOptionPane.showMessageDialog(
                frame,
                "You can review this product only after buying it and receiving the order.",
                "Review Not Allowed",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    ratingCombo.setSelectedItem(5);

    JTextArea commentArea = new JTextArea(4, 22);
    commentArea.setLineWrap(true);
    commentArea.setWrapStyleWord(true);

    if (oldReview != null) {
    ratingCombo.setSelectedItem(oldReview.rating);

    if (oldReview.comment != null) {
        commentArea.setText(oldReview.comment);
    }
}
    
    
    JPanel panel = new JPanel(new BorderLayout(8, 8));

    JPanel top = new JPanel(new GridLayout(2, 2, 8, 8));
    top.add(new JLabel("Product:"));
    top.add(new JLabel(product.name));
    top.add(new JLabel("Rating:"));
    top.add(ratingCombo);

    panel.add(top, BorderLayout.NORTH);
    panel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Product Review",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        int rating = (Integer) ratingCombo.getSelectedItem();
        String comment = commentArea.getText().trim();


        boolean success = reviewDAO.addOrUpdateProductReview(
                currentUser,
                product.id,
                rating,
                comment
        );

        if (success) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Product review saved successfully.",
                    "Review",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshProducts();
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to save product review.",
                    "Review Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
  

private void refreshWebsiteReviewButtonText() {
    if (websiteReviewButton == null) {
        return;
    }

    if (isAdmin) {
        websiteReviewButton.setText("Product Reviews");
        websiteReviewButton.setVisible(true);
        return;
    }

    websiteReviewButton.setVisible(true);

    if (!loggedIn) {
        websiteReviewButton.setText("Rate Website");
        return;
    }

    ReviewDAO reviewDAO = new ReviewDAO();

    if (reviewDAO.hasWebsiteReview(currentUser)) {
        websiteReviewButton.setText("Edit Rating");
    } else {
        websiteReviewButton.setText("Rate Website");
    }
}


private void openWebsiteReviewForm() {
if (!loggedIn) {
    JOptionPane.showMessageDialog(
            frame,
            "Error: You must log in first to review the website.",
            "Access Denied",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

    ReviewDAO reviewDAO = new ReviewDAO();
    ReviewInfo oldReview = reviewDAO.getWebsiteReview(currentUser);


    JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    ratingCombo.setSelectedItem(5);

    JTextArea commentArea = new JTextArea(4, 22);
    commentArea.setLineWrap(true);
    commentArea.setWrapStyleWord(true);

    if (oldReview != null) {
    ratingCombo.setSelectedItem(oldReview.rating);

    if (oldReview.comment != null) {
        commentArea.setText(oldReview.comment);
    }
}
    
    
    JPanel panel = new JPanel(new BorderLayout(8, 8));

    JPanel top = new JPanel(new GridLayout(1, 2, 8, 8));
    top.add(new JLabel("Website Rating:"));
    top.add(ratingCombo);

    panel.add(top, BorderLayout.NORTH);
    panel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Website Review",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        int rating = (Integer) ratingCombo.getSelectedItem();
        String comment = commentArea.getText().trim();

       

        boolean success = reviewDAO.addOrUpdateWebsiteReview(
                currentUser,
                rating,
                comment
        );

        if (success) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Website review saved successfully.",
                    "Review",
                    JOptionPane.INFORMATION_MESSAGE
            );
    refreshWebsiteReviewButtonText();
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to save website review.",
                    "Review Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

private void showAdminWebsiteReviews() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    ReviewDAO reviewDAO = new ReviewDAO();
    List<String> reviews = reviewDAO.getAllWebsiteReviewsForAdmin();

    if (reviews.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "No website reviews found.",
                "Website Reviews",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    StringBuilder sb = new StringBuilder();

    for (String review : reviews) {
        sb.append(review);
    }

    JTextArea textArea = new JTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(620, 420));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "Website Reviews - Admin View",
            JOptionPane.INFORMATION_MESSAGE
    );
}


private void showAdminProductReviews() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    ReviewDAO reviewDAO = new ReviewDAO();
    List<String> reviews = reviewDAO.getAllProductReviewsForAdmin();

    if (reviews.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "No product reviews found.",
                "Product Reviews",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    StringBuilder sb = new StringBuilder();

    for (String review : reviews) {
        sb.append(review);
    }

    JTextArea textArea = new JTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(650, 430));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "Product Reviews - Admin View",
            JOptionPane.INFORMATION_MESSAGE
    );
}


private void showProductRatings(Product product) {
    ReviewDAO reviewDAO = new ReviewDAO();
    List<ReviewInfo> reviews = reviewDAO.getProductReviews(product.id);

    if (reviews.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "No ratings yet for this product.",
                "Product Ratings",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    StringBuilder sb = new StringBuilder();

    double total = 0;

    for (ReviewInfo review : reviews) {
        total += review.rating;

        sb.append("Customer: ").append(review.customerPhone).append("\n");
        sb.append("Rating: ").append(review.rating).append(" / 5\n");
        sb.append("Comment: ").append(review.comment).append("\n");
        sb.append("Date: ").append(review.reviewDate).append("\n");
        sb.append("----------------------------------\n");
    }

    double average = total / reviews.size();

    JTextArea textArea = new JTextArea(
            "Average Rating: " + String.format("%.1f", average) + " / 5\n"
                    + "Total Reviews: " + reviews.size() + "\n"
                    + "==================================\n\n"
                    + sb
    );

    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(520, 350));

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "Ratings - " + product.name,
            JOptionPane.INFORMATION_MESSAGE
    );
}

private void updateAdminView() {
    if (adminPanelButton != null) {
        adminPanelButton.setVisible(isAdmin);
    }

    if (wishlistButton != null) {
        wishlistButton.setVisible(!isAdmin);
    }

    if (ordersButton != null) {
        ordersButton.setVisible(!isAdmin);
    }

    if (cartButton != null) {
        cartButton.setVisible(!isAdmin);
    }

    if (cartCountLabel != null) {
        cartCountLabel.setVisible(!isAdmin);
    }

    if (cartSidebarWrapper != null) {
        cartSidebarWrapper.setVisible(!isAdmin);
    }

if (sellingsCardWrapper != null) {
    sellingsCardWrapper.setVisible(true);
}

    if (seeWebsiteReviewsButton != null) {
        seeWebsiteReviewsButton.setVisible(isAdmin);
    }

    if (frame != null) {
        frame.revalidate();
        frame.repaint();
    }
}



    private void createAndShowGUI() {
//        seedProducts();
loadProductsFromDatabase();
        frame = new JFrame("MixMart Online Store");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1420, 860);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(buildTopWrapper(), BorderLayout.NORTH);
        frame.add(buildMainContent(), BorderLayout.CENTER);
        frame.add(buildFooter(), BorderLayout.SOUTH);

        refreshProducts();
        refreshCart();

        updateAdminView();
        frame.setVisible(true);
    }

    private JPanel buildTopWrapper() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(buildUtilityBar());
        wrapper.add(buildHeader());
        wrapper.add(buildNavBar());
        return wrapper;
    }

    private JPanel buildUtilityBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(18, 24, 34));
        bar.setBorder(new EmptyBorder(8, 18, 8, 18));

        JLabel left = new JLabel("High quality products  •  User friendly website  •  All in one place");
        left.setForeground(Color.WHITE);
        left.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel right = new JLabel("Call us: 056-299-0922   |   meladsamds@gmail.com");
        right.setForeground(new Color(220, 220, 220));
        right.setFont(new Font("SansSerif", Font.PLAIN, 13));

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }
    
    private void showAdminAllProducts() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    loadProductsFromDatabase();

    if (allProducts.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "No products found.",
                "All Products",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    StringBuilder sb = new StringBuilder();

    for (Product product : allProducts) {
sb.append("ID: ").append(product.id).append("\n");
sb.append("Name: ").append(product.name).append("\n");
sb.append("Category: ").append(product.category).append("\n");
sb.append("Price: $").append(String.format("%.2f", product.price)).append("\n");
sb.append("Quantity: ").append(product.stockQuantity).append("\n");
sb.append("Discount: ").append(product.discount ? "Yes" : "No").append("\n");
sb.append("----------------------------------\n");
    }

    JTextArea textArea = new JTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(520, 420));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "All Products - Admin View",
            JOptionPane.INFORMATION_MESSAGE
    );
}
    
    private void showAdminAllOrders() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    OrderDAO orderDAO = new OrderDAO();
    List<String> orders = orderDAO.getAllOrdersForAdmin();

    if (orders.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "No orders found.",
                "All Orders",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    StringBuilder sb = new StringBuilder();

    for (String order : orders) {
        sb.append(order);
    }

    JTextArea textArea = new JTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(650, 430));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "All Orders - Admin View",
            JOptionPane.INFORMATION_MESSAGE
    );
}
    
    
    private void showAdminStatistics() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    AdminStatsDAO statsDAO = new AdminStatsDAO();
    String stats = statsDAO.getStatisticsSummary();

    JTextArea textArea = new JTextArea(stats);
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(620, 430));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "Admin Statistics",
            JOptionPane.INFORMATION_MESSAGE
    );
}
    
    
    
    
private void showAdminSellingRequests() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    SellingRequestDAO sellingRequestDAO = new SellingRequestDAO();
    List<SellingRequestInfo> requests = sellingRequestDAO.getAllPendingRequests();

    if (requests.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "No pending selling requests.",
                "Selling Requests",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    JPanel requestsPanel = new JPanel();
    requestsPanel.setLayout(new BoxLayout(requestsPanel, BoxLayout.Y_AXIS));
    requestsPanel.setBackground(Color.WHITE);
    requestsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    for (SellingRequestInfo request : requests) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel info = new JLabel(
                "<html>"
                        + "<b>Request ID:</b> " + request.requestId + "<br/>"
                        + "<b>Customer:</b> " + request.customerPhone + "<br/>"
                        + "<b>Product:</b> " + request.productName + "<br/>"
                        + "<b>Price:</b> $" + String.format("%.2f", request.price) + "<br/>"
                        + "<b>Status:</b> " + request.requestStatus + "<br/>"
                        + "<b>Image:</b> " + request.imageUrl
                        + "</html>"
        );

        JButton acceptBtn = buildPrimaryButton("Accept");
        JButton rejectBtn = buildSecondaryButton("Reject");

        acceptBtn.addActionListener(e -> {
            boolean success = sellingRequestDAO.acceptRequest(request);

            if (success) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Request accepted and product added to Used Products.",
                        "Selling Request",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadProductsFromDatabase();
                refreshProducts();

                Window window = SwingUtilities.getWindowAncestor(requestsPanel);
                if (window != null) {
                    window.dispose();
                }

                showAdminSellingRequests();

            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Failed to accept request.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        rejectBtn.addActionListener(e -> {
            boolean success = sellingRequestDAO.rejectRequest(request.requestId);

            if (success) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Request rejected.",
                        "Selling Request",
                        JOptionPane.INFORMATION_MESSAGE
                );

                Window window = SwingUtilities.getWindowAncestor(requestsPanel);
                if (window != null) {
                    window.dispose();
                }

                showAdminSellingRequests();

            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Failed to reject request.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        JPanel buttons = new JPanel(new GridLayout(2, 1, 6, 6));
        buttons.setOpaque(false);
        buttons.add(acceptBtn);
        buttons.add(rejectBtn);

        card.add(info, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.EAST);

        requestsPanel.add(card);
        requestsPanel.add(Box.createVerticalStrut(10));
    }

    JScrollPane scrollPane = new JScrollPane(requestsPanel);
    scrollPane.setPreferredSize(new Dimension(650, 430));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "Pending Selling Requests",
            JOptionPane.INFORMATION_MESSAGE
    );
}
    
    private void openAdminPanel() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

String[] options = {
    "Statistics",
    "View All Products",
    "View All Orders",
    "View Selling Requests",
    "Add Product",
    "Edit Product",
    "Delete Product"
};

    int choice = JOptionPane.showOptionDialog(
            frame,
            "Choose an admin action:",
            "Admin Panel",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
    );

if (choice == 0) {
    showAdminStatistics();
} else if (choice == 1) {
    showAdminAllProducts();
} else if (choice == 2) {
    showAdminAllOrders();
} else if (choice == 3) {
    showAdminSellingRequests();
} else if (choice == 4) {
    openAdminAddProductForm();
} else if (choice == 5) {
    openAdminEditProductForm();
} else if (choice == 6) {
    openAdminDeleteProductForm();
}

    }
    private void openAdminAddProductForm() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

JTextField nameField = new JTextField();
JTextArea descriptionArea = new JTextArea(3, 20);
JTextField priceField = new JTextField();
JTextField quantityField = new JTextField();

    CategoryDAO categoryDAO = new CategoryDAO();
List<String> categories = categoryDAO.getAllCategoryNames();

categories.removeIf(cat ->
        cat.equalsIgnoreCase("Home")
        || cat.equalsIgnoreCase("Used Products")
);

JComboBox<String> categoryCombo = new JComboBox<>(categories.toArray(new String[0]));

    JCheckBox discountCheck = new JCheckBox("On Sale / Discount");

    JLabel imagePathLabel = new JLabel("No image selected");
    JButton browseBtn = new JButton("Browse Image");

    final String[] imagePath = {""};

    browseBtn.addActionListener(e -> {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            imagePath[0] = file.getAbsolutePath();
            imagePathLabel.setText(file.getName());
        }
    });

    JPanel imagePanel = new JPanel(new BorderLayout(6, 0));
    imagePanel.add(browseBtn, BorderLayout.WEST);
    imagePanel.add(imagePathLabel, BorderLayout.CENTER);

    JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
    form.add(new JLabel("Product Name:"));
    form.add(nameField);

    form.add(new JLabel("Description:"));
    form.add(new JScrollPane(descriptionArea));

form.add(new JLabel("Price:"));
form.add(priceField);

form.add(new JLabel("Quantity:"));
form.add(quantityField);

form.add(new JLabel("Category:"));
form.add(categoryCombo);

    form.add(new JLabel("Discount:"));
    form.add(discountCheck);

    form.add(new JLabel("Product Image:"));
    form.add(imagePanel);

    int option = JOptionPane.showConfirmDialog(
            frame,
            form,
            "Add Product",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (option == JOptionPane.OK_OPTION) {
String productName = nameField.getText().trim();
String description = descriptionArea.getText().trim();
String priceText = priceField.getText().trim();
String quantityText = quantityField.getText().trim();
String selectedCategory = String.valueOf(categoryCombo.getSelectedItem());
boolean discount = discountCheck.isSelected();
String img = imagePath[0].trim();

if (productName.isEmpty() || description.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
    JOptionPane.showMessageDialog(
                    frame,
                    "Product name, description, price, and quantity are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double price;

try {
    price = Double.parseDouble(priceText);

    if (price <= 0) {
        throw new NumberFormatException();
    }

} catch (NumberFormatException ex) {
    JOptionPane.showMessageDialog(
            frame,
            "Price must be a valid positive number.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}

int stockQuantity;

try {
    stockQuantity = Integer.parseInt(quantityText);

    if (stockQuantity < 0) {
        throw new NumberFormatException();
    }

} catch (NumberFormatException ex) {
    JOptionPane.showMessageDialog(
            frame,
            "Quantity must be a valid number and cannot be negative.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}
        
        
        

int categoryId = categoryDAO.getCategoryIdByName(selectedCategory);

if (categoryId == -1) {
    JOptionPane.showMessageDialog(
            frame,
            "Invalid category selected.",
            "Category Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}
        ProductDAO productDAO = new ProductDAO();

boolean success = productDAO.addProduct(
        productName,
        description,
        price,
        discount,
        categoryId,
        img,
        stockQuantity
);

        if (success) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Product added successfully.",
                    "Admin Panel",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadProductsFromDatabase();
            refreshProducts();

        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to add product.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
    
   private void openAdminEditProductForm() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    String idText = JOptionPane.showInputDialog(
            frame,
            "Enter Product ID:",
            "Edit Product",
            JOptionPane.PLAIN_MESSAGE
    );

    if (idText == null) {
        return;
    }

    idText = idText.trim();

    if (idText.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "Product ID is required.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int productId;

    try {
        productId = Integer.parseInt(idText);

        if (productId <= 0) {
            throw new NumberFormatException();
        }

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
                frame,
                "Product ID must be a valid number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    ProductDAO productDAO = new ProductDAO();
    Product product = productDAO.getProductById(productId);

    if (product == null) {
        JOptionPane.showMessageDialog(
                frame,
                "No product found with this ID.",
                "Product Not Found",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    JTextField nameField = new JTextField(product.name);

    JTextArea descriptionArea = new JTextArea(3, 20);
    descriptionArea.setText(product.description == null ? "" : product.description);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

JTextField priceField = new JTextField(String.valueOf(product.price));
JTextField quantityField = new JTextField(String.valueOf(product.stockQuantity));

JCheckBox discountCheck = new JCheckBox("On Sale / Discount");
    discountCheck.setSelected(product.discount);

    JLabel imagePathLabel = new JLabel(
            product.imagePath == null || product.imagePath.trim().isEmpty()
                    ? "No image selected"
                    : product.imagePath
    );

    JButton browseBtn = new JButton("Change Image");

    final String[] imagePath = {
            product.imagePath == null ? "" : product.imagePath
    };

    browseBtn.addActionListener(e -> {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            imagePath[0] = file.getAbsolutePath();
            imagePathLabel.setText(file.getName());
        }
    });

    JPanel imagePanel = new JPanel(new BorderLayout(6, 0));
    imagePanel.add(browseBtn, BorderLayout.WEST);
    imagePanel.add(imagePathLabel, BorderLayout.CENTER);

    JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

    form.add(new JLabel("Product ID:"));
    form.add(new JLabel(String.valueOf(product.id)));

    form.add(new JLabel("Product Name:"));
    form.add(nameField);

    form.add(new JLabel("Description:"));
    form.add(new JScrollPane(descriptionArea));

form.add(new JLabel("Price:"));
form.add(priceField);

form.add(new JLabel("Quantity:"));
form.add(quantityField);

form.add(new JLabel("Discount:"));
form.add(discountCheck);

    form.add(new JLabel("Product Image:"));
    form.add(imagePanel);

    int option = JOptionPane.showConfirmDialog(
            frame,
            form,
            "Edit Product",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (option == JOptionPane.OK_OPTION) {
String productName = nameField.getText().trim();
String description = descriptionArea.getText().trim();
String priceText = priceField.getText().trim();
String quantityText = quantityField.getText().trim();
boolean discount = discountCheck.isSelected();
String img = imagePath[0].trim();

if (productName.isEmpty() || description.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
    JOptionPane.showMessageDialog(
                    frame,
                    "Product name, description, price, and quantity are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double price;

        try {
            price = Double.parseDouble(priceText);

            if (price <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Price must be a valid positive number.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        int stockQuantity;

try {
    stockQuantity = Integer.parseInt(quantityText);

    if (stockQuantity < 0) {
        throw new NumberFormatException();
    }

} catch (NumberFormatException ex) {
    JOptionPane.showMessageDialog(
            frame,
            "Quantity must be a valid number and cannot be negative.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}

boolean success = productDAO.updateProduct(
        productId,
        productName,
        description,
        price,
        discount,
        img,
        stockQuantity
);

        if (success) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Product updated successfully.",
                    "Admin Panel",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadProductsFromDatabase();
            refreshProducts();

        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to update product.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
    
    
    private void openAdminDeleteProductForm() {
    if (!isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Access denied. Admin only.",
                "Admin Panel",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    String idText = JOptionPane.showInputDialog(
            frame,
            "Enter Product ID to delete:",
            "Delete Product",
            JOptionPane.WARNING_MESSAGE
    );

    if (idText == null) {
        return;
    }

    idText = idText.trim();

    if (idText.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "Product ID is required.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int productId;

    try {
        productId = Integer.parseInt(idText);

        if (productId <= 0) {
            throw new NumberFormatException();
        }

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
                frame,
                "Product ID must be a valid number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    ProductDAO productDAO = new ProductDAO();
    Product product = productDAO.getProductById(productId);

    if (product == null) {
        JOptionPane.showMessageDialog(
                frame,
                "No product found with this ID.",
                "Product Not Found",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to delete this product?\n\n"
                    + "ID: " + product.id + "\n"
                    + "Name: " + product.name + "\n"
                    + "Price: $" + String.format("%.2f", product.price),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    boolean success = productDAO.deleteProductById(productId);

    if (success) {
        JOptionPane.showMessageDialog(
                frame,
                "Product hidden successfully.\\nIt was removed from carts, wishlists, and pending orders.",
                "Admin Panel",
                JOptionPane.INFORMATION_MESSAGE
        );

        loadProductsFromDatabase();
        refreshProducts();

    } else {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to delete product.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    
    
    private boolean isValidName(String name) {
    return name != null && name.trim().matches("[a-zA-Z ]{3,50}");
}

private boolean isValidPhoneLastDigits(String digits) {
    return digits != null && digits.trim().matches("\\d{7}");
}

private boolean isValidFullPhone(String phone) {
    return phone != null && phone.trim().matches("(059|056)\\d{7}");
}

private boolean isValidPositivePrice(String priceText) {
    if (priceText == null || priceText.trim().isEmpty()) {
        return false;
    }

    try {
        double price = Double.parseDouble(priceText.trim());
        return price > 0;
    } catch (NumberFormatException e) {
        return false;
    }
}

private boolean isValidNonNegativeQuantity(String quantityText) {
    if (quantityText == null || quantityText.trim().isEmpty()) {
        return false;
    }

    try {
        int quantity = Integer.parseInt(quantityText.trim());
        return quantity >= 0;
    } catch (NumberFormatException e) {
        return false;
    }
}
    
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(Color.WHITE);
     header.setBorder(new CompoundBorder(
        new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
        new EmptyBorder(8, 14, 8, 14)
));
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MixMart Online Store");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(193, 34, 45));

        JLabel subtitle = new JLabel("Electronics • Accessories • Used Products • etc...");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(95, 95, 95));

        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(subtitle);

      JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
searchPanel.setOpaque(false);
searchPanel.setPreferredSize(new Dimension(360, 38));

searchField = new JTextField();
searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
searchField.setPreferredSize(new Dimension(260, 38));
searchField.setBackground(new Color(255, 248, 248));
searchField.setBorder(new CompoundBorder(
        new LineBorder(new Color(193, 34, 45), 1, true),
        new EmptyBorder(6, 10, 6, 10)
));

searchField.addKeyListener(new KeyAdapter() {
    @Override
    public void keyReleased(KeyEvent e) {
        refreshProducts();
    }
});

JButton searchButton = buildPrimaryButton("Search");
searchButton.setPreferredSize(new Dimension(90, 38));
searchButton.addActionListener(e -> refreshProducts());

searchPanel.add(searchField, BorderLayout.CENTER);
searchPanel.add(searchButton, BorderLayout.EAST);
JButton accountButton = buildSecondaryButton("My Account");
ordersButton = buildSecondaryButton("Orders");
wishlistButton = buildSecondaryButton("My Wishlist");
cartButton = buildPrimaryButton("Cart");

adminPanelButton = buildPrimaryButton("Admin Panel");
adminPanelButton.setVisible(false);

adminPanelButton.addActionListener(e -> openAdminPanel());

JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
searchWrapper.setOpaque(false);
searchWrapper.add(wishlistButton);
searchWrapper.add(searchPanel);

JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
actionsPanel.setOpaque(false);
        
        cartButton.addActionListener(e -> {
    JOptionPane.showMessageDialog(
            frame,
            buildCartSummary(),
            "Shopping Cart",
            JOptionPane.INFORMATION_MESSAGE
    );
});
        
        wishlistButton.addActionListener(e -> {
    if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first to view your wishlist.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (isAdmin) {
    JOptionPane.showMessageDialog(
            frame,
            "Admin does not use wishlist.",
            "Admin Mode",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}
    
    WishlistDAO wishlistDAO = new WishlistDAO();
    List<Product> wishlistProducts = wishlistDAO.getWishlistProducts(currentUser);

    if (wishlistProducts.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "Your wishlist is empty.",
                "My Wishlist",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    JPanel wishlistPanel = new JPanel();
    wishlistPanel.setLayout(new BoxLayout(wishlistPanel, BoxLayout.Y_AXIS));
    wishlistPanel.setBackground(Color.WHITE);
    wishlistPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    for (Product product : wishlistProducts) {
        JPanel itemCard = new JPanel(new BorderLayout(10, 8));
        itemCard.setBackground(new Color(250, 250, 250));
        itemCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel itemLabel = new JLabel(
                "<html>"
                        + "<b>Product ID:</b> " + product.id + "<br/>"
                        + "<b>Name:</b> " + product.name + "<br/>"
                        + "<b>Category:</b> " + product.category + "<br/>"
                        + "<b>Price:</b> $" + String.format("%.2f", product.price)
                        + "</html>"
        );

        JButton removeBtn = buildSecondaryButton("Remove");

        removeBtn.addActionListener(ev -> {
            boolean success = wishlistDAO.removeFromWishlist(currentUser, product.id);

            if (success) {
                JOptionPane.showMessageDialog(
                        frame,
                        product.name + " removed from wishlist.",
                        "Wishlist",
                        JOptionPane.INFORMATION_MESSAGE
                );

                Window window = SwingUtilities.getWindowAncestor(wishlistPanel);
                if (window != null) {
                    window.dispose();
                }

            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Failed to remove product from wishlist.",
                        "Wishlist Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        itemCard.add(itemLabel, BorderLayout.CENTER);
        itemCard.add(removeBtn, BorderLayout.EAST);

        wishlistPanel.add(itemCard);
        wishlistPanel.add(Box.createVerticalStrut(10));
    }

    JScrollPane scrollPane = new JScrollPane(wishlistPanel);
    scrollPane.setPreferredSize(new Dimension(560, 400));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "My Wishlist",
            JOptionPane.INFORMATION_MESSAGE
    );
});
        
        
 ordersButton.addActionListener(e -> {
    if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first to view your orders.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (isAdmin) {
    JOptionPane.showMessageDialog(
            frame,
            "Admin panel will be used to manage orders and products.",
            "Admin Mode",
            JOptionPane.INFORMATION_MESSAGE
    );
    return;
}
    
    
    OrderDAO orderDAO = new OrderDAO();
    List<OrderInfo> orders = orderDAO.getOrdersByCustomerPhone(currentUser);

    if (orders.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "You do not have any orders yet.",
                "My Orders",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    JPanel ordersPanel = new JPanel();
    ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
    ordersPanel.setBackground(Color.WHITE);
    ordersPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    for (OrderInfo order : orders) {
        JPanel orderCard = new JPanel(new BorderLayout(12, 8));
        orderCard.setBackground(new Color(250, 250, 250));
        orderCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        String orderText =
                "<html>"
                        + "<b>Order ID:</b> " + order.orderId + "<br/>"
                        + "<b>Date:</b> " + order.orderDate + "<br/>"
                        + "<b>Status:</b> " + order.orderStatus + "<br/>"
                        + "<b>Total:</b> $" + String.format("%.2f", order.totalAmount) + "<br/>"
                        + "<b>Receiver:</b> " + order.receiverName + "<br/>"
                        + "<b>Receiver Phone:</b> " + order.receiverPhone + "<br/>"
                        + "<b>Address:</b> " + order.city + ", " + order.street + "<br/>"
                        + "<b>Payment:</b> " + order.paymentMethod + " / " + order.paymentStatus
                        + "</html>";

        JLabel orderLabel = new JLabel(orderText);
        orderLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JButton confirmBtn = buildPrimaryButton("Confirm");

       confirmBtn.addActionListener(ev -> {

    JComboBox<String> companyCombo = new JComboBox<>(new String[]{
            "Aramex - $5 - Delivery within 5 days",
            "Shibly - $20 - Delivery within 2 days",
            "Tayyar - $10 - Delivery within 3 days"
    });

   

    JPanel shipmentPanel = new JPanel(new BorderLayout(10, 10));
shipmentPanel.add(new JLabel("Choose shipping company:"), BorderLayout.NORTH);
    shipmentPanel.add(companyCombo, BorderLayout.CENTER);

    int shipmentResult = JOptionPane.showConfirmDialog(
            frame,
            shipmentPanel,
            "Shipping",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (shipmentResult != JOptionPane.OK_OPTION) {
        return;
    }

    String selectedCompany = String.valueOf(companyCombo.getSelectedItem());

    String shippingCompany;
    int deliveryDays;

    if (selectedCompany.startsWith("Aramex")) {
        shippingCompany = "Aramex";
        deliveryDays = 5;
    } else if (selectedCompany.startsWith("Shibly")) {
        shippingCompany = "Shibly";
        deliveryDays = 2;
    } else {
        shippingCompany = "Tayyar";
        deliveryDays = 3;
    }

    if ("Card".equalsIgnoreCase(order.paymentMethod)) {
        JTextField cardNumberField = new JTextField();
        JTextField cardHolderField = new JTextField();
        JTextField expiryField = new JTextField();
        JPasswordField cvvField = new JPasswordField();

        JPanel cardPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        cardPanel.add(new JLabel("Card Number:"));
        cardPanel.add(cardNumberField);
        cardPanel.add(new JLabel("Card Holder:"));
        cardPanel.add(cardHolderField);
        cardPanel.add(new JLabel("Expiry Date (MM/YY):"));
        cardPanel.add(expiryField);
        cardPanel.add(new JLabel("CVV:"));
        cardPanel.add(cvvField);

        int cardResult = JOptionPane.showConfirmDialog(
                frame,
                cardPanel,
                "Card Payment Information",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (cardResult != JOptionPane.OK_OPTION) {
            return;
        }

        String cardNumber = cardNumberField.getText().trim().replace(" ", "");
        String cardHolder = cardHolderField.getText().trim();
        String expiry = expiryField.getText().trim();
        String cvv = new String(cvvField.getPassword()).trim();

        if (cardNumber.isEmpty() || cardHolder.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
    JOptionPane.showMessageDialog(
            frame,
            "All card fields are required.",
            "Card Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (!cardNumber.matches("\\d{12,19}")) {
    JOptionPane.showMessageDialog(
            frame,
            "Card number must contain digits only and should be between 12 and 19 digits.",
            "Card Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (!cardHolder.matches("[a-zA-Z ]{3,50}")) {
    JOptionPane.showMessageDialog(
            frame,
            "Card holder name must contain letters only and be at least 3 characters.",
            "Card Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
    JOptionPane.showMessageDialog(
            frame,
            "Expiry date must be in MM/YY format.",
            "Card Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

String[] expiryParts = expiry.split("/");
int month = Integer.parseInt(expiryParts[0]);
int year = Integer.parseInt("20" + expiryParts[1]);

java.time.YearMonth cardExpiry = java.time.YearMonth.of(year, month);
java.time.YearMonth currentMonth = java.time.YearMonth.now();

if (cardExpiry.isBefore(currentMonth)) {
    JOptionPane.showMessageDialog(
            frame,
            "Card is expired.",
            "Card Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (!cvv.matches("\\d{3,4}")) {
    JOptionPane.showMessageDialog(
            frame,
            "CVV must be 3 or 4 digits.",
            "Card Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}
    }

    int confirm = JOptionPane.showConfirmDialog(
            frame,
            "After confirming this order, the payment/shipping information will be saved.\n"
                    + "You cannot return this order to the cart.\n\n"
                    + "Do you want to confirm?",
            "Final Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

String trackingNumber = "TRK" + order.orderId + System.currentTimeMillis();

OrderDAO confirmOrderDAO = new OrderDAO();

boolean success = confirmOrderDAO.confirmOrderPaymentAndShipment(
        order.orderId,
        order.paymentMethod,
        shippingCompany,
        trackingNumber,
        deliveryDays
);

    if (success) {
        JOptionPane.showMessageDialog(
                frame,
                "Order confirmed successfully.\n"
                        + "Shipping Company: " + shippingCompany + "\n"
                        + "Tracking Number: " + trackingNumber,
                "Confirmed",
                JOptionPane.INFORMATION_MESSAGE
        );

        confirmBtn.setEnabled(false);
        confirmBtn.setText("Confirmed");

        OrderDAO firstOrderDAO = new OrderDAO();

if (firstOrderDAO.isFirstOrderForCustomer(currentUser, order.orderId)
        && firstConfirmPopupShownOrders.add(order.orderId)) {

    JOptionPane.showMessageDialog(
            frame,
            "Thank you for confirming your first order!\n"
                    + "Please don't forget to rate our website and Your the Products you orderd.",
            "First Order Confirmed",
            JOptionPane.INFORMATION_MESSAGE
    );
}
        
        
    } else {
        JOptionPane.showMessageDialog(
                
                frame,
                "Failed to confirm order.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
});

        
       if ("Shipped".equalsIgnoreCase(order.orderStatus)) {
    confirmBtn.setEnabled(false);
    confirmBtn.setText("Confirmed");
} else if ("Delivered".equalsIgnoreCase(order.orderStatus)) {
    confirmBtn.setEnabled(false);
    confirmBtn.setText("Delivered");
} 
       
JPanel buttonPanel = new JPanel();
buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
buttonPanel.setOpaque(false);

buttonPanel.add(confirmBtn);

if ("Shipped".equalsIgnoreCase(order.orderStatus)
        || "Delivered".equalsIgnoreCase(order.orderStatus)) {

    ReviewDAO reviewDAO = new ReviewDAO();
    OrderDAO firstOrderDAO = new OrderDAO();

    if ("Delivered".equalsIgnoreCase(order.orderStatus)
            && firstOrderDAO.isFirstOrderForCustomer(currentUser, order.orderId)
            && !reviewDAO.hasWebsiteReview(currentUser)
            && deliveredReviewPopupShownOrders.add(order.orderId)) {

        JOptionPane.showMessageDialog(
                frame,
                "Your first order has been delivered!\n"
                        + "Please don't forget to rate our website.",
                "Rate Our Website",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}


        orderCard.add(orderLabel, BorderLayout.CENTER);
        orderCard.add(buttonPanel, BorderLayout.EAST);

        ordersPanel.add(orderCard);
        ordersPanel.add(Box.createVerticalStrut(10));
    }

    JScrollPane scrollPane = new JScrollPane(ordersPanel);
    scrollPane.setPreferredSize(new Dimension(650, 450));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "My Orders",
            JOptionPane.INFORMATION_MESSAGE
    );
});

        cartCountLabel = new JLabel("0 items");
        cartCountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cartCountLabel.setForeground(new Color(193, 34, 45));

        accountButton.addActionListener(e -> {
    if (loggedIn) {
        int ch = JOptionPane.showConfirmDialog(
                frame,
                "You are logged in as " + currentUser + ".\nDo you want to logout?",
                "Account",
                JOptionPane.YES_NO_OPTION
        );

        if (ch == JOptionPane.YES_OPTION) {
loggedIn = false;
isAdmin = false;
currentUser = "";
cartItems.clear();
refreshCart();
accountButton.setText("My Account");

updateAdminView();
refreshWebsiteReviewButtonText();

updateAdminView();
            if (statusLabel != null) {
                statusLabel.setText("Logged out");
            }

            refreshProducts();
            refreshWebsiteReviewButtonText();

        }

        return;
    }

    String[] options = {"Login", "Register"};

    int choice = JOptionPane.showOptionDialog(
            frame,
            "Choose an option:",
            "My Account",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
    );

    CustomerDAO customerDAO = new CustomerDAO();

    if (choice == 0) {
        JTextField phoneField = new JTextField();
        JPasswordField passField = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(
                frame,
                panel,
                "Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
      String phone = phoneField.getText().trim().replace(" ", "");          
      String password = new String(passField.getPassword()).trim();

           if (phone.isEmpty() || password.isEmpty()) {
    JOptionPane.showMessageDialog(
            frame,
            "Phone number and password are required.",
            "Login Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}
           
           if (!isValidFullPhone(phone)) {
    JOptionPane.showMessageDialog(
            frame,
            "Phone number must start with 059 or 056 and contain 10 digits.",
            "Login Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (phone.equals("0593089647") && password.equals("3089")) {
    loggedIn = true;
    isAdmin = true;
    currentUser = phone;

    accountButton.setText("Admin");

    updateAdminView();
    refreshWebsiteReviewButtonText();

    if (statusLabel != null) {
        statusLabel.setText("Logged in as Admin");
    }

    refreshProducts();

    JOptionPane.showMessageDialog(
            frame,
            "Admin login successful.",
            "Admin",
            JOptionPane.INFORMATION_MESSAGE
    );

    return;
}

boolean success = customerDAO.loginCustomer(phone, password);

if (success) {
    loggedIn = true;
    currentUser = phone;

    String customerName = customerDAO.getCustomerNameByPhone(phone);
    String firstName = customerName.split(" ")[0];

    accountButton.setText("Hi, " + firstName);

    loadCartFromDatabase();

    refreshWebsiteReviewButtonText();

    if (statusLabel != null) {
        statusLabel.setText("Logged in as " + currentUser);
    }

    JOptionPane.showMessageDialog(
            frame,
            "Login successful.",
            "Login",
            JOptionPane.INFORMATION_MESSAGE
    );
} else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Invalid phone number or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

} else if (choice == 1) {
    JTextField nameField = new JTextField();

    JComboBox<String> phonePrefixCombo = new JComboBox<>(new String[]{"059", "056"});
    JTextField phoneLastDigitsField = new JTextField();

JComboBox<String> cityCombo = new JComboBox<>(new String[]{
        "Nablus",
        "Tulkarm",
        "Ramallah",
        "Jenin",
        "Qalqilya",
        "Hebron",
        "Bethlehem",
        "Jericho",
        "Jerusalem",
        "Salfit",
        "Tubas"
});

    JPasswordField passField = new JPasswordField();

    JPanel phonePanel = new JPanel(new BorderLayout(6, 0));
    phonePanel.add(phonePrefixCombo, BorderLayout.WEST);
    phonePanel.add(phoneLastDigitsField, BorderLayout.CENTER);

    JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
    panel.add(new JLabel("Name:"));
    panel.add(nameField);

    panel.add(new JLabel("Phone Number:"));
    panel.add(phonePanel);

    panel.add(new JLabel("City:"));
    panel.add(cityCombo);

    panel.add(new JLabel("Password:"));
    panel.add(passField);

    int result = JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Register",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        String name = nameField.getText().trim();

        String phonePrefix = String.valueOf(phonePrefixCombo.getSelectedItem());
        String phoneLastDigits = phoneLastDigitsField.getText().trim().replace(" ", "");

        String phone = phonePrefix + phoneLastDigits;

        String city = String.valueOf(cityCombo.getSelectedItem());
        String password = new String(passField.getPassword()).trim();

       if (name.isEmpty() || phoneLastDigits.isEmpty() || city.isEmpty() || password.isEmpty()) {
    JOptionPane.showMessageDialog(
            frame,
            "All fields are required.",
            "Register Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (!isValidName(name)) {
    JOptionPane.showMessageDialog(
            frame,
            "Name must contain letters only and be between 3 and 50 characters.",
            "Register Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (!isValidPhoneLastDigits(phoneLastDigits)) {
    JOptionPane.showMessageDialog(
                    frame,
                    "Phone number must contain exactly 7 digits after the prefix.",
                    "Register Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean success = customerDAO.registerCustomer(phone, name, city, password);

        if (success) {
            loggedIn = true;
            currentUser = phone;

            String firstName = name.split(" ")[0];
            accountButton.setText("Hi, " + firstName);

            loadCartFromDatabase();

            refreshWebsiteReviewButtonText();

            if (statusLabel != null) {
                statusLabel.setText("Registered and logged in as " + currentUser);
            }

            JOptionPane.showMessageDialog(
                    frame,
                    "Account created successfully.",
                    "Register",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Registration failed. Phone number may already exist.",
                    "Register Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
});

actionsPanel.add(accountButton);
actionsPanel.add(adminPanelButton);
actionsPanel.add(ordersButton);
actionsPanel.add(cartButton);
actionsPanel.add(cartCountLabel);


        header.add(brandPanel, BorderLayout.WEST);
header.add(searchWrapper, BorderLayout.CENTER);
header.add(actionsPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildNavBar() {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBackground(new Color(245, 245, 245));
    wrapper.setBorder(new EmptyBorder(0, 14, 0, 14));
    wrapper.setPreferredSize(new Dimension(100, 48));

    JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    nav.setBackground(new Color(245, 245, 245));

    String[] items = {
        "Home",
        "Computer Parts",
        "Computer Accessories",
        "Electronics & Arduino",
        "Used Products",
        "Mobile Phones & Tablets",
        "Mobile & Tablet Accessories",
        "Clothes",
        "Perfumes & Watches",
        "Boots & Socks",
        "Home Electronics",
        "Home Accessories",
        "Books",
        "Kids Toys",
        "Garden Accessories",
        "Other Products"
    };

    for (String item : items) {
        JButton btn = new JButton(item);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

btn.addActionListener(e -> {
    selectedCategory = item;
    refreshProducts();
});

        nav.add(btn);
    }

    JScrollPane scrollPane = new JScrollPane(nav);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(null);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
    scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
    scrollPane.getViewport().setBackground(new Color(245, 245, 245));

    wrapper.add(scrollPane, BorderLayout.CENTER);
    return wrapper;
}

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(new Color(246, 247, 249));
        content.setBorder(new EmptyBorder(14, 14, 14, 14));

        content.add(buildLeftSidebar(), BorderLayout.WEST);
        content.add(buildCenterPanel(), BorderLayout.CENTER);
        content.add(buildCartSidebar(), BorderLayout.EAST);
        return content;
    }

    private JPanel buildLeftSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(255, 100));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(246, 247, 249));

        side.add(buildSideCard("Filters", buildFiltersPanel()));
        side.add(Box.createVerticalStrut(12));

sellingsCardWrapper = buildSideCard("Support Chat", buildSellingsPanel());
side.add(sellingsCardWrapper);

        return side;
    }
private void openCustomerChatPanel() {
    ChatDAO chatDAO = new ChatDAO();

    JTextArea chatArea = new JTextArea(16, 42);
    chatArea.setEditable(false);
    chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

    JTextField messageField = new JTextField();

    JButton sendBtn = buildPrimaryButton("Send");
    JButton refreshBtn = buildSecondaryButton("Refresh");

    Runnable loadMessages = () -> {
        List<ChatMessageInfo> messages = chatDAO.getMessagesByCustomer(currentUser);

        StringBuilder sb = new StringBuilder();

        for (ChatMessageInfo msg : messages) {
            sb.append("[")
                    .append(msg.sentAt)
                    .append("] ");

String displayText = msg.messageText;

if (displayText != null && displayText.startsWith("[ALL] ")) {
    displayText = displayText.substring(6);
}

if ("Admin".equalsIgnoreCase(msg.senderType)) {
    if (msg.messageText != null && msg.messageText.startsWith("[ALL] ")) {
        sb.append("Admin to all: ");
    } else {
        sb.append("Admin: ");
    }
} else {
    sb.append("You: ");
}

sb.append(displayText).append("\n\n");
        }

        chatArea.setText(sb.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    };

    sendBtn.addActionListener(e -> {
        
        String text = messageField.getText().trim();

        if (text.isEmpty()) {
            return;
        }

        boolean success = chatDAO.sendMessage(currentUser, "Customer", text);

        if (success) {
            messageField.setText("");
            loadMessages.run();
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to send message.",
                    "Chat Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    });

    refreshBtn.addActionListener(e -> loadMessages.run());

    JPanel bottom = new JPanel(new BorderLayout(8, 0));
    bottom.add(messageField, BorderLayout.CENTER);
    bottom.add(sendBtn, BorderLayout.EAST);

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
    panel.add(bottom, BorderLayout.SOUTH);
    panel.add(refreshBtn, BorderLayout.NORTH);

    loadMessages.run();

    JOptionPane.showMessageDialog(
            frame,
            panel,
            "Chat with Admin",
            JOptionPane.PLAIN_MESSAGE
    );
}
private void openAdminChatPanel() {
ChatDAO chatDAO = new ChatDAO();
List<String> customers = chatDAO.getAllCustomers();
if (customers.isEmpty()) {
    JOptionPane.showMessageDialog(
            frame,
            "No customer messages yet.",
            "Admin Chat",
            JOptionPane.INFORMATION_MESSAGE
    );
    return;
}

customers.add(0, "All");

JComboBox<String> customerCombo = new JComboBox<>(customers.toArray(new String[0]));

    JTextArea chatArea = new JTextArea(16, 42);
    chatArea.setEditable(false);
    chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

    JTextField messageField = new JTextField();

    JButton sendBtn = buildPrimaryButton("Reply");
    JButton refreshBtn = buildSecondaryButton("Refresh");
    
    
Runnable loadMessages = () -> {
    String selectedCustomer = String.valueOf(customerCombo.getSelectedItem());

    List<ChatMessageInfo> messages;

    if ("All".equalsIgnoreCase(selectedCustomer)) {
        messages = chatDAO.getAllMessages();
    } else {
        messages = chatDAO.getMessagesByCustomer(selectedCustomer);
    }

    StringBuilder sb = new StringBuilder();
    Set<String> shownBroadcasts = new HashSet<>();
    for (ChatMessageInfo msg : messages) {
    String displayText = msg.messageText;

    boolean isBroadcast =
            displayText != null && displayText.startsWith("[ALL] ");

    if (isBroadcast) {
        displayText = displayText.substring(6);
    }

    if ("All".equalsIgnoreCase(selectedCustomer) && isBroadcast) {
        String broadcastKey = msg.sentAt + "|" + displayText;

        if (shownBroadcasts.contains(broadcastKey)) {
            continue;
        }

        shownBroadcasts.add(broadcastKey);

        sb.append("[")
                .append(msg.sentAt)
                .append("] ");

        sb.append("Admin to all: ");
        sb.append(displayText).append("\n\n");
        continue;
    }

    sb.append("[")
            .append(msg.sentAt)
            .append("] ");

    if ("Admin".equalsIgnoreCase(msg.senderType)) {
        if (isBroadcast) {
            sb.append("Admin to all: ");
        } else {
            sb.append("Admin to ")
                    .append(msg.customerPhone)
                    .append(": ");
        }
    } else {
        sb.append("Customer ")
                .append(msg.customerPhone)
                .append(": ");
    }

    sb.append(displayText).append("\n\n");
}

    chatArea.setText(sb.toString());
    chatArea.setCaretPosition(chatArea.getDocument().getLength());
};

    customerCombo.addActionListener(e -> loadMessages.run());

sendBtn.addActionListener(e -> {
    String selectedCustomer = String.valueOf(customerCombo.getSelectedItem());
    String text = messageField.getText().trim();

    if (text.isEmpty()) {
        return;
    }

    boolean success;

    if ("All".equalsIgnoreCase(selectedCustomer)) {
        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "This message will be sent to all customers.\nDo you want to continue?",
                "Send to All",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        success = chatDAO.sendMessageToAllCustomers(text);

    } else {
        success = chatDAO.sendMessage(selectedCustomer, "Admin", text);
    }

    if (success) {
        messageField.setText("");
        loadMessages.run();
    } else {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to send message.",
                "Chat Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
});

    refreshBtn.addActionListener(e -> loadMessages.run());

    JPanel top = new JPanel(new BorderLayout(8, 0));
    top.add(new JLabel("Customer:"), BorderLayout.WEST);
    top.add(customerCombo, BorderLayout.CENTER);
    top.add(refreshBtn, BorderLayout.EAST);

    JPanel bottom = new JPanel(new BorderLayout(8, 0));
    bottom.add(messageField, BorderLayout.CENTER);
    bottom.add(sendBtn, BorderLayout.EAST);

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.add(top, BorderLayout.NORTH);
    panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
    panel.add(bottom, BorderLayout.SOUTH);

    loadMessages.run();

    JOptionPane.showMessageDialog(
            frame,
            panel,
            "Admin Chat",
            JOptionPane.PLAIN_MESSAGE
    );
}

private JPanel buildFiltersPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    sortPriceCombo = new JComboBox<>(new String[]{
            "Default",
            "Highest Rating",
            "In Stock First",
            "Price: Low to High",
            "Price: High to Low"
    });

    sortPriceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    sortPriceCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
    sortPriceCombo.addActionListener(e -> refreshProducts());

    discountedOnlyCheck = new JCheckBox("Only discounted (SALE)");
    discountedOnlyCheck.setOpaque(false);
    discountedOnlyCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
    discountedOnlyCheck.addActionListener(e -> refreshProducts());

    JButton resetBtn = buildSecondaryButton("Reset Filters");
    resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    resetBtn.addActionListener(e -> {
        selectedCategory = "Home";
        sortPriceCombo.setSelectedItem("Default");
        discountedOnlyCheck.setSelected(false);
        refreshProducts();
    });

panel.add(sortPriceCombo);
panel.add(Box.createVerticalStrut(12));

panel.add(discountedOnlyCheck);
panel.add(Box.createVerticalStrut(12));

panel.add(resetBtn);
panel.add(Box.createVerticalStrut(18));

panel.add(new JSeparator());
panel.add(Box.createVerticalStrut(14));

JLabel sellTitle = new JLabel("Sell your product");
sellTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
sellTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

JLabel sellInfo = new JLabel("<html>Submit your own item<br/>for admin approval.</html>");
sellInfo.setFont(new Font("SansSerif", Font.PLAIN, 13));
sellInfo.setForeground(new Color(70, 70, 70));
sellInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

JButton showItemBtn = buildPrimaryButton("Show your item");
showItemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
showItemBtn.addActionListener(e -> openSellItemForm());

JButton myRequestsBtn = buildSecondaryButton("My Selling Requests");
myRequestsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
myRequestsBtn.addActionListener(e -> showMySellingRequests());

panel.add(sellTitle);
panel.add(Box.createVerticalStrut(8));
panel.add(sellInfo);
panel.add(Box.createVerticalStrut(12));
panel.add(showItemBtn);
panel.add(Box.createVerticalStrut(8));
panel.add(myRequestsBtn);

return panel;
}

private JPanel buildSellingsPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JLabel info = new JLabel("<html>Need help?<br/>Chat with support.</html>");
    info.setFont(new Font("SansSerif", Font.PLAIN, 13));
    info.setForeground(new Color(70, 70, 70));
    info.setAlignmentX(Component.LEFT_ALIGNMENT);

    JButton chatBtn = buildPrimaryButton("Open Chat");
    chatBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

    chatBtn.addActionListener(e -> {
        if (!loggedIn) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please log in first to use chat.",
                    "Chat",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (isAdmin) {
            openAdminChatPanel();
        } else {
            openCustomerChatPanel();
        }
    });

    panel.add(info);
    panel.add(Box.createVerticalStrut(12));
    panel.add(chatBtn);

    return panel;
}



private void openSellItemForm() {
    if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first to submit a selling request.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Admin does not use selling requests.",
                "Admin Mode",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    JTextField phoneField = new JTextField(currentUser);
    phoneField.setEditable(false);

    // باقي الكود خليه زي ما هو

    JTextField productNameField = new JTextField();
    JTextField priceField = new JTextField();

    JLabel imagePathLabel = new JLabel("No image selected");
    JButton browseBtn = new JButton("Browse Image");

    final String[] imagePath = {""};

    browseBtn.addActionListener(ev -> {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            imagePath[0] = file.getAbsolutePath();
            imagePathLabel.setText(file.getName());
        }
    });

    JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

    form.add(new JLabel("Customer Phone:"));
    form.add(phoneField);

    form.add(new JLabel("Product Name:"));
    form.add(productNameField);

    form.add(new JLabel("Product Price:"));
    form.add(priceField);

    form.add(new JLabel("Product Image:"));

    JPanel imageRow = new JPanel(new BorderLayout(6, 0));
    imageRow.add(browseBtn, BorderLayout.WEST);
    imageRow.add(imagePathLabel, BorderLayout.CENTER);
    form.add(imageRow);

    int option = JOptionPane.showConfirmDialog(
            frame,
            form,
            "Selling Request",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (option == JOptionPane.OK_OPTION) {
        String customerPhone = phoneField.getText().trim();
        String productName = productNameField.getText().trim();
        String priceText = priceField.getText().trim();
        String img = imagePath[0].trim();

        if (productName.isEmpty() || priceText.isEmpty() || img.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please enter product name, price, and choose an image.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double priceValue;

        try {
            priceValue = Double.parseDouble(priceText);

            if (priceValue <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Price must be a valid positive number.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String description = "Used product submitted by customer " + customerPhone;

        SellingRequestDAO sellingRequestDAO = new SellingRequestDAO();

        boolean success = sellingRequestDAO.submitSellingRequest(
                customerPhone,
                productName,
                description,
                priceValue,
                img
        );

        if (success) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Selling request submitted successfully.\nYour request is now pending admin approval.",
                    "Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (statusLabel != null) {
                statusLabel.setText("Selling request submitted by " + customerPhone);
            }

        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to submit selling request.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

private void showMySellingRequests() {
    if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first to view your selling requests.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Admin does not use selling requests.",
                "Admin Mode",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    SellingRequestDAO sellingRequestDAO = new SellingRequestDAO();
    List<SellingRequestInfo> requests =
            sellingRequestDAO.getRequestsByCustomerPhone(currentUser);

    if (requests.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "You do not have any selling requests yet.",
                "My Selling Requests",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    JPanel requestsPanel = new JPanel();
    requestsPanel.setLayout(new BoxLayout(requestsPanel, BoxLayout.Y_AXIS));
    requestsPanel.setBackground(Color.WHITE);
    requestsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    for (SellingRequestInfo request : requests) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel info = new JLabel(
                "<html>"
                        + "<b>Request ID:</b> " + request.requestId + "<br/>"
                        + "<b>Product:</b> " + request.productName + "<br/>"
                        + "<b>Price:</b> $" + String.format("%.2f", request.price) + "<br/>"
                        + "<b>Status:</b> " + request.requestStatus + "<br/>"
                        + "<b>Submitted At:</b> " + request.submittedAt + "<br/>"
                        + "<b>Image:</b> " + request.imageUrl
                        + "</html>"
        );

        JPanel buttons = new JPanel(new GridLayout(2, 1, 6, 6));
        buttons.setOpaque(false);

        if ("Pending".equalsIgnoreCase(request.requestStatus)) {
            JButton editBtn = buildPrimaryButton("Edit");
            JButton deleteBtn = buildSecondaryButton("Delete");

            editBtn.addActionListener(e -> {
                openPendingSellingRequestEditForm(request);

                Window window = SwingUtilities.getWindowAncestor(requestsPanel);
                if (window != null) {
                    window.dispose();
                }

                showMySellingRequests();
            });

            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to delete this selling request?",
                        "Delete Selling Request",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean success = sellingRequestDAO.deletePendingRequest(
                        request.requestId,
                        currentUser
                );

                if (success) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Selling request deleted successfully.",
                            "Selling Request",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    Window window = SwingUtilities.getWindowAncestor(requestsPanel);
                    if (window != null) {
                        window.dispose();
                    }

                    showMySellingRequests();

                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Failed to delete selling request.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            buttons.add(editBtn);
            buttons.add(deleteBtn);

        } else {
            JLabel lockedLabel = new JLabel("No actions");
            lockedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            lockedLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            lockedLabel.setForeground(new Color(120, 120, 120));
            buttons.add(lockedLabel);
        }

        card.add(info, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.EAST);

        requestsPanel.add(card);
        requestsPanel.add(Box.createVerticalStrut(10));
    }

    JScrollPane scrollPane = new JScrollPane(requestsPanel);
    scrollPane.setPreferredSize(new Dimension(650, 430));
    scrollPane.getVerticalScrollBar().setUnitIncrement(14);

    JOptionPane.showMessageDialog(
            frame,
            scrollPane,
            "My Selling Requests",
            JOptionPane.INFORMATION_MESSAGE
    );
}
    
    
    private void openPendingSellingRequestEditForm(SellingRequestInfo request) {

    if (!"Pending".equalsIgnoreCase(request.requestStatus)) {
        JOptionPane.showMessageDialog(
                frame,
                "Only pending selling requests can be edited.",
                "Edit Not Allowed",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    JTextField productNameField = new JTextField(request.productName);

    JTextArea descriptionArea = new JTextArea(3, 22);
    descriptionArea.setText(request.description == null ? "" : request.description);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    JTextField priceField = new JTextField(String.valueOf(request.price));

    JLabel imagePathLabel = new JLabel(
            request.imageUrl == null || request.imageUrl.trim().isEmpty()
                    ? "No image selected"
                    : request.imageUrl
    );

    JButton browseBtn = new JButton("Change Image");

    final String[] imagePath = {
            request.imageUrl == null ? "" : request.imageUrl
    };

    browseBtn.addActionListener(e -> {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            imagePath[0] = file.getAbsolutePath();
            imagePathLabel.setText(file.getName());
        }
    });

    JPanel imagePanel = new JPanel(new BorderLayout(6, 0));
    imagePanel.add(browseBtn, BorderLayout.WEST);
    imagePanel.add(imagePathLabel, BorderLayout.CENTER);

    JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

    form.add(new JLabel("Request ID:"));
    form.add(new JLabel(String.valueOf(request.requestId)));

    form.add(new JLabel("Product Name:"));
    form.add(productNameField);

    form.add(new JLabel("Description:"));
    form.add(new JScrollPane(descriptionArea));

    form.add(new JLabel("Price:"));
    form.add(priceField);

    form.add(new JLabel("Image:"));
    form.add(imagePanel);

    int option = JOptionPane.showConfirmDialog(
            frame,
            form,
            "Edit Pending Selling Request",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (option != JOptionPane.OK_OPTION) {
        return;
    }

    String productName = productNameField.getText().trim();
    String description = descriptionArea.getText().trim();
    String priceText = priceField.getText().trim();
    String img = imagePath[0].trim();

    if (productName.isEmpty() || description.isEmpty() || priceText.isEmpty() || img.isEmpty()) {
        JOptionPane.showMessageDialog(
                frame,
                "Product name, description, price, and image are required.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    double price;

    try {
        price = Double.parseDouble(priceText);

        if (price <= 0) {
            throw new NumberFormatException();
        }

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
                frame,
                "Price must be a valid positive number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    SellingRequestDAO sellingRequestDAO = new SellingRequestDAO();

    boolean success = sellingRequestDAO.updatePendingRequest(
            request.requestId,
            currentUser,
            productName,
            description,
            price,
            img
    );

    if (success) {
        JOptionPane.showMessageDialog(
                frame,
                "Selling request updated successfully.",
                "Selling Request",
                JOptionPane.INFORMATION_MESSAGE
        );
    } else {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to update selling request.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    
    
    
    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);

        center.add(buildHeroBanner(), BorderLayout.NORTH);

        productsGrid = new JPanel(new GridLayout(0, 3, 14, 14));
        productsGrid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(productsGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(246, 247, 249));

        center.add(scrollPane, BorderLayout.CENTER);
        return center;
    }
    
    
    
private void showStorePolicies() {
    String policiesText =
            "<html>"
            + "<div style='width:430px; font-family:SansSerif; font-size:11px;'>"
            + "<h2 style='color:#c1222d;'>Store Policies</h2>"

            + "<b>1. Order Confirmation</b><br>"
            + "All orders must be confirmed by the customer before shipping.<br><br>"

            + "<b>2. Delivery</b><br>"
            + "Delivery time depends on the selected shipping company. Customers can choose from three delivery options.<br><br>"

            + "<b>3. Payment</b><br>"
            + "Customers can pay using Cash on Delivery or Card Payment.<br><br>"

            + "<b>4. Product Reviews</b><br>"
            + "Customers can rate products only after the order is delivered.<br><br>"

            + "<b>5. Website Reviews</b><br>"
            + "Customers can rate the website and edit their rating later.<br><br>"

            + "<b>6. Selling Requests</b><br>"
            + "Customers can submit used products for sale. Pending requests can be edited or deleted before admin approval.<br><br>"

            + "<b>7. Product Availability</b><br>"
            + "Stock quantity and product availability may change depending on orders and admin updates.<br><br>"

            + "<b>8. Store Management</b><br>"
            + "The store can update products, prices, stock, and policies when needed."

            + "</div>"
            + "</html>";

    JLabel policiesLabel = new JLabel(policiesText);

    JOptionPane.showMessageDialog(
            frame,
            policiesLabel,
            "Store Policies",
            JOptionPane.INFORMATION_MESSAGE
    );
}
    private JPanel buildHeroBanner() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(new Color(193, 34, 45));
hero.setBorder(new CompoundBorder(
        new LineBorder(new Color(180, 20, 35), 1, true),
        new EmptyBorder(26, 34, 26, 34)
));

JPanel left = new JPanel();
left.setOpaque(false);
left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
left.setBorder(new EmptyBorder(0, 10, 0, 20));

JLabel big = new JLabel("Welcome to MixMart Online Store");
big.setFont(new Font("SansSerif", Font.BOLD, 32));
big.setForeground(Color.WHITE);
big.setAlignmentX(Component.LEFT_ALIGNMENT);

JLabel mid = new JLabel(
        "<html><div style='width:760px;'>"
                + "MixMart Online Store is a complete shopping platform for electronics, accessories, home products, "
                + "fashion items, books, toys, and used products."
                + "</div></html>"
);
mid.setFont(new Font("SansSerif", Font.PLAIN, 17));
mid.setForeground(new Color(255, 238, 238));
mid.setAlignmentX(Component.LEFT_ALIGNMENT);

JLabel small = new JLabel(
        "<html><div style='width:760px;'>"
                + "Customers can browse products, search and filter items, add products to the cart or wishlist, "
                + "confirm orders, choose flexible delivery, rate products and the website, and submit used items for sale."
                + "</div></html>"
);
small.setFont(new Font("SansSerif", Font.PLAIN, 16));
small.setForeground(new Color(255, 238, 238));
small.setAlignmentX(Component.LEFT_ALIGNMENT);




JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
buttons.setOpaque(false);
buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
buttons.setBorder(new EmptyBorder(4, 0, 0, 0));

seeWebsiteReviewsButton = buildLightButton("See Website Reviews");
websiteReviewButton = buildSecondaryDarkButton("Rate Website");
seeWebsiteReviewsButton.setPreferredSize(new Dimension(170, 36));
websiteReviewButton.setPreferredSize(new Dimension(145, 36));

seeWebsiteReviewsButton.addActionListener(e -> {
    showAdminWebsiteReviews();
});

websiteReviewButton.addActionListener(e -> {
    if (isAdmin) {
        showAdminProductReviews();
    } else {
        openWebsiteReviewForm();
        refreshWebsiteReviewButtonText();
    }
});

buttons.add(seeWebsiteReviewsButton);
buttons.add(websiteReviewButton);

refreshWebsiteReviewButtonText();

left.add(big);
left.add(Box.createVerticalStrut(12));
left.add(mid);
left.add(Box.createVerticalStrut(8));
left.add(small);
left.add(Box.createVerticalStrut(22));
left.add(buttons);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
right.add(buildStatCard(
        "Flexible Delivery Options",
        "Choose from 3 shipping companies to get the delivery option that suits you best.."
));

right.add(Box.createVerticalStrut(10));

right.add(buildStatCard(
        "Easy Payment Methods",
        "Pay comfortably using Cash on Delivery or Card Payment."
));

right.add(Box.createVerticalStrut(10));

JPanel policiesCard = buildStatCard(
        "Our Policies",
        "Click here to View our store rules, delivery terms, and payment policies."
);

policiesCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
policiesCard.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        showStorePolicies();
    }
});

right.add(policiesCard);
        hero.add(left, BorderLayout.CENTER);
        hero.add(right, BorderLayout.EAST);
        return hero;
    }

    
    
    private boolean validateCartStockBeforeCheckout() {
    if (cartItems.isEmpty()) {
        return true;
    }

    ProductDAO productDAO = new ProductDAO();
    CartDAO cartDAO = new CartDAO();

    StringBuilder warning = new StringBuilder();
    List<CartItem> itemsToRemove = new ArrayList<>();

    for (CartItem item : new ArrayList<>(cartItems)) {
        Product latestProduct = productDAO.getProductById(item.product.id);

        if (latestProduct == null || latestProduct.stockQuantity <= 0) {
            itemsToRemove.add(item);

            warning.append("- ")
                    .append(item.product.name)
                    .append(" was removed because it is out of stock.\n");

            continue;
        }

        if (item.quantity > latestProduct.stockQuantity) {
            int oldQuantity = item.quantity;
            item.quantity = latestProduct.stockQuantity;
            item.product.stockQuantity = latestProduct.stockQuantity;

            cartDAO.updateCartItemQuantity(
                    currentUser,
                    item.product.id,
                    latestProduct.stockQuantity
            );

            warning.append("- ")
                    .append(item.product.name)
                    .append(" quantity changed from ")
                    .append(oldQuantity)
                    .append(" to ")
                    .append(latestProduct.stockQuantity)
                    .append(" because stock changed.\n");
        }
    }

    for (CartItem item : itemsToRemove) {
        cartDAO.removeFromCart(currentUser, item.product.id);
        cartItems.remove(item);
    }

    if (warning.length() > 0) {
        refreshCart();
        loadProductsFromDatabase();
        refreshProducts();

        JOptionPane.showMessageDialog(
                frame,
                "Some cart items were updated before checkout:\n\n" + warning,
                "Stock Updated",
                JOptionPane.WARNING_MESSAGE
        );

        return false;
    }

    return true;
}
    
    
    private JPanel buildCartSidebar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        cartSidebarWrapper = wrapper;
        
        wrapper.setPreferredSize(new Dimension(265, 100));
        wrapper.setOpaque(false);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.WHITE);
        outer.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 225, 225), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Shopping Cart");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(cartPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(new Color(80, 80, 80));

        JButton checkoutBtn = buildPrimaryButton("Checkout");
      checkoutBtn.addActionListener(e -> {
    
          if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first before checkout.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

          if (isAdmin) {
    JOptionPane.showMessageDialog(
            frame,
            "Admin cannot checkout orders.",
            "Admin Mode",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}
          
          
 if (cartItems.isEmpty()) {
    JOptionPane.showMessageDialog(
            frame,
            "Your cart is empty. Add products before checkout.",
            "Checkout",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}

if (!validateCartStockBeforeCheckout()) {
    return;
}

if (cartItems.isEmpty()) {
    JOptionPane.showMessageDialog(
            frame,
            "Your cart is empty after updating stock.",
            "Checkout",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}

JTextField receiverNameField = new JTextField();
    JTextField receiverPhoneField = new JTextField();
JComboBox<String> cityCombo = new JComboBox<>(new String[]{
        "Nablus",
        "Tulkarm",
        "Ramallah",
        "Jenin",
        "Qalqilya",
        "Hebron",
        "Bethlehem",
        "Jericho",
        "Jerusalem",
        "Salfit",
        "Tubas"
});
 
    JTextField streetField = new JTextField();
    JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Card"});

    JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
    panel.add(new JLabel("Receiver Name:"));
    panel.add(receiverNameField);
    panel.add(new JLabel("Receiver Phone:"));
    panel.add(receiverPhoneField);
panel.add(new JLabel("City:"));
panel.add(cityCombo);
    panel.add(new JLabel("Street:"));
    panel.add(streetField);
    panel.add(new JLabel("Payment Method:"));
    panel.add(paymentMethodCombo);

    int result = JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Checkout Information",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        String receiverName = receiverNameField.getText().trim();
String receiverPhone = receiverPhoneField.getText().trim().replace(" ", "");
String city = String.valueOf(cityCombo.getSelectedItem());
String street = streetField.getText().trim();
        String paymentMethod = String.valueOf(paymentMethodCombo.getSelectedItem());

        if (receiverName.isEmpty() || receiverPhone.isEmpty() || city.isEmpty() || street.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "All fields are required.",
                    "Checkout Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        
        if (!isValidName(receiverName)) {
    JOptionPane.showMessageDialog(
            frame,
            "Receiver name must contain letters only and be between 3 and 50 characters.",
            "Checkout Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

if (!isValidFullPhone(receiverPhone)) {
    JOptionPane.showMessageDialog(
            frame,
            "Receiver phone must start with 059 or 056 and contain 10 digits.",
            "Checkout Error",
            JOptionPane.ERROR_MESSAGE
    );
    return;
}

        OrderDAO orderDAO = new OrderDAO();
        boolean success = orderDAO.createOrder(
                currentUser,
                receiverName,
                receiverPhone,
                city,
                street,
                paymentMethod,
                cartItems
        );

if (success) {
    cartItems.clear();
    refreshCart();

    loadProductsFromDatabase();
    refreshProducts();

    JOptionPane.showMessageDialog(
            frame,
            "Order created successfully.",
            "Checkout",
            JOptionPane.INFORMATION_MESSAGE
    );
    
            if (statusLabel != null) {
                statusLabel.setText("Order created successfully");
            }

        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to create order.",
                    "Checkout Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
});

        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(checkoutBtn, BorderLayout.SOUTH);

        outer.add(title, BorderLayout.NORTH);
        outer.add(scroll, BorderLayout.CENTER);
        outer.add(bottom, BorderLayout.SOUTH);
        wrapper.add(outer, BorderLayout.CENTER);
        return wrapper;
    }

   private JPanel buildFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(new Color(24, 28, 37));
    footer.setBorder(new EmptyBorder(12, 18, 12, 18));

    JLabel movingLabel = new JLabel("Discounts up to 30% on selected products!");
    movingLabel.setForeground(Color.WHITE);
    movingLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

    JPanel movingPanel = new JPanel(null);
    movingPanel.setBackground(new Color(24, 28, 37));
    movingPanel.setPreferredSize(new Dimension(100, 24));

    movingPanel.add(movingLabel);

javax.swing.Timer timer = new javax.swing.Timer(20, null);

timer.addActionListener(e -> {
    int panelWidth = movingPanel.getWidth();
    int labelWidth = movingLabel.getPreferredSize().width;

    if (movingLabel.getX() > panelWidth) {
        movingLabel.setLocation(-labelWidth, 3);
    } else {
        movingLabel.setLocation(movingLabel.getX() + 2, 3);
    }
});

    movingPanel.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            int labelWidth = movingLabel.getPreferredSize().width;
            movingLabel.setSize(labelWidth, 22);
movingLabel.setLocation(-labelWidth, 3);
        }
    });

    timer.start();

    footer.add(movingPanel, BorderLayout.CENTER);
    return footer;
}

    private JPanel buildSideCard(String title, JPanel body) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 225, 225), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));

        card.add(label, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatCard(String value, String label) {
        JPanel stat = new JPanel();
        stat.setLayout(new BoxLayout(stat, BoxLayout.Y_AXIS));
        stat.setBackground(new Color(255, 245, 245));
        stat.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 150, 160), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel v = new JLabel(value);
        v.setFont(new Font("Dialog", Font.BOLD, 20));
        v.setForeground(new Color(193, 34, 45));
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        v.setHorizontalAlignment(SwingConstants.CENTER);
//        v.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
v.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel l = new JLabel("<html><div style='text-align:center; width:200px;'>" + label + "</div></html>");
        l.setFont(new Font("Dialog", Font.PLAIN, 13));
        l.setForeground(new Color(90, 90, 90));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.CENTER);
//        l.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
l.setFont(new Font("SansSerif", Font.PLAIN, 13));

        stat.add(v);
        stat.add(Box.createVerticalStrut(6));
        stat.add(l);
        return stat;
    }

    private JButton buildPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(193, 34, 45));
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        return button;
    }

    private JButton buildSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(60, 60, 60));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return button;
    }

    private JButton buildLightButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(193, 34, 45));
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        return button;
    }

    private JButton buildSecondaryDarkButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(122, 18, 28));
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        return button;
    }
    private void loadProductsFromDatabase() {
    allProducts.clear();

    ProductDAO productDAO = new ProductDAO();
    allProducts.addAll(productDAO.getAllProducts());

    System.out.println("Products loaded from database: " + allProducts.size());
}

    private void seedProducts() {
        allProducts.add(new Product(201, "ASUS Gaming Mouse", "Gaming Gear", 25.00, true, 4.7));
        allProducts.add(new Product(202, "Redragon Mechanical Keyboard", "Gaming Gear", 55.00, false, 4.5));
        allProducts.add(new Product(203, "Laptop Stand", "Accessories", 18.00, false, 4.2));
        allProducts.add(new Product(204, "Wireless Headphones", "Audio", 90.00, true, 4.8));
        allProducts.add(new Product(205, "USB-C Hub", "Accessories", 35.00, false, 4.4));
        allProducts.add(new Product(206, "Used Dell Monitor", "Used Products", 120.00, false, 4.1));
        allProducts.add(new Product(207, "Office Chair", "Accessories", 150.00, true, 4.6));
        allProducts.add(new Product(208, "Used Tablet", "Used Products", 200.00, false, 4.0));
        allProducts.add(new Product(209, "Bluetooth Speaker", "Audio", 45.00, false, 4.3));
        allProducts.add(new Product(210, "Wireless Charger", "Accessories", 28.00, false, 4.4));
        allProducts.add(new Product(211, "Desk Organizer", "Accessories", 22.00, false, 4.1));
        allProducts.add(new Product(212, "Used Gaming Chair", "Used Products", 180.00, true, 4.5));
    }

    private void refreshProducts() {
        if (productsGrid == null) return;
        productsGrid.removeAll();

        String keyword = (searchField == null) ? "" : searchField.getText().trim().toLowerCase();
String sortMode = (sortPriceCombo == null) ? "Default" : String.valueOf(sortPriceCombo.getSelectedItem());
boolean discountedOnly = discountedOnlyCheck != null && discountedOnlyCheck.isSelected();

List<Product> filtered = allProducts.stream()
        .filter(p -> selectedCategory.equals("Home") || p.category.equalsIgnoreCase(selectedCategory))
        .filter(p -> !discountedOnly || p.discount)
        .filter(p -> keyword.isEmpty()
                || p.name.toLowerCase().contains(keyword)
                || p.category.toLowerCase().contains(keyword)
                || String.valueOf(p.id).contains(keyword))
        .collect(Collectors.toList());

if ("Highest Rating".equals(sortMode)) {
    ReviewDAO reviewDAO = new ReviewDAO();

    filtered.sort((a, b) -> {
        Double ratingA = reviewDAO.getAverageProductRating(a.id);
        Double ratingB = reviewDAO.getAverageProductRating(b.id);

        double rA = ratingA == null ? 0.0 : ratingA;
        double rB = ratingB == null ? 0.0 : ratingB;

        return Double.compare(rB, rA);
    });

} else if ("In Stock First".equals(sortMode)) {
    filtered.sort((a, b) -> Integer.compare(b.stockQuantity, a.stockQuantity));

} else if ("Price: Low to High".equals(sortMode)) {
    filtered.sort(Comparator.comparingDouble(this::getFinalPrice));

} else if ("Price: High to Low".equals(sortMode)) {
    filtered.sort((a, b) -> Double.compare(getFinalPrice(b), getFinalPrice(a)));
}

        for (Product product : filtered) {
            productsGrid.add(buildProductCard(product));
        }

        if (statusLabel != null) {
            statusLabel.setText(filtered.size() + " product(s) shown");
        }

        productsGrid.revalidate();
        productsGrid.repaint();
    }

    
    private double getDiscountRate(Product product) {
    if (!product.discount) {
        return 0.0;
    }

    if (product.price < 50) {
        return 0.1;   // 10%
    } else if (product.price <= 200) {
        return 0.20;   // 20%
    } else {
        return 0.30;   // 30%
    }
}

private double getFinalPrice(Product product) {
    double discountRate = getDiscountRate(product);
    return product.price - (product.price * discountRate);
}
    

private JLabel buildProductImageLabel(Product product) {
    JLabel imageLabel = new JLabel();
    imageLabel.setPreferredSize(new Dimension(180, 120));
    imageLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
    imageLabel.setMinimumSize(new Dimension(180, 120));
    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    imageLabel.setVerticalAlignment(SwingConstants.CENTER);
    imageLabel.setOpaque(true);
    imageLabel.setBackground(new Color(245, 245, 245));
    imageLabel.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

    String imagePath = product.imagePath;

    if (imagePath == null || imagePath.trim().isEmpty()) {
        imageLabel.setText("No Image");
        imageLabel.setForeground(new Color(120, 120, 120));
        return imageLabel;
    }

    System.out.println("Image path for product " + product.id + ": " + imagePath);
    imagePath = imagePath.trim();

    try {
        ImageIcon icon = new ImageIcon(imagePath);

        if (icon.getIconWidth() <= 0) {
            imageLabel.setText("No Image");
            imageLabel.setForeground(new Color(120, 120, 120));
            return imageLabel;
        }

        Image originalImage = icon.getImage();

        int boxWidth = 180;
        int boxHeight = 120;

        int imgWidth = icon.getIconWidth();
        int imgHeight = icon.getIconHeight();

        double scale = Math.min(
                (double) boxWidth / imgWidth,
                (double) boxHeight / imgHeight
        );

        int newWidth = (int) (imgWidth * scale);
        int newHeight = (int) (imgHeight * scale);

        Image scaledImage = originalImage.getScaledInstance(
                newWidth,
                newHeight,
                Image.SCALE_SMOOTH
        );

        imageLabel.setIcon(new ImageIcon(scaledImage));

    } catch (Exception e) {
        imageLabel.setText("No Image");
        imageLabel.setForeground(new Color(120, 120, 120));
    }

    return imageLabel;
}

    
    private JPanel buildProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 225, 225), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel imageBox = new JPanel(new BorderLayout());
imageBox.setPreferredSize(new Dimension(230, 120));
imageBox.setBackground(new Color(245, 247, 250));
imageBox.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));

String imgPath = product.imagePath;

if (imgPath != null && !imgPath.trim().isEmpty()) {
    imgPath = imgPath.trim();

    
    File imgFile = new File(imgPath);

    if (imgFile.exists()) {
        ImageIcon icon = new ImageIcon(imgPath);

        int imgWidth = icon.getIconWidth();
        int imgHeight = icon.getIconHeight();

        if (imgWidth > 0 && imgHeight > 0) {
            int boxWidth = 230;
            int boxHeight = 120;

            double scale = Math.min(
                    (double) boxWidth / imgWidth,
                    (double) boxHeight / imgHeight
            );

            int newWidth = (int) (imgWidth * scale);
            int newHeight = (int) (imgHeight * scale);

            Image scaled = icon.getImage().getScaledInstance(
                    newWidth,
                    newHeight,
                    Image.SCALE_SMOOTH
            );

            JLabel imageLabel = new JLabel(new ImageIcon(scaled));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            imageLabel.setToolTipText("Click to preview");

            String finalImgPath = imgPath;

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showImagePreview(finalImgPath, product.name);
                }
            });

            imageBox.add(imageLabel, BorderLayout.CENTER);
        } else {
            JLabel fallback = new JLabel("Invalid image", SwingConstants.CENTER);
            fallback.setForeground(new Color(140, 80, 80));
            imageBox.add(fallback, BorderLayout.CENTER);
        }

    } else {
        JLabel fallback = new JLabel("Image not found", SwingConstants.CENTER);
        fallback.setForeground(new Color(140, 80, 80));
        imageBox.add(fallback, BorderLayout.CENTER);
    }

} else {
    JLabel imageText = new JLabel("No image", SwingConstants.CENTER);
    imageText.setForeground(new Color(120, 120, 120));
    imageBox.add(imageText, BorderLayout.CENTER);
}

        JPanel topBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topBadges.setOpaque(false);
        topBadges.add(buildBadge("#" + product.id, new Color(60, 60, 60), new Color(240, 240, 240)));
        topBadges.add(buildBadge(product.category, new Color(193, 34, 45), new Color(255, 240, 242)));
        if (product.discount) {
            topBadges.add(buildBadge("SALE", new Color(22, 111, 52), new Color(230, 247, 236)));
        }

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(product.name);
        name.setFont(new Font("SansSerif", Font.BOLD, 17));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

JLabel price;

if (product.discount) {
    double finalPrice = getFinalPrice(product);
    double discountRate = getDiscountRate(product) * 100;

    price = new JLabel(
            "<html>"
                    + "<span style='text-decoration:line-through; color:gray;'>$"
                    + String.format("%.2f", product.price)
                    + "</span>"
                    + " <b>$" + String.format("%.2f", finalPrice) + "</b>"
                    + " <span style='color:#c1222d;'>("
                    + String.format("%.0f", discountRate)
                    + "% OFF)</span>"
                    + "</html>"
    );
} else {
    price = new JLabel("$" + String.format("%.2f", product.price));
}
price.setFont(new Font("SansSerif", Font.BOLD, 18));
        price.setForeground(new Color(193, 34, 45));
        price.setAlignmentX(Component.LEFT_ALIGNMENT);

        ReviewDAO reviewDAO = new ReviewDAO();
        Double avgRating = reviewDAO.getAverageProductRating(product.id);

JLabel rating;

if (avgRating == null) {
    rating = new JLabel("Rating: No Rating");
} else {
    rating = new JLabel(String.format("Rating: %.1f / 5", avgRating));
}
        rating.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rating.setForeground(new Color(90, 90, 90));
        rating.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel stockLabel;

if (product.stockQuantity <= 0) {
    stockLabel = new JLabel("Stock: Out of stock");
    stockLabel.setForeground(new Color(193, 34, 45));
} else {
    stockLabel = new JLabel("Stock: " + product.stockQuantity + " available");
    stockLabel.setForeground(new Color(70, 120, 70));
}

stockLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

String productDescription = product.description;

if (productDescription == null || productDescription.trim().isEmpty()) {
    productDescription = "No description available.";
}

JLabel desc = new JLabel(
        "<html><div style='width:220px;'>"
                + productDescription
                + "</div></html>"
);

        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(new Color(95, 95, 95));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(topBadges);
        center.add(Box.createVerticalStrut(10));
        center.add(name);
        center.add(Box.createVerticalStrut(8));
center.add(topBadges);
center.add(Box.createVerticalStrut(10));
center.add(name);
center.add(Box.createVerticalStrut(8));
center.add(price);
center.add(Box.createVerticalStrut(6));
center.add(rating);
center.add(Box.createVerticalStrut(6));
center.add(stockLabel);
center.add(Box.createVerticalStrut(6));
center.add(desc);

        boolean canDelete = loggedIn
                && product.userSubmitted
                && product.seller != null
                && product.seller.equals(currentUser);

JPanel actions = new JPanel(new GridLayout(1, isAdmin ? 1 : 3, 8, 0));
actions.setOpaque(false);

JButton wishlistBtn = buildSecondaryButton("Wishlist");
JButton ratingsBtn = buildSecondaryButton("Ratings");
JButton addToCart = buildPrimaryButton("Add to Cart");

if (product.stockQuantity <= 0) {
    addToCart.setEnabled(false);
    addToCart.setText("Out of Stock");
}

wishlistBtn.addActionListener(e -> {
    if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first before adding products to wishlist.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    WishlistDAO wishlistDAO = new WishlistDAO();
    boolean success = wishlistDAO.addToWishlist(currentUser, product.id);

    if (success) {
        JOptionPane.showMessageDialog(
                frame,
                product.name + " added to wishlist.",
                "Wishlist",
                JOptionPane.INFORMATION_MESSAGE
        );

        if (statusLabel != null) {
            statusLabel.setText(product.name + " added to wishlist");
        }

    } else {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to add product to wishlist.",
                "Wishlist Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
});

ratingsBtn.addActionListener(e -> {
    if (isAdmin) {
        showProductRatings(product);
        return;
    }

    String[] options = {"View Ratings", "Rate Product"};

    int choice = JOptionPane.showOptionDialog(
            frame,
            "Choose an option for " + product.name + ":",
            "Product Ratings",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
    );

    if (choice == 0) {
        showProductRatings(product);
    } else if (choice == 1) {
        openProductReviewForm(product);
    }
});



addToCart.addActionListener(e -> addToCart(product));

if (isAdmin) {
    actions.add(ratingsBtn);
} else {
    actions.add(wishlistBtn);
    actions.add(ratingsBtn);
    actions.add(addToCart);
}


        if (canDelete) {
            JButton deleteBtn = new JButton("Delete My Item");
            deleteBtn.setFocusPainted(false);
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.setBackground(new Color(120, 20, 28));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorder(new EmptyBorder(10, 12, 10, 12));

            deleteBtn.addActionListener(e -> {
                int ch = JOptionPane.showConfirmDialog(
                        frame,
                        "Delete this item: " + product.name + " ?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );
                if (ch == JOptionPane.YES_OPTION) {
                    allProducts.remove(product);
                    cartItems.removeIf(ci -> ci.product.id == product.id);
                    refreshProducts();
                    refreshCart();
                    if (statusLabel != null) {
                        statusLabel.setText("Item deleted: " + product.name);
                    }
                }
            });

            actions.add(deleteBtn);
        }

card.add(imageBox, BorderLayout.NORTH);
card.add(center, BorderLayout.CENTER);
card.add(actions, BorderLayout.SOUTH);

return card;
    }

    private void showImagePreview(String imagePath, String title) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No image available.", "Preview", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        
        File imgFile = new File(imagePath);
        if (!imgFile.exists()) {
            JOptionPane.showMessageDialog(frame, "Image file not found:\n" + imagePath, "Preview Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ImageIcon original = new ImageIcon(imagePath);
        int w = original.getIconWidth();
        int h = original.getIconHeight();

        int maxW = 700;
        int maxH = 500;

        double scale = Math.min((double) maxW / w, (double) maxH / h);
        if (scale > 1) scale = 1;

        int newW = (int) (w * scale);
        int newH = (int) (h * scale);

        Image scaled = original.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaled));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane pane = new JScrollPane(imageLabel);
        pane.setPreferredSize(new Dimension(newW + 20, newH + 20));

        JOptionPane.showMessageDialog(
                frame,
                pane,
                "Image Preview - " + title,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private JLabel buildBadge(String text, Color fg, Color bg) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(4, 7, 4, 7));
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        return badge;
    }

    private void showProductDetails(Product product) {
        String imageInfo = (product.imagePath == null || product.imagePath.isEmpty())
                ? "No image path" : product.imagePath;

        String contactInfo = (product.contact == null || product.contact.isEmpty())
                ? "-" : product.contact;

        String sellerInfo = (product.seller == null || product.seller.isEmpty())
                ? "-" : product.seller;

        String message = "Product ID: " + product.id
                + "\nName: " + product.name
                + "\nCategory: " + product.category
                + "\nPrice: $" + String.format("%.2f", product.price)
                + "\nRating: " + product.rating
                + "\nSeller: " + sellerInfo
                + "\nContact: " + contactInfo
                + "\nImage: " + imageInfo;
        JOptionPane.showMessageDialog(frame, message, "Product Details", JOptionPane.INFORMATION_MESSAGE);
    }

private void addToCart(Product product) {

    if (!loggedIn) {
        JOptionPane.showMessageDialog(
                frame,
                "Error: You must log in first before adding products to cart.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (isAdmin) {
        JOptionPane.showMessageDialog(
                frame,
                "Admin cannot add products to cart.",
                "Admin Mode",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    ProductDAO productDAO = new ProductDAO();
    Product latestProduct = productDAO.getProductById(product.id);

    if (latestProduct == null) {
        JOptionPane.showMessageDialog(
                frame,
                "This product is no longer available.",
                "Stock Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (latestProduct.stockQuantity <= 0) {
        JOptionPane.showMessageDialog(
                frame,
                "This product is out of stock.",
                "Stock Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    Optional<CartItem> existing = cartItems.stream()
            .filter(item -> item.product.id == product.id)
            .findFirst();

    if (existing.isPresent()) {
        if (existing.get().quantity + 1 > latestProduct.stockQuantity) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Not enough stock.\nAvailable quantity: " + latestProduct.stockQuantity,
                    "Stock Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
    }

    CartDAO cartDAO = new CartDAO();
    boolean success = cartDAO.addToCart(currentUser, product.id);

    if (!success) {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to add product to cart.",
                "Cart Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (existing.isPresent()) {
        existing.get().quantity++;
        existing.get().product.stockQuantity = latestProduct.stockQuantity;
    } else {
        latestProduct.rating = product.rating;
        cartItems.add(new CartItem(latestProduct, 1));
    }

    if (statusLabel != null) {
        statusLabel.setText(product.name + " added to cart");
    }

    refreshCart();
}

    
    private void loadCartFromDatabase() {
    if (!loggedIn || currentUser.isEmpty()) {
        return;
    }

    CartDAO cartDAO = new CartDAO();

    cartItems.clear();
    cartItems.addAll(cartDAO.getCartItems(currentUser));

    refreshCart();

    if (statusLabel != null) {
        statusLabel.setText("Cart loaded from database");
    }
}
    
    private void refreshCart() {
        if (cartPanel == null) return;
        cartPanel.removeAll();

        if (cartItems.isEmpty()) {
            JLabel empty = new JLabel("Cart is empty");
            empty.setForeground(new Color(100, 100, 100));
            empty.setBorder(new EmptyBorder(8, 4, 8, 4));
            cartPanel.add(empty);
        } else {
            for (CartItem item : cartItems) {
                cartPanel.add(buildCartItemCard(item));
                cartPanel.add(Box.createVerticalStrut(8));
            }
        }

        int count = cartItems.stream().mapToInt(i -> i.quantity).sum();
double total = cartItems.stream()
        .mapToDouble(i -> getFinalPrice(i.product) * i.quantity)
        .sum();

        if (cartCountLabel != null) cartCountLabel.setText(count + " items");

        JLabel totalLabel = new JLabel(String.format("Total: $%.2f", total));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        totalLabel.setBorder(new EmptyBorder(10, 4, 0, 4));
        cartPanel.add(totalLabel);

        cartPanel.revalidate();
        cartPanel.repaint();
    }

    private JPanel buildCartItemCard(CartItem item) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel name = new JLabel("<html><b>" + item.product.name + "</b><br/>Qty: " + item.quantity + "</html>");
double lineTotal = getFinalPrice(item.product) * item.quantity;
JLabel price = new JLabel(String.format("$%.2f", lineTotal));
price.setForeground(new Color(193, 34, 45));
        price.setFont(new Font("SansSerif", Font.BOLD, 14));

       JButton minus = new JButton("-");
minus.setFocusPainted(false);
minus.setMargin(new Insets(2, 8, 2, 8));

JButton plus = new JButton("+");
plus.setFocusPainted(false);
plus.setMargin(new Insets(2, 8, 2, 8));

JButton remove = new JButton("X");
remove.setFocusPainted(false);
remove.setMargin(new Insets(2, 8, 2, 8));

minus.addActionListener(e -> {
    if (item.quantity <= 1) {
        JOptionPane.showMessageDialog(
                frame,
                "Quantity cannot be less than 1. Use X to remove the item.",
                "Quantity Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int newQuantity = item.quantity - 1;

    CartDAO cartDAO = new CartDAO();
    boolean success = cartDAO.updateCartItemQuantity(currentUser, item.product.id, newQuantity);

    if (!success) {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to update quantity in database.",
                "Cart Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    item.quantity = newQuantity;
    refreshCart();
});

plus.addActionListener(e -> {
    ProductDAO productDAO = new ProductDAO();
    Product latestProduct = productDAO.getProductById(item.product.id);

    if (latestProduct == null) {
        JOptionPane.showMessageDialog(
                frame,
                "This product is no longer available.",
                "Stock Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (item.quantity + 1 > latestProduct.stockQuantity) {
        JOptionPane.showMessageDialog(
                frame,
                "Not enough stock.\nAvailable quantity: " + latestProduct.stockQuantity,
                "Stock Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int newQuantity = item.quantity + 1;

    CartDAO cartDAO = new CartDAO();
    boolean success = cartDAO.updateCartItemQuantity(currentUser, item.product.id, newQuantity);

    if (!success) {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to update quantity in database.",
                "Cart Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    item.quantity = newQuantity;
    refreshCart();
});

remove.addActionListener(e -> {
    CartDAO cartDAO = new CartDAO();
    boolean success = cartDAO.removeFromCart(currentUser, item.product.id);

    if (!success) {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to remove product from database.",
                "Cart Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    cartItems.remove(item);

    if (statusLabel != null) {
        statusLabel.setText(item.product.name + " removed");
    }

    refreshCart();
});
        card.add(name, BorderLayout.CENTER);
        card.add(price, BorderLayout.SOUTH);
JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 2, 2));
buttonsPanel.setOpaque(false);
buttonsPanel.add(plus);
buttonsPanel.add(minus);
buttonsPanel.add(remove);

card.add(buttonsPanel, BorderLayout.EAST);        return card;
    }

    private String buildCartSummary() {
        if (cartItems.isEmpty()) return "Cart is empty.";

        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (CartItem item : cartItems) {
double lineTotal = item.quantity * getFinalPrice(item.product);
total += lineTotal;
            sb.append(item.product.name)
                    .append("  x")
                    .append(item.quantity)
                    .append("  ->  $")
                    .append(String.format("%.2f", lineTotal))
                    .append("\n");
        }
        sb.append("\nTotal: $").append(String.format("%.2f", total));
        return sb.toString();
    }
    
    

    static class Product {
        int id;
        String name;
        String category;
        double price;
        boolean discount;
        double rating;
        int stockQuantity;
        String seller;
        String contact;
        String imagePath;
        String description;
        boolean userSubmitted;

        Product(int id, String name, String category, double price, boolean discount, double rating) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.price = price;
    this.discount = discount;
    this.rating = rating;
    this.stockQuantity = 0;
}
    }

    static class CartItem {
        Product product;
        int quantity;

        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
