package com.monitor.controller;

import com.monitor.model.Insta;
import com.monitor.repo.InstaRepo;
import com.monitor.service.InstaService;
import com.monitor.model.Video;
import com.monitor.repo.VideoRepo;
import com.monitor.service.YouTubeService;
import com.monitor.service.InstagramHashtagService;
import com.monitor.model.dto.HashtagAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final VideoRepo repo;
    private final YouTubeService service;
    
    private final InstaRepo instaRepo;
    private final InstaService instaService;
    
    private final InstagramHashtagService hashtagService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        model.addAttribute("videos", repo.findAll());
        return "dashboard";
    }

    @GetMapping("/dashboard/data")
    public String data(Model model,
                       @RequestParam("keyword") String keyword){

        if(keyword != null && keyword.length() > 2){
            service.fetch(keyword);
        }

        List<Video> videos;
        if(keyword != null && !keyword.isEmpty()){
            String[] keywords = keyword.split(",");
            List<String> keywordList = java.util.Arrays.stream(keywords)
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .toList();
            
            // If multiple keywords, find all that match any of them
            videos = repo.findAll().stream()
                    .filter(v -> v.getKeyword() != null && 
                                 keywordList.stream().anyMatch(k -> v.getKeyword().equalsIgnoreCase(k)))
                    .toList();
        } else {
            videos = repo.findAll();
        }

        model.addAttribute("videos", videos);
        return "fragments :: rows";
    }

    @GetMapping("/insta")
    public String instaDashboard(Model model){
        model.addAttribute("posts", instaRepo.findAll());
        return "insta";
    }

    @GetMapping("/insta/data")
    public String instaData(Model model,
                            @RequestParam("keyword") String keyword){

        if(keyword != null && !keyword.isEmpty()){
            System.out.println("Processing Instagram search for keywords: " + keyword);
            instaService.fetch(keyword);
        }

        List<Insta> posts;
        if(keyword != null && !keyword.isEmpty()){
            String[] keywords = keyword.split(",");
            List<String> keywordList = java.util.Arrays.stream(keywords)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(k -> !k.isEmpty())
                    .toList();
            
            posts = instaRepo.findAll().stream()
                    .filter(p -> p.getKeyword() != null && 
                                 keywordList.stream().anyMatch(k -> p.getKeyword().toLowerCase().trim().contains(k)))
                    .toList();
        } else {
            posts = instaRepo.findAll();
        }

        model.addAttribute("posts", posts);
        return "fragments_insta :: rows";
    }

    @GetMapping("/insta2")
    public String insta2Dashboard(Model model){
        return "insta2";
    }

    @GetMapping("/insta2/data")
    public String insta2Data(Model model, @RequestParam("keyword") String keyword){
        if(keyword != null && !keyword.isEmpty()){
            try {
                String tag = keyword.split(",")[0].trim();
                HashtagAnalysisResponse response = hashtagService.analyzeHashtag(tag);
                model.addAttribute("response", response);
                model.addAttribute("posts", response.getPosts());
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "fragments_insta2 :: rows";
    }
}
