package com.restore.userservice.service.impl;

import com.restore.core.dto.app.PracticeHour;
import com.restore.core.dto.app.ProviderGroup;
import com.restore.core.dto.app.Speciality;
import com.restore.core.dto.response.ResponseCode;
import com.restore.core.entity.PracticeHoursEntity;
import com.restore.core.entity.ProviderGroupEntity;
import com.restore.core.entity.SpecialityEntity;
import com.restore.core.exception.RestoreSkillsException;
import com.restore.core.service.AppService;

import com.restore.userservice.repository.ProviderGroupRepo;
import com.restore.userservice.repository.SpecialityRepo;
import com.restore.userservice.service.ProviderGroupService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProviderGroupServiceImpl extends AppService implements ProviderGroupService {

    private final ProviderGroupRepo providerGroupRepo;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SpecialityRepo specialityRepo;

    private String getTenantKey(ProviderGroup providerGroup) {
        return providerGroup.getName().replaceAll("[\\s\\p{P}.]", "").toLowerCase();
    }

    private Set<SpecialityEntity> toSpecialityEntity(Set<Speciality> specialities){
        return specialities.stream().map(speciality ->
                specialityRepo.findByName(speciality.getName())).collect(Collectors.toSet());
    }
    private Set<PracticeHoursEntity> toPracticeHoursEntity(Set<PracticeHour> practiceHours) {
        return practiceHours.stream().map(practiceHour -> modelMapper.map(practiceHour, PracticeHoursEntity.class)).collect(Collectors.toSet());
    }
    public ProviderGroupServiceImpl(ProviderGroupRepo providerGroupRepo) {
        this.providerGroupRepo = providerGroupRepo;
    }

    @Override
    public void add(ProviderGroup providerGroup) throws RestoreSkillsException {
        String tenantKey = getTenantKey(providerGroup);
        ProviderGroupEntity providerGroupEntity = modelMapper.map(providerGroup, ProviderGroupEntity.class);

        providerGroupEntity.setDbSchema(tenantKey);
        providerGroupEntity.setIamGroup(tenantKey);
        providerGroupEntity.setSubdomain(tenantKey);

        providerGroupEntity.setSpecialities(toSpecialityEntity(providerGroup.getSpecialities()));
        providerGroupEntity.setPracticeHours(toPracticeHoursEntity(providerGroup.getPracticeHours()));
       try {
            providerGroupEntity.setCreatedBy(getCurrentUser().getIamId());
            providerGroupEntity.setModifiedBy(getCurrentUser().getIamId());
            providerGroupEntity.setUuid(UUID.randomUUID());
            providerGroupRepo.save(providerGroupEntity);
       } catch (Exception e) {
           throwError(ResponseCode.DB_ERROR, "Failed to add Provider Group " + providerGroup.getName());
       }
    }
}
