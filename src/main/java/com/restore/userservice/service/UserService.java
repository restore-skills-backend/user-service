package com.restore.userservice.service;

import com.restore.core.dto.app.User;
import com.restore.core.entity.UserEntity;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.userservice.dto.SendEmailForgotPasswordRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UUID signup(User user, UUID providerGroupId) throws RestoreSkillsException;

    User getProfile() throws RestoreSkillsException;

    User getUserByEmail(String email, String subdomain) throws RestoreSkillsException;

    UUID sendEmailForForgotPassword(SendEmailForgotPasswordRequest sendEmailForgotPasswordRequest)
            throws RestoreSkillsException;

    User getUserById(UUID uuid, UUID providerGroupId) throws RestoreSkillsException;

    UserEntity createUser(User user, UUID providerGroupId) throws RestoreSkillsException;

    Page<User> getAllUsers(Pageable pageable, String schemaName);

//    UserEntity createStaffUser(User user, String iamGroup) throws RestoreSkillsException;
}
