package gov.nih.brics.auth.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Body1
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-16T10:51:52.786-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Body1   {
  @JsonProperty("username")
  private String username = null;

  @JsonProperty("password")
  private String password = null;

  /**
   * username for the user who needs access
   * @return username
  **/
  @ApiModelProperty(required = true, value = "username for the user who needs access")
  @NotNull

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * password for the user hashed using HashMethods.getServerHash(username, HashMethods.convertFromByte(account.getPassword())).  It is deprecated from inception because it should not be used when another option is available.
   * @return password
  **/
  @ApiModelProperty(required = true, value = "password for the user hashed using HashMethods.getServerHash(username, HashMethods.convertFromByte(account.getPassword())).  It is deprecated from inception because it should not be used when another option is available.")
  @NotNull

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
