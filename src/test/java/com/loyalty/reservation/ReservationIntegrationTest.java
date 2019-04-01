package com.loyalty.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.loyalty.reservation.controller.request.ReservationRequest;
import com.loyalty.reservation.domain.STATUS;
import com.loyalty.reservation.service.Hotel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ReservationIntegrationTest {


    public WireMockServer hotelService;


    public WireMockServer customerService;


    public WireMockServer emailService;


    @BeforeEach
    public void setup() {
        hotelService = new WireMockServer(8082);
        hotelService.start();

        customerService = new WireMockServer(8083);
        customerService.start();

        emailService = new WireMockServer(5000);
        emailService.start();

    }

    @AfterEach
    public void teardown() {
        hotelService.stop();
        customerService.stop();
        emailService.stop();
    }


    @Autowired
    private MockMvc mockMvc;


    @Test
    public void testCreateReservationWhenHotelCouldNotBeFound() throws Exception {

        //given
        UUID customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();


        Hotel hotel = new Hotel(hotelId.toString(), 5L, "casa", 3L);

        mockGetApi(hotelId, hotelService, "/hotel/", asJsonString(hotel), BAD_REQUEST);

        ReservationRequest reservationRequest = new ReservationRequest(hotelId.toString(), customerId.toString());

        //when
        String reservationId = createReservation(reservationRequest);

        //then
        String reservationResponse = findReservation(reservationId);

        final ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = mapper.readValue(reservationResponse, Reservation.class);

        assertThat(reservation.getStatus().equals(STATUS.FAILED)).isTrue();

        String reservationEvents = findAllReservationEvents(reservationId);

        assertThat(reservationEvents).contains(STATUS.INITIATED.toString());

    }



    @Test
    public void testCreateReservationWhenCustomerCouldNotBeFound() throws Exception {

        //given
        UUID customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();

        mockGetApi(customerId, customerService, "/points/", "100", BAD_REQUEST);

        Hotel hotel = new Hotel(hotelId.toString(), 5L, "casa", 3L);

        mockGetApi(hotelId, hotelService, "/hotel/", asJsonString(hotel), OK);

        ReservationRequest reservationRequest = new ReservationRequest(hotelId.toString(), customerId.toString());

        //when
        String reservationId = createReservation(reservationRequest);

        //then
        String reservationResponse = findReservation(reservationId);

        final ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = mapper.readValue(reservationResponse, Reservation.class);

        assertThat(reservation.getStatus().equals(STATUS.FAILED)).isTrue();

        String reservationEvents = findAllReservationEvents(reservationId);

        assertThat(reservationEvents).contains(STATUS.INITIATED.toString());

    }

    @Test
    public void testCreateReservationWhenRoomIsAvailableAndCustomerHasBalance() throws Exception {

        //given
        UUID customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();

        mockGetApi(customerId, customerService, "/points/", "100", OK);

        Hotel hotel = new Hotel(hotelId.toString(), 5L, "casa", 3L);

        mockGetApi(hotelId, hotelService, "/hotel/", asJsonString(hotel), OK);

        AlterAvailableRoomsRequest alterAvailableRoomsRequest = new AlterAvailableRoomsRequest(hotelId.toString(), 1L);

        mockPutApi(hotelService, "/rooms/deduct", asJsonString(alterAvailableRoomsRequest), OK);

        PointsRequest pointsRequest = new PointsRequest(customerId.toString(), 5);

        mockPutApi(customerService, "/points/deduct", asJsonString(pointsRequest), OK);

        emailService.stubFor(post(urlPathEqualTo("/email")).willReturn(aResponse().withStatus(OK.value())));

        ReservationRequest reservationRequest = new ReservationRequest(hotelId.toString(), customerId.toString());

        //when
        String reservationId = createReservation(reservationRequest);

        //then
        String reservationResponse = findReservation(reservationId);

        final ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = mapper.readValue(reservationResponse, Reservation.class);

        assertThat(reservation.getStatus().equals(STATUS.RESERVED)).isTrue();

        String reservationEvents = findAllReservationEvents(reservationId);

        assertThat(reservationEvents).contains(STATUS.INITIATED.toString());

    }


    @Test
    public void testCreateReservationWhenRoomIsNotAvailable() throws Exception {

        //given
        UUID customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();

        mockGetApi(customerId, customerService, "/points/", "100", OK);

        Hotel hotel = new Hotel(hotelId.toString(), 5L, "casa", 0L);

        mockGetApi(hotelId, hotelService, "/hotel/", asJsonString(hotel), OK);

        AlterAvailableRoomsRequest alterAvailableRoomsRequest = new AlterAvailableRoomsRequest(hotelId.toString(), 1L);

        mockPutApi(hotelService, "/rooms/deduct", asJsonString(alterAvailableRoomsRequest), OK);

        PointsRequest pointsRequest = new PointsRequest(customerId.toString(), 5);

        mockPutApi(customerService, "/points/deduct", asJsonString(pointsRequest), OK);

        emailService.stubFor(post(urlPathEqualTo("/email")).willReturn(aResponse().withStatus(OK.value())));

        ReservationRequest reservationRequest = new ReservationRequest(hotelId.toString(), customerId.toString());

        //when
        String reservationId = createReservation(reservationRequest);

        //then
        String reservationResponse = findReservation(reservationId);

        final ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = mapper.readValue(reservationResponse, Reservation.class);

        assertThat(reservation.getStatus().equals(STATUS.PENDING_APPROVAL)).isTrue();

        String reservationEvents = findAllReservationEvents(reservationId);

        assertThat(reservationEvents).contains(STATUS.INITIATED.toString());

    }


    @Test
    public void testCreateReservationWhenRoomIsAvailableButCustomerHasInsufficientBalance() throws Exception {

        //given
        UUID customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();

        mockGetApi(customerId, customerService, "/points/", "1", OK);


        Hotel hotel = new Hotel(hotelId.toString(), 5L, "casa", 3L);

        mockGetApi(hotelId, hotelService, "/hotel/", asJsonString(hotel), OK);

        AlterAvailableRoomsRequest alterAvailableRoomsRequest = new AlterAvailableRoomsRequest(hotelId.toString(), 1L);

        mockPutApi(hotelService, "/rooms/deduct", asJsonString(alterAvailableRoomsRequest), OK);

        PointsRequest pointsRequest = new PointsRequest(customerId.toString(), 5);

        mockPutApi(customerService, "/points/deduct", asJsonString(pointsRequest), OK);

        emailService.stubFor(post(urlPathEqualTo("/email")).willReturn(aResponse().withStatus(OK.value())));


        ReservationRequest reservationRequest = new ReservationRequest(hotelId.toString(), customerId.toString());


        //when
        String reservationId = createReservation(reservationRequest);

        //then
        String reservationResponse = findReservation(reservationId);

        final ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = mapper.readValue(reservationResponse, Reservation.class);

        assertThat(reservation.getStatus().equals(STATUS.PENDING_APPROVAL)).isTrue();

        String reservationEvents = findAllReservationEvents(reservationId);

        assertThat(reservationEvents).contains(STATUS.INITIATED.toString());

    }


    @Test
    public void testCreateReservationWhenCustomerPointsAreNotDeductable() throws Exception {

        //given
        UUID customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();

        mockGetApi(customerId, customerService, "/points/", "100", OK);


        Hotel hotel = new Hotel(hotelId.toString(), 5L, "casa", 3L);

        mockGetApi(hotelId, hotelService, "/hotel/", asJsonString(hotel), OK);


        AlterAvailableRoomsRequest alterAvailableRoomsRequest = new AlterAvailableRoomsRequest(hotelId.toString(), 1L);


        mockPutApi(hotelService, "/rooms/deduct", asJsonString(alterAvailableRoomsRequest), OK);

        PointsRequest pointsRequest = new PointsRequest(customerId.toString(), 5);

        mockPutApi(customerService, "/points/deduct", asJsonString(pointsRequest), INTERNAL_SERVER_ERROR);

        emailService.stubFor(post(urlPathEqualTo("/email")).willReturn(aResponse().withStatus(OK.value())));


        ReservationRequest reservationRequest = new ReservationRequest(hotelId.toString(), customerId.toString());


        //when
        String reservationId = createReservation(reservationRequest);

        //then
        String reservationResponse = findReservation(reservationId);

        final ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = mapper.readValue(reservationResponse, Reservation.class);

        assertThat(reservation.getStatus().equals(STATUS.PENDING_APPROVAL)).isTrue();

        String reservationEvents = findAllReservationEvents(reservationId);

        assertThat(reservationEvents).contains(STATUS.INITIATED.toString());

    }


    @Test
    public void testCreateReservationWhenRoomsInHotelAreNotDeductable() throws Exception {

        //given
        UUID customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();

        mockGetApi(customerId, customerService, "/points/", "100", OK);


        Hotel hotel = new Hotel(hotelId.toString(), 5L, "casa", 3L);

        mockGetApi(hotelId, hotelService, "/hotel/", asJsonString(hotel), OK);


        AlterAvailableRoomsRequest alterAvailableRoomsRequest = new AlterAvailableRoomsRequest(hotelId.toString(), 1L);


        mockPutApi(hotelService, "/rooms/deduct", asJsonString(alterAvailableRoomsRequest), INTERNAL_SERVER_ERROR);

        PointsRequest pointsRequest = new PointsRequest(customerId.toString(), 5);

        mockPutApi(customerService, "/points/deduct", asJsonString(pointsRequest), OK);

        emailService.stubFor(post(urlPathEqualTo("/email")).willReturn(aResponse().withStatus(OK.value())));


        ReservationRequest reservationRequest = new ReservationRequest(hotelId.toString(), customerId.toString());


        //when
        String reservationId = createReservation(reservationRequest);

        //then
        String reservationResponse = findReservation(reservationId);

        final ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = mapper.readValue(reservationResponse, Reservation.class);

        assertThat(reservation.getStatus().equals(STATUS.PENDING_APPROVAL)).isTrue();

        String reservationEvents = findAllReservationEvents(reservationId);

        assertThat(reservationEvents).contains(STATUS.INITIATED.toString());

    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockGetApi(UUID customerId, WireMockServer customerService, String urlPath, String responseBody, HttpStatus status) {
        customerService.stubFor(get(urlPathEqualTo(urlPath + customerId.toString()))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(status.value())
                        .withBody(responseBody)));
    }

    private void mockPutApi(WireMockServer hotelService, String urlPath, String requestBody, HttpStatus responseStatus) {
        hotelService.stubFor(put(urlPathEqualTo(urlPath))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(responseStatus.value())));
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class AlterAvailableRoomsRequest {
        private String id;
        private long rooms;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class PointsRequest {
        private String id;
        private long points;
    }

    private String findAllReservationEvents(String reservationId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/events/" + reservationId)
                .header("X-API-Key", "abcdef123456")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String findReservation(String reservationId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/reservation/" + reservationId)
                .header("X-API-Key", "abcdef123456")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String createReservation(ReservationRequest reservationRequest) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/create")
                .content(asJsonString(reservationRequest))
                .header("X-API-Key", "abcdef123456")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getAsyncResult().toString();
    }
}

