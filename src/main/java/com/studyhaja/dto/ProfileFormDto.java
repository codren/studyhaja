package com.studyhaja.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter @Setter
public class ProfileFormDto {

    @Length(max = 35)
    private String bio;

    @Length(max = 60)
    private String refUrl;

    @Length(max = 35)
    private String occupation;

    @Length(max = 35)
    private String location;

    private String profileImg;
}