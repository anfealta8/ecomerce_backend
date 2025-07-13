CREATE DATABASE  IF NOT EXISTS `eecomerce_backend` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `eecomerce_backend`;
-- MySQL dump 10.13  Distrib 8.0.30, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: eecomerce_backend
-- ------------------------------------------------------
-- Server version	8.4.5

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `inventarios`
--

DROP TABLE IF EXISTS `inventarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad_disponible` int NOT NULL,
  `cantidad_minima` int NOT NULL,
  `cantidad_reservada` int NOT NULL,
  `fecha_actualizacion` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `producto_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4xirt8ux8pvdhtj9cqwbxn2qx` (`producto_id`),
  CONSTRAINT `FKl6umb4q9iu5a17t9bpvn62u4e` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventarios`
--

LOCK TABLES `inventarios` WRITE;
/*!40000 ALTER TABLE `inventarios` DISABLE KEYS */;
INSERT INTO `inventarios` VALUES (1,2000,20,10,'2025-07-12 17:49:58.729265','2025-07-11 21:25:31.317667',1),(2,46,15,5,'2025-07-12 17:22:46.156249','2025-07-11 21:25:48.665231',2),(4,200,5,0,'2025-07-12 17:55:15.515586','2025-07-12 17:55:15.515586',3);
/*!40000 ALTER TABLE `inventarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orden_detalles`
--

DROP TABLE IF EXISTS `orden_detalles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orden_detalles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `fecha_actualizacion` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `subtotal_linea` decimal(10,2) NOT NULL,
  `orden_id` bigint NOT NULL,
  `producto_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq8bl569i57pt5l3wuuwbi28yi` (`orden_id`),
  KEY `FKhcesdn3s324ahwtmu3yohgekd` (`producto_id`),
  CONSTRAINT `FKhcesdn3s324ahwtmu3yohgekd` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `FKq8bl569i57pt5l3wuuwbi28yi` FOREIGN KEY (`orden_id`) REFERENCES `ordenes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orden_detalles`
--

