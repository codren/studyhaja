package com.studyhaja;

import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithMemberSecurityContextFactory implements WithSecurityContextFactory<WithMember> {

    private final MemberService memberService;

    @Override
    public SecurityContext createSecurityContext(WithMember withMember) {

        String nickname = withMember.value();

        JoinFormDto joinFormDto = new JoinFormDto();
        joinFormDto.setNickname(nickname);
        joinFormDto.setEmail(nickname + "@naver.com");
        joinFormDto.setPassword("12341234");
        memberService.saveMember(joinFormDto);

        UserDetails principal = memberService.loadUserByUsername(nickname);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal,
                        principal.getPassword(), principal.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        return securityContext;
    }
}