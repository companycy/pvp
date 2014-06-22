-- mysqldump -uroot new_project base_equipments_lnu > base_equipments_lnu.sql

-- MySQL dump 10.13  Distrib 5.6.17, for osx10.9 (x86_64)
--
-- Host: localhost    Database: new_project
-- ------------------------------------------------------
-- Server version	5.6.17

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `base_equipments_lnu`
--

DROP TABLE IF EXISTS `base_equipments_lnu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `base_equipments_lnu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_id` smallint(5) NOT NULL,
  `display_name` char(64) NOT NULL COMMENT '6',
  `lvl` smallint(10) NOT NULL COMMENT '65535',
  `min_physical_atk` smallint(10) DEFAULT NULL,
  `max_physical_atk` smallint(10) DEFAULT NULL,
  `armor` smallint(10) DEFAULT NULL COMMENT '65535',
  `element_def` smallint(10) NOT NULL,
  `element_atk` smallint(10) DEFAULT NULL,
  `element_atk_type` tinyint(10) DEFAULT NULL,
  `luck_value` smallint(10) NOT NULL,
  `greed` decimal(10,4) NOT NULL,
  `hp_ratio` decimal(10,4) NOT NULL,
  `armor_ratio` decimal(10,4) NOT NULL,
  `evasion` decimal(10,4) NOT NULL,
  `move_speed` decimal(10,4) NOT NULL,
  `attack_speed` decimal(10,4) NOT NULL,
  `hit_rate` decimal(10,4) NOT NULL,
  `mp_regen` decimal(10,4) NOT NULL,
  `critical_rate` decimal(10,4) NOT NULL,
  `critical_dmg_ratio` decimal(10,4) NOT NULL,
  `crushing_blow_chance` decimal(10,4) NOT NULL,
  `delta_dmg_discount` decimal(10,4) NOT NULL,
  `dmg_immortal_chance` decimal(10,4) NOT NULL,
  `hp_drain` smallint(10) NOT NULL,
  `dmg_reflective` smallint(10) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`display_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_equipments_lnu`
--

LOCK TABLES `base_equipments_lnu` WRITE;
/*!40000 ALTER TABLE `base_equipments_lnu` DISABLE KEYS */;
INSERT INTO `base_equipments_lnu` VALUES (1,2,'test_equipment',10,1000,1500,10,4,15,3,10,0.7000,0.5000,0.6000,0.3000,201.0000,101.0000,0.9000,0.8000,0.1000,0.2000,0.3000,0.2000,0.1000,10,500);
/*!40000 ALTER TABLE `base_equipments_lnu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-22 16:36:56
