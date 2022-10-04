package revolver.desal.api.services.users;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import revolver.desal.api.adapter.UserTypeAdapter;
import revolver.desal.api.model.response.BaseResponse;

public class Session extends BaseResponse {

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("username")
    private String username;

    @SerializedName("verified")
    private boolean isVerified;

    @SerializedName("token")
    private String token;

    @JsonAdapter(UserTypeAdapter.class)
    @SerializedName("type")
    private UserType userType;

    @SerializedName("validation")
    private PendingValidation validation;

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getToken() {
        return token;
    }

    public UserType getUserType() {
        return userType;
    }

    public PendingValidation getValidation() {
        return validation;
    }

}
