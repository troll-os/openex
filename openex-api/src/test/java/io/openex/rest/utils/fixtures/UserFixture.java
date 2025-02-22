package io.openex.rest.utils.fixtures;

import io.openex.rest.user.form.login.LoginUserInput;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class UserFixture {

    public static final String RAW_PASSWORD = "myPwd24!@";
    public static final String ENCODED_PASSWORD = getEncodePwd("myPwd24!@");

    private static String getEncodePwd(String rawPws) {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode(rawPws);
    }

    public static final String EMAIL = "user@filigran.io";

    public static LoginUserInput.LoginUserInputBuilder getDefault() {
        return LoginUserInput.builder();
    }

    public static LoginUserInput.LoginUserInputBuilder getDefaultWithPwd() {
        return LoginUserInput.builder().password(RAW_PASSWORD);
    }

    public static LoginUserInput getLoginUserInput() {
        return LoginUserInput.builder().login(EMAIL).password(RAW_PASSWORD).build();
    }

}
