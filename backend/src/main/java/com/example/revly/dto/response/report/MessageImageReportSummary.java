package com.example.revly.dto.response.report;

import java.util.List;

public class MessageImageReportSummary extends ReportSummary {

    private Integer messageImageId;
    private List<String> imageUrls;

    private Integer authorId;
    private String authorName;
    private String authorEmail;
    private String authorProfilePic;

    // Getters and setters
    public Integer getMessageImageId() { return messageImageId; }
    public void setMessageImageId(Integer messageImageId) { this.messageImageId = messageImageId; }

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
