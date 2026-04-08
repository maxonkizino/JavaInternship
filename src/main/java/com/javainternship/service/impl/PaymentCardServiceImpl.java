package com.javainternship.service.impl;

import com.javainternship.dto.request.create.CreatePaymentCardRequest;
import com.javainternship.dto.request.update.UpdatePaymentCardRequest;
import com.javainternship.dto.response.PaymentCardResponse;
import com.javainternship.exception.MaxCardsPerUserExceededException;
import com.javainternship.exception.PaymentCardNotFoundException;
import com.javainternship.exception.UserNotFoundException;
import com.javainternship.mapper.PaymentCardMapper;
import com.javainternship.model.PaymentCard;
import com.javainternship.model.User;
import com.javainternship.model.specification.PaymentCardSpecification;
import com.javainternship.repository.PaymentCardRepository;
import com.javainternship.repository.UserRepository;
import com.javainternship.config.PaymentCardLimitProperties;
import com.javainternship.security.SecurityUtils;
import com.javainternship.service.PaymentCardService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {
    private static final String PAYMENT_CARD_NOT_FOUND_WITH_ID = "Payment Card Not Found with id:";
    private static final String ACCESS_DENIED_CARD = "Access denied to payment card";
    private static final String ACCESS_DENIED_CARDS_FOR_USER = "Access denied to cards for user";
    private static final String ACCESS_DENIED_CREATE_FOR_OTHERS = "Access denied: can only create cards for yourself";

    private final PaymentCardRepository repository;
    private final PaymentCardMapper mapper;
    private final UserRepository userRepository;
    private final PaymentCardLimitProperties limitProperties;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public PaymentCardResponse findPaymentCardByCardNumber(String cardNumber) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasNumber(cardNumber))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        PaymentCard card = repository.findOne(spec)
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment Card Not Found with number:" + cardNumber));
        assertCardOwnerOrAdmin(card);
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentCardResponse findPaymentCardById(Long id) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasId(id))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        PaymentCard card = repository.findOne(spec)
                .orElseThrow(() -> new PaymentCardNotFoundException(PAYMENT_CARD_NOT_FOUND_WITH_ID + id));
        assertCardOwnerOrAdmin(card);
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentCardResponse> findAllPaymentCards() {
        if (!securityUtils.isCurrentUserAdmin()) {
            throw new AccessDeniedException(ACCESS_DENIED_CARD);
        }
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        List<PaymentCard> cards = repository.findAll(spec);
        return mapper.toPaymentCardResponses(cards);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentCardResponse> searchPaymentCards(String name, String surname, Pageable pageable) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive())
                .and(PaymentCardSpecification.hasUserName(name))
                .and(PaymentCardSpecification.hasUserSurname(surname));

        if (!securityUtils.isCurrentUserAdmin()) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new AccessDeniedException(ACCESS_DENIED_CARDS_FOR_USER);
            }
            spec = spec.and(PaymentCardSpecification.hasUserId(currentUserId));
        }

        Page<PaymentCard> page = repository.findAll(spec, pageable);
        return page.map(mapper::toPaymentCardResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cardsByUserId", key = "#p0 + ':' + #p1.pageNumber + ':' + #p1.pageSize")
    public Page<PaymentCardResponse> findCardsByUserId(Long userId, Pageable pageable) {
        if (!securityUtils.isOwnerOrAdmin(userId)) {
            throw new AccessDeniedException(ACCESS_DENIED_CARDS_FOR_USER);
        }
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasUserId(userId))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        return repository.findAll(spec, pageable)
                .map(mapper::toPaymentCardResponse);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
            @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),

    })
    public PaymentCardResponse createPaymentCard(CreatePaymentCardRequest request) {
        Long userId = request.getUserId();
        if (!securityUtils.isCurrentUserAdmin()) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(userId)) {
                throw new AccessDeniedException(ACCESS_DENIED_CREATE_FOR_OTHERS);
            }
        }
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!user.isActive()) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        Specification<PaymentCard> countSpec = Specification
                .where(PaymentCardSpecification.hasUserId(userId))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        long count = repository.count(countSpec);
        if (count >= limitProperties.getMaxPerUser()) {
            throw new MaxCardsPerUserExceededException(
                    "User with id " + userId + " already has maximum number of cards (" + limitProperties.getMaxPerUser() + ")");
        }

        PaymentCard card = mapper.toPaymentCard(request);
        card.setUser(user);
        card = repository.save(card);
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
            @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
            @CacheEvict(cacheNames = "cardsById", key = "#id")
    })
    public PaymentCardResponse updatePaymentCard(UpdatePaymentCardRequest request, Long id) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasId(id))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        PaymentCard card = repository.findOne(spec)
                .orElseThrow(() -> new PaymentCardNotFoundException(PAYMENT_CARD_NOT_FOUND_WITH_ID + id));
        assertCardOwnerOrAdmin(card);
        mapper.toPaymentCard(request, card);
        card = repository.save(card);
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
            @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
            @CacheEvict(cacheNames = "cardsById", key = "#id")
    })
    public void deletePaymentCard(Long id) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasId(id))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        PaymentCard card = repository.findOne(spec)
                .orElseThrow(() -> new PaymentCardNotFoundException(PAYMENT_CARD_NOT_FOUND_WITH_ID + id));
        assertCardOwnerOrAdmin(card);
        card.setActive(false);
        repository.save(card);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
            @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
            @CacheEvict(cacheNames = "cardsById", key = "#id")
    })
    public void activatePaymentCard(Long id) {
        PaymentCard card = repository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(PAYMENT_CARD_NOT_FOUND_WITH_ID + id));
        assertCardOwnerOrAdmin(card);
        card.setActive(true);
        repository.save(card);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
            @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
            @CacheEvict(cacheNames = "cardsById", key = "#id")
    })
    public void deactivatePaymentCard(Long id) {
        PaymentCard card = repository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(PAYMENT_CARD_NOT_FOUND_WITH_ID + id));
        assertCardOwnerOrAdmin(card);
        card.setActive(false);
        repository.save(card);
    }

    private void assertCardOwnerOrAdmin(PaymentCard card) {
        if (card.getUser() == null || card.getUser().getId() == null) {
            throw new AccessDeniedException(ACCESS_DENIED_CARD);
        }
        if (!securityUtils.isOwnerOrAdmin(card.getUser().getId())) {
            throw new AccessDeniedException(ACCESS_DENIED_CARD);
        }
    }
}
