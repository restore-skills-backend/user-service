package com.restore.userservice.client;


import com.restore.core.dto.response.Response;
import com.restore.userservice.dto.ForgotPasswordEmailRequest;
import com.restore.userservice.dto.SetPasswordEmailRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "restore-notification-service", path = "/api/notification")
public interface CommunicationClient {

    @GetMapping("/email/expire-link")
    ResponseEntity<Response> expireLinkForPassword(@RequestParam("linkId") UUID linkId);


    @PostMapping("/email/forgot-password")
    ResponseEntity<Response> sendEmailForForgotPassword(@Valid @RequestBody ForgotPasswordEmailRequest forgotPasswordSendEmailDTO);

    @PostMapping("/email/set-password")
    ResponseEntity<Response> sendEmailForSetPassword(@RequestHeader(name = "X-TENANT-ID") String requester, @Valid @RequestBody SetPasswordEmailRequest setPasswordEmailRequest);

}
