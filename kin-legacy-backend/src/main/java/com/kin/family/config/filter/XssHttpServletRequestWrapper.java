package com.kin.family.config.filter;

import com.kin.family.util.XssUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return XssUtil.stripXss(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }

        return Arrays.stream(values)
                .map(XssUtil::stripXss)
                .toArray(String[]::new);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        Map<String, String[]> result = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            String[] strippedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                strippedValues[i] = XssUtil.stripXss(values[i]);
            }
            result.put(entry.getKey(), strippedValues);
        }

        return result;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return XssUtil.stripXss(value);
    }
}
