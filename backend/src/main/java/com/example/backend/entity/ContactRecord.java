package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 联络记录实体
 */
@Data
@Entity
@Table(name = "contact_record")
public class ContactRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contact_no", length = 50, unique = true)
    private String contactNo;                   // 联络单号

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "contact_date", nullable = false)
    private LocalDateTime contactDate;          // 联络时间

    @Column(name = "contact_way", nullable = false, length = 20)
    private String contactWay;                  // 联络方式：phone/wechat/visit/email/dinner/other

    @Column(length = 500, nullable = false)
    private String content;                     // 沟通内容

    @Column(length = 20, nullable = false)
    private String outcome;                     // 结果：signed/interested/thinking/rejected/no_answer

    @Column(length = 100)
    private String location;                    // 定位地址

    @Column(name = "file_path", length = 255)
    private String filePath;                    // 附件凭证

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
    
    // 虚拟字段：用于JSON序列化/反序列化销售员ID
    @JsonProperty("salesmanId")
    public Long getSalesmanId() {
        return salesman != null ? salesman.getId() : null;
    }
    
    @JsonProperty("salesmanId")
    public void setSalesmanId(Long salesmanId) {
        if (salesmanId != null) {
            if (salesman == null) {
                salesman = new Salesman();
            }
            salesman.setId(salesmanId);
        }
    }
    
    // 虚拟字段：用于JSON序列化/反序列化客户ID
    @JsonProperty("customerId")
    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }
    
    @JsonProperty("customerId")
    public void setCustomerId(Long customerId) {
        if (customerId != null) {
            if (customer == null) {
                customer = new Customer();
            }
            customer.setId(customerId);
        }
    }
}
