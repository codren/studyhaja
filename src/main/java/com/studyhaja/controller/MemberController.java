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
import org.springframework.web.bind.annotation.*;
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

        model.addAttribute("email", member.getEmail());
        return "member/emailCheck";

    }

    // 인증 메일 재발송
    @GetMapping("/email/send-token")
    public String sendEmailToken(@CurrentMember Member member, Model model) {

        if (!member.canSendEmailToken()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한 번만 전송할 수 있습니다.");
        } else {

        }
        memberService.sendEmailToken(member);
        model.addAttribute("email", member.getEmail());
        return "member/emailCheck";
    }


    // 이메일 인증
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

        memberService.completeJoin(member);

        model.addAttribute("nickname", member.getNickname());
        model.addAttribute("numberOfMember", memberRepository.count());

        return view;
    }

    // 로그인
    @GetMapping("/login")
    public String loginForm() {

        return "member/loginForm";
    }

    // 로그인 실패
    @GetMapping("/login/fail")
    public String memberLoginFail(Model model) {
        model.addAttribute("loginFailMsg", "회원 정보가 올바르지 않습니다. 이메일(닉네임) 또는 비밀번호를 확인해주세요");
        return "member/loginForm";
    }

    // 프로필 페이지
    @GetMapping("/profile/{nickname}")
    public String memberProfile(@PathVariable String nickname, @CurrentMember Member curMember, Model model) {

        Member savedMember = memberRepository.findByNickname(nickname);
        if (savedMember == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute(savedMember);
        model.addAttribute("isOwner", savedMember.equals(curMember));
        return "member/profile";
    }


}

