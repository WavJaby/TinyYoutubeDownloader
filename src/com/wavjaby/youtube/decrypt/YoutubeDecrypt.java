package com.wavjaby.youtube.decrypt;

import com.wavjaby.youtube.VideoInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wavjaby.youtube.URLDataGetter.getUrlData;
import static com.wavjaby.youtube.decrypt.ErrorType.*;


public class YoutubeDecrypt {
    private final Map<String, Integer> functionType = new HashMap<>();
    private String[] steps;

    public YoutubeDecrypt(String baseJsUrl, VideoInfo.GetVideoErrorEvent event) {
        String baseJS = getUrlData(baseJsUrl);
        if (baseJS == null) {
            event.onError(CANT_GET_BASE_JS, "Could not get base.js");
            System.err.println("Could not get baseJS");
            return;
        }

        //尋找亂序函式
        Pattern pattern = Pattern.compile("function\\(\\w+\\)\\{[\\w=.(\")]+;([\\w=.,(\");]*);return \\w+\\.join[(\")]*\\}\\;");
        Matcher match = pattern.matcher(baseJS);
        if (match.find()) {
            steps = match.group(1).split(";");
        } else {
            event.onError(SIGNATURE_ENTRY_FUNCTION_NOT_FOUND, "Could not find the entry function for signature");
            return;
        }

        //尋找函式定義
        String fnMapName = steps[0].split("\\.")[0];
        pattern = Pattern.compile("\\w+ " + fnMapName + "=\\{(.*\\n*.*\\n*.*\\})\\}\\;");
        match = pattern.matcher(baseJS);
        String functions;
        if (match.find()) {
            functions = match.group(1).replace("\n", "");
        } else {
            event.onError(SIGNATURE_DECIPHER_FUNCTION_NOT_FOUND, "Could not find the signature decipher function body");
            return;
        }
        //分出函式
        pattern = Pattern.compile("(\\w+):function\\(\\w*,*\\w*\\)\\{([\\w,.() =;%\\[\\]]*)\\}");
        match = pattern.matcher(functions);
        while (match.find()) {
            String fct = match.group(2);
            int type;
            if (fct.contains("splice"))
                type = DecipherFunctionType.SLICE;
            else if (fct.contains("reverse"))
                type = DecipherFunctionType.REVERSE;
            else if (fct.contains("var") && fct.contains("="))
                type = DecipherFunctionType.SWAP;
            else {
                type = 4;
                event.onError(UNKNOWN_SIGNATURE_TYPE, "unknown signature decipher function");
            }
            functionType.put(match.group(1), type);
        }
    }

    public String decode(String signature) {
        //解譯signature
        for (String i : steps) {
            String funName = i.split("\\.")[1];
            funName = funName.substring(0, funName.indexOf("("));
            String valueString = i.split(",")[1];
            int value = Integer.parseInt(valueString.substring(0, valueString.indexOf(")")));
            int type = functionType.get(funName);


            if (type == DecipherFunctionType.REVERSE) {
                StringBuilder builder = new StringBuilder(signature);
                signature = builder.reverse().toString();
            } else if (type == DecipherFunctionType.SLICE) {
                signature = signature.substring(value);
            } else if (type == DecipherFunctionType.SWAP) {
                char c = signature.charAt(0);
                value = value % signature.length();
                StringBuilder builder = new StringBuilder(signature);
                builder.setCharAt(0, signature.charAt(value));
                builder.setCharAt(value, c);
                signature = builder.toString();
            }
        }
        return signature;
    }
}
