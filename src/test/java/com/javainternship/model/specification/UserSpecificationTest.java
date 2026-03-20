package com.javainternship.model.specification;

import com.javainternship.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserSpecificationTest {

    @Test
    void hasName_nullReturnsNullPredicate() {
        Specification<User> spec = UserSpecification.hasName(null);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        @SuppressWarnings("unchecked")
        Predicate predicate = spec.toPredicate((Root<User>) root, query, cb);
        assertThat(predicate).isNull();
    }

    @Test
    void isActive_buildsIsTruePredicate() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> activePath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("active")).thenReturn(activePath);
        when(cb.isTrue(any())).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = UserSpecification.isActive().toPredicate((Root<User>) root, query, cb);

        assertThat(result).isSameAs(predicate);
        verify(cb).isTrue(any());
    }

    @Test
    void hasName_buildsLikeOnLowercase() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> namePath = mock(Path.class);
        Expression<String> lowered = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("name")).thenReturn(namePath);
        when(cb.lower(any())).thenReturn(lowered);
        when(cb.like(lowered, "%john%")).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = UserSpecification.hasName("John").toPredicate((Root<User>) root, query, cb);

        assertThat(result).isSameAs(predicate);
    }

    @Test
    void hasSurname_buildsLikeOnLowercase() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> surnamePath = mock(Path.class);
        Expression<String> lowered = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("surname")).thenReturn(surnamePath);
        when(cb.lower(any())).thenReturn(lowered);
        when(cb.like(lowered, "%doe%")).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = UserSpecification.hasSurname("Doe").toPredicate((Root<User>) root, query, cb);

        assertThat(result).isSameAs(predicate);
    }

    @Test
    void hasId_buildsEquality() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> idPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("id")).thenReturn(idPath);
        when(cb.equal(any(), eq(123L))).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = UserSpecification.hasId(123L).toPredicate((Root<User>) root, query, cb);

        assertThat(result).isSameAs(predicate);
    }

    @Test
    void hasEmail_buildsEqualityOnLowercase() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> emailPath = mock(Path.class);
        Expression<String> loweredEmail = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("email")).thenReturn(emailPath);
        when(cb.lower(any())).thenReturn(loweredEmail);
        when(cb.equal(loweredEmail, "test@example.com")).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = UserSpecification.hasEmail("test@example.com").toPredicate((Root<User>) root, query, cb);
        assertThat(result).isSameAs(predicate);
    }
}

