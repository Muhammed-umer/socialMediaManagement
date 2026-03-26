package com.monitor.controller;

import com.monitor.repo.InstaRepo;
import com.monitor.model.Video;
import com.monitor.repo.VideoRepo;
import com.monitor.service.YouTubeService;
import com.monitor.service.InstagramHashtagService;
import com.monitor.model.dto.HashtagAnalysisResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DashboardController {

    private final VideoRepo repo;
    private final YouTubeService service;
    
    private final InstaRepo instaRepo;
    
    private final InstagramHashtagService hashtagService;

    public DashboardController(VideoRepo repo, YouTubeService service, InstaRepo instaRepo, InstagramHashtagService hashtagService) {
        this.repo = repo;
        this.service = service;
        this.instaRepo = instaRepo;
        this.hashtagService = hashtagService;
    }

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
            try {
                String tag = keyword.split(",")[0].trim();
                HashtagAnalysisResponse response = hashtagService.fetchByHashtag(tag);
                model.addAttribute("response", response);
                model.addAttribute("posts", response.getPosts());
                model.addAttribute("hashtag", tag);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        return "fragments_insta2 :: rows";
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
                HashtagAnalysisResponse response = hashtagService.fetchByHashtag(tag);
                model.addAttribute("response", response);
                model.addAttribute("posts", response.getPosts());
                model.addAttribute("hashtag", tag);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "fragments_insta2 :: rows";
    }
}
