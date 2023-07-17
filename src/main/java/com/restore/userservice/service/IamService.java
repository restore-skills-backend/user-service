package com.restore.userservice.service;

import com.restore.core.dto.app.User;
import com.restore.core.dto.app.enums.Roles;
import com.restore.core.exception.RestoreSkillsException;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;

import java.util.Optional;

public interface IamService {

    String addUser(User user, String iamGroup) throws RestoreSkillsException;

    Optional<User> getUser(String userId) throws RestoreSkillsException;

    void changePassword(String userId, String password, UsersResource userResource) throws RestoreSkillsException;

    void verifyEmail(String userId) throws RestoreSkillsException;

    void enableUser(String userId, boolean status) throws RestoreSkillsException;

    Optional<User> findByEmail(String email) throws RestoreSkillsException;

    void updateRole(String userId, Roles oldRole, Roles newRole) throws RestoreSkillsException;

    void updateUser(User user) throws RestoreSkillsException;

    Boolean resetPassword(String iamId, String newPassword) throws RestoreSkillsException;

    void deleteIamUser(String iamId) throws RestoreSkillsException;
}
