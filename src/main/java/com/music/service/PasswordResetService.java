package com.music.service;


import com.music.common.R;
import com.music.model.request.PasswordResetRequest;

public interface PasswordResetService {

    R sendVerificationCode(String email);

    R resetPassword(PasswordResetRequest request);
}
