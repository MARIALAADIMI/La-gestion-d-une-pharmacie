-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3307
-- Généré le : mar. 27 mai 2025 à 23:48
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `pharmacie_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `client`
--

CREATE TABLE `client` (
  `CIN` varchar(20) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `tele` varchar(20) DEFAULT NULL,
  `adresse` text DEFAULT NULL,
  `date_inscription` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `client`
--

INSERT INTO `client` (`CIN`, `nom`, `prenom`, `tele`, `adresse`, `date_inscription`) VALUES
('AB1234', 'Bennani', 'Karim', '0612345677', '12 Avenue Hassan II, Rabat', '2025-05-10'),
('BC45343', 'Allaoui', 'anoir', '0656453423', '10 Rue Mohammed 5,Rabat', '2025-05-26'),
('CD5678', 'El Fassi', 'Amina', '0623456789', '45 Rue Mohammed V, Rabat', '2025-05-15'),
('EF9012', 'Rahmouni', 'Youssef', '0634567890', '8 Boulevard Alal Ben Abdellah, Rabat', '2025-05-20'),
('GH3456', 'Cherkaoui', 'Fatima', '0645678901', '22 Avenue Fal Ould Oumeir, Rabat', '2025-05-25'),
('IJ567890', 'Cherkaoui', 'Youssef', '068765433', '10 Rue Mohammed V, Marrakech', '2025-05-02'),
('IJ7890', 'Alaoui', 'Mehdi', '0656789012', '5 Rue Souika, Rabat', '2025-06-01');

-- --------------------------------------------------------

--
-- Structure de la table `facture`
--

CREATE TABLE `facture` (
  `id_Fac` int(11) NOT NULL,
  `CIN` varchar(20) DEFAULT NULL,
  `date_fac` date DEFAULT NULL,
  `montant_total` decimal(10,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `facture`
--

INSERT INTO `facture` (`id_Fac`, `CIN`, `date_fac`, `montant_total`) VALUES
(53, 'IJ7890', '2025-05-26', 30.35),
(54, 'GH3456', '2025-05-26', 184.50),
(55, 'AB1234', '2025-05-26', 206.05),
(56, 'CD5678', '2025-05-26', 154.10),
(57, 'AB1234', '2025-05-26', 30.35),
(58, 'BC45343', '2025-05-26', 119.15),
(59, 'CD5678', '2025-05-26', 111.75);

-- --------------------------------------------------------

--
-- Structure de la table `facture_details`
--

CREATE TABLE `facture_details` (
  `id_Detail` int(11) NOT NULL,
  `id_Fac` int(11) DEFAULT NULL,
  `id_Med` int(11) DEFAULT NULL,
  `quantite` int(11) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `facture_details`
--

INSERT INTO `facture_details` (`id_Detail`, `id_Fac`, `id_Med`, `quantite`, `prix_unitaire`) VALUES
(111, 53, 3, 1, 3.20),
(112, 53, 4, 1, 8.75),
(113, 53, 5, 1, 5.90),
(114, 53, 7, 1, 12.50),
(115, 54, 13, 1, 35.00),
(116, 54, 14, 3, 28.50),
(117, 54, 15, 2, 32.00),
(118, 55, 16, 1, 45.75),
(119, 55, 17, 1, 38.20),
(120, 55, 18, 1, 52.30),
(121, 55, 19, 1, 27.80),
(122, 55, 20, 1, 42.00),
(123, 56, 19, 1, 27.80),
(124, 56, 20, 1, 42.00),
(125, 56, 18, 1, 52.30),
(126, 56, 15, 1, 32.00),
(127, 57, 3, 1, 3.20),
(128, 57, 4, 1, 8.75),
(129, 57, 5, 1, 5.90),
(130, 57, 7, 1, 12.50),
(131, 58, 3, 1, 3.20),
(132, 58, 15, 1, 32.00),
(133, 58, 16, 1, 45.75),
(134, 58, 17, 1, 38.20),
(135, 59, 16, 1, 45.75),
(136, 59, 17, 1, 38.20),
(137, 59, 19, 1, 27.80);

-- --------------------------------------------------------

--
-- Structure de la table `medicament`
--

CREATE TABLE `medicament` (
  `id_Med` int(11) NOT NULL,
  `code_barre` varchar(20) DEFAULT NULL,
  `nom_Med` varchar(100) NOT NULL,
  `forme_pharmaceutique` varchar(50) DEFAULT NULL,
  `dosage` varchar(30) DEFAULT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  `stock_dispo` int(11) NOT NULL DEFAULT 0,
  `remboursable` tinyint(1) DEFAULT 0,
  `date_ajout` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `medicament`
--

INSERT INTO `medicament` (`id_Med`, `code_barre`, `nom_Med`, `forme_pharmaceutique`, `dosage`, `prix_unitaire`, `stock_dispo`, `remboursable`, `date_ajout`) VALUES
(3, '234567890123', 'Ibuprofène', 'Gélule', '200mg', 3.20, 66, 1, '2025-05-12 20:21:34'),
(4, '345678901234', 'Amoxicilline', 'Poudre', '500mg', 8.75, 84, 1, '2025-05-12 20:21:34'),
(5, '456789012345', 'Vitamine C', 'Comprimé', '1g', 5.90, 10, 1, '2025-05-12 20:21:34'),
(7, '611115111111', 'Doliprane', 'Comprimé', '500mg', 12.50, 146, 1, '2025-05-25 17:33:00'),
(13, '567890123456', 'Zyrtec', 'Comprimé', '10mg', 35.00, 7, 1, '2025-05-25 17:38:11'),
(14, '678901234567', 'Metformine', 'Comprimé', '850mg', 28.50, 72, 1, '2025-05-25 17:38:11'),
(15, '789012345678', 'Maalox', 'Comprimé', '250ml', 32.00, 34, 0, '2025-05-25 17:38:11'),
(16, '890123456789', 'Paroxétine', 'Comprimé', '20mg', 45.75, 25, 1, '2025-05-25 17:38:11'),
(17, '901234567890', 'Amlodipine', 'Comprimé', '10mg', 38.20, 51, 1, '2025-05-25 17:38:11'),
(18, '012345678901', 'Ventoline', 'doseur', '100μg/dose', 52.30, 9, 1, '2025-05-25 17:38:11'),
(19, '123450987654', 'Daktarin', 'Crème', '2%', 27.80, 57, 0, '2025-05-25 17:38:11'),
(20, '234561098765', 'Tramadol', 'Gélule', '50mg', 42.00, 31, 1, '2025-05-25 17:38:11'),
(21, '345672109876', 'Microval', 'Comprimé', '75μg', 65.50, 50, 1, '2025-05-25 17:38:11'),
(22, '456783210987', 'Primperan', 'Comprimé', '10mg', 18.90, 67, 1, '2025-05-25 17:38:11'),
(23, '567894321098', 'Previscan', 'Comprimé', '20mg', 58.75, 41, 1, '2025-05-25 17:38:11');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `client`
--
ALTER TABLE `client`
  ADD PRIMARY KEY (`CIN`);

--
-- Index pour la table `facture`
--
ALTER TABLE `facture`
  ADD PRIMARY KEY (`id_Fac`),
  ADD KEY `CIN` (`CIN`);

--
-- Index pour la table `facture_details`
--
ALTER TABLE `facture_details`
  ADD PRIMARY KEY (`id_Detail`),
  ADD KEY `id_Fac` (`id_Fac`),
  ADD KEY `id_Med` (`id_Med`);

--
-- Index pour la table `medicament`
--
ALTER TABLE `medicament`
  ADD PRIMARY KEY (`id_Med`),
  ADD UNIQUE KEY `code_barre` (`code_barre`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `facture`
--
ALTER TABLE `facture`
  MODIFY `id_Fac` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=60;

--
-- AUTO_INCREMENT pour la table `facture_details`
--
ALTER TABLE `facture_details`
  MODIFY `id_Detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=138;

--
-- AUTO_INCREMENT pour la table `medicament`
--
ALTER TABLE `medicament`
  MODIFY `id_Med` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `facture`
--
ALTER TABLE `facture`
  ADD CONSTRAINT `facture_ibfk_1` FOREIGN KEY (`CIN`) REFERENCES `client` (`CIN`);

--
-- Contraintes pour la table `facture_details`
--
ALTER TABLE `facture_details`
  ADD CONSTRAINT `facture_details_ibfk_1` FOREIGN KEY (`id_Fac`) REFERENCES `facture` (`id_Fac`),
  ADD CONSTRAINT `facture_details_ibfk_2` FOREIGN KEY (`id_Med`) REFERENCES `medicament` (`id_Med`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