LOCK TABLES `orden_detalles` WRITE;
/*!40000 ALTER TABLE `orden_detalles` DISABLE KEYS */;
INSERT INTO `orden_detalles` VALUES (1,5,'2025-07-11 21:36:27.036571','2025-07-11 21:36:27.036571',1400.50,7002.50,1,1),(2,2,'2025-07-11 21:36:27.052572','2025-07-11 21:36:27.052572',1400.50,2801.00,1,2),(3,8,'2025-07-11 21:37:40.649607','2025-07-11 21:37:40.649607',1400.50,11204.00,2,2),(4,10,'2025-07-11 21:37:40.652609','2025-07-11 21:37:40.652609',1400.50,14005.00,2,1),(5,8,'2025-07-11 21:52:17.476228','2025-07-11 21:52:17.476228',1400.50,11204.00,3,2),(6,10,'2025-07-11 21:52:17.499229','2025-07-11 21:52:17.499229',1400.50,14005.00,3,1),(7,8,'2025-07-11 21:54:30.588374','2025-07-11 21:54:30.588374',1400.50,11204.00,4,2),(8,10,'2025-07-11 21:54:30.591358','2025-07-11 21:54:30.591358',1400.50,14005.00,4,1),(9,5,'2025-07-11 21:57:54.661426','2025-07-11 21:57:54.661426',1400.50,7002.50,5,2),(10,5,'2025-07-11 21:57:54.664422','2025-07-11 21:57:54.664422',1400.50,7002.50,5,1),(11,5,'2025-07-11 21:59:03.920221','2025-07-11 21:59:03.920221',1400.50,7002.50,6,2),(12,5,'2025-07-11 21:59:03.928224','2025-07-11 21:59:03.928224',1400.50,7002.50,6,1),(13,5,'2025-07-11 21:59:24.264937','2025-07-11 21:59:24.264937',1400.50,7002.50,7,2),(14,5,'2025-07-11 21:59:24.269936','2025-07-11 21:59:24.269936',1400.50,7002.50,7,1),(15,3,'2025-07-12 17:22:46.150111','2025-07-12 17:22:46.150111',1400.50,4201.50,8,2);
/*!40000 ALTER TABLE `orden_detalles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenes`
--

DROP TABLE IF EXISTS `ordenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descuento_total` decimal(10,2) NOT NULL,
  `estado` enum('CANCELADA','COMPLETADA','ENTREGADA','ENVIADA','PENDIENTE') NOT NULL,
  `fecha_actualizacion` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsqu43gsd6mtx7b1siww96324` (`usuario_id`),
  CONSTRAINT `FKsqu43gsd6mtx7b1siww96324` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenes`
--

LOCK TABLES `ordenes` WRITE;
/*!40000 ALTER TABLE `ordenes` DISABLE KEYS */;
INSERT INTO `ordenes` VALUES (1,980.35,'COMPLETADA','2025-07-12 17:11:25.697094','2025-07-11 21:36:27.020567',9803.50,8823.15,1),(2,2520.90,'PENDIENTE','2025-07-11 21:37:40.641615','2025-07-11 21:37:40.641615',25209.00,22688.10,1),(3,2520.90,'PENDIENTE','2025-07-11 21:52:17.451228','2025-07-11 21:52:17.451228',25209.00,22688.10,1),(4,2520.90,'PENDIENTE','2025-07-11 21:54:30.584355','2025-07-11 21:54:30.584355',25209.00,22688.10,1),(5,1400.50,'PENDIENTE','2025-07-11 21:57:54.655427','2025-07-11 21:57:54.655427',14005.00,12604.50,1),(6,2030.73,'PENDIENTE','2025-07-11 21:59:03.912225','2025-07-11 21:59:03.912225',14005.00,11974.28,1),(7,2030.73,'PENDIENTE','2025-07-11 21:59:24.259938','2025-07-11 21:59:24.259938',14005.00,11974.28,1),(8,420.15,'ENVIADA','2025-07-12 17:23:11.283565','2025-07-12 17:22:46.120411',4201.50,3781.35,3);
/*!40000 ALTER TABLE `ordenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `categoria` varchar(100) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `fecha_actualizacion` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `sku` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8bwvjlh8b1xi4cc4ar819q61y` (`sku`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,_binary '','Electronicos','Laptop potente y ligera para profesionales.','2025-07-11 21:24:06.735338','2025-07-11 21:24:06.735338','Laptop Ultrabook X10',1400.50,'LTOP-UX1-2022'),(2,_binary '','Electronicos','Laptop potente y ligera para profesionales.','2025-07-11 21:24:13.754474','2025-07-11 21:24:13.754474','Laptop Ultrabook X10',1400.50,'LTOP-UX1-2023'),(3,_binary '','Electronicos','Laptop potente y ligera para profesionales.','2025-07-11 21:24:31.233560','2025-07-11 21:24:31.233560','Laptop Ultrabook X10',1600.00,'LTOP-UX1-2025');
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_roles`
--

DROP TABLE IF EXISTS `usuario_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_roles` (
  `usuario_id` bigint NOT NULL,
  `roles` enum('ADMIN','USER') DEFAULT NULL,
  KEY `FKuu9tea04xb29m2km5lwe46ua` (`usuario_id`),
  CONSTRAINT `FKuu9tea04xb29m2km5lwe46ua` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_roles`
--

LOCK TABLES `usuario_roles` WRITE;
/*!40000 ALTER TABLE `usuario_roles` DISABLE KEYS */;
INSERT INTO `usuario_roles` VALUES (1,'ADMIN'),(3,'USER'),(2,'ADMIN');
/*!40000 ALTER TABLE `usuario_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contrasena` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `fecha_actualizacion` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`),
  UNIQUE KEY `UKof5vabgukahdwmgxk4kjrbu98` (`nombre_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'$2a$10$hkrVx7DDS.kiEP.BreTpnujlDgl0AM3qpOcnEuDFI11kf2naY3rV.','anfealta@gmail.com','2025-07-11 21:22:32.959437','2025-07-11 21:22:32.959437','anfealta'),(2,'$2a$10$bVenad3Lk.JgtTZB0GWLMe1nMRFQaHOkrj1JGhd/HtFLL1s04xvUK','andres@gmail.com','2025-07-12 15:47:58.273229','2025-07-12 10:39:33.537240','andres'),(3,'$2a$10$EKV8u7W13a6Vq7VkriDlGeTgXLNvMCAk2s7jLCQKBCNVS4KXw.9dG','anfealta8@gmail.com','2025-07-12 15:43:04.741329','2025-07-12 12:27:20.564600','anfealta8');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-12 19:13:39
