package com.loyalty.reservation;

import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationQueryHandler {


    private final EventStore eventStore;

    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationQueryHandler(EventStore eventStore, ReservationRepository reservationRepository) {
        this.eventStore = eventStore;
        this.reservationRepository = reservationRepository;
    }

    public Reservation getReservation(String reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("unable to find reservation with this Id"));
    }


    public List<Object> getAllChangesForReservation(String reservationId){
        return eventStore.readEvents(reservationId).asStream().map(s -> s.getPayload()).collect(Collectors.toList());
    }
}
