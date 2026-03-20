package com.javainternship.model.specification;

import com.javainternship.model.PaymentCard;
import com.javainternship.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentCardSpecificationTest {

    @Test
    void hasUserName_nullReturnsNullPredicate() {
        Specification<PaymentCard> spec = PaymentCardSpecification.hasUserName(null);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        @SuppressWarnings("unchecked")
        Predicate predicate = spec.toPredicate((Root<PaymentCard>) root, query, cb);
        assertThat(predicate).isNull();
    }

    @Test
    void hasUserName_buildsLikeOnLowercase() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Join userJoin = mock(Join.class);
        Path<Object> namePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowered = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.join("user")).thenReturn(userJoin);
        when(userJoin.get("name")).thenReturn(namePath);
        when(cb.lower(any())).thenReturn(lowered);
        when(cb.like(lowered, "%john%")).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = PaymentCardSpecification.hasUserName("John")
                .toPredicate((Root<PaymentCard>) root, query, cb);

        assertThat(result).isSameAs(predicate);
    }

    @Test
    void isUserActive_joinsUserAndBuildsIsTrue() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Join userJoin = mock(Join.class);
        Path<Object> userActivePath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.join("user")).thenReturn(userJoin);
        when(userJoin.get("active")).thenReturn(userActivePath);
        when(cb.isTrue(any())).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = PaymentCardSpecification.isUserActive().toPredicate((Root<PaymentCard>) root, query, cb);

        assertThat(result).isSameAs(predicate);
        verify(cb).isTrue(any());
    }

    @Test
    void hasUserSurname_buildsLikeOnLowercase() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Join userJoin = mock(Join.class);
        Path<Object> surnamePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowered = (Expression<String>) mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.join("user")).thenReturn(userJoin);
        when(userJoin.get("surname")).thenReturn(surnamePath);
        when(cb.lower(any())).thenReturn(lowered);
        when(cb.like(lowered, "%doe%")).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = PaymentCardSpecification.hasUserSurname("Doe").toPredicate((Root<PaymentCard>) root, query, cb);
        assertThat(result).isSameAs(predicate);
    }

    @Test
    void isActive_buildsIsTruePredicate() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> activePath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("active")).thenReturn(activePath);
        when(cb.isTrue(any())).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = PaymentCardSpecification.isActive().toPredicate((Root<PaymentCard>) root, query, cb);
        assertThat(result).isSameAs(predicate);
        verify(cb).isTrue(any());
    }

    @Test
    void hasId_buildsEquality() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> idPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("id")).thenReturn(idPath);
        when(cb.equal(any(), eq(7L))).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = PaymentCardSpecification.hasId(7L).toPredicate((Root<PaymentCard>) root, query, cb);
        assertThat(result).isSameAs(predicate);
    }

    @Test
    void hasUserId_buildsEqualityViaJoin() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Join userJoin = mock(Join.class);
        Path<Object> userIdPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.join("user")).thenReturn(userJoin);
        when(userJoin.get("id")).thenReturn(userIdPath);
        when(cb.equal(any(), eq(10L))).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = PaymentCardSpecification.hasUserId(10L).toPredicate((Root<PaymentCard>) root, query, cb);
        assertThat(result).isSameAs(predicate);
    }

    @Test
    void hasNumber_buildsEquality() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<PaymentCard> query = mock(CriteriaQuery.class);
        @SuppressWarnings("rawtypes")
        Root root = mock(Root.class);

        Path<Object> numberPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("number")).thenReturn(numberPath);
        when(cb.equal(any(), eq("4111111111111111"))).thenReturn(predicate);

        @SuppressWarnings("unchecked")
        Predicate result = PaymentCardSpecification.hasNumber("4111111111111111").toPredicate((Root<PaymentCard>) root, query, cb);
        assertThat(result).isSameAs(predicate);
    }
}

