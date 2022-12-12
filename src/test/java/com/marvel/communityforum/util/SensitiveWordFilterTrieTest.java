package com.marvel.communityforum.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class SensitiveWordFilterTrieTest {
    @Autowired
    private SensitiveWordFilterTrie trie;

    @Test
    public void filterTest() {
        Map<String, String> map = new HashMap<>();
        map.put("毒品123fuck123shit123hell123bitchh", "***123***123***123***123***h");
        map.put("!@#海`洛`因`", "!@#***`");
        map.put("这是^毒^品^", "这是^***^");

        for (Map.Entry<String, String> en : map.entrySet()) {
            String input = en.getKey();
            String expected = en.getValue();
            String output = trie.filter(input);
            if (expected.equals(output)) {
                System.out.println("succeed");
            } else {
                System.out.println("fail! input: " + input + ", output: " + output + ", expected: " + expected);
            }
        }
    }
}
