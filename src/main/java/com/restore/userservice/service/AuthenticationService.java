package com.restore.userservice.service;

import com.restore.core.exception.RestoreSkillsException;
import com.restore.userservice.dto.AuthRequest;
import com.restore.userservice.dto.AuthResponse;
import com.restore.userservice.dto.SetPasswordRequest;
import com.restore.userservice.dto.PasswordChangeRequest;

public interface AuthenticationService {

    AuthResponse getAccessToken(AuthRequest authRequest) throws RestoreSkillsException;

    Boolean setPassword(SetPasswordRequest setPasswordRequest) throws RestoreSkillsException;

    Boolean changePassword(PasswordChangeRequest passwordChangeRequest) throws RestoreSkillsException;
}
