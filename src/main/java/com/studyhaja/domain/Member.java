package com.studyhaja.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
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
    private String emailLoginToken;
    private LocalDateTime emailLoginTokenGeneratedTime;

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
    private boolean studyCreatedByWeb = true;
    private boolean studyEnrollResultByEmail;
    private boolean studyEnrollResultByWeb = true;
    private boolean studyUpdateByEmail;
    private boolean studyUpdateByWeb = true;

    @ManyToMany()
    private Set<Tag> tags;

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

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public boolean canSendEmailLoginToken() {

        if (this.emailLoginTokenGeneratedTime == null) {
            return true;
        }
        return this.emailLoginTokenGeneratedTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void generateEmailLoginToken() {
        this.emailLoginToken = UUID.randomUUID().toString();
        this.emailLoginTokenGeneratedTime = LocalDateTime.now();
    }
}
