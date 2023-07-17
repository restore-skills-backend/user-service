package com.restore.userservice.client;

import com.restore.core.dto.response.Response;
import com.restore.core.exception.RestoreSkillsException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "restore-admin-service", path = "/api/admin")
public interface AdminClient {

    @GetMapping("/provider-group/auth/{id}")
    public ResponseEntity<Response> getProviderGroupById(@RequestHeader(name = "X-TENANT-ID") String requester, @PathVariable("id") UUID uuid) throws RestoreSkillsException;

    @GetMapping("/provider-group/schema/{groupName}")
    public ResponseEntity<Response> getProviderGroupByName(@RequestHeader(name = "X-TENANT-ID") String requester, @PathVariable("groupName") String groupName) throws RestoreSkillsException;

    @GetMapping("/provider-group/auth/subdomain/{subdomain}")
    ResponseEntity<Response> getProviderGroupBySubdomain(@RequestHeader(name = "X-TENANT-ID") String requester,@PathVariable("subdomain") String subdomain);

    @GetMapping("/auth/user/{email}")
    ResponseEntity<Response> getUser(@RequestHeader(name = "X-TENANT-ID") String requester,@PathVariable String email);

    @GetMapping("/auth/user-id/{id}")
    ResponseEntity<Response> getUserById(@RequestHeader(name = "X-TENANT-ID") String requester,@PathVariable UUID id);

}
