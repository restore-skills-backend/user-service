package com.restore.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderGroup {

    private Long id;

    private UUID uuid;

    private String name;

    private String dbSchema;

    private String iamGroup;

    private String subdomain;

    private String contactNumber;

    private Long npiNumber;

    private String email;

    private String website;

    private String fax;

    private String description;

    private boolean active;

    private boolean archive;


}
