package com.restore.userservice.controller;

import com.restore.core.controller.AppController;
import com.restore.core.dto.app.User;
import com.restore.core.dto.response.Response;
import com.restore.core.dto.response.ResponseCode;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.userservice.dto.SendEmailForgotPasswordRequest;
import com.restore.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController extends AppController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create/{providerGroupId}")
    public ResponseEntity<Response> signup(@Valid @RequestBody User user, @PathVariable UUID providerGroupId) throws RestoreSkillsException {
        return success(ResponseCode.CREATED, userService.signup(user, providerGroupId));
    }

    @GetMapping("/profile")
    public ResponseEntity<Response> getProfile() throws RestoreSkillsException {
        return data(ResponseCode.OK, "Profile found successfully", userService.getProfile());
    }

    @PostMapping("/send-forgot-password-email")
    public ResponseEntity<Response> sendEmailForForgotPassword(@Valid @RequestBody SendEmailForgotPasswordRequest sendEmailForgotPasswordRequest) throws RestoreSkillsException {
        return data(ResponseCode.OK,"email send successfully", userService.sendEmailForForgotPassword(sendEmailForgotPasswordRequest));
    }

    @PostMapping("/create-user/{providerGroupId}")
    public ResponseEntity<Response> addUser(@RequestBody User user, @PathVariable UUID providerGroupId) throws RestoreSkillsException {
        return data(ResponseCode.OK, "User Created Successfully.", userService.createUser(user,providerGroupId));
    }
    @GetMapping("/all/{schemaName}")
    public ResponseEntity<Response> getAllUsers(Pageable pageable , @PathVariable String schemaName) {
        return data(ResponseCode.OK, "List of user's fetched successfully...", userService.getAllUsers(pageable,schemaName));
    }


}
