package com.loyalty.reservation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Component
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${http.auth-token-header-name}")
    private String principalRequestHeader;

    @Value("${http.private.auth-token}")
    private String principalRequestValue;

    @Value("${email.service.uri}")
    private String emailServiceUri;

    private RestTemplate restTemplate;

    private HttpHeaders headers;


    @PostConstruct
    public void init(){
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.set(principalRequestHeader, principalRequestValue);
    }

    @Async
    public Boolean notifyServiceOwner(String receiver, String body, String subject) {

        final HttpEntity<EmailRequest> entity=new HttpEntity(new EmailRequest(receiver,body,subject),headers);

        try{
            ResponseEntity<Void> responseEntity = restTemplate.exchange(emailServiceUri + "/email", HttpMethod.POST, entity, Void.class);
            return responseEntity.getStatusCode().equals(HttpStatus.OK);
        }catch (RestClientException e){
            LOGGER.error("Unable to deduct points",e);
        }
        return false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class EmailRequest {
        private  String receiver;
        private  String body;
        private  String subject;
    }

}
