package com.marvel.communityforum.controller;

import com.marvel.communityforum.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StatController {
    @Autowired
    private StatService statService;

    @PostMapping("/stat/uv")
    public Map<String, Object> getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date begin,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        Map<String, Object> res = new HashMap<>();
        long stat = statService.getRangeUV(begin, end);
        res.put("uvStat", stat);
        res.put("uvBeginDate", begin);
        res.put("uvEndDate", end);
        return res;
    }

    @PostMapping("/stat/dau")
    public Map<String, Object> getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date begin,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        Map<String, Object> res = new HashMap<>();
        long stat = statService.getRangeAU(begin, end);
        res.put("dauStat", stat);
        res.put("dauBeginDate", begin);
        res.put("dauEndDate", end);
        return res;
    }
}
