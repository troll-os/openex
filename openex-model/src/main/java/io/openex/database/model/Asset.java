package io.openex.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import io.openex.database.audit.ModelBaseListener;
import io.openex.helper.MultiIdDeserializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.persistence.DiscriminatorType.STRING;
import static java.time.Instant.now;
import static lombok.AccessLevel.NONE;

@Data
@Entity
@Table(name = "assets")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "asset_type", discriminatorType = STRING)
@EntityListeners(ModelBaseListener.class)
public class Asset implements Base {

  @Id
  @Column(name = "asset_id")
  @GeneratedValue(generator = "UUID")
  @UuidGenerator
  @JsonProperty("asset_id")
  @NotBlank
  private String id;

  @Column(name = "asset_type", insertable = false, updatable = false)
  @JsonProperty("asset_type")
  @Setter(NONE)
  private String type;

  @Column(name = "asset_sources")
  @JsonProperty("asset_sources")
  @Type(PostgreSQLHStoreType.class)
  private Map<String, String> sources = new HashMap<>();

  @Column(name = "asset_blobs")
  @JsonProperty("asset_blobs")
  @Type(PostgreSQLHStoreType.class)
  private Map<String, String> blobs = new HashMap<>();

  @NotBlank
  @Column(name = "asset_name")
  @JsonProperty("asset_name")
  private String name;

  @Column(name = "asset_description")
  @JsonProperty("asset_description")
  private String description;

  @Column(name = "asset_last_seen")
  @JsonProperty("asset_last_seen")
  private Instant lastSeen;

  // -- TAG --

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "assets_tags",
      joinColumns = @JoinColumn(name = "asset_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @JsonSerialize(using = MultiIdDeserializer.class)
  @JsonProperty("asset_tags")
  private List<Tag> tags = new ArrayList<>();

  // -- AUDIT --

  @Column(name = "asset_created_at")
  @JsonProperty("asset_created_at")
  private Instant createdAt = now();

  @Column(name = "asset_updated_at")
  @JsonProperty("asset_updated_at")
  private Instant updatedAt = now();
}
