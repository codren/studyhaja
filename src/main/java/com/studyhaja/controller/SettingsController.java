package com.studyhaja.controller;

import com.studyhaja.annotation.CurrentMember;
import com.studyhaja.domain.Member;
import com.studyhaja.dto.PasswordFormDto;
import com.studyhaja.dto.ProfileFormDto;
import com.studyhaja.service.MemberService;
import com.studyhaja.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/settings")
public class SettingsController {

    private final MemberService memberService;
    private final PasswordFormValidator passwordFormValidator;

    @InitBinder("passwordFormDto")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    // 프로필 수정 페이지 요청
    @GetMapping(value = "/profile")

    public String profileForm(@CurrentMember Member member, Model model) {

        model.addAttribute(member);
        model.addAttribute("profile", new ProfileFormDto(member));
        return "settings/profileForm";
    }

    // 프로필 수정
    @PostMapping(value = "/profile")
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

    // 비밀번호 변경 페이지 요청
    @GetMapping("/password")
    public String changePasswordForm(PasswordFormDto passwordFormDto, Model model) {

        model.addAttribute(passwordFormDto);
        return "/settings/passwordForm";
    }

    // 비밀번호 변경
    @PostMapping("/password")
    public String changePassword(@CurrentMember Member member, @Valid PasswordFormDto passwordFormDto, Errors errors,
                                 Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            return "/settings/passwordForm";
        }

        // 현재 비밀번호가 일치하는지 판단
        if (!memberService.checkCurrentPassword(member.getPassword(), passwordFormDto.getCurrentPassword())) {
            errors.rejectValue("currentPassword","wrong.value","현재 비밀번호가 일치하지 않습니다");
            return "/settings/passwordForm";
        }

        memberService.changePassword(member, passwordFormDto);
        attributes.addFlashAttribute("message", "비밀번호가 변경되었습니다");
        return "redirect:/settings/password";
    }
}