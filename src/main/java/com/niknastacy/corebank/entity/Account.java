package com.niknastacy.corebank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal moneyAmount;

    public Account() {}

    public Account(Long userId, BigDecimal moneyAmount) {
        this.userId = userId;
        this.moneyAmount = moneyAmount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getMoneyAmount() { return moneyAmount; }
    public void setMoneyAmount(BigDecimal moneyAmount) { this.moneyAmount = moneyAmount; }
}