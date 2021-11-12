package com.studyhaja.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class PasswordFormDto {

    @NotBlank
    @Length(min = 8, max = 50)
    private String currentPassword;

    @NotBlank
    @Length(min = 8, max = 50)
    private String newPassword;

    @NotBlank
    @Length(min = 8, max = 50)
    private String newPasswordConfirm;

}
