package com.studyhaja.service;

import com.studyhaja.domain.Member;
import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.adapter.MemberToUser;
import com.studyhaja.dto.NotificationDto;
import com.studyhaja.dto.PasswordFormDto;
import com.studyhaja.dto.ProfileFormDto;
import com.studyhaja.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // 회원가입
    public Member saveMember(JoinFormDto joinFormDto) {
        Member newMember = createMember(joinFormDto);
        sendEmailToken(newMember);
        return newMember;
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

    // 인증 이메일 발송
    public void sendEmailToken(Member newMember) {
        newMember.generateEmailToken();
        memberRepository.save(newMember);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newMember.getEmail());
        mailMessage.setSubject("스터디하자, 회원가입 인증");
        mailMessage.setText("/member/email/check-token?token=" + newMember.getEmailToken() +
                "&email=" + newMember.getEmail());
        javaMailSender.send(mailMessage);
    }

    // 자동 로그인
    public void memberLogin(Member member) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new MemberToUser(member), // "member" 멤버변수를 가지고 있는 User 객체
                member.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        securityContext.setAuthentication(token);
    }

    @Transactional(readOnly=true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(emailOrNickname);

        if (member == null) { member = memberRepository.findByNickname(emailOrNickname); }
        if (member == null) { throw new UsernameNotFoundException(emailOrNickname); }

        return new MemberToUser(member);
    }

    public void completeJoin(Member member) {
        member.completeJoin();
        memberLogin(member);
    }

    public void updateProfile(Member member, ProfileFormDto profileFormDto) {
        modelMapper.map(profileFormDto, member);
        memberRepository.save(member);
    }

    public boolean checkCurrentPassword(String savedPassword, String currentPassword) {
        return passwordEncoder.matches(currentPassword, savedPassword);
    }

    public void changePassword(Member member, PasswordFormDto passwordFormDto) {
        member.changePassword(passwordEncoder.encode(passwordFormDto.getNewPassword()));
        memberRepository.save(member);
    }

    public void updateNotifications(Member member, NotificationDto notificationDto) {
        modelMapper.map(notificationDto, member);
        memberRepository.save(member);
    }
}