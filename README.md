# Mix Mart Online

Mix Mart Online is a Java Swing and MySQL online shopping store project developed for a Database course.  
The project demonstrates database design, SQL operations, ERD relationships, normalization, and Java database connectivity using JDBC.

---

## Team Members

| Name | Student ID |
|---|---|
| Milad Sam | 12323283 |
| Emad Ataout | 12322222 |

---

## Project Description

Mix Mart Online is a desktop shopping system that allows customers to browse products, add items to cart, place orders, review products, submit used products for sale, and chat with the admin.

The admin can manage products, view orders, check statistics, handle selling requests, view reviews, and communicate with customers.

---

## Technologies Used

- Java
- Java Swing
- MySQL
- JDBC
- NetBeans IDE
- GitHub

---

## Main Features

### Customer Features

- Register and login
- Browse products by category
- Search and sort products
- Add products to cart
- Add products to wishlist
- Checkout and place orders
- Choose payment and delivery options
- View order history
- Rate the website
- Review products after delivery
- Submit used products for sale
- Chat with admin

### Admin Features

- Admin login
- View all products
- Add, edit, and hide products
- View all orders
- View system statistics
- View website and product reviews
- Accept or reject selling requests
- Chat with customers
- Send messages to all customers

---

## Database

Database name:

```sql
online_shoping_store_database

Main tables:

Customer
Category
Product
ShoppingCart
ShoppingCartItem
Orders
OrderItem
Payment
Shipment
Review
Wishlist
SellingRequest
ChatMessage

The database is designed using primary keys, foreign keys, and normalized tables to reduce repeated data and organize relationships between customers, products, orders, carts, reviews, and chat messages.

How to Run the Project
Open MySQL Workbench or any MySQL tool.
Run the SQL script included with the project to create and fill the database.
Make sure the database name is:
online_shoping_store_database
Open the Java project in NetBeans.
Open DBConnection.java and update the database connection information:
private static final String URL =
        "jdbc:mysql://localhost:3306/online_shoping_store_database";

private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
Make sure the MySQL JDBC driver is added to the project libraries.
Run the main file:
LaptopStoreGUI.java
Product Images

Product images are loaded from paths stored in the database.

The expected images folder is:

C:/Photos

Example:

C:/Photos/Screenshot_1.png

If the images are not available on the same path, the program may show an image error or placeholder.

Admin Account

Use this account to log in as admin:

Phone: 0593089647
Password: 3089
Validation Rules

The system includes basic validation, such as:

Phone number must start with 059 or 056.
Customer name must contain letters only.
Product price must be positive.
Product quantity cannot be negative.
Cart quantity cannot exceed available stock.
Card payment information must be valid.
Submitted Files

The project repository may include:

Java source code
SQL database script
Project report
ERD diagram
Normalization document
PowerPoint presentation
Product images folder or image references
Notes
Customers cannot access the admin panel.
Admin cannot use customer-only features such as cart and wishlist.
Product reviews are allowed only after the order is delivered.
Pending selling requests can be edited or deleted by the customer.
Accepted selling requests are added as used products.
Chat messages are stored in the database.
Conclusion

Mix Mart Online is a practical database project that connects a Java Swing GUI with a MySQL database.
It applies important database concepts such as ERD design, normalization, SQL operations, primary keys, foreign keys, and JDBC connection.
