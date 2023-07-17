package com.restore.userservice.service;

import com.restore.core.dto.app.ProviderGroup;
import com.restore.core.exception.RestoreSkillsException;

public interface ProviderGroupService {
    void add(ProviderGroup providerGroup) throws RestoreSkillsException;
}
