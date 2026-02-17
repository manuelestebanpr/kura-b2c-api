package co.com.kura.b2c.api.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String cedula;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private boolean consentLey1581;
}
