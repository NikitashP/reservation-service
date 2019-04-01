package com.loyalty.reservation;

import com.loyalty.reservation.event.MakeNewReservationEvent;
import com.loyalty.reservation.service.CustomerService;
import com.loyalty.reservation.service.EmailService;
import com.loyalty.reservation.service.Hotel;
import com.loyalty.reservation.service.HotelService;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.loyalty.reservation.domain.STATUS.*;

@Component
public class ReservationEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationEventHandler.class);


    private final ReservationRepository reservationRepository;

    private final CustomerService customerService;

    private final HotelService hotelService;

    private final EmailService emailService;

    final int numberOfRoomsNeeded = 1;

    @Value("${service.owner.mail.id}")
    private String serviceOwnerMailId;


    @Autowired
    public ReservationEventHandler(ReservationRepository reservationRepository, CustomerService customerService, HotelService hotelService, EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.customerService = customerService;
        this.hotelService = hotelService;
        this.emailService = emailService;
    }

    @EventHandler
    public void on(MakeNewReservationEvent event){

        Hotel hotel = hotelService.getHotel(event.getHotelId()).orElseThrow(()-> {
            reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), FAILED));
            return new IllegalStateException("Hotel must exist to make a reservation");
        });

        long customerBonusPointsBalance=customerService.getAvailableBonusPoints(event.getCustomerId()).orElseThrow(() -> {
            reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), FAILED));
            return new IllegalStateException("Customer must have bonus points to make reservation");
        });


        if(!hotel.reservationIsPossible(numberOfRoomsNeeded,customerBonusPointsBalance)){
            reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), PENDING_APPROVAL));
            emailService.notifyServiceOwner(serviceOwnerMailId, "Status changed from INITIATED to PENDING_APPROVAL", "Reservation Needs Attention | Id: " + event.getId());
            return;
        }

        if(!hotelService.deductAvailableRoom(event.getHotelId(), numberOfRoomsNeeded)){
            reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), PENDING_APPROVAL));
            emailService.notifyServiceOwner(serviceOwnerMailId, "Status changed from INITIATED to PENDING_APPROVAL", "Reservation Needs Attention | Id: " + event.getId());
            LOGGER.error("Unable deduct available rooms from Hotel {}. Reservation is pending approval",event.getHotelId());
            return;
        }

        if (!customerService.deductBonusPoints(event.getCustomerId(), hotel.getRequiredBonusPoints())) {
            reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), PENDING_APPROVAL));
            emailService.notifyServiceOwner(serviceOwnerMailId, "Status changed from INITIATED to PENDING_APPROVAL", "Reservation Needs Attention | Id: " + event.getId());
            LOGGER.error("Unable deduct customer bonus points. Reservation is pending approval for customer {}",event.getCustomerId());
            return;
        }

        reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), RESERVED));
        emailService.notifyServiceOwner(serviceOwnerMailId, "Status changed from INITIATED to RESERVED", "Reservation Confirmed | Id: " + event.getId());
    }

}
