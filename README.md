# Stock Bite - School Canteen Management System
Java (Swing) + MySQL (XAMPP).

## Folder structure
    StockBite/
    +-- src/                       all the Java code (one flat folder, no sub-folders)
    |   +-- Main.java              START HERE - run this file
    |   +-- Database.java          all MySQL code (connection, queries, data classes)
    |   +-- Style.java             colors, fonts, buttons, tables
    |   +-- Notify.java            pop-up messages
    |   +-- PosScreen.java         Point of Sale tab
    |   +-- ProductsScreen.java    Products tab
    |   +-- InventoryScreen.java   Inventory tab
    |   +-- TransactionsScreen.java  Transactions tab
    |   +-- Receipt.java           receipt window (print or save)
    +-- lib/                       put the MySQL connector .jar here
    +-- fonts/                     Lora + Inter fonts (do not move)
    +-- database/stockbite.sql     import this in phpMyAdmin

## How to run
1. Install JDK 17+ and the "Extension Pack for Java" in VS Code.
2. Open XAMPP, click Start on MySQL.
3. Open http://localhost/phpmyadmin -> Import -> choose database/stockbite.sql -> Go.
4. Download mysql-connector-j-8.4.0.jar and put it in the lib folder:
   https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
5. In VS Code: File -> Open Folder -> choose the StockBite folder (the one that contains src).
6. Open src/Main.java and click Run.

If your MySQL root user has a password, set it in src/Database.java (PASSWORD).
This copy connects on port 3307 (see URL in src/Database.java); XAMPP MySQL must use the same port.
