package com.studyhaja.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NotificationDto {

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;

    private boolean studyEnrollResultByEmail;
    private boolean studyEnrollResultByWeb;

    private boolean studyUpdateByEmail;
    private boolean studyUpdateByWeb;
}
