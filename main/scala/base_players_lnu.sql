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
-- Table structure for table `base_players_lnu`
--

DROP TABLE IF EXISTS `base_players_lnu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `base_players_lnu` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色唯一标识号',
  `user_id` int(11) NOT NULL COMMENT '用户唯一标识号',
  `nickname` char(255) NOT NULL,
  `sex` tinyint(4) NOT NULL DEFAULT '3' COMMENT '性别',
  `role_type` tinyint(4) NOT NULL COMMENT 'warrior/loli',
  `lvl` int(11) NOT NULL DEFAULT '1' COMMENT 'level',
  `exp` bigint(20) NOT NULL DEFAULT '1',
  `atk` smallint(10) NOT NULL COMMENT 'physics attack',
  `armor` smallint(10) NOT NULL,
  `def` smallint(10) NOT NULL COMMENT 'physics defense',
  `element_def` decimal(10,4) NOT NULL,
  `element_dmg_ratio` decimal(10,4) NOT NULL,
  `hp` mediumint(10) NOT NULL COMMENT '最大生命值',
  `weapon` int(10) NOT NULL DEFAULT '0' COMMENT '武器',
  `gloves` int(10) NOT NULL COMMENT '手套',
  `clothes` int(10) NOT NULL DEFAULT '0' COMMENT '衣服',
  `helmet` int(10) NOT NULL DEFAULT '0' COMMENT '头盔',
  `boots` int(10) NOT NULL DEFAULT '0' COMMENT '鞋子',
  `ring` int(10) NOT NULL DEFAULT '0' COMMENT '戒指',
  `pendant` int(10) NOT NULL DEFAULT '0' COMMENT '项链',
  `skill` varchar(64) DEFAULT NULL,
  `status` varchar(64) DEFAULT NULL,
  `title` varchar(64) DEFAULT NULL,
  `channel` int(11) DEFAULT NULL,
  `zone` int(11) NOT NULL,
  `server_id` int(11) NOT NULL,
  `vip_lv` int(11) NOT NULL,
  `created_at` int(11) NOT NULL,
  `updated_at` int(11) NOT NULL,
  `deleted_at` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_players_lnu`
--

LOCK TABLES `base_players_lnu` WRITE;
/*!40000 ALTER TABLE `base_players_lnu` DISABLE KEYS */;
INSERT INTO `base_players_lnu` VALUES (1,1,'test_nick',1,2,3,4,5,6,7,8.0000,0.9000,1000,1,2,3,4,5,6,7,'kick','freeze','nb',1,2,3,4,5,6,7);
/*!40000 ALTER TABLE `base_players_lnu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-22 17:47:46
