package com.studyhaja.controller;

import com.studyhaja.annotation.CurrentMember;
import com.studyhaja.domain.Member;
import com.studyhaja.dto.ProfileFormDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping(value = "/settings/profile")
    public String profileForm(@CurrentMember Member member, Model model) {

        model.addAttribute(member);
        model.addAttribute("profile", new ProfileFormDto(member));
        return "settings/profileForm";
    }
}