package com.githubresearch.GitResearch.DTO;

import java.util.List;

public class GitHubSearchRequest {
    private List<String> domains; // Supports multiple domains
    private List<String> keywords;
    private String commitFilter; // "greater" or "less"
    private int commitThreshold;
    private int minStars; // Minimum number of stars for repo filtering
    private int maxModifiedFiles; // Maximum number of modified files filter

    // Getters and Setters
    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getCommitFilter() {
        return commitFilter;
    }

    public void setCommitFilter(String commitFilter) {
        this.commitFilter = commitFilter;
    }

    public int getCommitThreshold() {
        return commitThreshold;
    }

    public void setCommitThreshold(int commitThreshold) {
        this.commitThreshold = commitThreshold;
    }

    public int getMinStars() {
        return minStars;
    }

    public void setMinStars(int minStars) {
        this.minStars = minStars;
    }

    public int getMaxModifiedFiles() {
        return maxModifiedFiles;
    }

    public void setMaxModifiedFiles(int maxModifiedFiles) {
        this.maxModifiedFiles = maxModifiedFiles;
    }
}
