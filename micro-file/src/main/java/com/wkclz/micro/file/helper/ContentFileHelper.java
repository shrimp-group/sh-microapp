package com.wkclz.micro.file.helper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shrimp
 */
public class ContentFileHelper {

    private static final Pattern URL_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");

    public static List<String> extractUrls(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        List<String> urls = new ArrayList<>();
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            String url = matcher.group(1);
            urls.add(url);
        }
        return urls;
    }

    public static String replaceUrls(String text, List<String> from, List<String> to) {
        if (StringUtils.isBlank(text) || CollectionUtils.isEmpty(from) || CollectionUtils.isEmpty(to)) {
            return text;
        }
        for (int i = 0; i < from.size(); i++) {
            String f = from.get(i);
            String t = to.get(i);
            text = text.replace(f, t);
        }
        return text;
    }


    public static void main(String[] args) {
        String text = """
            <p>下面是一些图片：</p>
             <img src='https://example.com/image1.jpg' alt='Image 1'>
             <img src="https://example.com/image2.png" alt='Image 2'>
             <div><img src='/local/path/to/image3.gif'></div>
            """;

        List<String> strings = extractUrls(text);
        System.out.println(strings);
    }

}
