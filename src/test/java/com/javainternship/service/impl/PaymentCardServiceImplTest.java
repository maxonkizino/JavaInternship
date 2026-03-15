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
import com.javainternship.repository.PaymentCardRepository;
import com.javainternship.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentCardMapper mapper;

    @InjectMocks
    private PaymentCardServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findPaymentCardByCardNumber_returnsDto() {
        String number = "1234";
        PaymentCard card = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();

        when(repository.findByNumber(number)).thenReturn(Optional.of(card));
        when(mapper.toPaymentCardResponse(card)).thenReturn(response);

        PaymentCardResponse result = service.findPaymentCardByCardNumber(number);

        assertThat(result).isSameAs(response);
        verify(repository).findByNumber(number);
    }

    @Test
    void findPaymentCardByCardNumber_throwsWhenMissing() {
        when(repository.findByNumber("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findPaymentCardByCardNumber("missing"))
                .isInstanceOf(PaymentCardNotFoundException.class);
    }

    @Test
    void findPaymentCardById_returnsDto() {
        PaymentCard card = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();

        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(mapper.toPaymentCardResponse(card)).thenReturn(response);

        PaymentCardResponse result = service.findPaymentCardById(1L);

        assertThat(result).isSameAs(response);
    }

    @Test
    void findAllPaymentCards_returnsListOfDtos() {
        List<PaymentCard> cards = List.of(new PaymentCard());
        List<PaymentCardResponse> responses = List.of(new PaymentCardResponse());

        when(repository.findAll()).thenReturn(cards);
        when(mapper.toPaymentCardResponses(cards)).thenReturn(responses);

        List<PaymentCardResponse> result = service.findAllPaymentCards();

        assertThat(result).isEqualTo(responses);
    }

    @Test
    void searchPaymentCards_returnsPageOfDtos() {
        List<PaymentCard> cards = List.of(new PaymentCard());
        Page<PaymentCard> page = new PageImpl<>(cards);
        PaymentCardResponse response = new PaymentCardResponse();

        when(repository.findAll((Specification<PaymentCard>) any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toPaymentCardResponse(any(PaymentCard.class))).thenReturn(response);

        Page<PaymentCardResponse> result = service.searchPaymentCards("John", "Doe", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll((Specification<PaymentCard>) any(), eq(Pageable.unpaged()));
    }

    @Test
    void findCardsByUserId_returnsList() {
        Long userId = 1L;
        List<PaymentCard> cards = List.of(new PaymentCard());
        List<PaymentCardResponse> responses = List.of(new PaymentCardResponse());

        when(repository.findByUserId(userId)).thenReturn(cards);
        when(mapper.toPaymentCardResponses(cards)).thenReturn(responses);

        List<PaymentCardResponse> result = service.findCardsByUserId(userId);

        assertThat(result).isEqualTo(responses);
        verify(repository).findByUserId(userId);
    }

    @Test
    void createPaymentCard_mapsAndSaves() {
        CreatePaymentCardRequest request = new CreatePaymentCardRequest();
        request.setUserId(1L);
        User user = new User();
        user.setId(1L);
        PaymentCard card = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.countByUserId(1L)).thenReturn(2L);
        when(mapper.toPaymentCard(request)).thenReturn(card);
        when(repository.save(card)).thenReturn(card);
        when(mapper.toPaymentCardResponse(card)).thenReturn(response);

        PaymentCardResponse result = service.createPaymentCard(request);

        assertThat(result).isSameAs(response);
        assertThat(card.getUser()).isSameAs(user);
        verify(repository).save(card);
    }

    @Test
    void createPaymentCard_throwsWhenUserNotFound() {
        CreatePaymentCardRequest request = new CreatePaymentCardRequest();
        request.setUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPaymentCard(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createPaymentCard_throwsWhenMaxCardsExceeded() {
        CreatePaymentCardRequest request = new CreatePaymentCardRequest();
        request.setUserId(1L);
        User user = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.countByUserId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> service.createPaymentCard(request))
                .isInstanceOf(MaxCardsPerUserExceededException.class);
    }

    @Test
    void updatePaymentCard_updatesExisting() {
        UpdatePaymentCardRequest request = new UpdatePaymentCardRequest();
        PaymentCard existing = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(mapper).toPaymentCard(request, existing);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toPaymentCardResponse(existing)).thenReturn(response);

        PaymentCardResponse result = service.updatePaymentCard(request, 1L);

        assertThat(result).isSameAs(response);
        verify(mapper).toPaymentCard(request, existing);
        verify(repository).save(existing);
    }

    @Test
    void deletePaymentCard_deletesById() {
        service.deletePaymentCard(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void activatePaymentCard_setsActiveTrue() {
        PaymentCard card = new PaymentCard();
        card.setActive(false);

        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(repository.save(card)).thenReturn(card);

        service.activatePaymentCard(1L);

        assertThat(card.isActive()).isTrue();
        verify(repository).save(card);
    }

    @Test
    void deactivatePaymentCard_setsActiveFalse() {
        PaymentCard card = new PaymentCard();
        card.setActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(card));
        when(repository.save(card)).thenReturn(card);

        service.deactivatePaymentCard(1L);

        assertThat(card.isActive()).isFalse();
        verify(repository).save(card);
    }
}