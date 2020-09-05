package be.nosuid.bubble.komga;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Media {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("mediaType")
    @Expose
    private String mediaType;
    @SerializedName("pagesCount")
    @Expose
    private Integer pagesCount;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(Integer pagesCount) {
        this.pagesCount = pagesCount;
    }

}