-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: salesman_performance_management_system
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `collection_record`
--

DROP TABLE IF EXISTS `collection_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collection_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_due` decimal(10,2) DEFAULT NULL,
  `amount_received` decimal(10,2) DEFAULT NULL,
  `collection_date` date NOT NULL,
  `collection_method` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `sales_record_id` bigint DEFAULT NULL,
  `salesman_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4wx6yrl2w0w5qo0o95kijae08` (`customer_id`),
  KEY `FKe2j3a9l59kaseb4284oa0a7vs` (`sales_record_id`),
  KEY `FKh0qbiuo09ms1p3q9km9w47k4m` (`salesman_id`),
  CONSTRAINT `FK4wx6yrl2w0w5qo0o95kijae08` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `FKe2j3a9l59kaseb4284oa0a7vs` FOREIGN KEY (`sales_record_id`) REFERENCES `sales_record` (`id`),
  CONSTRAINT `FKh0qbiuo09ms1p3q9km9w47k4m` FOREIGN KEY (`salesman_id`) REFERENCES `salesman` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collection_record`
--

LOCK TABLES `collection_record` WRITE;
/*!40000 ALTER TABLE `collection_record` DISABLE KEYS */;
INSERT INTO `collection_record` VALUES (1,600.00,600.00,'2025-11-21','电话','2025-12-01 18:04:34.483676','2025-11-26','催款记录 1','已回款','2025-12-01 18:04:34.483676',1,1,1),(2,175.00,87.50,'2025-11-22','现场','2025-12-01 18:04:34.488694','2025-11-26','催款记录 2','部分回款','2025-12-01 18:04:34.488694',2,2,1),(3,13.50,6.75,'2025-11-23','电话','2025-12-01 18:04:34.491675','2025-11-26','催款记录 3','未回款','2025-12-01 18:04:34.491675',2,3,1),(4,405.00,405.00,'2025-11-24','现场','2025-12-01 18:04:34.494202','2025-11-26','催款记录 4','已回款','2025-12-01 18:04:34.494202',3,4,1),(5,330.00,165.00,'2025-11-25','电话','2025-12-01 18:04:34.497220','2025-11-26','催款记录 5','部分回款','2025-12-01 18:04:34.497220',3,5,1),(6,234.00,117.00,'2025-11-26','现场','2025-12-01 18:04:34.500203','2025-11-26','催款记录 6','未回款','2025-12-01 18:04:34.500203',3,6,1),(7,4950.00,4950.00,'2025-11-27','电话','2025-12-01 18:04:34.502713','2025-11-26','催款记录 7','已回款','2025-12-01 18:04:34.502713',4,7,1),(8,2600.00,1300.00,'2025-11-28','现场','2025-12-01 18:04:34.505816','2025-11-26','催款记录 8','部分回款','2025-12-01 18:04:34.505816',5,8,2),(9,180.00,90.00,'2025-11-29','电话','2025-12-01 18:04:34.509833','2025-11-26','催款记录 9','未回款','2025-12-01 18:04:34.509833',5,9,2),(10,420.00,420.00,'2025-11-30','现场','2025-12-01 18:04:34.513872','2025-11-26','催款记录 10','已回款','2025-12-01 18:04:34.513872',6,10,2),(11,595.00,297.50,'2025-11-21','电话','2025-12-01 18:04:34.516872','2025-11-26','催款记录 11','部分回款','2025-12-01 18:04:34.516872',6,11,2),(12,418.00,209.00,'2025-11-22','现场','2025-12-01 18:04:34.519873','2025-11-26','催款记录 12','未回款','2025-12-01 18:04:34.519873',6,12,2),(13,306.00,306.00,'2025-11-23','电话','2025-12-01 18:04:34.524413','2025-11-26','催款记录 13','已回款','2025-12-01 18:04:34.524413',7,13,2),(14,161.50,80.75,'2025-11-24','现场','2025-12-01 18:04:34.529386','2025-11-26','催款记录 14','部分回款','2025-12-01 18:04:34.529386',8,14,2),(15,315.00,157.50,'2025-11-25','电话','2025-12-01 18:04:34.533448','2025-11-26','催款记录 15','未回款','2025-12-01 18:04:34.533448',8,15,2);
/*!40000 ALTER TABLE `collection_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `complaint_record`
--

