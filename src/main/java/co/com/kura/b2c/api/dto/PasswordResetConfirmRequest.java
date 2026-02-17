package co.com.kura.b2c.api.dto;

import lombok.Data;

@Data
public class PasswordResetConfirmRequest {

    private String token;
    private String newPassword;
}
