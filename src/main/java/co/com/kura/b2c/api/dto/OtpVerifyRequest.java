package co.com.kura.b2c.api.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {

    private String email;
    private String code;
}
