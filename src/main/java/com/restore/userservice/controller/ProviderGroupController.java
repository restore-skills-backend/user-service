package com.restore.userservice.controller;

import com.restore.core.controller.AppController;
import com.restore.core.dto.app.ProviderGroup;
import com.restore.core.dto.response.Response;
import com.restore.core.dto.response.ResponseCode;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.userservice.service.ProviderGroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/")
public class ProviderGroupController extends AppController {
    @Autowired
    private ProviderGroupService providerGroupService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/provider-group")
    public ResponseEntity<Response> createProviderGroup(@Valid @RequestBody ProviderGroup providerGroup) throws RestoreSkillsException {
        providerGroupService.add(providerGroup);
        return success(ResponseCode.CREATED,"Provider group created successfully");
    }
}
