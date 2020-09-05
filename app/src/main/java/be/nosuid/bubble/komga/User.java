package be.nosuid.bubble.komga;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    @Expose
    private String id = null;
    @SerializedName("email")
    @Expose
    private String email;

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
