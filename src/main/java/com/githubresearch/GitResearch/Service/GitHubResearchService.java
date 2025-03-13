package com.githubresearch.GitResearch.Service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.githubresearch.GitResearch.Util.ExcelGenerator;
import com.githubresearch.GitResearch.Util.ExcelGenerator.CommitData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubResearchService {

    private static final Logger logger = LogManager.getLogger(GitHubResearchService.class);

    @Value("${github.api.token}")
    private String githubApiToken;

    @Autowired
    private GitHubApiService gitHubApiService;

    public String processRepositories(List<String> domains, List<String> keywords, String commitFilter, int commitThreshold, int minStars, int maxModifiedFiles) {
        List<String> repoUrls = gitHubApiService.searchRepositories(domains, minStars, commitFilter, commitThreshold);

        if (repoUrls.isEmpty()) {
            logger.warn("No repositories found matching the criteria: domains={}, minStars={}, commitFilter={}, commitThreshold={}",
                    String.join(", ", domains), minStars, commitFilter, commitThreshold);
            return "No repositories found matching the criteria.";
        }

        logger.info("Processing {} repositories", repoUrls.size());

        List<CommitData> commitDataList = new ArrayList<>();
        for (String repoUrl : repoUrls) {
            try {
                logger.info("Fetching commits from repository: {}", repoUrl);
                List<CommitData> commits = fetchCommitsFromRepo(repoUrl, keywords, maxModifiedFiles);
                commitDataList.addAll(commits);
                logger.info("Found {} relevant commits in repository: {}", commits.size(), repoUrl);
            } catch (GitAPIException | IOException e) {
                logger.error("Error processing repository: {}. Reason: {}", repoUrl, e.getMessage(), e);
            }
        }

        if (commitDataList.isEmpty()) {
            logger.warn("No relevant commits found in the {} repositories.", repoUrls.size());
            return "No relevant commits found.";
        }

        try {
            ExcelGenerator.generateExcel(commitDataList);
            logger.info("Excel file generated successfully with {} commits from {} repositories.", commitDataList.size(), repoUrls.size());
            return "Excel file generated successfully.";
        } catch (IOException e) {
            logger.error("Error generating Excel file: {}", e.getMessage(), e);
            return "Error generating Excel file.";
        }
    }

    private List<CommitData> fetchCommitsFromRepo(String repoUrl, List<String> keywords, int maxModifiedFiles) throws GitAPIException, IOException {
        List<CommitData> commitDataList = new ArrayList<>();

        String repoName = sanitizeFilePath(repoUrl.substring(repoUrl.lastIndexOf("/") + 1, repoUrl.lastIndexOf(".git")));
        File localPath = new File("tempRepos/" + repoName);

        if (localPath.exists()) {
            deleteDirectory(localPath);
        }

        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubApiToken, "");

        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setCredentialsProvider(credentialsProvider)
                .setBare(true)
                .call()) {

            logger.info("Cloned repository: {} into {}", repoUrl, localPath.getAbsolutePath());

            Iterable<RevCommit> commits = git.log().call();
            int matchedCommitCount = 0;

            for (RevCommit commit : commits) {
                List<String> matchedKeywords = new ArrayList<>();

                for (String keyword : keywords) {
                    if (commit.getFullMessage().toLowerCase().contains(keyword.toLowerCase())) {
                        matchedKeywords.add(keyword);
                    }
                }

                if (!matchedKeywords.isEmpty()) {
                    String commitId = commit.getName();
                    String commitUrl = repoUrl.replace(".git", "") + "/commit/" + commitId;
                    commitDataList.add(new CommitData(
                            commitUrl,
                            commitId,
                            commit.getAuthorIdent().getName(),
                            commit.getAuthorIdent().getWhen().toString(),
                            commit.getFullMessage()
                    ));
                    matchedCommitCount++;
                }
            }

            List<RevCommit> commitList = new ArrayList<>();
            commits.forEach(commitList::add);
            logger.info("Repository: {} - Total commits scanned: {}, Matched commits: {}", repoUrl, commitList.size(), matchedCommitCount);
            
        } catch (Exception e) {
            logger.error("Error while processing repository: {}. Reason: {}", repoUrl, e.getMessage(), e);
        }
        return commitDataList;
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    public static String sanitizeFilePath(String filePath) {
        return filePath.replaceAll("[:*?\"<>|]", "_");
    }
}
