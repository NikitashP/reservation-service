package com.loyalty.reservation.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    public ReservationRequest(@NotNull String hotelId, @NotNull String customerId) {
        this.hotelId = hotelId;
        this.customerId = customerId;
    }

    @NotNull
    private String hotelId;
    @NotNull
    private String customerId;
}
