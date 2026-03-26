package com.monitor.controller;

import com.monitor.model.Insta;
import com.monitor.repo.InstaRepo;
import com.monitor.service.InstaService;
import com.monitor.model.Video;
import com.monitor.repo.VideoRepo;
import com.monitor.service.YouTubeService;
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
            final String searchKeyword = keyword.trim();
            videos = repo.findAll().stream()
                    .filter(v -> v.getKeyword() != null && 
                                 v.getKeyword().equalsIgnoreCase(searchKeyword))
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
            final String searchKeyword = keyword.trim().toLowerCase();
            posts = instaRepo.findAll().stream()
                    .filter(p -> p.getKeyword() != null && 
                                 p.getKeyword().toLowerCase().trim().contains(searchKeyword))
                    .toList();
        } else {
            posts = instaRepo.findAll();
        }

        model.addAttribute("posts", posts);
        return "fragments_insta :: rows";
    }
}
