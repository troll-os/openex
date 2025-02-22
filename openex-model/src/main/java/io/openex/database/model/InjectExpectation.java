package io.openex.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openex.database.audit.ModelBaseListener;
import io.openex.helper.MonoIdDeserializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;

import static java.time.Instant.now;

@Getter
@Entity
@Table(name = "injects_expectations")
@EntityListeners(ModelBaseListener.class)
public class InjectExpectation implements Base {

  public enum EXPECTATION_TYPE {
    TEXT,
    DOCUMENT,
    ARTICLE,
    CHALLENGE,
    MANUAL,
    TECHNICAL,
  }

  @Setter
  @Column(name = "inject_expectation_type")
  @JsonProperty("inject_expectation_type")
  @Enumerated(EnumType.STRING)
  private EXPECTATION_TYPE type;

  // region basic
  @Id
  @NotBlank
  @Setter
  @GeneratedValue(generator = "UUID")
  @UuidGenerator
  @Column(name = "inject_expectation_id")
  @JsonProperty("injectexpectation_id")
  private String id;

  @Setter
  @Column(name = "inject_expectation_name")
  @JsonProperty("inject_expectation_name")
  private String name;

  @Setter
  @Column(name = "inject_expectation_description")
  @JsonProperty("inject_expectation_description")
  private String description;

  @Setter
  @Column(name = "inject_expectation_result")
  @JsonProperty("inject_expectation_result")
  private String result;

  @Setter
  @Column(name = "inject_expectation_score")
  @JsonProperty("inject_expectation_score")
  private Integer score;

  @Setter
  @Column(name = "inject_expectation_expected_score")
  @JsonProperty("inject_expectation_expected_score")
  private Integer expectedScore;

  @Setter
  @Column(name = "inject_expectation_created_at")
  @JsonProperty("inject_expectation_created_at")
  private Instant createdAt = now();

  @Setter
  @Column(name = "inject_expectation_updated_at")
  @JsonProperty("inject_expectation_updated_at")
  private Instant updatedAt = now();

  @Setter
  @Column(name = "inject_expectation_group")
  @JsonProperty("inject_expectation_group")
  private boolean expectationGroup;
  // endregion

  // region contextual relations
  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_exercise")
  private Exercise exercise;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inject_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_inject")
  private Inject inject;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_user")
  private User user;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_team")
  private Team team;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_asset")
  private Asset asset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_group_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_asset_group")
  private AssetGroup assetGroup;
  // endregion

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_article")
  private Article article;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "challenge_id")
  @JsonSerialize(using = MonoIdDeserializer.class)
  @JsonProperty("inject_expectation_challenge")
  private Challenge challenge;

  public void setArticle(Article article) {
    this.type = EXPECTATION_TYPE.ARTICLE;
    this.article = article;
  }

  public void setChallenge(Challenge challenge) {
    this.type = EXPECTATION_TYPE.CHALLENGE;
    this.challenge = challenge;
  }

  public void setTechnical(
      @NotNull final Asset asset,
      @NotNull final AssetGroup assetGroup) {
    this.type = EXPECTATION_TYPE.TECHNICAL;
    this.asset = asset;
    this.assetGroup = assetGroup;
  }

  public boolean isUserHasAccess(User user) {
    return getExercise().isUserHasAccess(user);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !Base.class.isAssignableFrom(o.getClass())) {
      return false;
    }
    Base base = (Base) o;
    return id.equals(base.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
