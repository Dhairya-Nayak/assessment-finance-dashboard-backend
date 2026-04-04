package com.financedashboard.dto.request;

import com.financedashboard.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    private User.UserStatus status;

    private Set<String> roles;
}
