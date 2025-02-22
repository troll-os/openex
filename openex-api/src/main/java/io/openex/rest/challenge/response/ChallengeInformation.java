package io.openex.rest.challenge.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.openex.database.model.Challenge;
import io.openex.database.model.InjectExpectation;

public class ChallengeInformation {

    @JsonProperty("challenge_detail")
    private final PublicChallenge challenge;

    @JsonProperty("challenge_expectation")
    private final InjectExpectation expectation;

    public ChallengeInformation(Challenge challenge, InjectExpectation expectation) {
        this.challenge = new PublicChallenge(challenge);
        this.expectation = expectation;
    }

    public PublicChallenge getChallenge() {
        return challenge;
    }

    public InjectExpectation getExpectation() {
        return expectation;
    }
}
