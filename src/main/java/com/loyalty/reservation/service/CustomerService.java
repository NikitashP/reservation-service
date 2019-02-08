package com.loyalty.reservation.service;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

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

    public long getAvailableBonusPoints(String customerId) {
        final HttpEntity entity=new HttpEntity(headers);
        ResponseEntity<Long> hotelResponseEntity = restTemplate.exchange(customerServiceUri + "/points/" + customerId, HttpMethod.GET, entity, Long.class);
        return hotelResponseEntity.getBody();
    }

    public Boolean deductBonusPoints(String customerId, long requiredBonusPoints) {
        final HttpEntity<PointsRequest> entity=new HttpEntity(new PointsRequest(customerId,requiredBonusPoints),headers);

       try{
           ResponseEntity<Void> responseEntity = restTemplate.exchange(customerServiceUri + "/points/deduct", HttpMethod.PUT, entity, Void.class);
           return responseEntity.getStatusCode().equals(HttpStatus.OK);
       }catch (RestClientException e){
           LOGGER.error("Unable to deduct points",e);
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
