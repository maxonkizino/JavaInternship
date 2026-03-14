package com.javainternship.model.specification;

import com.javainternship.model.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

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
}

