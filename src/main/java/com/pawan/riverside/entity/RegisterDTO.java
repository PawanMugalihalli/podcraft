package com.pawan.riverside.entity;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterDTO {
    @NotEmpty
    private String username;
    @Size(min=6, message = "Password size should be at least of 6")
    private String password;
    private String confirmPassword;
}
