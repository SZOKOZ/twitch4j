package me.philippheuer.twitch4j.events.event.system;

import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.auth.model.OAuthCredential;
import me.philippheuer.twitch4j.events.Event;
import me.philippheuer.twitch4j.exceptions.RestException;
import me.philippheuer.twitch4j.streamlabs.StreamlabsClient;
import me.philippheuer.twitch4j.auth.model.twitch.Authorize;
import me.philippheuer.twitch4j.streamlabs.model.StreamlabsAuthorize;
import me.philippheuer.util.rest.HeaderRequestInterceptor;
import me.philippheuer.util.rest.LoggingRequestInterceptor;

import java.util.Calendar;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = false)
@Slf4j
public class ApplicationAuthorizationEvent extends Event
{
	@Getter
	@Setter
	private String error = null;
	@Getter
	@Setter
	private String errorDesc = null;
	
	private String authorizationCode = null;
	
	public ApplicationAuthorizationEvent(String authorizationCode)
	{
		this.authorizationCode = authorizationCode;
	}
	
	
	/**
	 * Returns error string.
	 * @return error string
	 */
	/* When ur just 2 lazy to write get/setter docs so use lombok and say yolo.
	 * public Optional<String> getError()
	{
		return Optional.ofNullable(error);
	}
	public String getErrorIfPresent()
	{
		return error;
	}
	public void setError(String error)
	{
		this.error = error;
	}*/
	
	
	/**
	 * Returns an authorization code to be used in a token request.
	 * The authorization code expires soon after being created so create a token request with it immediately.
	 * 
	 * @return    An optional that may contain the code if the user accepted access for your application.
	 */
	public Optional<String> getCode()
	{
		return Optional.ofNullable(authorizationCode);
	}
	
	
	/**
	 * Returns an authorization code to be used in a token request.
	 * The authorization code expires soon after being created so create a token request with it immediately.
	 * 
	 * @return    The code or null if the user declined access for your application.
	 */
	public String getCodeIfPresent()
	{
		return authorizationCode;
	}
	
	public OAuthCredential getOAuth(StreamlabsClient client)
	{
		// Endpoint
		String requestUrl = String.format("%s/token", client.getEndpointUrl());
		RestTemplate restTemplate = client.getRestClient().getRestTemplate();

		// Post Data
		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<String, Object>();
		postBody.add("grant_type", "authorization_code");
		postBody.add("client_id", client.getClientId());
		postBody.add("client_secret", client.getClientSecret());
		postBody.add("redirect_uri", "http://127.0.0.1:7090/oauth_authorize_streamlabs");
		postBody.add("code", authorizationCode);
		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());

		// REST Request
		try 
		{
			StreamlabsAuthorize responseObject = restTemplate.postForObject(requestUrl, postBody, StreamlabsAuthorize.class);
			log.debug("Streamlabs: Attempting to retrieve streamlabs OAuth token.");

			OAuthCredential auth = new OAuthCredential();
			auth.setToken(responseObject.getAccessToken());
			auth.setRefreshToken(responseObject.getRefreshToken());
			auth.setType(responseObject.getTokenType());
			
			return auth;
		} 
		catch (RestException restException) 
		{
			log.error("RestException: " + restException.getRestError().toString());
			log.trace(ExceptionUtils.getStackTrace(restException));
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	
	
	public OAuthCredential getOAuth(TwitchClient client)
	{
		// Endpoint
		String requestUrl = String.format("https://id.twitch.tv/oauth2/authorize");
		RestTemplate restTemplate = client.getRestClient().getRestTemplate();

		// Post Data
		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<String, Object>();
		postBody.add("client_id", client.getClientId());
		postBody.add("client_secret", client.getClientSecret());
		postBody.add("code", authorizationCode);
		postBody.add("grant_type", "authorization_code");
		postBody.add("redirect_uri", client.getTwitchRedirectUri());
		
		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
		for (ClientHttpRequestInterceptor requestinterceptor: restTemplate.getInterceptors())
		{
			if (!(requestinterceptor instanceof HeaderRequestInterceptor))
			{
				continue;
			}
			
			HeaderRequestInterceptor header = (HeaderRequestInterceptor) requestinterceptor;
			if (header.getName().equals("Client-ID"))
			{
				restTemplate.getInterceptors().remove(header);
				log.debug("removed clientid header");
				break;
			}
		}

		// REST Request
		try 
		{
			Authorize responseObject = restTemplate.postForObject(requestUrl, postBody, Authorize.class);
			log.debug("Streamlabs: Attempting to retrieve streamlabs OAuth token.");

			OAuthCredential auth = new OAuthCredential();
			auth.setToken(responseObject.getAccessToken());
			auth.setRefreshToken(responseObject.getRefreshToken());
			auth.setType(responseObject.getTokenType());
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, responseObject.getExpiresIn().intValue());
			auth.setTokenExpiresAt(calendar);
			auth.getOAuthScopes().addAll(responseObject.getScope());
			return auth;
		} 
		catch (RestException restException) 
		{
			log.error("RestException: " + restException.getRestError().toString());
			log.trace(ExceptionUtils.getStackTrace(restException));
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		
		return null;
	}

}
