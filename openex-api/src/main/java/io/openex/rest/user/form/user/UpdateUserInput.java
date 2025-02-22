package io.openex.rest.user.form.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;

import java.util.ArrayList;
import java.util.List;

import static io.openex.config.AppConfig.EMAIL_FORMAT;
import static io.openex.config.AppConfig.PHONE_FORMAT;

@Getter
@Setter
public class UpdateUserInput {

  @Email(message = EMAIL_FORMAT)
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

  @JsonProperty("user_pgp_key")
  private String pgpKey;

  @JsonProperty("user_phone")
  @Pattern(regexp = "^\\+[\\d\\s\\-.()]+$", message = PHONE_FORMAT)
  private String phone;

  @JsonProperty("user_phone2")
  @Pattern(regexp = "^\\+[\\d\\s\\-.()]+$", message = PHONE_FORMAT)
  private String phone2;

  @JsonProperty("user_tags")
  private List<String> tagIds = new ArrayList<>();

}
