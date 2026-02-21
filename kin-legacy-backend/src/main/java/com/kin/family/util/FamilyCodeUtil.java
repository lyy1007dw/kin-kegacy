package com.kin.family.util;

import java.util.Random;

public class FamilyCodeUtil {
    private static final Random RANDOM = new Random();

    public static String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }
}
