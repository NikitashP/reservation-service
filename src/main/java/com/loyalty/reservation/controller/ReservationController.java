package com.loyalty.reservation.controller;

import com.loyalty.reservation.command.MakeNewReservationCommand;
import com.loyalty.reservation.controller.request.ReservationRequest;
import com.loyalty.reservation.query.Reservation;
import com.loyalty.reservation.query.ReservationQueryHandler;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.axonframework.common.IdentifierFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final IdentifierFactory identifierFactory = IdentifierFactory.getInstance();

    private final CommandGateway commandGateway;

    private final ReservationQueryHandler reservationQueryHandler;

    @PostMapping("/create")
    public CompletableFuture<String> createReservation(@Valid @RequestBody ReservationRequest reservation){
        return commandGateway.send(new MakeNewReservationCommand(identifierFactory.generateIdentifier(),reservation.getHotelId(),reservation.getCustomerId()));
    }

    @GetMapping("/events/{id}")
    public List<Object> getAllChangesToReservation(@NonNull @PathVariable("id") String reservationId){
        return reservationQueryHandler.getAllChangesForReservation(reservationId);
    }

    @GetMapping("/reservation/{id}")
    public Reservation getReservation(@NonNull @PathVariable("id") String reservationId){
        return reservationQueryHandler.getReservation(reservationId);
    }


    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidInput(IllegalArgumentException exception) {
        return exception.getMessage();
    }
}
