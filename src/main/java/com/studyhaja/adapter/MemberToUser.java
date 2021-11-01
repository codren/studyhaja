package com.studyhaja.adapter;


import com.studyhaja.domain.Member;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.List;

// member 객체를 User 객체로 변환
@Getter
public class MemberToUser extends User {

    private Member member;

    public MemberToUser(Member member) {
        super(member.getNickname(), member.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.member = member;
    }
}
