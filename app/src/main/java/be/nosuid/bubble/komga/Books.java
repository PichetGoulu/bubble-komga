package be.nosuid.bubble.komga;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Books {

    @SerializedName("content")
    @Expose
    private List<Content> content = null;
    @SerializedName("last")
    @Expose
    private Boolean last;

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    public Boolean isLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public static class Content {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("seriesId")
        @Expose
        private String seriesId;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("number")
        @Expose
        private Integer number;
        @SerializedName("media")
        @Expose
        private Media media;
        @SerializedName("readProgress")
        @Expose
        private ReadProgress readProgress;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSeriesId() {
            return seriesId;
        }

        public void setSeriesId(String seriesId) {
            this.seriesId = seriesId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public Media getMedia() {
            return media;
        }

        public void setMedia(Media media) {
            this.media = media;
        }

        public ReadProgress getReadProgress() {
            if (this.readProgress != null) {
                return readProgress;
            }

            // No status for this book, create an empty one
            ReadProgress readProgress = new ReadProgress();
            readProgress.setCompleted(false);
            readProgress.setPage(0);
            return readProgress;
        }

        public void setReadProgress(ReadProgress readProgress) {
            this.readProgress = readProgress;
        }

    }

}