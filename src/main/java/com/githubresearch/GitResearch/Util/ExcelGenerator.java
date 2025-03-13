package com.githubresearch.GitResearch.Util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelGenerator {

    public static class CommitData {
        private String repoUrl;
        private String commitId;
        private String authorName;
        private String date;
        private String message;

        public CommitData(String repoUrl, String commitId, String authorName, String date, String message) {
            this.repoUrl = repoUrl;
            this.commitId = commitId;
            this.authorName = authorName;
            this.date = date;
            this.message = message;
        }

        public String getRepoUrl() { return repoUrl; }
        public String getCommitId() { return commitId; }
        public String getAuthorName() { return authorName; }
        public String getDate() { return date; }
        public String getMessage() { return message; }
    }

    public static void generateExcel(List<CommitData> commitDataList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("output.xlsx")) {

            Sheet sheet = workbook.createSheet("Commits");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Repository URL", "Commit ID", "Author Name", "Date", "Commit Message"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowNum = 1;
            for (CommitData commitData : commitDataList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(commitData.getRepoUrl());
                row.createCell(1).setCellValue(commitData.getCommitId());
                row.createCell(2).setCellValue(commitData.getAuthorName());
                row.createCell(3).setCellValue(commitData.getDate());
                row.createCell(4).setCellValue(commitData.getMessage());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fileOut);
        }
    }
}
