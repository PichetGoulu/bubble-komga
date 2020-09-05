package be.nosuid.bubble.komga;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Series {

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
        @SerializedName("booksCount")
        @Expose
        private Integer booksCount;
        @SerializedName("booksReadCount")
        @Expose
        private Integer booksReadCount;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("libraryId")
        @Expose
        private String libraryId;
        @SerializedName("name")
        @Expose
        private String name;

        public Integer getBooksCount() {
            return booksCount;
        }

        public void setBooksCount(Integer booksCount) {
            this.booksCount = booksCount;
        }

        public Integer getBooksReadCount() {
            return booksReadCount;
        }

        public void setBooksReadCount(Integer booksReadCount) {
            this.booksReadCount = booksReadCount;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLibraryId() {
            return libraryId;
        }

        public void setLibraryId(String libraryId) {
            this.libraryId = libraryId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}

