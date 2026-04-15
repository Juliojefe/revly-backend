package com.example.revly.dto.response.report;

import java.util.List;

public class CommentReportSummary extends ReportSummary {

    private Integer commentId;
    private String content;
    private List<String> imageUrls;

    private Integer authorId;
    private String authorName;
    private String authorEmail;
    private String authorProfilePic;

    // Getters and setters
    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getAuthorProfilePic() { return authorProfilePic; }
    public void setAuthorProfilePic(String authorProfilePic) { this.authorProfilePic = authorProfilePic; }
}
