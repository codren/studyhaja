package com.studyhaja.controller;
import com.studyhaja.domain.Member;
import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.repository.MemberRepository;
import com.studyhaja.validator.JoinFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    private final MemberRepository memberRepository;
    private final JoinFormValidator joinFormValidator;
    private final JavaMailSender javaMailSender;

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

        Member member = Member.builder()
                .email(joinFormDto.getEmail())
                .nickname(joinFormDto.getNickname())
                .password(joinFormDto.getPassword())    // TODO encoding
                .studyCreatedByWeb(true)
                .studyEnrollResultByWeb(true)
                .studyUpdateByWeb(true)
                .build();
        Member newMember = memberRepository.save(member);

        // 메일 인증 부분
        newMember.generateEmailToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newMember.getEmail());
        mailMessage.setSubject("스터디하자, 회원가입 인증");
        mailMessage.setText("/member/email/check-token?token=" + newMember.getEmailToken() +
                "&email=" + newMember.getEmail());
        javaMailSender.send(mailMessage);

        return "redirect:/";
    }
}
