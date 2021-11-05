package com.studyhaja.controller;


import com.studyhaja.annotation.CurrentMember;
import com.studyhaja.domain.Member;
import com.studyhaja.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping(value = "/")
    public String homePage(@CurrentMember Member member, Model model) {

        if (member != null) {
            model.addAttribute("member", member);
        }
        return "home";
    }
}

