package com.example.diplomathesisfinalcollection.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    @Builder.Default
    private boolean forcePasswordChange = false;

    private String firstName;
    private String lastName;

    private String phone;
    private String address;
    private String profileImageUrl;
    private String role;
    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    private int cancellationCount = 0;
    private UserProfile userProfile;

    public UserProfile getUserProfile() {
        if (this.userProfile == null) {
            this.userProfile = new UserProfile();
        }
        return this.userProfile;
    }
}

