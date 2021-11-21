package com.studyhaja.controller;

import com.studyhaja.annotation.CurrentMember;
import com.studyhaja.domain.Member;
import com.studyhaja.domain.Tag;
import com.studyhaja.dto.NotificationDto;
import com.studyhaja.dto.PasswordFormDto;
import com.studyhaja.dto.ProfileFormDto;
import com.studyhaja.dto.TagsFormDto;
import com.studyhaja.repository.TagRepository;
import com.studyhaja.service.MemberService;
import com.studyhaja.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/settings")
public class SettingsController {

    private final MemberService memberService;
    private final TagRepository tagRepository;
    private final PasswordFormValidator passwordFormValidator;
    private final ModelMapper modelMapper;

    @InitBinder("passwordFormDto")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    // 프로필 수정 페이지 요청
    @GetMapping(value = "/profile")

    public String profileForm(@CurrentMember Member member, Model model) {

        model.addAttribute(member);
        model.addAttribute("profile", modelMapper.map(member, ProfileFormDto.class));
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
    public String changePasswordForm(@CurrentMember Member member, PasswordFormDto passwordFormDto, Model model) {

        model.addAttribute(member);
        model.addAttribute(passwordFormDto);
        return "settings/passwordForm";
    }

    // 비밀번호 변경
    @PostMapping("/password")
    public String changePassword(@CurrentMember Member member, @Valid PasswordFormDto passwordFormDto, Errors errors,
                                 Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) { return "/settings/passwordForm"; }

        // 현재 비밀번호가 일치하는지 판단
        if (!memberService.checkCurrentPassword(member.getPassword(), passwordFormDto.getCurrentPassword())) {
            errors.rejectValue("currentPassword","wrong.value","현재 비밀번호가 일치하지 않습니다");
            return "settings/passwordForm";
        }

        memberService.changePassword(member, passwordFormDto);
        attributes.addFlashAttribute("message", "비밀번호가 변경되었습니다");
        return "redirect:/settings/password";
    }

    // 알림 설정 페이지 요청
    @GetMapping("/notifications")
    public String notificationsForm(@CurrentMember Member member, Model model) {

        model.addAttribute(member);
        model.addAttribute(modelMapper.map(member, NotificationDto.class));
        return "settings/notificationsForm";
    }

    // 알림 설정 변경
    @PostMapping("/notifications")
    public String updateNotifications(@CurrentMember Member member, @Valid NotificationDto notificationDto,
                                      Errors errors, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) { return "settings/notificationsForm"; }

        memberService.updateNotifications(member, notificationDto);
        redirectAttributes.addFlashAttribute("message", "알림 설정을 변경했습니다");
        return "redirect:/settings/notifications";
    }

    // 관심있는 스터디 주제 태그 등록 페이지 요청
    @GetMapping("/tags")
    public String tagsForm(@CurrentMember Member member, Model model) {

        model.addAttribute(member);
        Set<Tag> tags = memberService.getTags(member);
        model.addAttribute("tags", tags.stream().map(Tag::getTagName).collect(Collectors.toList()));
        return "settings/tagsForm";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentMember Member member,
                                 @RequestBody TagsFormDto tagsFormDto, Model model) {

        String tagName = tagsFormDto.getTagName();
        Tag tag = tagRepository.findByTagName(tagName).orElseGet(
                () -> tagRepository.save(Tag.builder().tagName(tagName).build()));

        memberService.addTag(member, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentMember Member member,
                                    @RequestBody TagsFormDto tagsFormDto, Model model) {

        String tagName = tagsFormDto.getTagName();
        Optional<Tag> tag = tagRepository.findByTagName(tagName);

        if (tag.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        memberService.removeTag(member, tag.get());
        return ResponseEntity.ok().build();
    }
}