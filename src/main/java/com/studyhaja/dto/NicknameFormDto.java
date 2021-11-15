package com.studyhaja.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter @Setter
public class NicknameFormDto {

    @NotBlank
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$",
            message = "한글, 영어, 숫자, ''-'', ''_'' 만 입력 가능합니다.")
    private String newNickname;
}
