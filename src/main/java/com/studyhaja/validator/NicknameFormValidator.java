package com.studyhaja.validator;

import com.studyhaja.dto.NicknameFormDto;
import com.studyhaja.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameFormValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(NicknameFormDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {

        NicknameFormDto nicknameFormDto = (NicknameFormDto) target;

        if (memberRepository.existsByNickname(nicknameFormDto.getNewNickname())) {
            errors.rejectValue("newNickname","invalid.nickname", "이미 사용중인 닉네임입니다");
        }

    }
}
