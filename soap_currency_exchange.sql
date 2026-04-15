-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 15, 2026 at 10:53 AM
-- Wersja serwera: 10.4.32-MariaDB
-- Wersja PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `soap_currency_exchange`
--

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `balances`
--

CREATE TABLE `balances` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `wallet_id` bigint(20) UNSIGNED NOT NULL,
  `currency_code` char(3) NOT NULL,
  `amount` decimal(19,4) NOT NULL DEFAULT 0.0000,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ;

--
-- Dumping data for table `balances`
--

INSERT INTO `balances` (`id`, `wallet_id`, `currency_code`, `amount`, `updated_at`) VALUES
(1, 1, 'PLN', 10000.0000, '2026-04-14 18:18:02'),
(2, 1, 'USD', 500.0000, '2026-04-14 18:18:02'),
(3, 1, 'EUR', 300.0000, '2026-04-14 18:18:02'),
(4, 2, 'PLN', 2000.0000, '2026-04-14 18:18:02'),
(5, 2, 'EUR', 150.0000, '2026-04-14 18:18:02');

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `documents`
--

CREATE TABLE `documents` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED DEFAULT NULL,
  `transaction_id` bigint(20) UNSIGNED DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_size` bigint(20) UNSIGNED DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `documents`
--

INSERT INTO `documents` (`id`, `user_id`, `transaction_id`, `file_name`, `stored_file_name`, `file_path`, `content_type`, `file_size`, `uploaded_at`) VALUES
(1, 1, 1, 'confirmation_1.pdf', 'a1b2c3_confirmation_1.pdf', '/uploads/a1b2c3_confirmation_1.pdf', 'application/pdf', 245760, '2026-04-14 18:18:02'),
(2, 2, NULL, 'id_scan.png', 'z9y8x7_id_scan.png', '/uploads/z9y8x7_id_scan.png', 'image/png', 53210, '2026-04-14 18:18:02');

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `exchange_transactions`
--

CREATE TABLE `exchange_transactions` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `wallet_id` bigint(20) UNSIGNED NOT NULL,
  `from_currency` char(3) NOT NULL,
  `to_currency` char(3) NOT NULL,
  `source_amount` decimal(19,4) NOT NULL,
  `target_amount` decimal(19,4) NOT NULL,
  `exchange_rate` decimal(19,8) NOT NULL,
  `status` enum('PENDING','COMPLETED','FAILED') NOT NULL DEFAULT 'COMPLETED',
  `external_rate_source` varchar(100) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ;

--
-- Dumping data for table `exchange_transactions`
--

INSERT INTO `exchange_transactions` (`id`, `wallet_id`, `from_currency`, `to_currency`, `source_amount`, `target_amount`, `exchange_rate`, `status`, `external_rate_source`, `created_at`) VALUES
(1, 1, 'USD', 'PLN', 100.0000, 392.5000, 3.92500000, 'COMPLETED', 'Xignite', '2026-04-14 18:18:02'),
(2, 1, 'PLN', 'EUR', 500.0000, 116.2500, 0.23250000, 'COMPLETED', 'Xignite', '2026-04-14 18:18:02'),
(3, 2, 'EUR', 'PLN', 50.0000, 214.5000, 4.29000000, 'COMPLETED', 'Xignite', '2026-04-14 18:18:02');

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `users`
--

CREATE TABLE `users` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `first_name`, `last_name`, `email`, `password_hash`, `created_at`) VALUES
(1, 'Jan', 'Kowalski', 'jan.kowalski@example.com', '$2a$10$exampleHash1234567890', '2026-04-14 18:18:02'),
(2, 'Anna', 'Nowak', 'anna.nowak@example.com', '$2a$10$exampleHash0987654321', '2026-04-14 18:18:02');

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `wallets`
--

CREATE TABLE `wallets` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `wallets`
--

INSERT INTO `wallets` (`id`, `user_id`, `created_at`) VALUES
(1, 1, '2026-04-14 18:18:02'),
(2, 2, '2026-04-14 18:18:02');

--
-- Indeksy dla zrzutów tabel
--

--
-- Indeksy dla tabeli `balances`
--
ALTER TABLE `balances`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_wallet_currency` (`wallet_id`,`currency_code`),
  ADD KEY `idx_balances_wallet_id` (`wallet_id`),
  ADD KEY `idx_balances_currency_code` (`currency_code`);

--
-- Indeksy dla tabeli `documents`
--
ALTER TABLE `documents`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_documents_user_id` (`user_id`),
  ADD KEY `idx_documents_transaction_id` (`transaction_id`);

--
-- Indeksy dla tabeli `exchange_transactions`
--
ALTER TABLE `exchange_transactions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_transactions_wallet_id` (`wallet_id`),
  ADD KEY `idx_transactions_created_at` (`created_at`);

--
-- Indeksy dla tabeli `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_users_email` (`email`);

--
-- Indeksy dla tabeli `wallets`
--
ALTER TABLE `wallets`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `balances`
--
ALTER TABLE `balances`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `documents`
--
ALTER TABLE `documents`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `exchange_transactions`
--
ALTER TABLE `exchange_transactions`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `wallets`
--
ALTER TABLE `wallets`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `balances`
--
ALTER TABLE `balances`
  ADD CONSTRAINT `fk_balance_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `documents`
--
ALTER TABLE `documents`
  ADD CONSTRAINT `fk_document_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `exchange_transactions` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_document_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `exchange_transactions`
--
ALTER TABLE `exchange_transactions`
  ADD CONSTRAINT `fk_transaction_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `wallets`
--
ALTER TABLE `wallets`
  ADD CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
