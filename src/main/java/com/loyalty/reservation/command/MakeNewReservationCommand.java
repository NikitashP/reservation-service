package com.loyalty.reservation.command;

import lombok.Value;
import org.axonframework.commandhandling.model.AggregateIdentifier;

@Value
public class MakeNewReservationCommand {

    @AggregateIdentifier
    private String id;

    private String hotelId;

    private String customerId;

}
