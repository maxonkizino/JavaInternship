package com.javainternship.model.specification;

import com.javainternship.model.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {
    private PaymentCardSpecification() {

    }

    public static Specification<PaymentCard> hasUserName(String name) {
        return (root, query, cb) ->
                name == null ? null :
                        cb.like(cb.lower(root.join("user").get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    public static Specification<PaymentCard> hasUserSurname(String surname) {
        return (root, query, cb) ->
                surname == null ? null :
                        cb.like(cb.lower(root.join("user").get("surname")),
                                "%" + surname.toLowerCase() + "%");
    }

    public static Specification<PaymentCard> hasNumber(String number) {
        return (root, query, cb) ->
                number == null ? null : cb.equal(root.get("number"), number);
    }

    public static Specification<PaymentCard> hasId(Long id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("id"), id);
    }

    public static Specification<PaymentCard> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.join("user").get("id"), userId);
    }

    public static Specification<PaymentCard> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<PaymentCard> isUserActive() {
        return (root, query, cb) -> cb.isTrue(root.join("user").get("active"));
    }
}

