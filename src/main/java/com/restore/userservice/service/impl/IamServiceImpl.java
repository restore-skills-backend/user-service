package com.restore.userservice.service.impl;

import com.restore.core.dto.app.User;
import com.restore.core.dto.app.enums.Roles;
import com.restore.core.dto.response.ResponseCode;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.core.service.AppService;
import com.restore.userservice.service.IamService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;

@Service
@Slf4j
public class IamServiceImpl extends AppService implements IamService {

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private Keycloak keycloak;

    private UsersResource usersResource = null;
    private RolesResource rolesResource = null;

    @PostConstruct()
    private void init() {
        try {
            RealmResource realmResource = this.keycloak.realm(realm);
            usersResource = realmResource.users();
            rolesResource = realmResource.roles();
        } catch (Exception e) {
            log.error("Error while initiating IAM Configuration", e);
        }
    }

    private UserResource findByUserId(String userId) throws RestoreSkillsException {
        UserResource existingUser = null;
        try {
            existingUser = usersResource.get(userId);
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }

        return Optional.of(existingUser).orElseThrow(() -> new RestoreSkillsException(ResponseCode.BAD_REQUEST, "Invalid User ID for IAM."));
    }

    private User mapToUser(UserRepresentation existingUser) {
        return User.builder()
                .firstName(existingUser.getFirstName())
                .lastName(existingUser.getLastName())
                .iamId(existingUser.getId())
                .active(existingUser.isEnabled())
                .email(existingUser.getEmail())
                .emailVerified(existingUser.isEmailVerified())
                .phone(existingUser.firstAttribute("phone"))
                .phoneVerified(Boolean.parseBoolean(existingUser.firstAttribute("phoneVerified")))
                .build();
    }

    @Override
    public Optional<User> findByEmail(String email) throws RestoreSkillsException {
        Optional<UserRepresentation> existingUser = Optional.empty();
        try {
            existingUser = usersResource.search(email).stream().findFirst();
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }

        return existingUser.isPresent() ? existingUser.map(this::mapToUser) : Optional.empty();
    }

    @Override
    public String addUser(User user, String iamGroup) throws RestoreSkillsException {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        RolesResource rolesResource = realmResource.roles();

        String userId = null;
        UserRepresentation iamUser = new UserRepresentation();
        iamUser.setUsername(user.getEmail());
        iamUser.setFirstName(user.getFirstName());
        iamUser.setLastName(user.getLastName() != null ? user.getLastName() : null);
        iamUser.setEmail(user.getEmail());
        iamUser.setEmailVerified(false);
        iamUser.setEnabled(true);
        iamUser.setGroups(Collections.singletonList(iamGroup));

        Map<String, List<String>> attribute = new HashMap<>();
//        if (user.getPhone() != null) {
//            iamUser.setAttributes(Map.of(
//                    "phone", List.of(user.getPhone()),
//                    "phoneVerified", List.of("false"),
//                    "groups", List.of(iamGroup)));
//        }
        if (user.getPhone() != null) {
            attribute.put("phone",List.of(user.getPhone()));
            attribute.put("phoneVerified", List.of("false"));
        }
        if (iamGroup != null) {
            attribute.put("groups",List.of(iamGroup));
        }
        if(!attribute.entrySet().isEmpty()){
            iamUser.setAttributes(attribute);
        }

        Optional<User> existingUser = findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throwError(ResponseCode.IAM_ERROR, "User email Id is already present");
        }

        // Create user (requires manage-users role)
        Response response = null;
        try {
            response = usersResource.create(iamUser);
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }

        log.info("User Create Response : {}", response.getStatusInfo());

