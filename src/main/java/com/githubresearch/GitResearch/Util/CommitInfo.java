package com.githubresearch.GitResearch.Util;

import java.util.Date;

public class CommitInfo {
    private String repoUrl;
    private String commitMessage;
    private String author;
    private Date date;

    // Constructors, getters, setters
    public CommitInfo(String repoUrl, String commitMessage, String author, Date date) {
        this.repoUrl = repoUrl;
        this.commitMessage = commitMessage;
        this.author = author;
        this.date = date;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
