package com.marvel.communityforum.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveWordFilterTrie {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordFilterTrie.class);
    private static final String REPLACEMENT = "***";
    private TrieNode root = new TrieNode();

    /**
     * 服务启动，Trie类加载到spring容器中，并被实例化为bean之后，调用的初始化方法
     * 读取敏感词文件，将敏感词添加到单词查找树中
     */
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive_words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String sensitiveWord;
            while ((sensitiveWord = reader.readLine()) != null) {
                addWord(sensitiveWord);
            }
        } catch (IOException ex) {
            logger.error("loading sensitive words file failed: " + ex.getMessage());
        }


    }

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        StringBuilder res = new StringBuilder();
        int begin = 0, end = 0;
        TrieNode curNode = root;

        while (end < text.length()) {
            char c = text.charAt(end);

            if (isSymbol(c)) {
                if (curNode == root) {
                    res.append(c);
                    begin++;
                }
                end++;
                continue;
            }

            curNode = curNode.getNextNode(c);
            if (curNode == null) {
                res.append(text.charAt(begin));
                end = ++begin;
                curNode = root;
                continue;
            }

            if (curNode.getIsWordEnding()) {
                res.append(REPLACEMENT);
                begin = ++end;
                curNode = root;
                continue;
            }

            end++;
        }
        res.append(text.substring(begin));

        return res.toString();
    }

    private boolean isSymbol(char c) {
        // 非字母非数字且非东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private static class TrieNode {
        private boolean isWordEnding = false;
        private Map<Character, TrieNode> next = new HashMap<>();

        private boolean getIsWordEnding() {
            return isWordEnding;
        }

        private void setIsWordEnding(boolean isWordEnding) {
            this.isWordEnding = isWordEnding;
        }

        private void setNextNode(Character c, TrieNode node) {
            next.put(c, node);
        }

        private TrieNode getNextNode(Character c) {
            return next.get(c);
        }
    }

    private void addWord(String word) {
        TrieNode curNode = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            TrieNode nextNode = curNode.getNextNode(c);
            if (nextNode == null) {
                nextNode = new TrieNode();
                curNode.setNextNode(c, nextNode);
            }
            curNode = nextNode;
            if (i == word.length() - 1) {
                curNode.setIsWordEnding(true);
            }
        }
    }
}
