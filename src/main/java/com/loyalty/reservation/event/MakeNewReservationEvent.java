package com.loyalty.reservation.event;

import com.loyalty.reservation.domain.STATUS;
import lombok.Getter;
import lombok.Value;

@Value
@Getter
public class MakeNewReservationEvent {
    private final String id;
    private final String customerId;
    private final String hotelId;
    private final STATUS status;

    public MakeNewReservationEvent(String id, String customerId, String hotelId, STATUS status) {
        this.id = id;
        this.customerId = customerId;
        this.hotelId = hotelId;
        this.status = status;
    }
}
