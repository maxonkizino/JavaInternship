package com.javainternship.model.specification;

import com.javainternship.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, cb) ->
                surname == null ? null :
                        cb.like(cb.lower(root.get("surname")),
                                "%" + surname.toLowerCase() + "%");
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) ->
                email == null ? null :
                        cb.equal(cb.lower(root.get("email")), email.toLowerCase());
    }

    public static Specification<User> hasId(Long id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("id"), id);
    }

    public static Specification<User> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
}
