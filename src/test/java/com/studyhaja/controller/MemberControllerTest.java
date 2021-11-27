package com.studyhaja.controller;

import com.studyhaja.adapter.MemberToUser;
import com.studyhaja.domain.Member;
import com.studyhaja.dto.EmailMessage;
import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.repository.MemberRepository;
import com.studyhaja.service.EmailService;
import com.studyhaja.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private EmailService emailService;

    private Member createMember() {

        JoinFormDto joinFormDto = new JoinFormDto();
        joinFormDto.setEmail("test@naver.com");
        joinFormDto.setNickname("test");
        joinFormDto.setPassword("12341234");
        Member member = memberService.saveMember(joinFormDto);
        return member;
    }


    @Test
    @DisplayName("회원가입 페이지 요청 테스트")
    void joinFormTest() throws Exception{

        mockMvc.perform(get("/member/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/joinForm"))
                .andExpect(model().attributeExists("joinFormDto"));
    }

    @Test
    @DisplayName("회원가입 처리 - 입력값 정상")
    void joinFormSubmit_valid_input() throws Exception {

        mockMvc.perform(post("/member/new")
                .param("email", "test@naver.com")
                .param("nickname", "test")
                .param("password", "12341234")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/emailCheck"))
                .andExpect(model().attributeExists("email"))
                .andExpect(authenticated());

        Member member = memberRepository.findByEmail("test@naver.com");
        assertNotNull(member);
        assertNotEquals(member.getPassword(), "12341234");

        // emailService 구현체가 send() 메소드를 통해서 EmailMessage 클래스의 아무(any) 인스턴스를 보내야함
        then(emailService).should().send(any(EmailMessage.class));
    }

    @Test
    @DisplayName("회원가입 처리 - 입력값 오류")
    void joinFormSubmit_invalid_input() throws Exception {

        mockMvc.perform(post("/member/new")
                .param("email", "test@naver.com")
                .param("nickname", "test@@")
                .param("password", "12345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/joinForm"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("이메일 인증 - 입력값 정상")
    void emailTokenCheckTest_valid_input() throws Exception {

        Member member = createMember();
        Member savedMember = memberRepository.save(member);

        mockMvc.perform(get("/member/email/check-token")
                .param("email", member.getEmail())
                .param("token", member.getEmailToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfMember"))
                .andExpect(view().name("member/emailCheckResult"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("이메일 인증 - 입력값 오류")
    void emailTokenCheckTest_invalid_input() throws Exception {

        mockMvc.perform(get("/member/email/check-token")
                .param("email", "test@naver.com")
                .param("token", "12341234"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("member/emailCheckResult"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("이메일 재전송 - 1시간 이내")
    void resendEmailTokenFail() throws Exception {

        Member member = createMember();
        User loginMember = new MemberToUser(member);

        mockMvc.perform(get("/member/email/send-token")
                .with(user(loginMember)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("member/emailCheck"))
                .andDo(print());
    }

    @Test
    @DisplayName("이메일 재전송 - 1시간 이후")
    void resendEmailTokenSuccess() throws Exception {

        Member member = createMember();
        member.setEmailTokenGeneratedTime(LocalDateTime.now().minusHours(2));
        User loginMember = new MemberToUser(member);

        mockMvc.perform(get("/member/email/send-token")
                .with(user(loginMember)))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("member/emailCheck"))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {

        createMember();
        mockMvc.perform(post("/member/login")
                .param("username", "test")
                .param("password", "12341234")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test"));
    }

    @Test
    @DisplayName("로그인 실패")
    void loginFail() throws Exception {

        createMember();
        mockMvc.perform(post("/member/login")
                .param("username", "test@naver.com")
                .param("password", "123412345")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login/fail"))
                .andExpect(unauthenticated());

        then(mockMvc.perform(get("/member/login/fail"))
                .andExpect(model().attributeExists("loginFailMsg"))
                .andExpect(view().name("member/loginForm")));
    }

    @Test
    @DisplayName("로그아웃")
    @WithMockUser
    void logout() throws Exception {

        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());

    }

    @Test
    @DisplayName("프로필 페이지 요청 - 주인")
    void memberProfilePage_owner() throws Exception {

       Member member = createMember();
       User loginMember = new MemberToUser(member);

        mockMvc.perform(get("/member/profile/" + loginMember.getUsername())
                .with(user(loginMember)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isOwner", true))
                .andExpect(view().name("member/profile"));
    }


    @Test
    @DisplayName("프로필 페이지 요청 - 방문자")
    void memberProfilePage_guest() throws Exception {

        Member member = createMember();
        User guest = new MemberToUser(member);

        Member owner = Member.builder()
                        .nickname("user")
                        .email("user@naver.com")
                        .password("12341234")
                        .build();
        memberRepository.save(owner);

        mockMvc.perform(get("/member/profile/" + owner.getNickname())
                .with(user(guest)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isOwner", false))
                .andExpect(view().name("member/profile"));
    }

    @Test
    @DisplayName("로그인을 위한 인증 이메일 요청 - 입력값 정상")
    void emailLoginTokenRequest_valid_input() throws Exception {

        Member member = createMember();

        mockMvc.perform(post("/member/email/send-login-token")
                .with(csrf())
                .param("email", member.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/email/login"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("로그인을 위한 인증 이메일 요청 - 입력값 오류")
    void emailLoginTokenRequest_invalid_input() throws Exception {

        Member member = createMember();

        // 이메일 존재하지 않음
        mockMvc.perform(post("/member/email/send-login-token")
                .with(csrf())
                .param("email", "wrong@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/email/login"))
                .andExpect(flash().attributeExists("error"));

        member.setEmailLoginTokenGeneratedTime(LocalDateTime.now());

        // 로그인 인증 이메일을 1시간내에 보낸적이 있음
        mockMvc.perform(post("/member/email/send-login-token")
                .with(csrf())
                .param("email", "test@naver.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/email/login"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("이메일 인증 로그인  - 입력값 정상")
    void checkEmailLoginToken_valid_input() throws Exception {

        Member member = createMember();
        memberService.sendEmailLoginToken(member);

        mockMvc.perform(get("/member/email/check-login-token")
                .param("email", member.getEmail())
                .param("token", member.getEmailLoginToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("member/emailLoginSuccess"))
                .andExpect(model().attributeExists("member"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("이메일 인증 로그인  - 입력값 오류")
    void checkEmailLoginToken_invalid_input() throws Exception {

        Member member = createMember();
        memberService.sendEmailLoginToken(member);

        mockMvc.perform(get("/member/email/check-login-token")
                .param("email", member.getEmail())
                .param("token", "wrongToken"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/emailLoginSuccess"))
                .andExpect(model().attributeDoesNotExist("member"))
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated());

    }
}

