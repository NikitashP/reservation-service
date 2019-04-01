package com.loyalty.reservation;

import com.loyalty.reservation.command.MakeNewReservationCommand;
import com.loyalty.reservation.domain.ReservationAggregate;
import com.loyalty.reservation.domain.STATUS;
import com.loyalty.reservation.event.MakeNewReservationEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReservationAggreateTest {


    private FixtureConfiguration fixture;

    @BeforeEach
    public void setUp() {
        fixture = new AggregateTestFixture(ReservationAggregate.class);
    }

    @Test
    public void makeAReservation() {
        fixture.given().when(new MakeNewReservationCommand("id","hid","cid")).expectSuccessfulHandlerExecution().expectEvents(
                new MakeNewReservationEvent("id","cid","hid", STATUS.INITIATED));
    }
}
