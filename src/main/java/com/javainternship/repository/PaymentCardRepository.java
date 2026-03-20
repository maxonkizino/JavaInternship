package com.javainternship.repository;


import com.javainternship.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends  JpaRepository<PaymentCard, Long>,
                                                JpaSpecificationExecutor<PaymentCard> {




    Page<PaymentCard> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    @Query("SELECT c FROM PaymentCard c WHERE c.active = true")
    List<PaymentCard> findActiveCards();

    @Query(value = "SELECT * FROM payment_cards WHERE number = :number",
            nativeQuery = true)
    Optional<PaymentCard> findByNumber(String number);


}
