-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 25, 2026 at 03:09 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `stockbite_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `product_id` int(11) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `category` varchar(50) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`product_id`, `product_name`, `category`, `price`, `stock`, `created_at`) VALUES
(1, 'Chicken Adobo Meal', 'Meals', 55.00, 30, '2026-10-24 16:06:37'),
(2, 'Hotdog Sandwich', 'Snacks', 25.00, 40, '2026-10-24 16:06:37'),
(3, 'Fries', 'Snacks', 30.00, 25, '2026-10-24 16:06:37'),
(4, 'Bottled Water', 'Drinks', 15.00, 60, '2026-10-24 16:06:37'),
(5, 'Iced Tea', 'Drinks', 20.00, 50, '2026-10-24 16:06:37'),
(6, 'Turon', 'Desserts', 15.00, 8, '2026-10-24 16:06:37'),
(7, 'Pancit Canton', 'Meals', 35.00, 20, '2026-10-24 16:06:37'),
(8, 'Coke Mismo', 'Drinks', 20.00, 50, '2026-10-24 16:46:22'),
(9, 'Mountain Dew Mismo', 'Drinks', 20.00, 50, '2026-10-24 16:46:22'),
(10, 'Royal Mismo', 'Drinks', 20.00, 50, '2026-10-24 16:46:22'),
(11, 'Siomai (3 Pieces)', 'Snacks', 20.00, 30, '2026-10-24 16:46:22'),
(12, 'Siomai Rice (3 Pieces)', 'Meals', 35.00, 30, '2026-10-24 16:46:22'),
(13, 'SkyFlakes', 'Biscuits', 8.00, 50, '2026-10-25 02:04:41'),
(14, 'Fita Crackers', 'Biscuits', 8.00, 50, '2026-10-25 02:04:41'),
(15, 'Hansel Mocha Sandwich', 'Biscuits', 10.00, 40, '2026-10-25 02:04:41'),
(16, 'Rebisco Crackers', 'Biscuits', 8.00, 50, '2026-10-25 02:04:41'),
(17, 'Presto Peanut Butter', 'Biscuits', 10.00, 40, '2026-10-25 02:04:41'),
(18, 'Cream-O Cookies', 'Biscuits', 10.00, 40, '2026-10-25 02:04:41'),
(19, 'Piattos Cheese', 'Chips', 20.00, 30, '2026-10-25 02:07:17'),
(20, 'Nova Multigrain Snacks', 'Chips', 20.00, 30, '2026-10-25 02:07:17'),
(21, 'Oishi Prawn Crackers', 'Chips', 10.00, 40, '2026-10-25 02:07:17'),
(22, 'V-Cut Potato Chips', 'Chips', 20.00, 30, '2026-10-25 02:07:17'),
(23, 'Clover Chips', 'Chips', 10.00, 40, '2026-10-25 02:07:17'),
(24, 'Chippy BBQ', 'Chips', 10.00, 40, '2026-10-25 02:07:17'),
(25, 'Boy Bawang Cornick', 'Chips', 10.00, 40, '2026-10-25 02:07:17'),
(26, 'Potato Fries Snack', 'Chips', 10.00, 30, '2026-10-25 02:07:17'),
(27, 'Tortillos', 'Chips', 10.00, 30, '2026-10-25 02:07:17'),
(28, 'Roller Coaster Potato Rings', 'Chips', 10.00, 30, '2026-10-25 02:07:17');

-- --------------------------------------------------------

--
-- Table structure for table `stock_log`
--

CREATE TABLE `stock_log` (
  `log_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity_added` int(11) NOT NULL,
  `log_date` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `transaction_id` int(11) NOT NULL,
  `transaction_date` datetime DEFAULT current_timestamp(),
  `total_amount` decimal(10,2) NOT NULL,
  `cash_received` decimal(10,2) NOT NULL,
  `change_amount` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transaction_items`
--

CREATE TABLE `transaction_items` (
  `item_id` int(11) NOT NULL,
  `transaction_id` int(11) NOT NULL,
  `product_id` int(11) DEFAULT NULL,
  `product_name` varchar(100) NOT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`);

--
-- Indexes for table `stock_log`
--
ALTER TABLE `stock_log`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`transaction_id`);

--
-- Indexes for table `transaction_items`
--
ALTER TABLE `transaction_items`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `transaction_id` (`transaction_id`),
  ADD KEY `product_id` (`product_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `stock_log`
--
ALTER TABLE `stock_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transaction_items`
--
ALTER TABLE `transaction_items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `stock_log`
--
ALTER TABLE `stock_log`
  ADD CONSTRAINT `stock_log_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE;

--
-- Constraints for table `transaction_items`
--
ALTER TABLE `transaction_items`
  ADD CONSTRAINT `transaction_items_ibfk_1` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `transaction_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
