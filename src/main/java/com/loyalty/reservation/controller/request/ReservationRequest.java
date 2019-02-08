package com.loyalty.reservation.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    @NotNull
    private String hotelId;
    @NotNull
    private String customerId;
}
