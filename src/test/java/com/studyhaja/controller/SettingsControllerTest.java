package com.studyhaja.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhaja.WithMember;
import com.studyhaja.domain.Member;
import com.studyhaja.domain.Tag;
import com.studyhaja.domain.Zone;
import com.studyhaja.dto.TagsFormDto;
import com.studyhaja.dto.ZonesFormDto;
import com.studyhaja.repository.MemberRepository;
import com.studyhaja.repository.TagRepository;
import com.studyhaja.repository.ZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.ModelResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Test
    @DisplayName("?????? ????????? ?????? - ????????? ??????")
    @WithMember(value = "test")
    public void updateProfile_valid_input() throws Exception {

        mockMvc.perform(post("/settings/profile")
                .with(csrf())
                .param("bio", "??? ??? ?????? ?????? ?????????"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("?????? ????????? ?????? - ????????? ??????")
    @WithMember(value = "test")
    public void updateProfile_invalid_input() throws Exception {

        String invalidInput = "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????";

        mockMvc.perform(post("/settings/profile")
                .with(csrf())
                .param("bio", invalidInput))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profileForm"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());    // BindingResult.errors ??????

        Member member = memberRepository.findByNickname("test");
        assertNull(member.getBio());
    }

    @Test
    @DisplayName("???????????? ?????? - ????????? ??????")
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
    @DisplayName("???????????? ?????? - ?????? ???????????? ?????????")
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
        assertEquals(errorMessage, "?????? ??????????????? ???????????? ????????????");
    }

    @Test
    @DisplayName("???????????? ?????? - ??? ???????????? ?????? ?????????")
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

    @Test
    @DisplayName("?????? ??????")
    @WithMember(value = "test")
    public void addTagTest() throws Exception {

        TagsFormDto tagsFormDto = new TagsFormDto();
        tagsFormDto.setTagName("test");

        mockMvc.perform(post("/settings/tags")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsFormDto)))
                .andExpect(status().isOk());

        Optional<Tag> tag = tagRepository.findByTagName("test");
        assertNotNull(tag.get());
        assertTrue(memberRepository.findByNickname("test").getTags().contains(tag.get()));
    }

     @Test
    @DisplayName("?????? ??????")
    @WithMember(value = "test")
    public void removeTagTest() throws Exception {

        Member member = memberRepository.findByNickname("test");
        Tag tag = tagRepository.save(Tag.builder().tagName("test").build());
        member.getTags().add(tag);

        assertTrue(member.getTags().contains(tag));

        TagsFormDto tagsFormDto = new TagsFormDto();
        tagsFormDto.setTagName("test");

        mockMvc.perform(delete("/settings/tags")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagsFormDto)))
                .andExpect(status().isOk());

        assertFalse(member.getTags().contains("test"));
    }

    public Zone createZone() {

        Zone zone = Zone.builder().city("test").localNameOfCity("????????????").province("????????????").build();
        zoneRepository.save(zone);
        return zone;
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    @WithMember(value = "test")
    public void addZoneTest() throws Exception {

        Zone zone = createZone();
        ZonesFormDto zonesFormDto = new ZonesFormDto();
        zonesFormDto.setZoneName(zone.getLocalNameOfCity());

        mockMvc.perform(post("/settings/zones")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zonesFormDto)))
                .andExpect(status().isOk());

        Member member = memberRepository.findByNickname("test");
        assertTrue(member.getZones().contains(zone));
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    @WithMember(value = "test")
    public void removeZoneTest() throws Exception {

        Zone zone = createZone();
        ZonesFormDto zonesFormDto = new ZonesFormDto();
        zonesFormDto.setZoneName(zone.getLocalNameOfCity());

        Member member = memberRepository.findByNickname("test");
        member.getZones().add(zone);

        assertTrue(member.getZones().contains(zone));

        mockMvc.perform(delete("/settings/zones")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zonesFormDto)))
                .andExpect(status().isOk());

        assertFalse(member.getZones().contains(zone));
    }
}