package co.com.kura.b2c.api.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;
}