        if (response.getStatus() == 201) {
            userId = CreatedResponseUtil.getCreatedId(response);
            log.info("Created IAM User with ID : {}", userId);

            UserResource userResource = usersResource.get(userId);

            // Assign Role to User
            try {
                RoleRepresentation realmRole = rolesResource.get(user.getRole().name()).toRepresentation();
                userResource.roles().realmLevel().add(Collections.singletonList(realmRole));
            } catch (Exception e) {
                throwError(ResponseCode.IAM_ERROR, e.getMessage());
            }

            // Assign Password to User
            //TODO: Need to create seperate set password API
//            if (user.getPassword() != null) {
//                changePassword(userId, user.getPassword(), usersResource);
//            }
        } else if (response.getStatus() == 409) {
            throwError(ResponseCode.IAM_ERROR, "Email Id Already Present.");
        }
        return userId;
    }

    @Override
    public Optional<User> getUser(String userId) throws RestoreSkillsException {
        UserResource userResource = findByUserId(userId);
        return Optional.of(mapToUser(userResource.toRepresentation()));
    }

//
//    @Override
//    public Optional<UserResource> getUser(String iamId) {
//        RealmResource realmResource = keycloak.realm(realmName);
//        return Optional.of(realmResource.users().get(iamId));
//    }

    @Override
    public void changePassword(String userId, String password, UsersResource resource) throws RestoreSkillsException {

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

//        UserResource userResource = findByUserId(userId);
        UserResource userResource = resource.get(userId);


        try {
            userResource.resetPassword(passwordCred);
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }
    }

    @Override
    public void verifyEmail(String userId) throws RestoreSkillsException {
        UserResource userResource = findByUserId(userId);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setEmailVerified(true);

        try {
            userResource.update(userRepresentation);
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }
    }

    @Override
    public void deleteIamUser(String iamId) throws RestoreSkillsException{
        try{
            usersResource.delete(iamId);
        }catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }
    }
    @Override
    public void enableUser(String userId, boolean status) throws RestoreSkillsException {
        UserResource userResource = findByUserId(userId);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setEnabled(status);

        try {
            userResource.update(userRepresentation);
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }
    }

    @Override
    public void updateRole(String userId, Roles oldRole, Roles newRole) throws RestoreSkillsException {
        UserResource userResource = findByUserId(userId);

        try {
            // Remove old role
            RoleRepresentation oldRoleValue = rolesResource.get(oldRole.name()).toRepresentation();
            userResource.roles().realmLevel().remove(Collections.singletonList(oldRoleValue));

            // Add new roles
            RoleRepresentation newRoleValue = rolesResource.get(newRole.name()).toRepresentation();
            userResource.roles().realmLevel().add(Collections.singletonList(newRoleValue));
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }
    }



    @Override
    public void updateUser(User user) throws RestoreSkillsException {

        // Update particular field in Keycloak
        UserRepresentation newChanges = new UserRepresentation();
        newChanges.setFirstName(user.getFirstName());
        newChanges.setLastName(user.getLastName());
        if(user.getPhone() != null) {
            newChanges.setAttributes(Map.of(
                    "phone", List.of(user.getPhone()),
                    "phoneVerified", List.of("false")));
        }

        try {
            findByUserId(user.getIamId()).update(newChanges);
        } catch (Exception e) {
            throwError(ResponseCode.IAM_ERROR, e.getMessage());
        }
    }

    @Override
    public Boolean resetPassword(String iamId, String newPassword) throws RestoreSkillsException {

        Optional<UserResource> optUserResource = getUser(iamId,realm);

        if(!optUserResource.isPresent())
            throwError(ResponseCode.IAM_ERROR, "Invalid User realm name");

        UserResource userResource = optUserResource.get();

        // Define password credential
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(newPassword);
        try {
            userResource.resetPassword(passwordCred);
        }catch (BadRequestException badRequestException){
            throwError(ResponseCode.IAM_ERROR, "Please enter different password you entered the same password again.");
        }catch (Exception exception){
            throwError(ResponseCode.IAM_ERROR, "Failed to set password");
        }
        return true;
    }

    private Optional<UserResource> getUser(String iamId, String realmName) {
        try {
            RealmResource realmResource = keycloak.realm(realmName);
            // This will trigger a Keycloak request
            realmResource.toRepresentation();
            return Optional.of(realmResource.users().get(iamId));
        } catch (NotFoundException e) {
            // Realm does not exist, return empty Optional
            return Optional.empty();
        }
    }
}

