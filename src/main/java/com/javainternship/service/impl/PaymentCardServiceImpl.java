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
import com.javainternship.service.interf.PaymentCardService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository repository;
    private final PaymentCardMapper mapper;
    private final UserRepository userRepository;
    private final PaymentCardLimitProperties limitProperties;

    @Override
    @Cacheable(cacheNames = "cardsByNumber", key = "#cardNumber")
    public PaymentCardResponse findPaymentCardByCardNumber(String cardNumber) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasNumber(cardNumber))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        PaymentCard card = repository.findOne(spec)
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment Card Not Found with number:" + cardNumber));
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    @Cacheable(cacheNames = "cardsById", key = "#id")
    public PaymentCardResponse findPaymentCardById(Long id) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasId(id))
                .and(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        PaymentCard card = repository.findOne(spec)
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment Card Not Found with id:" + id));
        return mapper.toPaymentCardResponse(card);
    }

    @Override
    public List<PaymentCardResponse> findAllPaymentCards() {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive());

        List<PaymentCard> cards = repository.findAll(spec);
        return mapper.toPaymentCardResponses(cards);
    }

    @Override
    public Page<PaymentCardResponse> searchPaymentCards(String name, String surname, Pageable pageable) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.isActive())
                .and(PaymentCardSpecification.isUserActive())
                .and(PaymentCardSpecification.hasUserName(name))
                .and(PaymentCardSpecification.hasUserSurname(surname));

        Page<PaymentCard> page = repository.findAll(spec, pageable);
        return page.map(mapper::toPaymentCardResponse);
    }

    @Override
    @Cacheable(cacheNames = "cardsByUserId", key = "#p0 + ':' + #p1.pageNumber + ':' + #p1.pageSize")
    public Page<PaymentCardResponse> findCardsByUserId(Long userId, Pageable pageable) {
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
            @CacheEvict(cacheNames = "cardsById", allEntries = true)
    })
    public PaymentCardResponse createPaymentCard(CreatePaymentCardRequest request) {
        Long userId = request.getUserId();
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
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment Card Not Found with id:" + id));
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
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment Card Not Found with id:" + id));
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
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment Card Not Found with id:" + id));
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
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment Card Not Found with id:" + id));
        card.setActive(false);
        repository.save(card);
    }
}
