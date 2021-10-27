package com.studyhaja.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private JavaMailSender javaMailSender;

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
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

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
                .andExpect(view().name("member/joinForm"));
    }
}
