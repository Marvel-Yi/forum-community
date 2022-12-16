package com.marvel.communityforum.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class CommunityUtilTest {
    @Test
    public void getOffsetTest() {
        Map<int[], Integer> map = new HashMap<>();
        map.put(new int[]{1, 10}, 0);
        map.put(new int[]{3, 5}, 10);
        for (Map.Entry<int[], Integer> en : map.entrySet()) {
            int current = en.getKey()[0];
            int limit = en.getKey()[1];
            int expected = en.getValue();
            int output = CommunityUtil.getOffset(current, limit);
            if (output == expected) {
                System.out.println("succeed");
            } else {
                System.out.println("fail! current: " + current + ", limit: " + limit + ", output: " + output + ", expected: " + expected);
            }
        }
    }

    @Test
    public void getPageCountTest() {
        Map<int[], Integer> map = new HashMap<>();
        map.put(new int[]{5, 13}, 3);
        map.put(new int[]{10, 66}, 7);
        for (Map.Entry<int[], Integer> en : map.entrySet()) {
            int limit = en.getKey()[0];
            int total = en.getKey()[1];
            int expected = en.getValue();
            int output = CommunityUtil.getPageCount(limit, total);
            if (output == expected) {
                System.out.println("succeed");
            } else {
                System.out.println("fail! limit: " + limit + ", total: " + total + ", output: " + output + ", expected: " + expected);
            }
        }
    }
}
