package io.openex.rest.exercise.form;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

import static io.openex.config.AppConfig.MANDATORY_MESSAGE;

public class DryrunCreateInput {
    @NotBlank(message = MANDATORY_MESSAGE)
    @JsonProperty("dryrun_name")
    private String name;

    @JsonProperty("dryrun_users")
    private List<String> userIds = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
