package com.unilead;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utils {
    public static final String SDK_VERSION = "0.7";

    public static String sha1(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString((0xFF & messageDigest[i]) | 0x100).substring(1));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (NullPointerException e) {
            return "";
        }
    }

    public static boolean deviceCanHandleIntent(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return (activities.size() > 0);
    }

	private Utils() {
    }
}

