package com.loyalty.reservation.service;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class HotelService {
    @Value("${http.auth-token-header-name}")
    private String principalRequestHeader;

    @Value("${http.private.auth-token}")
    private String principalRequestValue;

    @Value("${hotel.service.uri}")
    private String hotelServiceUri;

    private RestTemplate restTemplate;

    private HttpHeaders headers;

    private static final Logger LOGGER = LoggerFactory.getLogger(HotelService.class);


    HotelService(){
     }

    @PostConstruct
    public void init(){
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.set(principalRequestHeader, principalRequestValue);
    }

    public Hotel getHotel(String hotelId) {
        final HttpEntity entity=new HttpEntity(headers);
        ResponseEntity<Hotel> hotelResponseEntity = restTemplate.exchange(hotelServiceUri + "/hotel/" + hotelId, HttpMethod.GET, entity, Hotel.class);
        return hotelResponseEntity.getBody();
    }

    public Boolean deductAvailableRoom(String hotelId, long numberOfRoomsNeeded) {
        final HttpEntity<AlterAvailableRoomsRequest> entity=new HttpEntity(new AlterAvailableRoomsRequest(hotelId,numberOfRoomsNeeded),headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(hotelServiceUri + "/rooms/deduct", HttpMethod.PUT, entity, Void.class);
            return responseEntity.getStatusCode().equals(HttpStatus.OK);
        } catch (RestClientException e) {
            LOGGER.error("Unable to deduct rooms", e);
        }
        return false;
    }

    public Boolean addToAvailableRooms(String hotelId, long numberOfRoomsNeeded) {
        final HttpEntity<AlterAvailableRoomsRequest> entity = new HttpEntity(new AlterAvailableRoomsRequest(hotelId, numberOfRoomsNeeded), headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(hotelServiceUri + "/rooms/add", HttpMethod.PUT, entity, Void.class);
            return responseEntity.getStatusCode().equals(HttpStatus.OK);
        } catch (RestClientException e) {
            LOGGER.error("Unable to add rooms", e);
        }
        return false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class AlterAvailableRoomsRequest{
        private  String id;
        private  long rooms;
    }
}
