package com.studyhaja.validator;

import com.studyhaja.dto.PasswordFormDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PasswordFormDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        PasswordFormDto passwordFormDto = (PasswordFormDto) target;

        if (!passwordFormDto.getNewPassword().equals(passwordFormDto.getNewPasswordConfirm())) {
            errors.rejectValue("newPassword", "wrong.value", "입력한 새 비밀번호가 일치하지 않습니다");
        }
    }
}