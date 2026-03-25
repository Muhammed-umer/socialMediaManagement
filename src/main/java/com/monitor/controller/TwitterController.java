package com.monitor.controller;

import com.monitor.model.Tweet;
import com.monitor.service.TwitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TwitterController {

    private final TwitterService twitterService;

    @GetMapping("/twitter")
    public String twitterPage() {
        return "twitter";
    }

    @GetMapping("/api/twitter/search")
    @ResponseBody
    public List<Tweet> searchTwitter(@RequestParam("hashtag") String hashtag) {
        return twitterService.search(hashtag);
    }
}
