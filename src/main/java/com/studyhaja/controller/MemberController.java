package com.studyhaja.controller;
import com.studyhaja.annotation.CurrentMember;
import com.studyhaja.domain.Member;
import com.studyhaja.dto.JoinFormDto;
import com.studyhaja.repository.MemberRepository;
import com.studyhaja.service.MemberService;
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

    private final MemberService memberService;
    private final MemberRepository memberRepository;
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

    // 회원가입 및 인증 메일 발송
    @PostMapping("/new")
    public String JoinFormSubmit(@Valid JoinFormDto joinFormDto, Errors errors, Model model) {

        if (errors.hasErrors()) {
            return "member/joinForm";
        }

        Member savedMember = memberService.saveMember(joinFormDto);
        memberService.memberLogin(savedMember);

        model.addAttribute("email", joinFormDto.getEmail());
        return "member/emailCheck";
    }

    // 이메일 인증 화면
    @GetMapping("/email/check")
    public String emailCheckPage(@CurrentMember Member member, Model model) {

        model.addAttribute("email",member.getEmail());
        return "member/emailCheck";

    }

    // 이메일 검증 요청
    @GetMapping("/email/check-token")
    public String checkEmailToken(String token, String email, Model model) {

        String view = "member/emailCheckResult";
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            model.addAttribute("error", "wrongEmail");
            return view;
        }
        if (!token.equals(member.getEmailToken())) {
            model.addAttribute("error", "wrongToken");
            return view;
        }

        member.completeJoin();
        memberService.memberLogin(member);

        model.addAttribute("nickname", member.getNickname());
        model.addAttribute("numberOfMember", memberRepository.count());

        return view;
    }

}

