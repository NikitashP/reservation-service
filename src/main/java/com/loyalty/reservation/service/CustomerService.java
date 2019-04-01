package com.loyalty.reservation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);


    @Value("${http.auth-token-header-name}")
    private String principalRequestHeader;

    @Value("${http.private.auth-token}")
    private String principalRequestValue;

    @Value("${customer.service.uri}")
    private String customerServiceUri;

    private RestTemplate restTemplate;

    private HttpHeaders headers;


    CustomerService(){

    }

    @PostConstruct
    public void init(){
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.set(principalRequestHeader, principalRequestValue);
    }

    public Optional<Long> getAvailableBonusPoints(String customerId) {
        final HttpEntity entity=new HttpEntity(headers);
        try {
            ResponseEntity<Long> hotelResponseEntity = restTemplate.exchange(customerServiceUri + "/points/" + customerId, HttpMethod.GET, entity, Long.class);
            return Optional.ofNullable(hotelResponseEntity.getBody());
        } catch (RestClientException e) {
            LOGGER.error("Unable to find available points for customer {}",customerId,e);
        }
        return Optional.empty();
    }

    public Boolean deductBonusPoints(String customerId, long requiredBonusPoints) {
        final HttpEntity<PointsRequest> entity=new HttpEntity(new PointsRequest(customerId,requiredBonusPoints),headers);

       try{
           ResponseEntity<Void> responseEntity = restTemplate.exchange(customerServiceUri + "/points/deduct", HttpMethod.PUT, entity, Void.class);
           return responseEntity.getStatusCode().equals(HttpStatus.OK);
       }catch (RestClientException e){
           LOGGER.error("Unable to deduct points {} for customer {}",requiredBonusPoints,customerId,e);
       }
        return false;
    }

    public boolean addBonusPoints(String customerId, long requiredBonusPoints) {
        final HttpEntity<PointsRequest> entity=new HttpEntity(new PointsRequest(customerId,requiredBonusPoints),headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(customerServiceUri + "/points/add", HttpMethod.PUT, entity, Void.class);
            return responseEntity.getStatusCode().equals(HttpStatus.OK);
        } catch (RestClientException e) {
            LOGGER.error("Unable to add points", e);
        }
        return false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class PointsRequest {
        private  String id;
        private  long points;
    }
}
