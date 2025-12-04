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

            // 1. 插入20个医疗产品
            Product[] products = {
                createProduct("RI-001", "阿米娅急救血清", "药品", "10ml/支", "支", 120.00, "罗德岛研发的基础治疗血清，用于急救。"),
                createProduct("RI-002", "能天使注射器", "器械", "一次性注射器", "个", 8.50, "轻量注射器，适配多种给药方案。"),
                createProduct("RI-003", "银老板创口绷带", "消耗品", "5cm x 5m", "卷", 15.00, "高强度创口绷带。"),
                createProduct("RI-004", "龙门消毒液", "药品", "100ml", "瓶", 25.00, "用于消毒与伤口清洁。"),
                createProduct("RI-005", "罗德岛医用口罩", "防护", "一次性口罩", "个", 1.50, "医用防护口罩，过滤效率高。"),
                createProduct("RI-006", "临光防护面罩", "防护", "FFP2 型", "个", 10.00, "高级呼吸防护器具。"),
                createProduct("RI-007", "斯卡蒂输液包", "药品", "250ml", "袋", 45.00, "标配补液输注包。"),
                createProduct("RI-008", "红修复药膏", "药品", "20g", "支", 30.00, "外用修复药膏。"),
                createProduct("RI-009", "术师冷敷凝胶", "消耗品", "50g", "支", 18.00, "用于局部冷敷/护理。"),
                createProduct("RI-010", "乌萨斯血管支架", "器械", "小号/中号/大号", "套", 450.00, "血管支架（模型用途）。"),
                createProduct("RI-011", "黑钢手术刀", "器械", "手术刀（单支）", "把", 22.00, "一次性手术刀，锋利耐用。"),
                createProduct("RI-012", "源石检测试剂盒", "试剂", "单次/盒 (50)", "盒", 320.00, "快速检测试剂盒，用于常见感染检测。"),
                createProduct("RI-013", "白面鸮修复血清", "药品", "5ml/支", "支", 200.00, "高效型修复血清。"),
                createProduct("RI-014", "伊芙利特保温垫", "器械", "20x30cm", "片", 12.00, "急救保温垫片。"),
                createProduct("RI-015", "德克萨斯缝合线", "器械", "可吸收缝合线", "包", 60.00, "医用缝合线（可吸收）。"),
                createProduct("RI-016", "真理滴眼液", "药品", "10ml", "瓶", 28.00, "滴眼药水类产品。"),
                createProduct("RI-017", "陈固定夹板", "器械", "通用夹板", "个", 35.00, "骨折固定夹板。"),
                createProduct("RI-018", "拉普兰德生理盐水", "药品", "500ml", "袋", 22.00, "生理盐水补液。"),
                createProduct("RI-019", "星熊防护护臂", "防护", "医用护臂", "套", 18.00, "防护用护臂，耐磨。"),
                createProduct("RI-020", "塞雷娅示范疫苗", "疫苗", "单剂", "支", 480.00, "示范用疫苗（模拟）")
            };
            for (Product p : products) {
                productRepo.save(p);
            }
            System.out.println("已插入 20 个产品");

            // 2. 获取已存在的销售员（假设ID为1-5）
            List<Salesman> salesmen = salesmanRepo.findAll();
            if (salesmen.size() < 5) {
                System.out.println("警告：销售员数量不足5个，部分客户可能无法关联");
                return;
            }

            // 3. 为每个销售员创建4个客户（企业3个+个人1个，共20个）
            Customer[] customers = {
                createCustomer("罗德岛总医院", "阿米娅", "021-10001001", "amiya@rhodes.example", "罗德岛港区近地", "enterprise", "A", salesmen.get(0).getId(), "罗德岛直属合作医院"),
                createCustomer("龙门第七诊所", "玛莎医生", "021-20002002", "martha@lungmen.example", "龙门第七区", "enterprise", "B", salesmen.get(0).getId(), "社区诊所"),
                createCustomer("乌萨斯东方药房", "银狼", "031-30003003", "sw@ursus.example", "乌萨斯首都市场", "enterprise", "B", salesmen.get(0).getId(), "连锁药房"),
                createCustomer("博士", "博士", "021-88888888", "doctor@personal.example", "罗德岛宿舍区", "individual", "A", salesmen.get(0).getId(), "个人客户，长期采购医疗用品"),

                createCustomer("维克汉姆社区医院", "临光", "051-50005005", "nearl@wickham.example", "维克汉姆东区", "enterprise", "B", salesmen.get(1).getId(), "区域医院"),
                createCustomer("苔原流动医疗站", "伊芙利特", "061-60006006", "ifrit@tundra.example", "苔原3号路", "enterprise", "C", salesmen.get(1).getId(), "流动医疗点"),
                createCustomer("谢拉格综合诊所", "斯卡蒂医生", "071-70007007", "skadi@kjerag.example", "谢拉格山区", "enterprise", "C", salesmen.get(1).getId(), "兼顾兽医的综合诊所"),
                createCustomer("凯尔希", "凯尔希", "021-99999999", "kaltsit@personal.example", "罗德岛医疗部", "individual", "A", salesmen.get(1).getId(), "个人采购，医疗研究用"),

                createCustomer("希斯洛诊所", "塞雷娅", "091-90009009", "saria@heathrow.example", "希斯洛中心区", "enterprise", "B", salesmen.get(2).getId(), "社区医疗中心"),
                createCustomer("卡西米尔医疗中心", "银灰", "101-10010010", "sa@kazimierz.example", "卡西米尔老城", "enterprise", "A", salesmen.get(2).getId(), "高端门诊"),
                createCustomer("熔岩温泉药局", "熔泉", "111-11011011", "ifrit2@lava.example", "熔岩温泉区", "enterprise", "C", salesmen.get(2).getId(), "小型药局"),
                createCustomer("华法琳", "华法琳", "091-77777777", "warfarin@personal.example", "罗德岛血库", "individual", "B", salesmen.get(2).getId(), "个人客户，医疗耗材采购"),

                createCustomer("维多利亚广场诊所", "苏苏洛", "131-13013013", "sussurro@victoria.example", "维多利亚广场", "enterprise", "B", salesmen.get(3).getId(), "社区诊所"),
                createCustomer("盖亚康复中心", "瑕光", "141-14014014", "nearl2@gaia.example", "盖亚康复公园", "enterprise", "A", salesmen.get(3).getId(), "康复中心"),
                createCustomer("山口医院", "拉普兰德", "151-15015015", "lappland@mp.example", "山口要道", "enterprise", "B", salesmen.get(3).getId(), "山区医院"),
                createCustomer("闪灵", "闪灵", "131-66666666", "shining@personal.example", "圣堂医疗室", "individual", "A", salesmen.get(3).getId(), "个人采购，高端医疗器械"),

                createCustomer("应急响应诊疗点", "杜宾", "171-17017017", "amiya2@contingency.example", "应急区域", "enterprise", "B", salesmen.get(4).getId(), "快速响应诊疗点"),
                createCustomer("黑钢前哨站", "陈", "181-18018018", "chen@blacksteel.example", "黑钢要塞", "enterprise", "A", salesmen.get(4).getId(), "军用医疗保障"),
                createCustomer("第二风暴药房", "德克萨斯", "191-19019019", "texas@sstorm.example", "第二风暴区", "enterprise", "C", salesmen.get(4).getId(), "私人药房"),
                createCustomer("赫默", "赫默", "171-55555555", "silence@personal.example", "罗德岛研发部", "individual", "B", salesmen.get(4).getId(), "个人客户，实验器材采购")
            };
            // 确保客户姓名或联系人不会与销售员姓名重复，若重复则追加后缀
            List<String> salesmanNames = salesmen.stream().map(Salesman::getName).toList();
            for (Customer c : customers) {
                if (c.getName() != null && salesmanNames.contains(c.getName())) {
                    c.setName(c.getName() + "（客户）");
                }
                if (c.getContactPerson() != null && salesmanNames.contains(c.getContactPerson())) {
                    c.setContactPerson(c.getContactPerson() + "（客户）");
                }
                customerRepo.save(c);
            }
            System.out.println("已插入 20 个客户");

            // 重新获取保存后的实体（带ID）
            List<Product> savedProducts = productRepo.findAll();
            List<Customer> savedCustomers = customerRepo.findAll();

            // 4. 生成销售记录（为每个客户生成1-3条销售记录）
            int salesCount = 0;
            for (int i = 0; i < savedCustomers.size(); i++) {
                Customer cust = savedCustomers.get(i);
                Salesman sm = salesmen.get(i / 4); // 每个销售员对应4个客户
                
                // 每个客户生成1-3条销售记录
                int recordCount = 1 + (i % 3);
                for (int j = 0; j < recordCount; j++) {
                    Product prod = savedProducts.get((i * 3 + j) % savedProducts.size());
                    SalesRecord sr = new SalesRecord();
                    sr.setSalesman(sm);
                    sr.setCustomer(cust);
                    sr.setProduct(prod);
                    sr.setQuantity(5 + (i + j) * 2);
                    sr.setUnitPrice(prod.getPrice());
                    sr.setTotalAmount(prod.getPrice().multiply(BigDecimal.valueOf(sr.getQuantity())));
                    sr.setCommissionRate(BigDecimal.valueOf(0.05));
                    sr.setCommissionAmount(sr.getTotalAmount().multiply(sr.getCommissionRate()));
                    sr.setSaleDate(LocalDate.now().minusDays(30 - i));
                    sr.setRemark("测试销售记录 " + (salesCount + 1));
                    salesRecordRepo.save(sr);
                    salesCount++;
                }
            }
            System.out.println("已插入 " + salesCount + " 条销售记录");

            // 5. 生成联络记录
            int contactCount = 0;
            for (int i = 0; i < savedCustomers.size(); i++) {
                Customer cust = savedCustomers.get(i);
                Salesman sm = salesmen.get(i / 4);
                
                String[] methods = {"电话", "邮件", "微信", "面谈"};
                String[] summaries = {
                    "讨论新产品合作方案",
                    "了解客户近期采购需求",
                    "跟进上次订单执行情况",
                    "介绍公司新推出的产品线"
                };
                
                ContactRecord cr = new ContactRecord();
                cr.setSalesman(sm);
                cr.setCustomer(cust);
                cr.setContactTime(LocalDateTime.now().minusDays(20 - i % 20));
                cr.setContactMethod(methods[i % 4]);
                cr.setSummary(summaries[i % 4]);
                cr.setFeedback("客户表示满意，愿意继续合作");
                contactRecordRepo.save(cr);
                contactCount++;
            }
            System.out.println("已插入 " + contactCount + " 条联络记录");

            // 6. 生成服务记录
            int serviceCount = 0;
            for (int i = 0; i < savedCustomers.size(); i += 2) { // 每两个客户一条服务记录
                Customer cust = savedCustomers.get(i);
                Salesman sm = salesmen.get(i / 4);
                
                String[] serviceTypes = {"产品培训", "技术支持", "设备维护", "售后咨询"};
                String[] contents = {
                    "为客户进行产品使用培训，讲解操作要点",
                    "解决客户在使用中遇到的技术问题",
                    "对客户现有设备进行维护保养",
                    "解答客户关于产品售后的各类问题"
                };
                
                ServiceRecord sr = new ServiceRecord();
                sr.setSalesman(sm);
                sr.setCustomer(cust);
                sr.setServiceDate(LocalDate.now().minusDays(15 - i / 2));
                sr.setServiceType(serviceTypes[i % 4]);
                sr.setContent(contents[i % 4]);
                sr.setSatisfactionRating(4 + (i % 2));
                sr.setCustomerFeedback("服务态度好，解决问题及时");
                sr.setRemark("服务完成");
                serviceRecordRepo.save(sr);
                serviceCount++;
            }
            System.out.println("已插入 " + serviceCount + " 条服务记录");

            // 7. 生成催款记录
            List<SalesRecord> allSalesRecords = salesRecordRepo.findAll();
            int collectionCount = 0;
            for (int i = 0; i < Math.min(15, allSalesRecords.size()); i++) {
                SalesRecord sr = allSalesRecords.get(i);
                
                CollectionRecord cr = new CollectionRecord();
                cr.setSalesman(sr.getSalesman());
                cr.setCustomer(sr.getCustomer());
                cr.setSalesRecord(sr);
                cr.setCollectionDate(LocalDate.now().minusDays(10 - i % 10));
                cr.setCollectionMethod(i % 2 == 0 ? "电话" : "现场");
                cr.setAmountDue(sr.getTotalAmount());
                cr.setAmountReceived(sr.getTotalAmount().multiply(BigDecimal.valueOf(i % 3 == 0 ? 1.0 : 0.5)));
                cr.setStatus(i % 3 == 0 ? "已回款" : (i % 3 == 1 ? "部分回款" : "未回款"));
                cr.setDueDate(LocalDate.now().minusDays(5));
                cr.setRemark("催款记录 " + (i + 1));
                collectionRecordRepo.save(cr);
                collectionCount++;
            }
            System.out.println("已插入 " + collectionCount + " 条催款记录");

            // 8. 生成投诉记录
            int complaintCount = 0;
            for (int i = 0; i < 5; i++) { // 生成5条投诉记录
                Customer cust = savedCustomers.get(i * 4);
                Salesman sm = salesmen.get(i);
                
                String[] types = {"产品质量", "服务态度", "配送延迟", "价格争议"};
                String[] contents = {
                    "产品包装破损，需要重新发货",
                    "销售人员服务态度有待改善",
                    "订单配送时间超出预期",
                    "价格与之前报价不一致"
                };
                
                ComplaintRecord cpr = new ComplaintRecord();
                cpr.setCustomer(cust);
                cpr.setSalesman(sm);
                cpr.setComplaintDate(LocalDate.now().minusDays(7 - i));
                cpr.setComplaintType(types[i % 4]);
                cpr.setContent(contents[i % 4]);
                cpr.setHandler("客服部");
                cpr.setStatus(i < 3 ? "已处理" : "处理中");
                cpr.setResult(i < 3 ? "问题已解决，客户满意" : "正在处理中");
                cpr.setHandleDate(i < 3 ? LocalDate.now().minusDays(5 - i) : null);
                cpr.setRemark("投诉记录 " + (i + 1));
                complaintRecordRepo.save(cpr);
                complaintCount++;
            }
            System.out.println("已插入 " + complaintCount + " 条投诉记录");

            System.out.println("=======================================");
            System.out.println("测试数据初始化完成！");
            System.out.println("产品: 20 个");
            System.out.println("客户: 20 个");
            System.out.println("销售记录: " + salesCount + " 条");
            System.out.println("联络记录: " + contactCount + " 条");
            System.out.println("服务记录: " + serviceCount + " 条");
            System.out.println("催款记录: " + collectionCount + " 条");
            System.out.println("投诉记录: " + complaintCount + " 条");
            System.out.println("=======================================");
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
