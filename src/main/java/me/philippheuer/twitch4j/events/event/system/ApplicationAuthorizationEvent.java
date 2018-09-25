package me.philippheuer.twitch4j.events.event.system;

import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.auth.model.OAuthCredential;
import me.philippheuer.twitch4j.events.Event;
import me.philippheuer.twitch4j.exceptions.RestException;
import me.philippheuer.twitch4j.streamlabs.StreamlabsClient;
import me.philippheuer.twitch4j.auth.model.twitch.Authorize;
import me.philippheuer.twitch4j.streamlabs.model.StreamlabsAuthorize;
import me.philippheuer.util.rest.LoggingRequestInterceptor;
import me.philippheuer.util.rest.RestClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = false)
@Slf4j
public class ApplicationAuthorizationEvent extends Event
{
	public static final RestClient twitchAuthClient = new RestClient();
	public static final RestClient streamlabsAuthClient = new RestClient();
	
	@Getter
	@Setter
	private String error = null;
	@Getter
	@Setter
	private String errorDesc = null;
	
	private String authorizationCode = null;
	
	@Getter
	@Setter
	private String state = null;
	
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
		RestTemplate restTemplate = streamlabsAuthClient.getPlainRestTemplate();
		
		if (restTemplate.getInterceptors() == null)
		{
			restTemplate.setInterceptors(new ArrayList<ClientHttpRequestInterceptor>());
		}

		// Post Data
		MultiValueMap<String, String> postBody = new LinkedMultiValueMap<String, String>();
		postBody.add("client_id", client.getClientId());
		postBody.add("client_secret", client.getClientSecret());
		postBody.add("redirect_uri", "http://127.0.0.1:7090/oauth_authorize_streamlabs");
		postBody.add("code", authorizationCode);
		postBody.add("state", state);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(requestUrl)
		        // Add query parameter
		        .queryParams(postBody);
		log.debug(builder.build().toUriString());
		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());

		// REST Request
		try 
		{
			ResponseEntity<StreamlabsAuthorize> responseObject = 
					restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, 
					null, StreamlabsAuthorize.class);
			StreamlabsAuthorize responseBody = responseObject.getBody();
			log.debug("Streamlabs: Attempting to retrieve streamlabs OAuth token.");

			OAuthCredential auth = new OAuthCredential();
			auth.setToken(responseBody.getAccessToken());
			auth.setRefreshToken(responseBody.getRefreshToken());
			auth.setType(responseBody.getTokenType());
			
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
		String requestUrl = String.format("https://id.twitch.tv/oauth2/token");
		RestTemplate restTemplate = twitchAuthClient.getPlainRestTemplate();
		
		// Post Data
		MultiValueMap<String, String> postBody = new LinkedMultiValueMap<String, String>();
		postBody.add("client_id", client.getClientId());
		postBody.add("client_secret", client.getClientSecret());
		postBody.add("code", authorizationCode);
		postBody.add("grant_type", "authorization_code");
		postBody.add("redirect_uri", client.getTwitchRedirectUri());
		//postBody.add("state", state);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(requestUrl)
		        // Add query parameter
		        .queryParams(postBody);
		System.out.println(builder.build().toUriString());
		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
		/*
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
		*/
		
		// REST Request
		try 
		{
			ResponseEntity<Authorize> responseObject = 
					restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, 
					null, Authorize.class);
			Authorize responseBody = responseObject.getBody();
			log.debug("Twitch: Attempting to retrieve twitch OAuth token.");

			OAuthCredential auth = new OAuthCredential();
			auth.setToken(responseBody.getAccessToken());
			auth.setRefreshToken(responseBody.getRefreshToken());
			auth.setType(responseBody.getTokenType());
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, responseBody.getExpiresIn().intValue());
			auth.setTokenExpiresAt(calendar);
			auth.getOAuthScopes().addAll(responseBody.getScope());
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
