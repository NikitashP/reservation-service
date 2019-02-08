package com.loyalty.reservation.query;

import com.loyalty.reservation.event.MakeNewReservationEvent;
import com.loyalty.reservation.service.CustomerService;
import com.loyalty.reservation.service.EmailService;
import com.loyalty.reservation.service.Hotel;
import com.loyalty.reservation.service.HotelService;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import java.util.Objects;

import static com.loyalty.reservation.domain.STATUS.*;

@Component
public class ReservationEventHandler {

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


    @Transactional
    @EventHandler
    public void on(MakeNewReservationEvent event){
        Boolean ableToDeductAvailableRooms=null;
        Boolean ableToDeductBonusPoints=null;

        Hotel hotel = hotelService.getHotel(event.getHotelId());

        if(Objects.isNull(hotel)){
            throw new IllegalStateException("No Hotel Found");
        }

        long availableBonusPoints=customerService.getAvailableBonusPoints(event.getCustomerId());

        if (hotel.getAvailableRooms() >= numberOfRoomsNeeded && availableBonusPoints >= hotel.getRequiredBonusPoints()) {
            ableToDeductAvailableRooms = hotelService.deductAvailableRoom(event.getHotelId(), numberOfRoomsNeeded);
            ableToDeductBonusPoints = customerService.deductBonusPoints(event.getCustomerId(), hotel.getRequiredBonusPoints());

            if (ableToDeductAvailableRooms && ableToDeductBonusPoints) {
                reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), RESERVED));
                emailService.notifyServiceOwner(serviceOwnerMailId, "Status changed from INITIATED to RESERVED", "Reservation Confirmed | Id: " + event.getId());
            } else {
                reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), PENDING_APPROVAL));
                emailService.notifyServiceOwner(serviceOwnerMailId, "Status changed from INITIATED to PENDING_APPROVAL", "Reservation Needs Attention | Id: " + event.getId());
            }

        } else {
            reservationRepository.save(new Reservation(event.getId(), event.getHotelId(), event.getCustomerId(), PENDING_APPROVAL));
        }
        // enable retry on failure or email on failure
        if(Objects.nonNull(ableToDeductAvailableRooms) && Objects.nonNull(ableToDeductBonusPoints) && !ableToDeductAvailableRooms && ableToDeductBonusPoints){
            customerService.addBonusPoints(event.getCustomerId(),hotel.getRequiredBonusPoints());
        }
        if(Objects.nonNull(ableToDeductAvailableRooms) && Objects.nonNull(ableToDeductBonusPoints) && ableToDeductAvailableRooms && !ableToDeductBonusPoints){
            hotelService.addToAvailableRooms(hotel.getId(),numberOfRoomsNeeded);
        }

    }

}
