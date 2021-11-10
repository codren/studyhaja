package com.studyhaja.domain;

import com.studyhaja.dto.ProfileFormDto;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private String emailToken;
    private LocalDateTime emailTokenGeneratedTime;

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

    public void completeJoin() {
        this.emailVerified = true;
        this.joinedTime = LocalDateTime.now();
    }

    public void generateEmailToken() {
        this.emailToken = UUID.randomUUID().toString();
        this.emailTokenGeneratedTime = LocalDateTime.now();
    }

    public boolean canSendEmailToken() {
        return this.emailTokenGeneratedTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void updateProfile(ProfileFormDto profileFormDto) {
        this.bio = profileFormDto.getBio();
        this.refUrl = profileFormDto.getRefUrl();
        this.occupation = profileFormDto.getOccupation();
        this.location = profileFormDto.getLocation();
        this.profileImg = profileFormDto.getProfileImg();
    }
}
