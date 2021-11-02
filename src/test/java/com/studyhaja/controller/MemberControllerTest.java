package com.studyhaja.controller;

import com.studyhaja.adapter.MemberToUser;
import com.studyhaja.domain.Member;
import com.studyhaja.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.User;
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
    private MemberRepository memberRepository;

    @MockBean
    private JavaMailSender javaMailSender;

    private Member createMember() {
        Member member = Member.builder()
                .email("test@naver.com")
                .nickname("test")
                .password("12341234")
                .build();
        member.generateEmailToken();
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

        // javaMailSender 구현체가 send() 메소드를 통해서 SimpleMailMessage.class 클래스의 아무(any) 인스턴스를 보내야함
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
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
}

