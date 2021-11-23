package com.studyhaja.controller;

import com.studyhaja.WithMember;
import com.studyhaja.domain.Member;
import com.studyhaja.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.ModelResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원 프로필 수정 - 입력값 정상")
    @WithMember(value = "test")
    public void updateProfile_valid_input() throws Exception {

        mockMvc.perform(post("/settings/profile")
                .with(csrf())
                .param("bio", "한 줄 소개 변경 테스트"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("회원 프로필 수정 - 입력값 오류")
    @WithMember(value = "test")
    public void updateProfile_invalid_input() throws Exception {

        String invalidInput = "가나다라마바사가나다라마바사가나다라마바사가나다라마바사가나다라마바사가나다라마바사가나다라마바사가나다라마바사";

        mockMvc.perform(post("/settings/profile")
                .with(csrf())
                .param("bio", invalidInput))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profileForm"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());    // BindingResult.errors 확인

        Member member = memberRepository.findByNickname("test");
        assertNull(member.getBio());
    }

    @Test
    @DisplayName("비밀번호 수정 - 입력값 정상")
    @WithMember(value = "test")
    public void passwordChange_valid_input() throws Exception {

        mockMvc.perform(post("/settings/password")
                .param("currentPassword", "12341234")
                .param("newPassword", "11111111")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Member member = memberRepository.findByNickname("test");
        assertTrue(passwordEncoder.matches("11111111", member.getPassword()));
    }

    @Test
    @DisplayName("비밀번호 수정 - 현재 비밀번호 불일치")
    @WithMember(value = "test")
    public void passwordChange_invalid_currentPassword() throws Exception {

        ModelAndView mockResult = mockMvc.perform(post("/settings/password")
                .param("currentPassword", "11111111")
                .param("newPassword", "22222222")
                .param("newPasswordConfirm", "22222222")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/passwordForm"))
                .andExpect(model().attributeHasFieldErrors("passwordFormDto", "currentPassword"))
                .andReturn().getModelAndView();

        BindingResult bindingResult = (BindingResult) mockResult.getModel()
                                        .get("org.springframework.validation.BindingResult.passwordFormDto");
        String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
        assertEquals(errorMessage, "현재 비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("비밀번호 수정 - 새 비밀번호 확인 불일치")
    @WithMember(value = "test")
    public void passwordChange_invalid_newPasswordConfirm() throws Exception {

        mockMvc.perform(post("/settings/password")
                .param("currentPassword", "12341234")
                .param("newPassword", "11111111")
                .param("newPasswordConfirm", "22222222")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("/settings/passwordForm"))
                .andExpect(model().attributeHasFieldErrors("passwordFormDto", "newPassword"));
    }
}