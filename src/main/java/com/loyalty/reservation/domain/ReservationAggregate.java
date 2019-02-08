package com.loyalty.reservation.domain;

import com.loyalty.reservation.command.MakeNewReservationCommand;
import com.loyalty.reservation.event.MakeNewReservationEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import static com.loyalty.reservation.domain.STATUS.INITIATED;
import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class ReservationAggregate {

    private static final long serialVersionUID = -5977984483620451665L;

    @AggregateIdentifier
    private String id;

    private String hotelId;

    private String customerId;

    private STATUS status;

    ReservationAggregate(){
    }

    @CommandHandler
    public ReservationAggregate(MakeNewReservationCommand command){
        apply(new MakeNewReservationEvent(command.getId(),command.getCustomerId(),command.getHotelId(),INITIATED));
    }

    @EventSourcingHandler
    void on(MakeNewReservationEvent event){
        id=event.getId();
        hotelId=event.getHotelId();
        customerId= event.getCustomerId();
        status=event.getStatus();
    }


}
