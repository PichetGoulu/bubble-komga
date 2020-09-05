package be.nosuid.bubble.komga;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReadProgress {

    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("completed")
    @Expose
    private Boolean completed;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

}