package com.ist.leave_management_system.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfoDTO {
    private Map<String, Object> attributes;
    private String provider;
    
    public String getId() {
        return (String) attributes.get("sub");
    }
    
    public String getName() {
        return (String) attributes.get("name");
    }
    
    public String getEmail() {
        String email = (String) attributes.get("email");
        if (email == null) {
            // Microsoft sometimes puts email in preferred_username
            email = (String) attributes.get("preferred_username");
        }
        return email;
    }
    
    public String getFirstName() {
        return (String) attributes.get("given_name");
    }
    
    public String getLastName() {
        return (String) attributes.get("family_name");
    }
    
    public String getImageUrl() {
        return null; // Microsoft Graph API doesn't provide this in the basic scope
    }
}