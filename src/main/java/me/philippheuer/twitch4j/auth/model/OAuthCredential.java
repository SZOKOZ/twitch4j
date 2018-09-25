package me.philippheuer.twitch4j.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.philippheuer.twitch4j.enums.Scope;
import me.philippheuer.util.conversion.ScopeDeserializer;

@Data
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthCredential {

	private String type;

	private String token;

	private Calendar tokenExpiresAt;

	private String refreshToken;

	private final Set<String> oAuthScopes = new LinkedHashSet<>();

	private Long userId;

	private String userName;

	private String displayName;

	/**
	 * Class Constructor
	 *
	 * @param token The OAuth Token for a user.
	 */
	public OAuthCredential(String token) {
		setToken(token);
	}

}
