package argssearch.entity;

import java.util.List;

public class Context {

    private String sourceId;
    private String acquisitionTime;
    private String discussionTitle;
    private String sourceTitle;
    private String sourceUrl;
    private String mode;
    private String sourceDomain;
    private String sourceText;
    private String sourceTextConclusionStart;
    private String sourceTextConclusionEnd;
    private String sourceTextPremiseStart;
    private String sourceTextPremiseEnd;
    private List<Aspect> aspects;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(String acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public String getDiscussionTitle() {
        return discussionTitle;
    }

    public void setDiscussionTitle(String discussionTitle) {
        this.discussionTitle = discussionTitle;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSourceDomain() {
        return sourceDomain;
    }

    public void setSourceDomain(String sourceDomain) {
        this.sourceDomain = sourceDomain;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getSourceTextConclusionStart() {
        return sourceTextConclusionStart;
    }

    public void setSourceTextConclusionStart(String sourceTextConclusionStart) {
        this.sourceTextConclusionStart = sourceTextConclusionStart;
    }

    public String getSourceTextConclusionEnd() {
        return sourceTextConclusionEnd;
    }

    public void setSourceTextConclusionEnd(String sourceTextConclusionEnd) {
        this.sourceTextConclusionEnd = sourceTextConclusionEnd;
    }

    public String getSourceTextPremiseStart() {
        return sourceTextPremiseStart;
    }

    public void setSourceTextPremiseStart(String sourceTextPremiseStart) {
        this.sourceTextPremiseStart = sourceTextPremiseStart;
    }

    public String getSourceTextPremiseEnd() {
        return sourceTextPremiseEnd;
    }

    public void setSourceTextPremiseEnd(String sourceTextPremiseEnd) {
        this.sourceTextPremiseEnd = sourceTextPremiseEnd;
    }

    public List<Aspect> getAspects() {
        return aspects;
    }

    public void setAspects(List<Aspect> aspects) {
        this.aspects = aspects;
    }
}
