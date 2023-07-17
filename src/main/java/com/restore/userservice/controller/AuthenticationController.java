package com.restore.userservice.controller;

import com.restore.core.controller.AppController;
import com.restore.core.dto.response.Response;
import com.restore.core.dto.response.ResponseCode;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.userservice.dto.AuthRequest;
import com.restore.userservice.dto.PasswordChangeRequest;
import com.restore.userservice.dto.SetPasswordRequest;
import com.restore.userservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class AuthenticationController extends AppController {
    @Autowired
    private AuthenticationService authenticationService;
    @PostMapping("/auth/token")
    public ResponseEntity<Response> getAccessToken(@Valid @RequestBody AuthRequest authRequest) throws RestoreSkillsException {
        log.info("getAccessToken() of {} started",AuthenticationController.class);
        return data(ResponseCode.OK,"Token for the user has been created",authenticationService.getAccessToken(authRequest));
    }

    @PostMapping("/set-password")
    public ResponseEntity<Response> setPassword(@Valid @RequestBody SetPasswordRequest setPasswordRequest) throws RestoreSkillsException {
        return data(ResponseCode.OK,"Password changed successfully",authenticationService.setPassword(setPasswordRequest));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Response> changePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) throws RestoreSkillsException {
        return data(ResponseCode.OK,"Password changed successfully",authenticationService.changePassword(passwordChangeRequest));
    }
}
