package com.studyhaja.controller;

import com.studyhaja.WithMember;
import com.studyhaja.domain.Member;
import com.studyhaja.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
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
}