package com.studyhaja.service;

import com.studyhaja.dto.EmailMessage;

public interface EmailService {

    void send(EmailMessage emailMessage);
}