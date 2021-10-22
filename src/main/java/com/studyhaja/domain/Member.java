package com.studyhaja.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    // 로그인 부분
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;
    private boolean emailVerified;
    private boolean emailToken;

    // 프로필(마이페이지) 부분
    private LocalDateTime joinedTime;
    private String bio;
    private String refUrl;
    private String occupation;
    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImg;

    // 알림 설정 부분
    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyEnrollResultByEmail;
    private boolean studyEnrollResultByWeb;
    private boolean studyUpdateByEmail;
    private boolean studyUpdateByWeb;

}
