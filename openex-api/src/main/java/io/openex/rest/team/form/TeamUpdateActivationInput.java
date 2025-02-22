package io.openex.rest.team.form;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamUpdateActivationInput {

    @JsonProperty("team_enabled")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
