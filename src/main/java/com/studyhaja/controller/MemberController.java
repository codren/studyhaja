package com.studyhaja.controller;
import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.validator.JoinFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.validation.Valid;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final JoinFormValidator joinFormValidator;

    // 이메일 또는 닉네임 중복 검증
    @InitBinder("joinFormDto")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(joinFormValidator);
    }

    // 회원가입 페이지 요청
    @GetMapping("/new")
    public String memberJoinForm(JoinFormDto joinFormDto, Model model) {

        model.addAttribute(joinFormDto);
        return "member/joinForm";
    }

    // 회원가입 요청
    @PostMapping("/new")
    public String JoinFormSubmit(@Valid JoinFormDto joinFormDto, Errors errors) {

        if (errors.hasErrors()) {
            return "member/joinForm";
        }
        return "redirect:/";
    }
}
