package com.example.sandbox;

import java.util.Map;
import java.util.regex.Pattern;

public final class TemplateRenderer {
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([^{}]+?)\\s*\\}\\}");
    private TemplateRenderer() {}
    public static String render(String template, Map<String, String> variables) {
        if (template == null || template.length() > 20000) throw new IllegalArgumentException("模板长度超过限制");
        if (variables == null || variables.size() > 100) throw new IllegalArgumentException("变量参数超出限制");
        for (var entry : variables.entrySet()) if (entry.getKey() == null || entry.getValue() == null
                || entry.getKey().length() > 100 || entry.getValue().length() > 10000)
            throw new IllegalArgumentException("变量名称或内容过长");
        var matcher = VARIABLE.matcher(template);
        StringBuilder output = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String value = variables.get(name);
            if (value == null || value.isBlank()) throw new IllegalArgumentException("请填写变量：" + name);
            matcher.appendReplacement(output, java.util.regex.Matcher.quoteReplacement(value));
            if (output.length() > 20000) throw new IllegalArgumentException("渲染内容最多 20000 字");
        }
        matcher.appendTail(output);
        if (output.length() > 20000) throw new IllegalArgumentException("渲染内容最多 20000 字");
        return output.toString();
    }
}
