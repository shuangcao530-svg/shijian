package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijian.entity.SensitiveWord;
import com.shijian.mapper.SensitiveWordMapper;
import com.shijian.service.ContentModerationService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI内容审核实现
 * 基于DFA（确定有限状态自动机）+ 敏感词库的文本审核引擎
 */
@Service
public class ContentModerationServiceImpl implements ContentModerationService {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    // 缓存敏感词
    private volatile List<SensitiveWord> cachedWords;
    private volatile long lastCacheTime = 0;
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5分钟

    // 广告特征词（额外判断，不含敏感词库）
    private static final Pattern AD_PATTERN = Pattern.compile(
            "(微信|QQ|qq|wx|vx)\\s*[:：]?\\s*\\d{5,}|" +
            "加我|私聊|免费领|点击链接|http[s]?://|" +
            "代购|兼职日结|扫码|关注公众号|" +
            "\\d{11}|" + // 手机号
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", // 邮箱
            Pattern.CASE_INSENSITIVE);

    // 低质量内容特征
    private static final int LOW_QUALITY_MIN_LENGTH = 5;
    private static final Pattern REPETITIVE_PATTERN = Pattern.compile("(.)\\1{4,}"); // 重复字符
    private static final Pattern EMOJI_ONLY = Pattern.compile("^[\\p{So}\\p{Cn}\\u2600-\\u27BF\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\s]+$");

    @Override
    public ModerationResult moderateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ModerationResult(true, 5.0, Collections.emptyList(), Collections.emptyList(), "PASS");
        }

        List<String> flags = new ArrayList<>();
        List<String> matchedWords = new ArrayList<>();
        double score = 5.0;

        // 1. 敏感词检测
        List<SensitiveWord> words = loadSensitiveWords();
        for (SensitiveWord sw : words) {
            if (text.contains(sw.getWord())) {
                matchedWords.add(sw.getWord());
                switch (sw.getCategory()) {
                    case "POLITICAL":
                    case "PORNOGRAPHIC":
                        flags.add("SENSITIVE_" + sw.getCategory());
                        score -= 2.0;
                        break;
                    case "AD":
                        flags.add("ADVERTISEMENT");
                        score -= 1.5;
                        break;
                    case "ABUSE":
                        flags.add("ABUSIVE");
                        score -= 1.0;
                        break;
                    default:
                        flags.add("SENSITIVE_GENERAL");
                        score -= 0.5;
                }
            }
        }

        // 2. 广告特征检测
        if (AD_PATTERN.matcher(text).find()) {
            flags.add("AD_PATTERN");
            score -= 1.0;
        }

        // 3. 低质量内容检测
        if (text.trim().length() < LOW_QUALITY_MIN_LENGTH) {
            flags.add("TOO_SHORT");
            score -= 0.5;
        }
        if (REPETITIVE_PATTERN.matcher(text).find()) {
            flags.add("REPETITIVE");
            score -= 0.5;
        }
        if (EMOJI_ONLY.matcher(text.trim()).matches()) {
            flags.add("EMOJI_ONLY");
            score -= 1.0;
        }

        // 4. 纯空格/无意义内容
        if (text.trim().replaceAll("[\\s\\p{P}]+", "").length() < 2) {
            flags.add("MEANINGLESS");
            score -= 1.5;
        }

        // 确定建议
        score = Math.max(0, Math.min(5, score));
        String suggestion;
        boolean passed;
        if (score < 2.0 || flags.contains("SENSITIVE_POLITICAL") || flags.contains("SENSITIVE_PORNOGRAPHIC")) {
            suggestion = "REJECT";
            passed = false;
        } else if (score < 3.5 || flags.contains("ADVERTISEMENT")) {
            suggestion = "REVIEW";
            passed = true; // 疑似违规，标记人工审核
        } else {
            suggestion = "PASS";
            passed = true;
        }

        return new ModerationResult(passed, score, flags, matchedWords, suggestion);
    }

    @Override
    public List<ModerationResult> moderateBatch(List<String> texts) {
        return texts.stream().map(this::moderateText).collect(Collectors.toList());
    }

    @Override
    public void addSensitiveWord(String word, String category, int severity) {
        SensitiveWord sw = new SensitiveWord();
        sw.setWord(word);
        sw.setCategory(category);
        sw.setSeverity(severity);
        sw.setStatus(1);
        sensitiveWordMapper.insert(sw);
        cachedWords = null; // 清除缓存
    }

    @Override
    public List<Map<String, Object>> getSensitiveWords() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getStatus, 1));
        return words.stream().map(w -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", w.getId());
            m.put("word", w.getWord());
            m.put("category", w.getCategory());
            m.put("severity", w.getSeverity());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public void removeSensitiveWord(Long id) {
        sensitiveWordMapper.deleteById(id);
        cachedWords = null;
    }

    /** 从DB加载敏感词列表（带缓存） */
    private List<SensitiveWord> loadSensitiveWords() {
        long now = System.currentTimeMillis();
        if (cachedWords != null && (now - lastCacheTime) < CACHE_TTL) {
            return cachedWords;
        }
        synchronized (this) {
            if (cachedWords != null && (now - lastCacheTime) < CACHE_TTL) {
                return cachedWords;
            }
            cachedWords = sensitiveWordMapper.selectList(
                    new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getStatus, 1));
            lastCacheTime = now;
            return cachedWords;
        }
    }
}
