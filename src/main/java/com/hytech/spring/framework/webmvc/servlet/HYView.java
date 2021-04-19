package com.hytech.spring.framework.webmvc.servlet;

import com.hytech.spring.hyframework.util.StrUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dzp 2021/4/5
 */
public class HYView {

    private File viewFile;

    public HYView(File viewFile) {
        this.viewFile = viewFile;
    }

    public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse res) throws IOException {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");
        String line = null;
        while (null != (line = ra.readLine())) {
            line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String paramName = matcher.group();
                paramName.replaceAll("￥\\{|\\}", "");
                Object paramValue = model.get(paramName);
                line = matcher.replaceFirst(StrUtil.makeStringForRegexp(paramValue.toString()));
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(sb.toString());

    }



}
