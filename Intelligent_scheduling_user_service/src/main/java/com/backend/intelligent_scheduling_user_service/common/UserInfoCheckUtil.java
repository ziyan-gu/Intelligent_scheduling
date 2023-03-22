package com.backend.intelligent_scheduling_user_service.common;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInfoCheckUtil {
    public static boolean isValidEmail(String email) {
        if ((email != null) && (!email.isEmpty())) {
            return Pattern.matches("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$", email);
        }
        return false;
    }

    public static boolean isValidUserAccount(String userAccount){
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\.<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return true;
        }
        return false;
    }
}