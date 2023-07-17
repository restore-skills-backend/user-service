package com.restore.userservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restore.core.dto.app.User;
import com.restore.core.dto.app.enums.Roles;
import com.restore.core.dto.response.Response;
import com.restore.core.dto.response.ResponseCode;
import com.restore.core.entity.UserEntity;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.core.service.AppService;
import com.restore.userservice.client.AdminClient;
import com.restore.userservice.client.CommunicationClient;
import com.restore.userservice.dto.ForgotPasswordEmailRequest;
import com.restore.userservice.dto.ProviderGroup;
import com.restore.userservice.dto.SendEmailForgotPasswordRequest;
import com.restore.userservice.dto.SetPasswordEmailRequest;
import com.restore.userservice.repository.UserRepository;
import com.restore.userservice.service.IamService;
import com.restore.userservice.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl extends AppService implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IamService iamService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommunicationClient communicationClient;


    @Autowired
    private ModelMapper modelMapper;


    private UserEntity mapToEntity(User user) {
        return UserEntity.builder().email(user.getEmail())
                .iamId(user.getIamId())
                .created(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC))
                .modified(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC))
                .createdBy(user.getIamId())
                .modifiedBy(user.getIamId())
                .build();
    }
    private User toUser(UserEntity userEntity, String schemaName) {
        User user = modelMapper.map(userEntity, User.class);
        try {
            User iamUser = userByEmail(schemaName,userEntity.getEmail());
            user.setActive(iamUser.isActive());
            user.setFirstName(iamUser.getFirstName());
            user.setLastName(iamUser.getLastName());
            user.setRole(iamUser.getRole());
            user.setPhone(iamUser.getPhone());
            user.setLastLogin(iamUser.getLastLogin());
        }catch(Exception exception){
            log.error("User Not found in iam "+ userEntity.getEmail());
            return user;
        }
        return user;
    }

    private UserEntity saveUser(UserEntity user) throws RestoreSkillsException {
        return userRepository.save(user);
    }

    public UUID sendEmailForSetPassword(User newUser, String tenantKey) throws RestoreSkillsException {

        SetPasswordEmailRequest setPasswordEmailRequest = SetPasswordEmailRequest.builder()
                .email(newUser.getEmail())
                .name(newUser.getFirstName())
                .uuid(newUser.getUuid())
                .subdomain(tenantKey)
                .build();

        ResponseEntity<Response> uuid = null;
        try {
            uuid = communicationClient.sendEmailForSetPassword(tenantKey, setPasswordEmailRequest);
        } catch (Exception exception) {
//            throwError(ResponseCode.BAD_REQUEST, "failed to send set password email");
            log.error("failed to send set password email. : "+newUser.getEmail());
        }
        UUID setPasswordResponse = null;
        if (Objects.nonNull(uuid.getBody())) {
            setPasswordResponse = objectMapper.convertValue(uuid.getBody().getData(), UUID.class);
        }
        return setPasswordResponse;
    }
    @Override
    public UserEntity createUser(User user, UUID providerGroupId) throws RestoreSkillsException{
        User currentUser = getCurrentUser();

        ResponseEntity<Response> response = adminClient.getProviderGroupById("public", providerGroupId);
        if (Objects.isNull(response.getBody().getData()))
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Provider group not found with name : " + currentUser.getTenantKey());
        ProviderGroup providerGroup = objectMapper.convertValue(response.getBody().getData(), ProviderGroup.class);

        return saveUserEntity(user, providerGroup.getIamGroup());
    }

    private UserEntity saveUserEntity(User user, String tenantKey) throws RestoreSkillsException {
        UserEntity userEntity = null;
        String iamId = iamService.addUser(user, tenantKey);
        if(StringUtils.isEmpty(iamId)){
            throwError(ResponseCode.IAM_ERROR, "Failed to create user in keycloak.");
        }
        try {
            user.setIamId(iamId);
            userEntity = saveUser(mapToEntity(user));
        } catch (Exception e) {
            iamService.deleteIamUser(iamId);
            throwError(ResponseCode.DB_ERROR, "Failed to create user in user database.");
        }

        if(ObjectUtils.isEmpty(userEntity)){
            iamService.deleteIamUser(iamId);
        }

        // Send Email to set Password
        user.setUuid(userEntity.getUuid());
        sendEmailForSetPassword(user, tenantKey);
        return userEntity;
    }

