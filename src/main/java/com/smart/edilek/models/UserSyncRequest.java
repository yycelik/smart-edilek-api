package com.smart.edilek.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncRequest {
    
    private String keycloakId;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
}
