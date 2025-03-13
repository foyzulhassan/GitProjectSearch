package com.githubresearch.GitResearch.Contoller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.githubresearch.GitResearch.Service.GitHubResearchService;
import com.githubresearch.GitResearch.DTO.GitHubSearchRequest;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/github")
public class GitHubResearchController {

    @Autowired
    private GitHubResearchService gitHubResearchService;

    @PostMapping("/search")
    public ResponseEntity<?> searchRepositories(@RequestBody GitHubSearchRequest request) {
        List<String> domains = request.getDomains(); 
        List<String> keywords = request.getKeywords();
        String commitFilter = request.getCommitFilter(); 
        int commitThreshold = request.getCommitThreshold(); 
        int minStars = request.getMinStars(); 
        int maxModifiedFiles = request.getMaxModifiedFiles(); 

        String result = gitHubResearchService.processRepositories(domains, keywords, commitFilter, commitThreshold, minStars, maxModifiedFiles);
   
        if (result.contains("Error")) {
            return ResponseEntity.status(500).body(result); 
        }
        return ResponseEntity.ok(result);
    }
}

