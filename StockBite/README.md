# Stock Bite – School Canteen Management System

Java (Swing) + MySQL (XAMPP). Modules: Product Management, Inventory Management,
Point of Sale (cash only), Transaction Records, Receipt Generation.

## 1. Requirements
- JDK 17 or newer
- VS Code + **Extension Pack for Java**
- XAMPP (only the **MySQL** module is needed)
- MySQL Connector/J JAR (e.g. `mysql-connector-j-8.4.0.jar`)
  https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar

## 2. Set up the database
1. Open XAMPP Control Panel -> **Start** MySQL (and Apache if you want phpMyAdmin).
2. Open http://localhost/phpmyadmin -> **Import** -> choose `database/stockbite.sql` -> Go.
   (Creates `stockbite_db` with the tables and sample products.)

## 3. Run in VS Code
1. Put the connector JAR inside the `lib/` folder.
2. File -> Open Folder -> select the `StockBite` folder.
3. Open `src/stockbite/Main.java` and click **Run** (or press F5).

If your MySQL root user has a password, change it in `src/stockbite/db/DBConnection.java`.

## Command line (optional)
    javac -encoding UTF-8 -cp "lib/*" -d bin $(find src -name "*.java")
    java -cp "bin:lib/*" stockbite.Main        (use ";" instead of ":" on Windows)

## Project structure
    database/stockbite.sql        tables + sample data
    src/stockbite/Main.java       entry point
    src/stockbite/db              MySQL connection (XAMPP)
    src/stockbite/model           Product, CartItem, SaleTransaction
    src/stockbite/dao             all SQL (ProductDAO, TransactionDAO)
    src/stockbite/ui              screens (POS, Products, Inventory, Transactions, Receipt)
    receipts/                     saved receipt .txt files (created on demand)
