package com.studyhaja.service;

import com.studyhaja.domain.Member;
import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void saveMember(JoinFormDto joinFormDto) {
        Member newMember = createMember(joinFormDto);
        sendEmailToken(newMember);
    }

    // 회원객체 생성
    private Member createMember(JoinFormDto joinFormDto) {
        Member member = Member.builder()
                .email(joinFormDto.getEmail())
                .nickname(joinFormDto.getNickname())
                .password(passwordEncoder.encode(joinFormDto.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollResultByWeb(true)
                .studyUpdateByWeb(true)
                .build();
        Member newMember = memberRepository.save(member);
        return newMember;
    }

    // 메일 인증 부분
    private void sendEmailToken(Member newMember) {
        newMember.generateEmailToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newMember.getEmail());
        mailMessage.setSubject("스터디하자, 회원가입 인증");
        mailMessage.setText("/member/email/check-token?token=" + newMember.getEmailToken() +
                "&email=" + newMember.getEmail());
        javaMailSender.send(mailMessage);
    }


}
