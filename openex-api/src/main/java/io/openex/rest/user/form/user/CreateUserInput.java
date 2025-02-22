package io.openex.rest.user.form.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

import static io.openex.config.AppConfig.EMAIL_FORMAT;

@Getter
@Setter
public class CreateUserInput {

  @Email(message = EMAIL_FORMAT)
  @NotBlank
  @JsonProperty("user_email")
  private String email;

  @JsonProperty("user_admin")
  private boolean admin;

  @JsonProperty("user_firstname")
  private String firstname;

  @JsonProperty("user_lastname")
  private String lastname;

  @JsonProperty("user_organization")
  private String organizationId;

  @JsonProperty("user_plain_password")
  private String password;

  @JsonProperty("user_tags")
  private List<String> tagIds = new ArrayList<>();

}
