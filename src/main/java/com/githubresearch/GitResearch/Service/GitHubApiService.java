package com.githubresearch.GitResearch.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.util.logging.Logger;

@Service
public class GitHubApiService {

    @Value("${github.api.url}")
    private String githubApiUrl;

    @Value("${github.api.token}")
    private String githubApiToken;

    private static final Logger LOGGER = Logger.getLogger(GitHubApiService.class.getName());

    public List<String> searchRepositories(List<String> domains, int minStars, String commitFilter, int commitThreshold) {
        RestTemplate restTemplate = new RestTemplate();
        List<String> repoUrls = new ArrayList<>();

        String domainQuery = String.join(" OR ", domains);
        int page = 1;
        boolean hasMorePages = true;

        while (hasMorePages && repoUrls.size() < 5) { // Fetch up to 50 repositories
            String url = githubApiUrl + "search/repositories?q=" + domainQuery + "+stars:>" + minStars + "&page=" + page + "&per_page=50";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubApiToken);
            headers.set("Accept", "application/vnd.github.v3+json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                LOGGER.severe("GitHub API Request Failed. Status: " + response.getStatusCode());
                break;
            }

            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray items = jsonResponse.optJSONArray("items");
            if (items == null || items.isEmpty()) {
                hasMorePages = false;
            } else {
                for (int i = 0; i < items.length() && repoUrls.size() < 5; i++) {
                    JSONObject repo = items.getJSONObject(i);
                    String repoUrl = repo.optString("clone_url", null);
                    String repoFullName = repo.optString("full_name", null);

                    if (repoUrl != null && repoFullName != null) {
                        int commitCount = getRepositoryCommitCount(repoFullName);
                        LOGGER.info("Repo: " + repoFullName + " | Commits: " + commitCount);

                        if (commitFilter != null && commitThreshold > 0) {
                            if ("greater".equalsIgnoreCase(commitFilter) && commitCount <= commitThreshold) {
                                LOGGER.warning("Skipping repo " + repoFullName + " (Commits: " + commitCount + " ≤ Threshold: " + commitThreshold + ")");
                                continue;
                            } else if ("less".equalsIgnoreCase(commitFilter) && commitCount >= commitThreshold) {
                                LOGGER.warning("Skipping repo " + repoFullName + " (Commits: " + commitCount + " ≥ Threshold: " + commitThreshold + ")");
                                continue;
                            }
                        }

                        LOGGER.info("Adding repo: " + repoFullName + " (Commits: " + commitCount + ")");
                        repoUrls.add(repoUrl);
                    }
                }
                page++;
            }
        }
        return repoUrls;
    }

    private int getRepositoryCommitCount(String repoFullName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.github.com/repos/" + repoFullName + "/commits?per_page=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubApiToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                LOGGER.warning("Failed to get commit count for " + repoFullName);
                return 0;
            }

            String linkHeader = response.getHeaders().getFirst("Link");
            if (linkHeader != null && linkHeader.contains("rel=\"last\"")) {
                int lastPageIndex = linkHeader.lastIndexOf("page=");
                int endIndex = linkHeader.indexOf("&", lastPageIndex);
                if (endIndex == -1) {
                    endIndex = linkHeader.indexOf(">", lastPageIndex);
                }
                if (lastPageIndex != -1 && endIndex != -1) {
                    String lastPageStr = linkHeader.substring(lastPageIndex + 5, endIndex);
                    try {
                        return Integer.parseInt(lastPageStr);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Failed to parse commit count from link header for " + repoFullName);
                    }
                }
            } else {
                JSONArray commits = new JSONArray(response.getBody());
                return commits.length();
            }
        } catch (Exception e) {
            LOGGER.warning("Error getting commit count for " + repoFullName + ": " + e.getMessage());
        }
        return 0;
    }
}
