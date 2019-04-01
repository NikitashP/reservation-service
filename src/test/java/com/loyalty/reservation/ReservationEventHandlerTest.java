package com.loyalty.reservation;

import com.loyalty.reservation.domain.STATUS;
import com.loyalty.reservation.event.MakeNewReservationEvent;
import com.loyalty.reservation.service.CustomerService;
import com.loyalty.reservation.service.EmailService;
import com.loyalty.reservation.service.Hotel;
import com.loyalty.reservation.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Optional;

import static com.loyalty.reservation.domain.STATUS.PENDING_APPROVAL;
import static com.loyalty.reservation.domain.STATUS.RESERVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ReservationEventHandlerTest {

    @Mock
    private ReservationRepository mockReservationRepository;
    @Mock
    private CustomerService mockCustomerService;
    @Mock
    private HotelService mockHotelService;
    @Mock
    private EmailService mockEmailService;

    private ReservationEventHandler reservationEventHandlerUnderTest;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        reservationEventHandlerUnderTest = new ReservationEventHandler(mockReservationRepository, mockCustomerService, mockHotelService, mockEmailService);
    }

    @Test
    public void testOnMakeNewReservationEventWhenHotelIsNotFound() {
        // Setup
        final MakeNewReservationEvent event = new MakeNewReservationEvent("id", "cid", "hid", STATUS.INITIATED);
        doReturn(Optional.of(10L)).when(mockCustomerService).getAvailableBonusPoints(anyString());
        doReturn(Optional.empty()).when(mockHotelService).getHotel(anyString());
        Executable code = () -> reservationEventHandlerUnderTest.on(event);
        assertThrows(IllegalStateException.class, code);
    }

    @Test
    public void testOnMakeNewReservationEventWhenUnableToFindCustomerBonusPointsBalance() {
        // Setup
        final MakeNewReservationEvent event = new MakeNewReservationEvent("id", "cid", "hid", STATUS.INITIATED);
        Hotel hotel = new Hotel("1", 100L, "casa", 0L);
        doReturn(Optional.empty()).when(mockCustomerService).getAvailableBonusPoints(anyString());
        doReturn(Optional.of(hotel)).when(mockHotelService).getHotel(anyString());
        Executable code = () -> reservationEventHandlerUnderTest.on(event);
        assertThrows(IllegalStateException.class, code);
    }

    @Test
    public void testOnMakeNewReservationEventWhenThereAreNoRoomsAvailable() {
        // Setup
        final MakeNewReservationEvent event = new MakeNewReservationEvent("id", "cid", "hid", STATUS.INITIATED);
        Hotel hotel = new Hotel("1", 100L, "casa", 0L);
        doReturn(Optional.of(10L)).when(mockCustomerService).getAvailableBonusPoints(anyString());
        doReturn(Optional.of(hotel)).when(mockHotelService).getHotel(anyString());
        // Run the test
        reservationEventHandlerUnderTest.on(event);

        final ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        // Verify the results
        verify(mockReservationRepository, times(1)).save(captor.capture());

        Reservation reservation = captor.getValue();
        assertThat(reservation.getStatus()).isEqualByComparingTo(PENDING_APPROVAL);

    }


    @Test
    public void testOnMakeNewReservationEventWhenThereAreRoomsButCustomerBalanceIsInsufficient() {
        // Setup
        final MakeNewReservationEvent event = new MakeNewReservationEvent("id", "cid", "hid", STATUS.INITIATED);
        Hotel hotel = new Hotel("1", 100L, "casa", 10L);
        doReturn(Optional.of(10L)).when(mockCustomerService).getAvailableBonusPoints(anyString());
        doReturn(Optional.of(hotel)).when(mockHotelService).getHotel(anyString());
        // Run the test
        reservationEventHandlerUnderTest.on(event);

        final ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        // Verify the results
        verify(mockReservationRepository, times(1)).save(captor.capture());

        Reservation reservation = captor.getValue();
        assertThat(reservation.getStatus()).isEqualByComparingTo(PENDING_APPROVAL);

    }

    @Test
    public void testOnMakeNewReservationEventWhenThereAreRoomsAndCustomerBalanceIsSufficient() {
        // Setup
        final MakeNewReservationEvent event = new MakeNewReservationEvent("id", "cid", "hid", STATUS.INITIATED);
        Hotel hotel = new Hotel("1", 100L, "casa", 10L);
        doReturn(Optional.of(100L)).when(mockCustomerService).getAvailableBonusPoints(anyString());
        doReturn(Optional.of(hotel)).when(mockHotelService).getHotel(anyString());
        doReturn(true).when(mockEmailService).notifyServiceOwner(anyString(), anyString(), anyString());
        doReturn(true).when(mockHotelService).deductAvailableRoom(anyString(), anyLong());
        doReturn(true).when(mockCustomerService).deductBonusPoints(anyString(), anyLong());
        // Run the test
        reservationEventHandlerUnderTest.on(event);

        final ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        // Verify the results
        verify(mockReservationRepository, times(1)).save(captor.capture());

        Reservation reservation = captor.getValue();
        assertThat(reservation.getStatus()).isEqualByComparingTo(RESERVED);

    }
}
