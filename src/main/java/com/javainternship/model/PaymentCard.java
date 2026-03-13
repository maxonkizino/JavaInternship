package com.javainternship.model;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "payment_cards")
public class PaymentCard extends BaseAuditingEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String number;

    private String holder;

    private LocalDate expirationDate;

    private boolean active;




}
