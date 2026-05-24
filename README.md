# Mix Mart Online

Mix Mart Online is a Java Swing and MySQL desktop shopping system developed for a Database Systems course project.

The project demonstrates database design, ERD relationships, normalization, SQL operations, JDBC connectivity, and GUI-based shopping system management.

---

# Team Members

| Name | Student ID |
|------|------|
| Melad Sam | 12323283 |
| Emad Ataout | 12322222 |

---

# Technologies Used

- Java
- Java Swing
- MySQL
- JDBC
- NetBeans IDE
- GitHub

---

# Main Features

## Customer Features

- Register and login
- Browse products by category
- Search and sort products
- Shopping cart and wishlist
- Checkout and order placement
- Payment and shipment tracking
- Product and website reviews
- Submit used products for sale
- Chat with admin

## Admin Features

- Product management
- Order management
- Store statistics
- Review management
- Selling request management
- Customer support chat
- Broadcast messages to all customers

---

# Database

Database Name:

```sql
onlinestore

# Main Tables:

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
How to Run the Project
1. Clone the repository
git clone https://github.com/MeeladSam/MixMart_Online_Store_DataBase_Project.git
2. Open MySQL Workbench

Run the SQL(MySQL) script:

onlinestore.sql

The script will:

Create the database
Create all tables
Insert sample data and products
3. Update Database Connection

Open:

DBConnection.java

Update:

private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
4. Product Images

To display product images correctly:

Create this folder:
C:/Photos
Copy the provided images into that folder.
5. Run the Project

Open the project in NetBeans and run:

LaptopStoreGUI.java
Admin Account
Phone: 0593089647
Password: 3089
Validation Rules
```

# The system validates:

- Phone number format
- Customer names
- Product prices
- Product stock quantity
- Card payment information
- Cart quantity vs stock
 
# Project Assets

- The repository may include:
- Java Source Code
- SQL Database Script
- Project Report
- ER Diagram
- Normalization Document
- PowerPoint Presentation
- Product Images

# Notes:

- Customers cannot access admin features.
- Admin cannot use customer-only functions.
- Product reviews are allowed only after delivery.
- Chat messages are stored in the database.

