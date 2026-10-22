package com.shijian.service;

import java.util.List;
import java.util.Map;

/**
 * AI内容审核服务
 * 基于规则和敏感词库的文本内容审核
 */
public interface ContentModerationService {

    /** 审核结果 */
    class ModerationResult {
        private boolean passed;          // 是否通过
        private double score;            // 质量评分 0-5
        private List<String> flags;      // 触发的规则标签
        private List<String> matchedWords; // 匹配到的敏感词
        private String suggestion;       // 建议: PASS, REVIEW, REJECT

        public ModerationResult(boolean passed, double score, List<String> flags, List<String> matchedWords, String suggestion) {
            this.passed = passed; this.score = score; this.flags = flags;
            this.matchedWords = matchedWords; this.suggestion = suggestion;
        }

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public List<String> getFlags() { return flags; }
        public void setFlags(List<String> flags) { this.flags = flags; }
        public List<String> getMatchedWords() { return matchedWords; }
        public void setMatchedWords(List<String> matchedWords) { this.matchedWords = matchedWords; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }

    /** 审核文本内容 */
    ModerationResult moderateText(String text);

    /** 批量审核 */
    List<ModerationResult> moderateBatch(List<String> texts);

    /** 添加自定义敏感词 */
    void addSensitiveWord(String word, String category, int severity);

    /** 获取所有敏感词 */
    List<Map<String, Object>> getSensitiveWords();

    /** 删除敏感词 */
    void removeSensitiveWord(Long id);
}
