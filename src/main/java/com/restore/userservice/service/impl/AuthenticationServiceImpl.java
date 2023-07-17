package com.restore.userservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restore.core.dto.response.Response;
import com.restore.core.dto.response.ResponseCode;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.userservice.client.AdminClient;
import com.restore.userservice.client.CommunicationClient;
import com.restore.userservice.dto.*;
import com.restore.core.dto.app.User;
import com.restore.core.service.AppService;
import com.restore.userservice.dto.AuthRequest;
import com.restore.userservice.dto.AuthResponse;
import com.restore.userservice.repository.UserRepository;
import com.restore.userservice.service.AuthenticationService;
import com.restore.userservice.service.IamService;
import com.restore.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class AuthenticationServiceImpl extends AppService implements AuthenticationService {
    @Value("${keycloak.auth-url}")
    private String authUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IamService iamService;

    @Autowired
    private CommunicationClient communicationClient;

    @Autowired
    UserService userService;

    private User getAdminUserById(UUID id) throws RestoreSkillsException {
        ResponseEntity<Response> response;
        try{
             response = adminClient.getUserById("public",id);
        }catch (Exception exception){
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "User not found with id : " + id);
        }
        if (Objects.isNull(response.getBody().getData()))
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "User not found with id : " + id);
        return objectMapper.convertValue(response.getBody().getData(), User.class);
    }

    @Override
    public AuthResponse getAccessToken(AuthRequest authRequest) throws RestoreSkillsException {
        log.info("getAccessToken() of {} started", AuthenticationServiceImpl.class);

//        ResponseEntity<Response> providerRealm = adminClient.getProviderRealm(authRequest.getRealm());
//
//        if (Objects.isNull(Objects.requireNonNull(providerRealm.getBody()).getMessage()))
//            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Realm Not found with " + authRequest.getRealm());
//
//        ProviderGroupRealmDTO providerGroupRealmDTO = objectMapper.convertValue(providerRealm.getBody().getMessage(), ProviderGroupRealmDTO.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> mapForm = new LinkedMultiValueMap<>();
        mapForm.add("grant_type", "password");
        mapForm.add("username", authRequest.getUsername());
        mapForm.add("password", authRequest.getPassword());
        mapForm.add("client_id", clientId);
        mapForm.add("client_secret", clientSecret);
        mapForm.add("scope", "openid");

        org.springframework.http.HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapForm, headers);
        ResponseEntity<Object> response;
        String serverUrl = baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        try {
             response = restTemplate.exchange(serverUrl, HttpMethod.POST, request, Object.class);
        }catch (HttpClientErrorException httpClientErrorException){
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Invalid credentials. Please check your username and password and try again.");
        }catch (Exception exception){
            throw new RestoreSkillsException(ResponseCode.BAD_REQUEST, "Failed to login. Try again");
        }
        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) response.getBody();

        return AuthResponse.builder()
                .accessToken(map.get("access_token").toString())
                .expiresIn(Integer.parseInt(map.get("expires_in").toString()))
                .refreshToken(map.get("refresh_token").toString())
                .tokenType(map.get("token_type").toString())
                .build();
    }

    @Override
    public Boolean setPassword(SetPasswordRequest setPasswordRequest) throws RestoreSkillsException {
        User user;
        if (Objects.nonNull(setPasswordRequest.getProviderGroupId())) {
            user = userService.getUserById(setPasswordRequest.getUuid(), setPasswordRequest.getProviderGroupId());
        }else {
            user = getAdminUserById(setPasswordRequest.getUuid());
        }
        if (iamService.resetPassword(user.getIamId(), setPasswordRequest.getNewPassword())) {
            communicationClient.expireLinkForPassword(setPasswordRequest.getLinkId());
            return true;
        }
        return false;
    }

    @Override
    public Boolean changePassword(PasswordChangeRequest passwordChangeRequest) throws RestoreSkillsException {
        User user = getCurrentUser();
        return iamService.resetPassword(user.getIamId(), passwordChangeRequest.getNewPassword());
    }
}
