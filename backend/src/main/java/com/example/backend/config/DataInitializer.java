package com.example.backend.config;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepo,
            ProductRepository productRepo,
            CustomerRepository customerRepo,
            SalesRecordRepository salesRecordRepo,
            ContactRecordRepository contactRecordRepo,
            ServiceRecordRepository serviceRecordRepo,
            CollectionRecordRepository collectionRecordRepo,
            ComplaintRecordRepository complaintRecordRepo,
            SalesmanRepository salesmanRepo) {
        return args -> {
            // 只在产品表为空时插入测试数据
            if (productRepo.count() > 0) {
                System.out.println("数据库已有产品数据，跳过测试数据初始化");
                return;
            }

            System.out.println("开始初始化测试数据...");

            // // 1. 插入20个医疗产�?
            // Product[] products = {
            //     createProduct("RI-001", "阿米娅急救血�?, "药品", "10ml/�?, "�?, 120.00, "罗德岛研发的基础治疗血清，用于急救�?),
            //     createProduct("RI-002", "能天使注射器", "器械", "一次性注射器", "�?, 8.50, "轻量注射器，适配多种给药方案�?),
            //     createProduct("RI-003", "银老板创口绷带", "消耗品", "5cm x 5m", "�?, 15.00, "高强度创口绷带�?),
            //     createProduct("RI-004", "龙门消毒�?, "药品", "100ml", "�?, 25.00, "用于消毒与伤口清洁�?),
            //     createProduct("RI-005", "罗德岛医用口�?, "防护", "一次性口�?, "�?, 1.50, "医用防护口罩，过滤效率高�?),
            //     createProduct("RI-006", "临光防护面罩", "防护", "FFP2 �?, "�?, 10.00, "高级呼吸防护器具�?),
            //     createProduct("RI-007", "斯卡蒂输液包", "药品", "250ml", "�?, 45.00, "标配补液输注包�?),
            //     createProduct("RI-008", "红修复药�?, "药品", "20g", "�?, 30.00, "外用修复药膏�?),
            //     createProduct("RI-009", "术师冷敷凝胶", "消耗品", "50g", "�?, 18.00, "用于局部冷�?护理�?),
            //     createProduct("RI-010", "乌萨斯血管支�?, "器械", "小号/中号/大号", "�?, 450.00, "血管支架（模型用途）�?),
            //     createProduct("RI-011", "黑钢手术刀", "器械", "手术刀（单支）", "�?, 22.00, "一次性手术刀，锋利耐用�?),
            //     createProduct("RI-012", "源石检测试剂盒", "试剂", "单次/�?(50)", "�?, 320.00, "快速检测试剂盒，用于常见感染检测�?),
            //     createProduct("RI-013", "白面鸮修复血�?, "药品", "5ml/�?, "�?, 200.00, "高效型修复血清�?),
            //     createProduct("RI-014", "伊芙利特保温�?, "器械", "20x30cm", "�?, 12.00, "急救保温垫片�?),
            //     createProduct("RI-015", "德克萨斯缝合�?, "器械", "可吸收缝合线", "�?, 60.00, "医用缝合线（可吸收）�?),
            //     createProduct("RI-016", "真理滴眼�?, "药品", "10ml", "�?, 28.00, "滴眼药水类产品�?),
            //     createProduct("RI-017", "陈固定夹�?, "器械", "通用夹板", "�?, 35.00, "骨折固定夹板�?),
            //     createProduct("RI-018", "拉普兰德生理盐水", "药品", "500ml", "�?, 22.00, "生理盐水补液�?),
            //     createProduct("RI-019", "星熊防护护臂", "防护", "医用护臂", "�?, 18.00, "防护用护臂，耐磨�?),
            //     createProduct("RI-020", "塞雷娅示范疫�?, "疫苗", "单剂", "�?, 480.00, "示范用疫苗（模拟�?)
            // };
            // for (Product p : products) {
            //     productRepo.save(p);
            // }
            // System.out.println("已插�?20 个产�?);

            // // 2. 获取已存在的销售员（假设ID�?-5�?
            // List<Salesman> salesmen = salesmanRepo.findAll();
            // if (salesmen.size() < 5) {
            //     System.out.println("警告：销售员数量不足5个，部分客户可能无法关联");
            //     return;
            // }

            // // 3. 为每个销售员创建4个客户（企业3�?个人1个，�?0个）
            // Customer[] customers = {
            //     createCustomer("罗德岛总医�?, "阿米�?, "021-10001001", "amiya@rhodes.example", "罗德岛港区近�?, "enterprise", "A", salesmen.get(0).getId(), "罗德岛直属合作医�?),
            //     createCustomer("龙门第七诊所", "玛莎医生", "021-20002002", "martha@lungmen.example", "龙门第七�?, "enterprise", "B", salesmen.get(0).getId(), "社区诊所"),
            //     createCustomer("乌萨斯东方药�?, "银狼", "031-30003003", "sw@ursus.example", "乌萨斯首都市�?, "enterprise", "B", salesmen.get(0).getId(), "连锁药房"),
            //     createCustomer("博士", "博士", "021-88888888", "doctor@personal.example", "罗德岛宿舍区", "individual", "A", salesmen.get(0).getId(), "个人客户，长期采购医疗用�?),

            //     createCustomer("维克汉姆社区医院", "临光", "051-50005005", "nearl@wickham.example", "维克汉姆东区", "enterprise", "B", salesmen.get(1).getId(), "区域医院"),
            //     createCustomer("苔原流动医疗�?, "伊芙利特", "061-60006006", "ifrit@tundra.example", "苔原3号路", "enterprise", "C", salesmen.get(1).getId(), "流动医疗�?),
            //     createCustomer("谢拉格综合诊所", "斯卡蒂医�?, "071-70007007", "skadi@kjerag.example", "谢拉格山�?, "enterprise", "C", salesmen.get(1).getId(), "兼顾兽医的综合诊所"),
            //     createCustomer("凯尔�?, "凯尔�?, "021-99999999", "kaltsit@personal.example", "罗德岛医疗部", "individual", "A", salesmen.get(1).getId(), "个人采购，医疗研究用"),

            //     createCustomer("希斯洛诊所", "塞雷�?, "091-90009009", "saria@heathrow.example", "希斯洛中心区", "enterprise", "B", salesmen.get(2).getId(), "社区医疗中心"),
            //     createCustomer("卡西米尔医疗中心", "银灰", "101-10010010", "sa@kazimierz.example", "卡西米尔老城", "enterprise", "A", salesmen.get(2).getId(), "高端门诊"),
            //     createCustomer("熔岩温泉药局", "熔泉", "111-11011011", "ifrit2@lava.example", "熔岩温泉�?, "enterprise", "C", salesmen.get(2).getId(), "小型药局"),
            //     createCustomer("华法�?, "华法�?, "091-77777777", "warfarin@personal.example", "罗德岛血�?, "individual", "B", salesmen.get(2).getId(), "个人客户，医疗耗材采购"),

            //     createCustomer("维多利亚广场诊所", "苏苏�?, "131-13013013", "sussurro@victoria.example", "维多利亚广场", "enterprise", "B", salesmen.get(3).getId(), "社区诊所"),
            //     createCustomer("盖亚康复中心", "瑕光", "141-14014014", "nearl2@gaia.example", "盖亚康复公园", "enterprise", "A", salesmen.get(3).getId(), "康复中心"),
            //     createCustomer("山口医院", "拉普兰德", "151-15015015", "lappland@mp.example", "山口要道", "enterprise", "B", salesmen.get(3).getId(), "山区医院"),
            //     createCustomer("闪灵", "闪灵", "131-66666666", "shining@personal.example", "圣堂医疗�?, "individual", "A", salesmen.get(3).getId(), "个人采购，高端医疗器�?),

            //     createCustomer("应急响应诊疗点", "杜宾", "171-17017017", "amiya2@contingency.example", "应急区�?, "enterprise", "B", salesmen.get(4).getId(), "快速响应诊疗点"),
            //     createCustomer("黑钢前哨�?, "�?, "181-18018018", "chen@blacksteel.example", "黑钢要塞", "enterprise", "A", salesmen.get(4).getId(), "军用医疗保障"),
            //     createCustomer("第二风暴药房", "德克萨斯", "191-19019019", "texas@sstorm.example", "第二风暴�?, "enterprise", "C", salesmen.get(4).getId(), "私人药房"),
            //     createCustomer("赫默", "赫默", "171-55555555", "silence@personal.example", "罗德岛研发部", "individual", "B", salesmen.get(4).getId(), "个人客户，实验器材采�?)
            // };
            // // 确保客户姓名或联系人不会与销售员姓名重复，若重复则追加后缀
            // List<String> salesmanNames = salesmen.stream().map(Salesman::getName).toList();
            // for (Customer c : customers) {
            //     if (c.getName() != null && salesmanNames.contains(c.getName())) {
            //         c.setName(c.getName() + "（客户）");
            //     }
            //     if (c.getContactPerson() != null && salesmanNames.contains(c.getContactPerson())) {
            //         c.setContactPerson(c.getContactPerson() + "（客户）");
            //     }
            //     customerRepo.save(c);
            // }
            // System.out.println("已插�?20 个客�?);

            // // 重新获取保存后的实体（带ID�?
            // List<Customer> savedCustomers = customerRepo.findAll();

            // // 4. 生成销售记录（已手动插�?0条测试数据，此处不再自动生成�?
            // System.out.println("跳过销售记录自动生成（使用手动插入�?0条数据）");
            // /*
            // LocalDate startDate = LocalDate.of(2023, 1, 1);
            // LocalDate endDate = LocalDate.now();
            // long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            
            // java.util.Random random = new java.util.Random();
            // int salesCount = 0;
            // for (int i = 0; i < 55; i++) {
            //     // 随机选择销售员、客户、产�?
            //     Salesman sm = salesmen.get(random.nextInt(salesmen.size()));
            //     Customer cust = savedCustomers.get(random.nextInt(savedCustomers.size()));
            //     Product prod = savedProducts.get(random.nextInt(savedProducts.size()));
                
            //     // 随机生成销售日期（2023年至今）
            //     long randomDays = random.nextLong(daysBetween + 1);
            //     LocalDate saleDate = startDate.plusDays(randomDays);
                
            //     // 随机生成数量�?-50�?
            //     int quantity = 1 + random.nextInt(50);
                
            //     SalesRecord sr = new SalesRecord();
            //     sr.setOrderNo(String.format("O%08d", i + 1)); // 生成订单�?
            //     sr.setSalesman(sm);
            //     sr.setCustomer(cust);
            //     sr.setProduct(prod);
            //     sr.setQuantity(quantity);
            //     sr.setUnitPrice(prod.getPrice());
            //     sr.setTotalAmount(prod.getPrice().multiply(BigDecimal.valueOf(quantity)));
            //     sr.setCommissionRate(sm.getCommissionRate() != null ? sm.getCommissionRate() : BigDecimal.valueOf(0.05));
            //     sr.setCommissionAmount(sr.getTotalAmount().multiply(sr.getCommissionRate()));
            //     sr.setSaleDate(saleDate);
                
            //     String[] remarks = {
            //         "正常订单", "客户续订", "新客户首�?, "批量采购", 
            //         "促销活动订单", "老客户回�?, "推荐客户订单", "年度采购"
            //     };
            //     sr.setRemark(remarks[random.nextInt(remarks.length)]);
                
            //     salesRecordRepo.save(sr);
            //     salesCount++;
            // }
            // System.out.println("已插�?" + salesCount + " 条销售记录（时间跨度: 2023-01-01 �?" + endDate + "�?);
            // */

            // // 5. 生成联络记录
            // int contactCount = 0;
            // for (int i = 0; i < savedCustomers.size(); i++) {
            //     Customer cust = savedCustomers.get(i);
            //     Salesman sm = salesmen.get(i / 4);
                
            //     String[] methods = {"phone", "email", "wechat", "visit"};
            //     String[] summaries = {
            //         "讨论新产品合作方�?,
            //         "了解客户近期采购需�?,
            //         "跟进上次订单执行情况",
            //         "介绍公司新推出的产品�?
            //     };
                
            //     ContactRecord cr = new ContactRecord();
            //     cr.setSalesman(sm);
            //     cr.setCustomer(cust);
            //     cr.setContactDate(LocalDateTime.now().minusDays(20 - i % 20));
            //     cr.setContactWay(methods[i % 4]);
            //     cr.setContent(summaries[i % 4]);
            //     cr.setOutcome("interested");
            //     cr.setIntentionLevel("B");
                
            //     // 生成联络单号 CNT-YYYYMMDD-XXX
            //     String datePrefix = cr.getContactDate().toLocalDate().toString().replace("-", "");
            //     cr.setContactNo(String.format("CNT-%s-%03d", datePrefix, contactCount + 1));
                
            //     contactRecordRepo.save(cr);
            //     contactCount++;
            // }
            // System.out.println("已插�?" + contactCount + " 条联络记�?);

            // // 6. 生成服务记录 (7条测试数�?
            // int serviceCount = 0;
            // List<Product> allProducts = productRepo.findAll();
            
            // // 服务记录1: 凯尔�?- 核心设备维修
            // if (savedCustomers.size() > 0 && allProducts.size() > 13) {
            //     ServiceRecord sr1 = new ServiceRecord();
            //     sr1.setServiceNo("SVC-20251210-001");
            //     sr1.setSalesman(salesmen.get(0));
            //     sr1.setCustomer(savedCustomers.get(0));
            //     sr1.setProduct(allProducts.get(13));
            //     sr1.setRelatedOrderNo("ORD-20251210-011");
            //     sr1.setServiceType("repair");
            //     sr1.setStartTime(LocalDateTime.of(2025, 12, 10, 14, 0));
            //     sr1.setEndTime(LocalDateTime.of(2025, 12, 10, 18, 0));
            //     sr1.setContent("血液透析仪核心组件过热报警，影响正常透析作业");
            //     sr1.setSolution("更换了散热模块，并重新校准了源石供能参数，测试运行正常�?);
            //     sr1.setSatisfaction(5);
            //     sr1.setStatus("completed");
            //     serviceRecordRepo.save(sr1);
            //     serviceCount++;
            // }
            
            // // 服务记录2: 银灰 - 大客户定期保�?
            // if (savedCustomers.size() > 17 && allProducts.size() > 4) {
            //     ServiceRecord sr2 = new ServiceRecord();
            //     sr2.setServiceNo("SVC-20251212-002");
            //     sr2.setSalesman(salesmen.get(3));
            //     sr2.setCustomer(savedCustomers.get(17));
            //     sr2.setProduct(allProducts.get(4));
            //     sr2.setRelatedOrderNo("ORD-20251217-018");
            //     sr2.setServiceType("maintenance");
            //     sr2.setStartTime(LocalDateTime.of(2025, 12, 12, 10, 0));
            //     sr2.setEndTime(LocalDateTime.of(2025, 12, 12, 12, 0));
            //     sr2.setContent("黑钢前哨站防护服季度例行检�?);
            //     sr2.setSolution("检查了所有面罩的气密性和过滤层寿命，全部合格，无需更换�?);
            //     sr2.setSatisfaction(5);
            //     sr2.setStatus("completed");
            //     serviceRecordRepo.save(sr2);
            //     serviceCount++;
            // }
            
            // // 服务记录3: 阿米�?- 产品使用培训
            // if (savedCustomers.size() > 5 && allProducts.size() > 8) {
            //     ServiceRecord sr3 = new ServiceRecord();
            //     sr3.setServiceNo("SVC-20251213-003");
            //     sr3.setSalesman(salesmen.get(1));
            //     sr3.setCustomer(savedCustomers.get(5));
            //     sr3.setProduct(allProducts.get(8));
            //     sr3.setRelatedOrderNo("ORD-20251213-014");
            //     sr3.setServiceType("training");
            //     sr3.setStartTime(LocalDateTime.of(2025, 12, 13, 13, 0));
            //     sr3.setEndTime(LocalDateTime.of(2025, 12, 13, 16, 0));
            //     sr3.setContent("苔原医疗站急救人员培训：自锁式止血带的快速使�?);
            //     sr3.setSolution("进行了现场演示和全员实操考核，确保人人会用�?);
            //     sr3.setSatisfaction(5);
            //     sr3.setStatus("completed");
            //     serviceRecordRepo.save(sr3);
            //     serviceCount++;
            // }
            
            // // 服务记录4: 史尔特尔 - 疑难故障排查（进行中�?
            // if (savedCustomers.size() > 10 && allProducts.size() > 11) {
            //     ServiceRecord sr4 = new ServiceRecord();
            //     sr4.setServiceNo("SVC-20251214-004");
            //     sr4.setSalesman(salesmen.get(4));
            //     sr4.setCustomer(savedCustomers.get(10));
            //     sr4.setProduct(allProducts.get(11));
            //     sr4.setRelatedOrderNo("ORD-20251219-020");
            //     sr4.setServiceType("repair");
            //     sr4.setStartTime(LocalDateTime.of(2025, 12, 14, 9, 0));
            //     sr4.setContent("区域监测仪读数出现异常跳动，数值不稳定");
            //     sr4.setSolution("初步判断是源石粉尘干扰了传感器，正在拆机清理中�?);
            //     sr4.setStatus("processing");
            //     serviceRecordRepo.save(sr4);
            //     serviceCount++;
            // }
            
            // // 服务记录5: 凯尔�?- 专家咨询
            // if (savedCustomers.size() > 3) {
            //     ServiceRecord sr5 = new ServiceRecord();
            //     sr5.setServiceNo("SVC-20251215-005");
            //     sr5.setSalesman(salesmen.get(0));
            //     sr5.setCustomer(savedCustomers.get(3));
            //     sr5.setServiceType("consult");
            //     sr5.setStartTime(LocalDateTime.of(2025, 12, 15, 20, 0));
            //     sr5.setEndTime(LocalDateTime.of(2025, 12, 15, 21, 0));
            //     sr5.setContent("博士关于矿石病抑制剂下一阶段疗程的用药咨�?);
            //     sr5.setSolution("根据最新体检报告，制定了新的理智液配方和作息建议�?);
            //     sr5.setSatisfaction(5);
            //     sr5.setStatus("completed");
            //     serviceRecordRepo.save(sr5);
            //     serviceCount++;
            // }
            
            // // 服务记录6: W - 设备上门安装
            // if (savedCustomers.size() > 2 && allProducts.size() > 9) {
            //     ServiceRecord sr6 = new ServiceRecord();
            //     sr6.setServiceNo("SVC-20251216-006");
            //     sr6.setSalesman(salesmen.get(2));
            //     sr6.setCustomer(savedCustomers.get(2));
            //     sr6.setProduct(allProducts.get(9));
            //     sr6.setRelatedOrderNo("ORD-20251205-006");
            //     sr6.setServiceType("installation");
            //     sr6.setStartTime(LocalDateTime.of(2025, 12, 16, 10, 30));
            //     sr6.setEndTime(LocalDateTime.of(2025, 12, 16, 11, 30));
            //     sr6.setContent("上门安装高能源石能量稳定剂存放柜");
            //     sr6.setSolution("已固定在墙面并接通警报电源，交付钥匙�?);
            //     sr6.setSatisfaction(4);
            //     sr6.setStatus("completed");
            //     serviceRecordRepo.save(sr6);
            //     serviceCount++;
            // }
            
            // // 服务记录7: 银灰 - 服务取消
            // if (savedCustomers.size() > 12) {
            //     ServiceRecord sr7 = new ServiceRecord();
            //     sr7.setServiceNo("SVC-20251217-007");
            //     sr7.setSalesman(salesmen.get(3));
            //     sr7.setCustomer(savedCustomers.get(12));
            //     sr7.setServiceType("maintenance");
            //     sr7.setStartTime(LocalDateTime.of(2025, 12, 17, 9, 0));
            //     sr7.setContent("维多利亚广场诊所例行巡检");
            //     sr7.setSolution("客户临时有急诊手术，要求改期，本次服务单取消�?);
            //     sr7.setStatus("cancelled");
            //     serviceRecordRepo.save(sr7);
            //     serviceCount++;
            // }
            
            // System.out.println("已插�?" + serviceCount + " 条服务记�?);

            // // 7. 生成催款记录 (8条测试数�?
            // int collectionCount = 0;
            
            // // 催款记录1: 部分回款
            // CollectionRecord col1 = new CollectionRecord();
            // col1.setCollectionNo("COL-20251210-001");
            // col1.setOrderNo("ORD-20251210-011");
            // col1.setSalesman(salesmen.get(0));
            // col1.setCollectionDate(LocalDate.of(2025, 12, 10));
            // col1.setCurrentAmount(new BigDecimal("5000.00"));
            // col1.setRemainingAmount(new BigDecimal("3000.00"));
            // col1.setCollectionStatus("pending");
            // col1.setRemark("博士首次付款，银行转�?);
            // collectionRecordRepo.save(col1);
            // collectionCount++;
            
            // // 催款记录2: 全额回款
            // CollectionRecord col2 = new CollectionRecord();
            // col2.setCollectionNo("COL-20251212-002");
            // col2.setOrderNo("ORD-20251217-018");
            // col2.setSalesman(salesmen.get(3));
            // col2.setCollectionDate(LocalDate.of(2025, 12, 12));
            // col2.setCurrentAmount(new BigDecimal("4500.00"));
            // col2.setRemainingAmount(BigDecimal.ZERO);
            // col2.setCollectionStatus("completed");
            // col2.setRemark("黑钢前哨站全额付�?);
            // collectionRecordRepo.save(col2);
            // collectionCount++;
            
            // // 催款记录3: 仅催�?
            // CollectionRecord col3 = new CollectionRecord();
            // col3.setCollectionNo("COL-20251207-003");
            // col3.setOrderNo("ORD-20251218-019");
            // col3.setSalesman(salesmen.get(2));
            // col3.setCollectionDate(LocalDate.of(2025, 12, 7));
            // col3.setCurrentAmount(BigDecimal.ZERO);
            // col3.setRemainingAmount(new BigDecimal("3200.00"));
            // col3.setCollectionStatus("pending");
            // col3.setRemark("电话催收，客户表示下周付�?);
            // collectionRecordRepo.save(col3);
            // collectionCount++;
            
            // // 催款记录4: 部分回款
            // CollectionRecord col4 = new CollectionRecord();
            // col4.setCollectionNo("COL-20251215-004");
            // col4.setOrderNo("ORD-20251215-016");
            // col4.setSalesman(salesmen.get(1));
            // col4.setCollectionDate(LocalDate.of(2025, 12, 15));
            // col4.setCurrentAmount(new BigDecimal("2000.00"));
            // col4.setRemainingAmount(new BigDecimal("1500.00"));
            // col4.setCollectionStatus("pending");
            // col4.setRemark("苔原医疗站支付宝转账");
            // collectionRecordRepo.save(col4);
            // collectionCount++;
            
            // // 催款记录5: 第二次催�?
            // CollectionRecord col5 = new CollectionRecord();
            // col5.setCollectionNo("COL-20251208-005");
            // col5.setOrderNo("ORD-20251219-020");
            // col5.setSalesman(salesmen.get(4));
            // col5.setCollectionDate(LocalDate.of(2025, 12, 8));
            // col5.setCurrentAmount(BigDecimal.ZERO);
            // col5.setRemainingAmount(new BigDecimal("2800.00"));
            // col5.setCollectionStatus("pending");
            // col5.setRemark("微信联系，客户未回复");
            // collectionRecordRepo.save(col5);
            // collectionCount++;
            
            // // 催款记录6: 全额回款
            // CollectionRecord col6 = new CollectionRecord();
            // col6.setCollectionNo("COL-20251216-006");
            // col6.setOrderNo("ORD-20251216-017");
            // col6.setSalesman(salesmen.get(0));
            // col6.setCollectionDate(LocalDate.of(2025, 12, 16));
            // col6.setCurrentAmount(new BigDecimal("6000.00"));
            // col6.setRemainingAmount(BigDecimal.ZERO);
            // col6.setCollectionStatus("completed");
            // col6.setRemark("罗德岛总部现金支付");
            // collectionRecordRepo.save(col6);
            // collectionCount++;
            
            // // 催款记录7: 部分回款
            // CollectionRecord col7 = new CollectionRecord();
            // col7.setCollectionNo("COL-20251214-007");
            // col7.setOrderNo("ORD-20251214-015");
            // col7.setSalesman(salesmen.get(1));
            // col7.setCollectionDate(LocalDate.of(2025, 12, 14));
            // col7.setCurrentAmount(new BigDecimal("1500.00"));
            // col7.setRemainingAmount(new BigDecimal("2000.00"));
            // col7.setCollectionStatus("pending");
            // col7.setRemark("客户承诺下月补齐余款");
            // collectionRecordRepo.save(col7);
            // collectionCount++;
            
            // // 催款记录8: 全额回款
            // CollectionRecord col8 = new CollectionRecord();
            // col8.setCollectionNo("COL-20251205-008");
            // col8.setOrderNo("ORD-20251205-006");
            // col8.setSalesman(salesmen.get(2));
            // col8.setCollectionDate(LocalDate.of(2025, 12, 5));
            // col8.setCurrentAmount(new BigDecimal("3800.00"));
            // col8.setRemainingAmount(BigDecimal.ZERO);
            // col8.setCollectionStatus("completed");
            // col8.setRemark("卡西尔医疗中心银行转�?);
            // collectionRecordRepo.save(col8);
            // collectionCount++;
            
            // System.out.println("已插�?" + collectionCount + " 条催款记�?);

            // // 8. 生成投诉记录 (10条，包含不同严重等级和状�?
            // int complaintCount = 0;
            
            // // 投诉1: Critical - 严重质量问题 (pending)
            // if (savedCustomers.size() > 0 && allProducts.size() > 5) {
            //     ComplaintRecord cmp1 = new ComplaintRecord();
            //     cmp1.setComplaintNo("CMP-20251208-001");
            //     cmp1.setCustomer(savedCustomers.get(0));
            //     cmp1.setSalesman(salesmen.get(0));
            //     cmp1.setRelatedOrderNo("ORD-20251210-011");
            //     cmp1.setRelatedProduct(allProducts.get(5));
            //     cmp1.setComplaintType("quality");
            //     cmp1.setSeverity("Critical");
            //     cmp1.setContent("药品出现严重质量问题，导致患者过敏反应！需要立即召回该批次产品并进行全面检测！");
            //     cmp1.setEvidenceImage("/uploads/complaints/evidence_001.jpg");
            //     cmp1.setStatus("pending");
            //     cmp1.setComplaintTime(LocalDateTime.of(2025, 12, 8, 9, 30));
            //     complaintRecordRepo.save(cmp1);
            //     complaintCount++;
            // }
            
            // // 投诉2: High - 物流延误 (processing)
            // if (savedCustomers.size() > 4 && allProducts.size() > 8) {
            //     ComplaintRecord cmp2 = new ComplaintRecord();
            //     cmp2.setComplaintNo("CMP-20251207-002");
            //     cmp2.setCustomer(savedCustomers.get(4));
            //     cmp2.setSalesman(salesmen.get(1));
            //     cmp2.setRelatedOrderNo("ORD-20251215-016");
            //     cmp2.setRelatedProduct(allProducts.get(8));
            //     cmp2.setComplaintType("logistics");
            //     cmp2.setSeverity("High");
            //     cmp2.setContent("订单延迟3天未到货，影响患者治疗计划。已多次催促仍未得到明确答复�?);
            //     cmp2.setEvidenceImage("/uploads/complaints/evidence_002.jpg");
            //     cmp2.setSolution("已联系物流部门加急处理，预计明日送达�?);
            //     cmp2.setStatus("processing");
            //     cmp2.setComplaintTime(LocalDateTime.of(2025, 12, 7, 14, 20));
            //     complaintRecordRepo.save(cmp2);
            //     complaintCount++;
            // }
            
            // // 投诉3: Critical - 欺诈行为 (pending)
            // if (savedCustomers.size() > 8) {
            //     ComplaintRecord cmp3 = new ComplaintRecord();
            //     cmp3.setComplaintNo("CMP-20251206-003");
            //     cmp3.setCustomer(savedCustomers.get(8));
            //     cmp3.setSalesman(salesmen.get(2));
            //     cmp3.setRelatedOrderNo("ORD-20251218-019");
            //     cmp3.setComplaintType("fraud");
            //     cmp3.setSeverity("Critical");
            //     cmp3.setContent("销售员承诺的优惠价格与实际收费不符，涉嫌欺诈！要求退款并调查�?);
            //     cmp3.setEvidenceImage("/uploads/complaints/evidence_003.jpg");
            //     cmp3.setStatus("pending");
            //     cmp3.setComplaintTime(LocalDateTime.of(2025, 12, 6, 16, 45));
            //     complaintRecordRepo.save(cmp3);
            //     complaintCount++;
            // }
            
            // // 投诉4: Normal - 服务态度 (resolved)
            // if (savedCustomers.size() > 12) {
            //     ComplaintRecord cmp4 = new ComplaintRecord();
            //     cmp4.setComplaintNo("CMP-20251205-004");
            //     cmp4.setCustomer(savedCustomers.get(12));
            //     cmp4.setSalesman(salesmen.get(3));
            //     cmp4.setRelatedOrderNo("ORD-20251217-018");
            //     cmp4.setComplaintType("attitude");
            //     cmp4.setSeverity("Normal");
            //     cmp4.setContent("销售人员接听电话态度不佳，语气生硬，希望加强服务培训�?);
            //     cmp4.setSolution("已与销售员沟通，要求改进服务态度。安排客服回访道歉�?);
            //     cmp4.setCustomerFeedback("客户接受道歉，表示满�?);
            //     cmp4.setStatus("resolved");
            //     cmp4.setComplaintTime(LocalDateTime.of(2025, 12, 5, 10, 15));
            //     cmp4.setResolveTime(LocalDateTime.of(2025, 12, 6, 14, 30));
            //     complaintRecordRepo.save(cmp4);
            //     complaintCount++;
            // }
            
            // // 投诉5: High - 质量问题 (processing)
            // if (savedCustomers.size() > 16 && allProducts.size() > 12) {
            //     ComplaintRecord cmp5 = new ComplaintRecord();
            //     cmp5.setComplaintNo("CMP-20251204-005");
            //     cmp5.setCustomer(savedCustomers.get(16));
            //     cmp5.setSalesman(salesmen.get(4));
            //     cmp5.setRelatedOrderNo("ORD-20251219-020");
            //     cmp5.setRelatedProduct(allProducts.get(12));
            //     cmp5.setComplaintType("quality");
            //     cmp5.setSeverity("High");
            //     cmp5.setContent("医疗设备存在功能缺陷，无法正常使用。要求更换新设备�?);
            //     cmp5.setEvidenceImage("/uploads/complaints/evidence_005.jpg");
            //     cmp5.setSolution("技术部门已确认设备问题，正在安排更换事宜�?);
            //     cmp5.setStatus("processing");
            //     cmp5.setComplaintTime(LocalDateTime.of(2025, 12, 4, 11, 0));
            //     complaintRecordRepo.save(cmp5);
            //     complaintCount++;
            // }
            
            // // 投诉6: Low - 价格疑问 (resolved)
            // if (savedCustomers.size() > 3) {
            //     ComplaintRecord cmp6 = new ComplaintRecord();
            //     cmp6.setComplaintNo("CMP-20251203-006");
            //     cmp6.setCustomer(savedCustomers.get(3));
            //     cmp6.setSalesman(salesmen.get(0));
            //     cmp6.setRelatedOrderNo("ORD-20251216-017");
            //     cmp6.setComplaintType("price");
            //     cmp6.setSeverity("Low");
            //     cmp6.setContent("对报价单中的某项费用存在疑问，希望说明�?);
            //     cmp6.setSolution("已详细解释费用构成，客户表示理解�?);
            //     cmp6.setCustomerFeedback("问题已解决，无异�?);
            //     cmp6.setStatus("resolved");
            //     cmp6.setComplaintTime(LocalDateTime.of(2025, 12, 3, 15, 20));
            //     cmp6.setResolveTime(LocalDateTime.of(2025, 12, 3, 16, 0));
            //     complaintRecordRepo.save(cmp6);
            //     complaintCount++;
            // }
            
            // // 投诉7: Normal - 物流包装 (closed)
            // if (savedCustomers.size() > 7 && allProducts.size() > 3) {
            //     ComplaintRecord cmp7 = new ComplaintRecord();
            //     cmp7.setComplaintNo("CMP-20251202-007");
            //     cmp7.setCustomer(savedCustomers.get(7));
            //     cmp7.setSalesman(salesmen.get(1));
            //     cmp7.setRelatedOrderNo("ORD-20251214-015");
            //     cmp7.setRelatedProduct(allProducts.get(3));
            //     cmp7.setComplaintType("logistics");
            //     cmp7.setSeverity("Normal");
            //     cmp7.setContent("外包装箱有轻微破损，但产品完好。建议改进包装�?);
            //     cmp7.setSolution("已反馈至物流部门，加强包装质量。给予客�?0元优惠券补偿�?);
            //     cmp7.setCustomerFeedback("满意处理结果");
            //     cmp7.setStatus("closed");
            //     cmp7.setComplaintTime(LocalDateTime.of(2025, 12, 2, 9, 40));
            //     cmp7.setResolveTime(LocalDateTime.of(2025, 12, 2, 17, 0));
            //     complaintRecordRepo.save(cmp7);
            //     complaintCount++;
            // }
            
            // // 投诉8: High - 服务质量 (pending)
            // if (savedCustomers.size() > 11) {
            //     ComplaintRecord cmp8 = new ComplaintRecord();
            //     cmp8.setComplaintNo("CMP-20251201-008");
            //     cmp8.setCustomer(savedCustomers.get(11));
            //     cmp8.setSalesman(salesmen.get(2));
            //     cmp8.setComplaintType("attitude");
            //     cmp8.setSeverity("High");
            //     cmp8.setContent("销售员多次承诺的售后服务未兑现，电话不接、微信不回，严重失信�?);
            //     cmp8.setEvidenceImage("/uploads/complaints/evidence_008.jpg");
            //     cmp8.setStatus("pending");
            //     cmp8.setComplaintTime(LocalDateTime.of(2025, 12, 1, 13, 10));
            //     complaintRecordRepo.save(cmp8);
            //     complaintCount++;
            // }
            
            // // 投诉9: Low - 咨询响应�?(resolved)
            // if (savedCustomers.size() > 15) {
            //     ComplaintRecord cmp9 = new ComplaintRecord();
            //     cmp9.setComplaintNo("CMP-20251130-009");
            //     cmp9.setCustomer(savedCustomers.get(15));
            //     cmp9.setSalesman(salesmen.get(3));
            //     cmp9.setComplaintType("attitude");
            //     cmp9.setSeverity("Low");
            //     cmp9.setContent("咨询问题回复较慢，希望提高响应速度�?);
            //     cmp9.setSolution("已优化客服流程，确保2小时内响应�?);
            //     cmp9.setCustomerFeedback("改善明显，满�?);
            //     cmp9.setStatus("resolved");
            //     cmp9.setComplaintTime(LocalDateTime.of(2025, 11, 30, 14, 30));
            //     cmp9.setResolveTime(LocalDateTime.of(2025, 12, 1, 10, 0));
            //     complaintRecordRepo.save(cmp9);
            //     complaintCount++;
            // }
            
            // // 投诉10: Normal - 产品说明不清 (processing)
            // if (savedCustomers.size() > 19 && allProducts.size() > 15) {
            //     ComplaintRecord cmp10 = new ComplaintRecord();
            //     cmp10.setComplaintNo("CMP-20251129-010");
            //     cmp10.setCustomer(savedCustomers.get(19));
            //     cmp10.setSalesman(salesmen.get(4));
            //     cmp10.setRelatedOrderNo("ORD-20251205-006");
            //     cmp10.setRelatedProduct(allProducts.get(15));
            //     cmp10.setComplaintType("quality");
            //     cmp10.setSeverity("Normal");
            //     cmp10.setContent("产品使用说明书不够详细，部分功能说明不清楚�?);
            //     cmp10.setSolution("正在制作更详细的操作指南，并安排技术人员上门培训�?);
            //     cmp10.setStatus("processing");
            //     cmp10.setComplaintTime(LocalDateTime.of(2025, 11, 29, 10, 20));
            //     complaintRecordRepo.save(cmp10);
            //     complaintCount++;
            // }
            
            // System.out.println("已插�?" + complaintCount + " 条投诉记�?);

            // System.out.println("=======================================");
            // System.out.println("测试数据初始化完成！");
            // System.out.println("产品: 20 �?);
            // System.out.println("客户: 20 �?);
            // System.out.println("销售记�? 40 条（手动插入�?);
            // System.out.println("联络记录: " + contactCount + " �?);
            // System.out.println("服务记录: " + serviceCount + " �?);
            // System.out.println("催款记录: " + collectionCount + " �?);
            // System.out.println("投诉记录: " + complaintCount + " �?);
            // System.out.println("=======================================");
        };
    }

    private Product createProduct(String productNo, String name, String category, String spec, String unit, double price, String desc) {
        Product p = new Product();
        p.setProductNo(productNo);
        p.setName(name);
        p.setCategory(category);
        p.setSpecification(spec);
        p.setUnit(unit);
        p.setPrice(BigDecimal.valueOf(price));
        p.setDescription(desc);
        return p;
    }

    private Customer createCustomer(String name, String contact, String phone, String email, String addr, String type, String level, Long salesmanId, String remark) {
        Customer c = new Customer();
        c.setName(name);
        c.setContactPerson(contact);
        c.setPhone(phone);
        c.setEmail(email);
        c.setAddress(addr);
        c.setCustomerType(type);
        c.setLevel(level);
        c.setSalesmanId(salesmanId);
        c.setRemark(remark);
        return c;
    }
}