//    @Override
//    public UserEntity createStaffUser(User user, String iamGroup) throws RestoreSkillsException {
//        if (Objects.isNull(iamGroup))
//            iamGroup = getCurrentUser().getTenantKey();
//
//        user.setRole(Roles.STAFF);
//        return saveUserEntity(user, iamGroup);
//    }

    private Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private User getUser(String iamId) throws RestoreSkillsException {
        Optional<User> optionalUser = iamService.getUser(iamId);
        if (!optionalUser.isPresent()) {
            throwError(ResponseCode.BAD_REQUEST, "User with this iam id ID not exist");
        }
        return optionalUser.get();
    }

    private ProviderGroup getProviderGroupBySubdomain(String subdomain) throws RestoreSkillsException {
        ResponseEntity<Response> response;
        try {
             response = adminClient.getProviderGroupBySubdomain("public",subdomain);
        }catch (Exception exception){
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Provider group not found with subdomain : " + subdomain);
        }
        if (Objects.isNull(response.getBody().getData()))
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Provider group not found with subdomain : " + subdomain);
        return objectMapper.convertValue(response.getBody().getData(), ProviderGroup.class);
    }

    private ProviderGroup getProviderGroupById(UUID uuid) throws RestoreSkillsException {
        ResponseEntity<Response> response;
        try {
             response = adminClient.getProviderGroupById("public",uuid);
        }catch (Exception exception){
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Provider group not found with id : " + uuid);
        }
        if (Objects.isNull(response.getBody().getData()))
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Provider group not found with id : " + uuid);
        return objectMapper.convertValue(response.getBody().getData(), ProviderGroup.class);
    }

    private User userByEmail(String groupName, String email) throws RestoreSkillsException {
        ResponseEntity<Response> response = null;
        try {
            response = adminClient.getUser(groupName,email);
        }catch (Exception exception){
            throwError(ResponseCode.BAD_REQUEST, "user not found with email :"+email);
        }
        if (Objects.isNull(response.getBody().getData()))
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "User not found with email : " + email);
        return objectMapper.convertValue(response.getBody().getData(), User.class);
    }

    private UUID sendEmailForForgotPassword(ForgotPasswordEmailRequest forgotPasswordEmailRequest) throws RestoreSkillsException {
        ResponseEntity<Response> uuid = null;
        try {
            uuid = communicationClient.sendEmailForForgotPassword(forgotPasswordEmailRequest);
        } catch (Exception exception) {
            throwError(ResponseCode.BAD_REQUEST, "failed to send forgot password email");
        }
        UUID forgotPasswordResponse = null;
        if (Objects.nonNull(uuid.getBody())) {
            forgotPasswordResponse = objectMapper.convertValue(uuid.getBody().getData(), UUID.class);
        }
        return forgotPasswordResponse;
    }

    @Override
    public UUID signup(User user, UUID providerGroupId) throws RestoreSkillsException {
        UserEntity createdUser = null;
        log.info("Received request for creating User : {}", user.getEmail());

        ResponseEntity<Response> response = adminClient.getProviderGroupById("public",providerGroupId);
        if (Objects.isNull(response.getBody().getData()))
            throw new RestoreSkillsException(ResponseCode.NOT_FOUND, "Provider group not found with Id : "+providerGroupId);

        ProviderGroup providerGroup =  objectMapper.convertValue(response.getBody().getData(), ProviderGroup.class);

        // Validate User
        if(getUserByEmail(user.getEmail()).isPresent())
            throwError(ResponseCode.BAD_REQUEST, "User with this Email ID already exist");

        // Add User to IAM and Database
        Optional<User> existingUser = iamService.findByEmail(user.getEmail());

        if(existingUser.isPresent()) {
            // User present in IAM, but not in Database. Add User in Database
            createdUser = saveUser(mapToEntity(existingUser.get()));
        } else {
            // Add User to IAM
            String iamId = iamService.addUser(user, providerGroup.getIamGroup());
            log.info("Created user with IAM id : {}", iamId);
            if(StringUtils.isNotBlank(iamId)) {
                user.setIamId(iamId);
                user.setActive(true);
                user.setEmailVerified(false);
//                user.setCreatedBy(getCurrentUser().getIamId());
//                user.setModifiedBy(getCurrentUser().getIamId());

                createdUser = saveUser(mapToEntity(user));
            }
        }
        return Objects.requireNonNull(createdUser).getUuid();
    }

    @Override
    public User getProfile() throws RestoreSkillsException {
        return getCurrentUser();
    }

    @Override
    public User getUserByEmail(String email, String subdomain) throws RestoreSkillsException {
        ProviderGroup providerGroup = getProviderGroupBySubdomain(subdomain);
        UserEntity userEntity = getUserByEmailWithDynamicSchema(providerGroup.getDbSchema().toLowerCase(Locale.ROOT), email);
        User userInfo = getUser(userEntity.getIamId());
        userInfo.setUuid(userEntity.getUuid());
        return userInfo;
    }

    @Override
    public UUID sendEmailForForgotPassword(SendEmailForgotPasswordRequest sendEmailForgotPasswordRequest) throws RestoreSkillsException {

        User user;
        if (Objects.nonNull(sendEmailForgotPasswordRequest.getSubdomain())){
             user = getUserByEmail(sendEmailForgotPasswordRequest.getEmail(), sendEmailForgotPasswordRequest.getSubdomain());
        }else {
             user = userByEmail("public",sendEmailForgotPasswordRequest.getEmail());
        }
        ForgotPasswordEmailRequest forgotPasswordEmailRequest = ForgotPasswordEmailRequest.builder()
                .email(user.getEmail())
                .name(user.getFirstName() + " " + user.getLastName())
                .uuid(user.getUuid())
                .subdomain(sendEmailForgotPasswordRequest.getSubdomain())
                .build();
        return sendEmailForForgotPassword(forgotPasswordEmailRequest);
    }

    @Override
    public User getUserById(UUID uuid,UUID providerGroupId) throws RestoreSkillsException {
        ProviderGroup providerGroup = getProviderGroupById(providerGroupId);
        UserEntity userEntity = getUserByIdWithDynamicSchema(providerGroup.getDbSchema().toLowerCase(Locale.ROOT), uuid);
        User UserInfo = getUser(userEntity.getIamId());
        UserInfo.setUuid(userEntity.getUuid());
        return UserInfo;
    }

    private UserEntity getUserByIdWithDynamicSchema(String schema, UUID uuid) throws RestoreSkillsException {
        Optional<UserEntity> optionalUserEntity = userRepository.findByIdWithDynamicSchema(entityManager,schema,uuid);
        if (!optionalUserEntity.isPresent()){
            throwError(ResponseCode.BAD_REQUEST, "User cannot be found");
        }
        return optionalUserEntity.get();
    }

    private UserEntity getUserByEmailWithDynamicSchema(String schema, String email) throws RestoreSkillsException {
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailWithDynamicSchema(entityManager,schema,email);
        if (!optionalUserEntity.isPresent()){
            throwError(ResponseCode.BAD_REQUEST, "User cannot be found");
        }
        return optionalUserEntity.get();
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable, String schemaName){
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "created");
        Page<UserEntity> users = userRepository.findAllByArchiveIsFalse(pageable);
        return users.map(userEntity -> toUser(userEntity,schemaName));
    }

}
