package com.studyhaja.validator;

import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component
@RequiredArgsConstructor
public class JoinFormValidator implements Validator {

    private final MemberRepository memberRepository;

    // 어떤 객체를 검증할 것인지
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(JoinFormDto.class);
    }


    // 검증 수행 및 에러메시지 지정
    @Override
    public void validate(Object target, Errors errors) {

        JoinFormDto joinFormDto = (JoinFormDto) target;

        if (memberRepository.existsByEmail(joinFormDto.getEmail())) {
            errors.rejectValue("email", "invalid.email",
                    new Object[]{joinFormDto.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if (memberRepository.existsByNickname(joinFormDto.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname",
                    new Object[]{joinFormDto.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
    }
}

