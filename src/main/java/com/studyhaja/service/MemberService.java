package com.studyhaja.service;

import com.studyhaja.domain.Member;
import com.studyhaja.domain.Tag;
import com.studyhaja.domain.Zone;
import com.studyhaja.dto.*;
import com.studyhaja.adapter.MemberToUser;
import com.studyhaja.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;

    @Value("${app.host}")
    String host;

    // 회원가입
    public Member saveMember(JoinFormDto joinFormDto) {
        Member newMember = createMember(joinFormDto);
        return newMember;
    }

    // 회원객체 생성
    private Member createMember(JoinFormDto joinFormDto) {
        joinFormDto.setPassword(passwordEncoder.encode(joinFormDto.getPassword()));
        Member member = modelMapper.map(joinFormDto, Member.class);
        sendEmailToken(member);
        return memberRepository.save(member);
    }

    // 인증 이메일 발송
    public void sendEmailToken(Member newMember) {

        newMember.generateEmailToken();

        // 템플릿으로 넘기는 Model과 동일한 역할
        Context context = new Context();
        context.setVariable("link","/member/email/check-token?token=" + newMember.getEmailToken() +
                "&email=" + newMember.getEmail());
        context.setVariable("nickname", newMember.getNickname());
        context.setVariable("host",host);
        String message = templateEngine.process("mail/Joinlink", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newMember.getEmail())
                .subject("스터디하자, 회원가입 인증")
                .content(message)
                .build();
        emailService.send(emailMessage);
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

    // 비밀번호 없이 로그인을 위한 인증 메일 토큰 전송
    public void sendEmailLoginToken(Member member) {

        member.generateEmailLoginToken();

        // 템플릿으로 넘기는 Model과 동일한 역할
        Context context = new Context();
        context.setVariable("link","/member/email/check-login-token?token=" + member.getEmailLoginToken() +
                        "&email=" + member.getEmail());
        context.setVariable("nickname", member.getNickname());
        context.setVariable("host",host);
        String message = templateEngine.process("mail/emailLoginLink", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(member.getEmail())
                .subject("스터디하자, 이메일 인증을 통한 로그인")
                .content(message)
                .build();
        emailService.send(emailMessage);
    }

    public Set<Tag> getTags(Member member) {

        Optional<Member> savedMember = memberRepository.findById(member.getId());
        return savedMember.orElseThrow().getTags();
    }

    public void addTag(Member member, Tag tag) {

        Optional<Member> savedMember =  memberRepository.findById(member.getId());
        savedMember.get().getTags().add(tag);
    }

    public void removeTag(Member member, Tag tag) {

        Optional<Member> savedMember = memberRepository.findById(member.getId());
        savedMember.get().getTags().remove(tag);
    }

    public Set<Zone> getZones(Member member) {

        Optional<Member> savedMember = memberRepository.findById(member.getId());
        return savedMember.get().getZones();
    }

    public void addZone(Member member, Zone zone) {

        Optional<Member> savedMember = memberRepository.findById(member.getId());
        savedMember.get().getZones().add(zone);
    }

    public void removeZone(Member member, Zone zone) {

        Optional<Member> savedMember = memberRepository.findById(member.getId());
        savedMember.get().getZones().remove(zone);
    }
}