DROP TABLE IF EXISTS `complaint_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complaint_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complaint_date` date NOT NULL,
  `complaint_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `handle_date` date DEFAULT NULL,
  `handler` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `result` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `salesman_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgqpha2o0lofaeaphqayuw3qeo` (`customer_id`),
  KEY `FK6xpyh5dofs3488y3mj34vf1qh` (`salesman_id`),
  CONSTRAINT `FK6xpyh5dofs3488y3mj34vf1qh` FOREIGN KEY (`salesman_id`) REFERENCES `salesman` (`id`),
  CONSTRAINT `FKgqpha2o0lofaeaphqayuw3qeo` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `complaint_record`
--

LOCK TABLES `complaint_record` WRITE;
/*!40000 ALTER TABLE `complaint_record` DISABLE KEYS */;
INSERT INTO `complaint_record` VALUES (1,'2025-11-24','产品质量','产品包装破损，需要重新发货','2025-12-01 18:04:34.537452','2025-11-26','客服部','投诉记录 1','问题已解决，客户满意','已处理','2025-12-01 18:04:34.537452',1,1),(2,'2025-11-25','服务态度','销售人员服务态度有待改善','2025-12-01 18:04:34.543528','2025-11-27','客服部','投诉记录 2','问题已解决，客户满意','已处理','2025-12-01 18:04:34.543528',5,2),(3,'2025-11-26','配送延迟','订单配送时间超出预期','2025-12-01 18:04:34.547532','2025-11-28','客服部','投诉记录 3','问题已解决，客户满意','已处理','2025-12-01 18:04:34.547532',9,3),(4,'2025-11-27','价格争议','价格与之前报价不一致','2025-12-01 18:04:34.550529',NULL,'客服部','投诉记录 4','正在处理中','处理中','2025-12-01 18:04:34.550529',13,4),(5,'2025-11-28','产品质量','产品包装破损，需要重新发货','2025-12-01 18:04:34.555591',NULL,'客服部','投诉记录 5','正在处理中','处理中','2025-12-01 18:04:34.555591',17,5);
/*!40000 ALTER TABLE `complaint_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contact_record`
--

DROP TABLE IF EXISTS `contact_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact_method` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_time` datetime(6) NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `feedback` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `summary` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `salesman_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2xjwpy6lcu9800r8ydogug9u3` (`customer_id`),
  KEY `FKqs2ohekhc32srikqfbhoumxgv` (`salesman_id`),
  CONSTRAINT `FK2xjwpy6lcu9800r8ydogug9u3` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `FKqs2ohekhc32srikqfbhoumxgv` FOREIGN KEY (`salesman_id`) REFERENCES `salesman` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact_record`
--

LOCK TABLES `contact_record` WRITE;
/*!40000 ALTER TABLE `contact_record` DISABLE KEYS */;
INSERT INTO `contact_record` VALUES (1,'电话','2025-11-11 18:04:34.305979','2025-12-01 18:04:34.306978','客户表示满意，愿意继续合作','讨论新产品合作方案','2025-12-01 18:04:34.306978',1,1),(2,'邮件','2025-11-12 18:04:34.313653','2025-12-01 18:04:34.313653','客户表示满意，愿意继续合作','了解客户近期采购需求','2025-12-01 18:04:34.313653',2,1),(3,'微信','2025-11-13 18:04:34.318168','2025-12-01 18:04:34.318168','客户表示满意，愿意继续合作','跟进上次订单执行情况','2025-12-01 18:04:34.318168',3,1),(4,'面谈','2025-11-14 18:04:34.321691','2025-12-01 18:04:34.322707','客户表示满意，愿意继续合作','介绍公司新推出的产品线','2025-12-01 18:04:34.322707',4,1),(5,'电话','2025-11-15 18:04:34.327853','2025-12-01 18:04:34.327853','客户表示满意，愿意继续合作','讨论新产品合作方案','2025-12-01 18:04:34.327853',5,2),(6,'邮件','2025-11-16 18:04:34.331854','2025-12-01 18:04:34.332363','客户表示满意，愿意继续合作','了解客户近期采购需求','2025-12-01 18:04:34.332363',6,2),(7,'微信','2025-11-17 18:04:34.335902','2025-12-01 18:04:34.335902','客户表示满意，愿意继续合作','跟进上次订单执行情况','2025-12-01 18:04:34.335902',7,2),(8,'面谈','2025-11-18 18:04:34.338881','2025-12-01 18:04:34.339882','客户表示满意，愿意继续合作','介绍公司新推出的产品线','2025-12-01 18:04:34.339882',8,2),(9,'电话','2025-11-19 18:04:34.342446','2025-12-01 18:04:34.342446','客户表示满意，愿意继续合作','讨论新产品合作方案','2025-12-01 18:04:34.342446',9,3),(10,'邮件','2025-11-20 18:04:34.345427','2025-12-01 18:04:34.345427','客户表示满意，愿意继续合作','了解客户近期采购需求','2025-12-01 18:04:34.345427',10,3),(11,'微信','2025-11-21 18:04:34.348466','2025-12-01 18:04:34.348466','客户表示满意，愿意继续合作','跟进上次订单执行情况','2025-12-01 18:04:34.348466',11,3),(12,'面谈','2025-11-22 18:04:34.351455','2025-12-01 18:04:34.351455','客户表示满意，愿意继续合作','介绍公司新推出的产品线','2025-12-01 18:04:34.351455',12,3),(13,'电话','2025-11-23 18:04:34.353765','2025-12-01 18:04:34.354765','客户表示满意，愿意继续合作','讨论新产品合作方案','2025-12-01 18:04:34.354765',13,4),(14,'邮件','2025-11-24 18:04:34.357765','2025-12-01 18:04:34.357765','客户表示满意，愿意继续合作','了解客户近期采购需求','2025-12-01 18:04:34.357765',14,4),(15,'微信','2025-11-25 18:04:34.360787','2025-12-01 18:04:34.360787','客户表示满意，愿意继续合作','跟进上次订单执行情况','2025-12-01 18:04:34.360787',15,4),(16,'面谈','2025-11-26 18:04:34.364295','2025-12-01 18:04:34.364295','客户表示满意，愿意继续合作','介绍公司新推出的产品线','2025-12-01 18:04:34.364295',16,4),(17,'电话','2025-11-27 18:04:34.366846','2025-12-01 18:04:34.366846','客户表示满意，愿意继续合作','讨论新产品合作方案','2025-12-01 18:04:34.366846',17,5),(18,'邮件','2025-11-28 18:04:34.369863','2025-12-01 18:04:34.370860','客户表示满意，愿意继续合作','了解客户近期采购需求','2025-12-01 18:04:34.370860',18,5),(19,'微信','2025-11-29 18:04:34.374005','2025-12-01 18:04:34.374005','客户表示满意，愿意继续合作','跟进上次订单执行情况','2025-12-01 18:04:34.374005',19,5),(20,'面谈','2025-11-30 18:04:34.376996','2025-12-01 18:04:34.377978','客户表示满意，愿意继续合作','介绍公司新推出的产品线','2025-12-01 18:04:34.377978',20,5);
/*!40000 ALTER TABLE `contact_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_person` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `customer_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `salesman_id` bigint DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'罗德岛港区近地','阿米娅','2025-12-01 18:04:33.960587','enterprise','amiya@rhodes.example','A','罗德岛总医院','021-10001001','罗德岛直属合作医院',1,'2025-12-01 18:04:33.960587'),(2,'龙门第七区','玛莎医生','2025-12-01 18:04:33.967972','enterprise','martha@lungmen.example','B','龙门第七诊所','021-20002002','社区诊所',1,'2025-12-01 18:04:33.967972'),(3,'乌萨斯首都市场','银狼','2025-12-01 18:04:33.974497','enterprise','sw@ursus.example','B','乌萨斯东方药房','031-30003003','连锁药房',1,'2025-12-01 18:04:33.974497'),(4,'罗德岛宿舍区','博士','2025-12-01 18:04:33.979494','individual','doctor@personal.example','A','博士','021-88888888','个人客户，长期采购医疗用品',1,'2025-12-01 18:04:33.979494'),(5,'维克汉姆东区','临光','2025-12-01 18:04:33.985744','enterprise','nearl@wickham.example','B','维克汉姆社区医院','051-50005005','区域医院',2,'2025-12-01 18:04:33.985744'),(6,'苔原3号路','伊芙利特','2025-12-01 18:04:33.989747','enterprise','ifrit@tundra.example','C','苔原流动医疗站','061-60006006','流动医疗点',2,'2025-12-01 18:04:33.990745'),(7,'谢拉格山区','斯卡蒂医生','2025-12-01 18:04:33.994255','enterprise','skadi@kjerag.example','C','谢拉格综合诊所','071-70007007','兼顾兽医的综合诊所',2,'2025-12-01 18:04:33.994255'),(8,'罗德岛医疗部','凯尔希','2025-12-01 18:04:33.999406','individual','kaltsit@personal.example','A','凯尔希','021-99999999','个人采购，医疗研究用',2,'2025-12-01 18:04:33.999406'),(9,'希斯洛中心区','塞雷娅','2025-12-01 18:04:34.004521','enterprise','saria@heathrow.example','B','希斯洛诊所','091-90009009','社区医疗中心',3,'2025-12-01 18:04:34.004521'),(10,'卡西米尔老城','银灰','2025-12-01 18:04:34.008498','enterprise','sa@kazimierz.example','A','卡西米尔医疗中心','101-10010010','高端门诊',3,'2025-12-01 18:04:34.008498'),(11,'熔岩温泉区','熔泉','2025-12-01 18:04:34.013864','enterprise','ifrit2@lava.example','C','熔岩温泉药局','111-11011011','小型药局',3,'2025-12-01 18:04:34.013864'),(12,'罗德岛血库','华法琳','2025-12-01 18:04:34.019863','individual','warfarin@personal.example','B','华法琳','091-77777777','个人客户，医疗耗材采购',3,'2025-12-01 18:04:34.019863'),(13,'维多利亚广场','苏苏洛','2025-12-01 18:04:34.026347','enterprise','sussurro@victoria.example','B','维多利亚广场诊所','131-13013013','社区诊所',4,'2025-12-01 18:04:34.026347'),(14,'盖亚康复公园','瑕光','2025-12-01 18:04:34.030346','enterprise','nearl2@gaia.example','A','盖亚康复中心','141-14014014','康复中心',4,'2025-12-01 18:04:34.030346'),(15,'山口要道','拉普兰德','2025-12-01 18:04:34.036401','enterprise','lappland@mp.example','B','山口医院','151-15015015','山区医院',4,'2025-12-01 18:04:34.036401'),(16,'圣堂医疗室','闪灵','2025-12-01 18:04:34.040403','individual','shining@personal.example','A','闪灵','131-66666666','个人采购，高端医疗器械',4,'2025-12-01 18:04:34.040403'),(17,'应急区域','杜宾','2025-12-01 18:04:34.044437','enterprise','amiya2@contingency.example','B','应急响应诊疗点','171-17017017','快速响应诊疗点',5,'2025-12-01 18:04:34.044437'),(18,'黑钢要塞','陈','2025-12-01 18:04:34.048473','enterprise','chen@blacksteel.example','A','黑钢前哨站','181-18018018','军用医疗保障',5,'2025-12-01 18:04:34.048473'),(19,'第二风暴区','德克萨斯','2025-12-01 18:04:34.052937','enterprise','texas@sstorm.example','C','第二风暴药房','191-19019019','私人药房',5,'2025-12-01 18:04:34.052937'),(20,'罗德岛研发部','赫默','2025-12-01 18:04:34.058935','individual','silence@personal.example','B','赫默','171-55555555','个人客户，实验器材采购',5,'2025-12-01 18:04:34.058935');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `product_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `specification` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stock` int DEFAULT NULL,
  `unit` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKto65i5rfdrg8yvq42pf8jahut` (`product_no`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'药品','2025-12-01 18:04:33.679303','罗德岛研发的基础治疗血清，用于急救。','阿米娅急救血清',120.00,'RI-001','10ml/支',NULL,NULL,'支','2025-12-01 18:04:33.679303'),(2,'器械','2025-12-01 18:04:33.752842','轻量注射器，适配多种给药方案。','能天使注射器',8.50,'RI-002','一次性注射器',NULL,NULL,'个','2025-12-01 18:04:33.752842'),(3,'消耗品','2025-12-01 18:04:33.759919','高强度创口绷带。','银老板创口绷带',15.00,'RI-003','5cm x 5m',NULL,NULL,'卷','2025-12-01 18:04:33.759919'),(4,'药品','2025-12-01 18:04:33.785544','用于消毒与伤口清洁。','龙门消毒液',25.00,'RI-004','100ml',NULL,NULL,'瓶','2025-12-01 18:04:33.785544'),(5,'防护','2025-12-01 18:04:33.793622','医用防护口罩，过滤效率高。','罗德岛医用口罩',1.50,'RI-005','一次性口罩',NULL,NULL,'个','2025-12-01 18:04:33.793622'),(6,'防护','2025-12-01 18:04:33.804005','高级呼吸防护器具。','临光防护面罩',10.00,'RI-006','FFP2 型',NULL,NULL,'个','2025-12-01 18:04:33.804005'),(7,'药品','2025-12-01 18:04:33.810016','标配补液输注包。','斯卡蒂输液包',45.00,'RI-007','250ml',NULL,NULL,'袋','2025-12-01 18:04:33.810016'),(8,'药品','2025-12-01 18:04:33.816537','外用修复药膏。','红修复药膏',30.00,'RI-008','20g',NULL,NULL,'支','2025-12-01 18:04:33.816537'),(9,'消耗品','2025-12-01 18:04:33.825174','用于局部冷敷/护理。','术师冷敷凝胶',18.00,'RI-009','50g',NULL,NULL,'支','2025-12-01 18:04:33.825174'),(10,'器械','2025-12-01 18:04:33.832702','血管支架（模型用途）。','乌萨斯血管支架',450.00,'RI-010','小号/中号/大号',NULL,NULL,'套','2025-12-01 18:04:33.832702'),(11,'器械','2025-12-01 18:04:33.838702','一次性手术刀，锋利耐用。','黑钢手术刀',22.00,'RI-011','手术刀（单支）',NULL,NULL,'把','2025-12-01 18:04:33.839703'),(12,'试剂','2025-12-01 18:04:33.845952','快速检测试剂盒，用于常见感染检测。','源石检测试剂盒',320.00,'RI-012','单次/盒 (50)',NULL,NULL,'盒','2025-12-01 18:04:33.845952'),(13,'药品','2025-12-01 18:04:33.851460','高效型修复血清。','白面鸮修复血清',200.00,'RI-013','5ml/支',NULL,NULL,'支','2025-12-01 18:04:33.852469'),(14,'器械','2025-12-01 18:04:33.857472','急救保温垫片。','伊芙利特保温垫',12.00,'RI-014','20x30cm',NULL,NULL,'片','2025-12-01 18:04:33.857472'),(15,'器械','2025-12-01 18:04:33.864922','医用缝合线（可吸收）。','德克萨斯缝合线',60.00,'RI-015','可吸收缝合线',NULL,NULL,'包','2025-12-01 18:04:33.864922'),(16,'药品','2025-12-01 18:04:33.873476','滴眼药水类产品。','真理滴眼液',28.00,'RI-016','10ml',NULL,NULL,'瓶','2025-12-01 18:04:33.873476'),(17,'器械','2025-12-01 18:04:33.879483','骨折固定夹板。','陈固定夹板',35.00,'RI-017','通用夹板',NULL,NULL,'个','2025-12-01 18:04:33.879483'),(18,'药品','2025-12-01 18:04:33.884818','生理盐水补液。','拉普兰德生理盐水',22.00,'RI-018','500ml',NULL,NULL,'袋','2025-12-01 18:04:33.884818'),(19,'防护','2025-12-01 18:04:33.889074','防护用护臂，耐磨。','星熊防护护臂',18.00,'RI-019','医用护臂',NULL,NULL,'套','2025-12-01 18:04:33.889074'),(20,'疫苗','2025-12-01 18:04:33.896587','示范用疫苗（模拟）','塞雷娅示范疫苗',480.00,'RI-020','单剂',NULL,NULL,'支','2025-12-01 18:04:33.896587');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales_record`
