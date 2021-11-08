package com.studyhaja.controller;

import com.studyhaja.annotation.CurrentMember;
import com.studyhaja.domain.Member;
import com.studyhaja.dto.ProfileFormDto;
import com.studyhaja.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final MemberService memberService;

    @GetMapping(value = "/settings/profile")
    public String profileForm(@CurrentMember Member member, Model model) {

        model.addAttribute(member);
        model.addAttribute("profile", new ProfileFormDto(member));
        return "settings/profileForm";
    }

    @PostMapping(value = "/settings/profile")
    public String updateProfile(@CurrentMember Member member, @Valid ProfileFormDto profileFormDto,
                                Errors errors, Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute(member);
            model.addAttribute("profile", profileFormDto);
            return "settings/profileForm";
        }

        memberService.updateProfile(member, profileFormDto);
        redirectAttributes.addFlashAttribute("message", "프로필을 수정했습니다");
        return "redirect:/settings/profile";
    }
}