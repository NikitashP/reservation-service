package com.loyalty.reservation.query;

import com.loyalty.reservation.domain.STATUS;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(of = { "id" })
public class Reservation {

    @Id
    private String id;

    private String hotelId;

    private String customerId;

    @Setter private STATUS status;

    public Reservation(String id, String hotelId, String customerId, STATUS status) {
        this.id = id;
        this.hotelId = hotelId;
        this.customerId = customerId;
        this.status = status;
    }
}