--

DROP TABLE IF EXISTS `sales_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `commission_amount` decimal(10,2) DEFAULT NULL,
  `commission_rate` decimal(5,2) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `quantity` int NOT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sale_date` date NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `salesman_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKicbqw5plafmrrsoq1ety6ssr` (`customer_id`),
  KEY `FK2uch1o6uneajiij1fdl2ejlk5` (`product_id`),
  KEY `FK4qey2dulo1ql4gvlw7qm2tuaw` (`salesman_id`),
  CONSTRAINT `FK2uch1o6uneajiij1fdl2ejlk5` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FK4qey2dulo1ql4gvlw7qm2tuaw` FOREIGN KEY (`salesman_id`) REFERENCES `salesman` (`id`),
  CONSTRAINT `FKicbqw5plafmrrsoq1ety6ssr` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_record`
--

LOCK TABLES `sales_record` WRITE;
/*!40000 ALTER TABLE `sales_record` DISABLE KEYS */;
INSERT INTO `sales_record` VALUES (1,30.00,0.05,'2025-12-01 18:04:34.088585',5,'测试销售记录 1','2025-11-01',600.00,120.00,'2025-12-01 18:04:34.088585',1,1,1),(2,8.75,0.05,'2025-12-01 18:04:34.097900',7,'测试销售记录 2','2025-11-02',175.00,25.00,'2025-12-01 18:04:34.097900',2,4,1),(3,0.68,0.05,'2025-12-01 18:04:34.103417',9,'测试销售记录 3','2025-11-02',13.50,1.50,'2025-12-01 18:04:34.103417',2,5,1),(4,20.25,0.05,'2025-12-01 18:04:34.109145',9,'测试销售记录 4','2025-11-03',405.00,45.00,'2025-12-01 18:04:34.109145',3,7,1),(5,16.50,0.05,'2025-12-01 18:04:34.115172',11,'测试销售记录 5','2025-11-03',330.00,30.00,'2025-12-01 18:04:34.115172',3,8,1),(6,11.70,0.05,'2025-12-01 18:04:34.121688',13,'测试销售记录 6','2025-11-03',234.00,18.00,'2025-12-01 18:04:34.121688',3,9,1),(7,247.50,0.05,'2025-12-01 18:04:34.127711',11,'测试销售记录 7','2025-11-04',4950.00,450.00,'2025-12-01 18:04:34.127711',4,10,1),(8,130.00,0.05,'2025-12-01 18:04:34.133274',13,'测试销售记录 8','2025-11-05',2600.00,200.00,'2025-12-01 18:04:34.133274',5,13,2),(9,9.00,0.05,'2025-12-01 18:04:34.137274',15,'测试销售记录 9','2025-11-05',180.00,12.00,'2025-12-01 18:04:34.137274',5,14,2),(10,21.00,0.05,'2025-12-01 18:04:34.143814',15,'测试销售记录 10','2025-11-06',420.00,28.00,'2025-12-01 18:04:34.143814',6,16,2),(11,29.75,0.05,'2025-12-01 18:04:34.148097',17,'测试销售记录 11','2025-11-06',595.00,35.00,'2025-12-01 18:04:34.148097',6,17,2),(12,20.90,0.05,'2025-12-01 18:04:34.152608',19,'测试销售记录 12','2025-11-06',418.00,22.00,'2025-12-01 18:04:34.152608',6,18,2),(13,15.30,0.05,'2025-12-01 18:04:34.158617',17,'测试销售记录 13','2025-11-07',306.00,18.00,'2025-12-01 18:04:34.158617',7,19,2),(14,8.08,0.05,'2025-12-01 18:04:34.166912',19,'测试销售记录 14','2025-11-08',161.50,8.50,'2025-12-01 18:04:34.166912',8,2,2),(15,15.75,0.05,'2025-12-01 18:04:34.173435',21,'测试销售记录 15','2025-11-08',315.00,15.00,'2025-12-01 18:04:34.173435',8,3,2),(16,1.58,0.05,'2025-12-01 18:04:34.180434',21,'测试销售记录 16','2025-11-09',31.50,1.50,'2025-12-01 18:04:34.180434',9,5,3),(17,11.50,0.05,'2025-12-01 18:04:34.187313',23,'测试销售记录 17','2025-11-09',230.00,10.00,'2025-12-01 18:04:34.187313',9,6,3),(18,56.25,0.05,'2025-12-01 18:04:34.194331',25,'测试销售记录 18','2025-11-09',1125.00,45.00,'2025-12-01 18:04:34.194331',9,7,3),(19,34.50,0.05,'2025-12-01 18:04:34.200340',23,'测试销售记录 19','2025-11-10',690.00,30.00,'2025-12-01 18:04:34.200340',10,8,3),(20,27.50,0.05,'2025-12-01 18:04:34.206387',25,'测试销售记录 20','2025-11-11',550.00,22.00,'2025-12-01 18:04:34.206387',11,11,3),(21,432.00,0.05,'2025-12-01 18:04:34.211895',27,'测试销售记录 21','2025-11-11',8640.00,320.00,'2025-12-01 18:04:34.211895',11,12,3),(22,16.20,0.05,'2025-12-01 18:04:34.217914',27,'测试销售记录 22','2025-11-12',324.00,12.00,'2025-12-01 18:04:34.217914',12,14,3),(23,87.00,0.05,'2025-12-01 18:04:34.222836',29,'测试销售记录 23','2025-11-12',1740.00,60.00,'2025-12-01 18:04:34.222836',12,15,3),(24,43.40,0.05,'2025-12-01 18:04:34.227009',31,'测试销售记录 24','2025-11-12',868.00,28.00,'2025-12-01 18:04:34.227009',12,16,3),(25,50.75,0.05,'2025-12-01 18:04:34.232527',29,'测试销售记录 25','2025-11-13',1015.00,35.00,'2025-12-01 18:04:34.232527',13,17,4),(26,744.00,0.05,'2025-12-01 18:04:34.237535',31,'测试销售记录 26','2025-11-14',14880.00,480.00,'2025-12-01 18:04:34.237535',14,20,4),(27,198.00,0.05,'2025-12-01 18:04:34.242812',33,'测试销售记录 27','2025-11-14',3960.00,120.00,'2025-12-01 18:04:34.242812',14,1,4),(28,24.75,0.05,'2025-12-01 18:04:34.247332',33,'测试销售记录 28','2025-11-15',495.00,15.00,'2025-12-01 18:04:34.247332',15,3,4),(29,43.75,0.05,'2025-12-01 18:04:34.251843',35,'测试销售记录 29','2025-11-15',875.00,25.00,'2025-12-01 18:04:34.251843',15,4,4),(30,2.78,0.05,'2025-12-01 18:04:34.256853',37,'测试销售记录 30','2025-11-15',55.50,1.50,'2025-12-01 18:04:34.256853',15,5,4),(31,17.50,0.05,'2025-12-01 18:04:34.261395',35,'测试销售记录 31','2025-11-16',350.00,10.00,'2025-12-01 18:04:34.261395',16,6,4),(32,33.30,0.05,'2025-12-01 18:04:34.265952',37,'测试销售记录 32','2025-11-17',666.00,18.00,'2025-12-01 18:04:34.265952',17,9,5),(33,877.50,0.05,'2025-12-01 18:04:34.270059',39,'测试销售记录 33','2025-11-17',17550.00,450.00,'2025-12-01 18:04:34.270059',17,10,5),(34,624.00,0.05,'2025-12-01 18:04:34.274572',39,'测试销售记录 34','2025-11-18',12480.00,320.00,'2025-12-01 18:04:34.274572',18,12,5),(35,410.00,0.05,'2025-12-01 18:04:34.279575',41,'测试销售记录 35','2025-11-18',8200.00,200.00,'2025-12-01 18:04:34.279575',18,13,5),(36,25.80,0.05,'2025-12-01 18:04:34.283599',43,'测试销售记录 36','2025-11-18',516.00,12.00,'2025-12-01 18:04:34.283599',18,14,5),(37,123.00,0.05,'2025-12-01 18:04:34.287599',41,'测试销售记录 37','2025-11-19',2460.00,60.00,'2025-12-01 18:04:34.287599',19,15,5),(38,47.30,0.05,'2025-12-01 18:04:34.291599',43,'测试销售记录 38','2025-11-20',946.00,22.00,'2025-12-01 18:04:34.291599',20,18,5),(39,40.50,0.05,'2025-12-01 18:04:34.296956',45,'测试销售记录 39','2025-11-20',810.00,18.00,'2025-12-01 18:04:34.296956',20,19,5);
/*!40000 ALTER TABLE `sales_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `salesman`
--

DROP TABLE IF EXISTS `salesman`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesman` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `commission_rate` decimal(5,4) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `employee_no` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `hire_date` date DEFAULT NULL,
  `level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `qq` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `wechat` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK88t9gdsr0ljuulv40p7xmyesm` (`employee_no`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salesman`
--

LOCK TABLES `salesman` WRITE;
/*!40000 ALTER TABLE `salesman` DISABLE KEYS */;
INSERT INTO `salesman` VALUES (1,0.0600,'2025-12-01 14:39:00.644437','353830@luodedao.cn','EMP1764571140641','2025-12-01','senior','凯尔希','15671635683','1877581648',NULL,'2025-12-01 23:34:49.518898','Kaierxi','/uploads/avatars/d739a568-40e1-4780-85de-e949edf26796.jpg'),(2,0.0400,'2025-12-01 16:18:11.005113','353543@whut.edu.cn','EMP1764577091001','2025-12-01','junior','阿米娅','13036122820','2820205662',NULL,'2025-12-01 23:07:32.014647','Amiya','/uploads/avatars/a74fd197-ae3c-42c6-b147-963d3cd9700c.jpg'),(3,0.0400,'2025-12-01 16:51:57.794404','353854@whut.edu.cn','EMP1764579117792','2025-12-01','junior','维什戴尔','19573493808','1346473883',NULL,'2025-12-01 16:53:16.166764','Wiš\'adel','/uploads/avatars/3bb218c6-8939-4884-bfb1-4e569e0c7f75.jpg'),(4,0.0400,'2025-12-01 16:56:39.680757','gialinhhoang497@gmail.com','EMP1764579399680','2025-12-01','junior','银灰','18815970856','2835859791',NULL,'2025-12-01 16:57:25.427260','Yinghui','/uploads/avatars/1c67200c-ad5d-490c-9491-41f2af10be96.jpg'),(5,0.0400,'2025-12-01 16:58:28.813993','lijinjie23@gmail.com','EMP1764579508812','2025-12-01','junior','史尔特尔','15859501183','1299154732',NULL,'2025-12-01 16:59:47.002072','Surtr','/uploads/avatars/32352ede-349a-4745-a50d-14431d087e5d.jpg');
/*!40000 ALTER TABLE `salesman` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_record`
--

DROP TABLE IF EXISTS `service_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `customer_feedback` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `satisfaction_rating` int DEFAULT NULL,
  `service_date` date NOT NULL,
  `service_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `salesman_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7rwg1y72ofivw7lkg7f5dbasx` (`customer_id`),
  KEY `FKt5mpby87m44ppirvxq3r6wm7u` (`salesman_id`),
  CONSTRAINT `FK7rwg1y72ofivw7lkg7f5dbasx` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `FKt5mpby87m44ppirvxq3r6wm7u` FOREIGN KEY (`salesman_id`) REFERENCES `salesman` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_record`
--

LOCK TABLES `service_record` WRITE;
/*!40000 ALTER TABLE `service_record` DISABLE KEYS */;
INSERT INTO `service_record` VALUES (1,'为客户进行产品使用培训，讲解操作要点','2025-12-01 18:04:34.381483','服务态度好，解决问题及时','服务完成',4,'2025-11-16','产品培训','2025-12-01 18:04:34.381483',1,1),(2,'对客户现有设备进行维护保养','2025-12-01 18:04:34.386511','服务态度好，解决问题及时','服务完成',4,'2025-11-17','设备维护','2025-12-01 18:04:34.386511',3,1),(3,'为客户进行产品使用培训，讲解操作要点','2025-12-01 18:04:34.390028','服务态度好，解决问题及时','服务完成',4,'2025-11-18','产品培训','2025-12-01 18:04:34.390028',5,2),(4,'对客户现有设备进行维护保养','2025-12-01 18:04:34.394541','服务态度好，解决问题及时','服务完成',4,'2025-11-19','设备维护','2025-12-01 18:04:34.394541',7,2),(5,'为客户进行产品使用培训，讲解操作要点','2025-12-01 18:04:34.398542','服务态度好，解决问题及时','服务完成',4,'2025-11-20','产品培训','2025-12-01 18:04:34.398542',9,3),(6,'对客户现有设备进行维护保养','2025-12-01 18:04:34.401542','服务态度好，解决问题及时','服务完成',4,'2025-11-21','设备维护','2025-12-01 18:04:34.401542',11,3),(7,'为客户进行产品使用培训，讲解操作要点','2025-12-01 18:04:34.405153','服务态度好，解决问题及时','服务完成',4,'2025-11-22','产品培训','2025-12-01 18:04:34.405153',13,4),(8,'对客户现有设备进行维护保养','2025-12-01 18:04:34.408166','服务态度好，解决问题及时','服务完成',4,'2025-11-23','设备维护','2025-12-01 18:04:34.408166',15,4),(9,'为客户进行产品使用培训，讲解操作要点','2025-12-01 18:04:34.411661','服务态度好，解决问题及时','服务完成',4,'2025-11-24','产品培训','2025-12-01 18:04:34.411661',17,5),(10,'对客户现有设备进行维护保养','2025-12-01 18:04:34.414684','服务态度好，解决问题及时','服务完成',4,'2025-11-25','设备维护','2025-12-01 18:04:34.414684',19,5);
/*!40000 ALTER TABLE `service_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `salesman_id` bigint DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2025-12-01 14:37:31.054470','123','admin',NULL,'2025-12-01 14:37:31.054470','admin'),(2,'2025-12-01 14:39:00.663158','123','salesman',1,'2025-12-01 14:39:00.663158','salesman1'),(3,'2025-12-01 16:18:11.015436','123','salesman',2,'2025-12-01 16:18:11.015436','salesman2'),(4,'2025-12-01 16:51:57.810941','123','salesman',3,'2025-12-01 16:51:57.810941','salesman3'),(5,'2025-12-01 16:56:39.688077','123','salesman',4,'2025-12-01 16:56:39.688077','salesman4'),(6,'2025-12-01 16:58:28.819991','123','salesman',5,'2025-12-01 16:58:28.819991','salesman5');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'salesman_performance_management_system'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-07 12:54:43
