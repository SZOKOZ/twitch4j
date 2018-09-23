package me.philippheuer.twitch4j.streamlabs.endpoints;

//import com.jcabi.log.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.philippheuer.twitch4j.auth.model.OAuthCredential;
import me.philippheuer.twitch4j.exceptions.RestException;
import me.philippheuer.util.rest.LoggingRequestInterceptor;
import me.philippheuer.twitch4j.streamlabs.StreamlabsClient;
import me.philippheuer.twitch4j.streamlabs.enums.AlertType;
import me.philippheuer.twitch4j.streamlabs.model.AlertCreate;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

@Getter
@Setter
@Slf4j
public class AlertEndpoint extends AbstractStreamlabsEndpoint {

	/**
	 * Holds the credentials to the current user
	 */
	private OAuthCredential oAuthCredential;

	/**
	 * Stream Labs - Authenticated Endpoint
	 *
	 * @param streamlabsClient todo
	 * @param credential todo
	 */
	public AlertEndpoint(StreamlabsClient streamlabsClient, OAuthCredential credential) {
		super(streamlabsClient);
		setOAuthCredential(credential);
	}

	/**
	 * Endpoint: Create Alert
	 * Trigger a custom alert for the authenticated user.
	 * Requires Scope: alerts.create
	 *
	 * @param type               This parameter determines which alert box this alert will show up in, and thus should be one of the following: follow, subscription, donation, or host
	 * @param message            The message to show with this alert. If not supplied, no message will be shown. Surround special tokens with *s, for example: This is my *special* alert!
	 * @param duration           How many seconds this alert should be displayed.
	 * @param special_text_color The color to use for special tokens. Must be a valid CSS color string.
	 * @param imageUrl           The href pointing to an image resource to play when this alert shows. If an empty string is supplied, no image will be displayed.
	 * @param soundUrl           The href pointing to a sound resource to play when this alert shows. If an empty string is supplied, no sound will be played.
	 * @return Success?
	 */
	public Boolean createAlert(AlertType type, Optional<String> message, Optional<Integer> duration, Optional<String> special_text_color, Optional<String> imageUrl, Optional<String> soundUrl) {
		// Endpoint
		String requestUrl = String.format("%s/alerts", getStreamlabsClient().getEndpointUrl());
		RestTemplate restTemplate = getStreamlabsClient().getRestClient().getRestTemplate();

		// Post Data
		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<String, Object>();
		postBody.add("access_token", getOAuthCredential().getToken());
		postBody.add("type", type.toString());
		postBody.add("message", message.orElse(""));
		postBody.add("duration", duration.orElse(10).toString());
		postBody.add("special_text_color", special_text_color.orElse(""));
		postBody.add("image_href", imageUrl.orElse(""));
		postBody.add("sound_href", soundUrl.orElse(""));

		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());

		// REST Request
		try {
			AlertCreate responseObject = restTemplate.postForObject(requestUrl, postBody, AlertCreate.class);

			//Logger.debug(this, "Sreamlabs: Created new Alert for %s", getOAuthCredential().getDisplayName());
			log.debug("Streamlabs: Created new Alert for %s", getOAuthCredential().getDisplayName());

			return responseObject.getSuccess();
		} catch (RestException restException) {
			//Logger.error(this, "RestException: " + restException.getRestError().toString());
			log.error("RestException: " + restException.getRestError().toString());
			log.trace(ExceptionUtils.getStackTrace(restException));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public Boolean createAlert(AlertType type, String message, int duration)
	{
		return createAlert(type, Optional.ofNullable(message), Optional.of(duration), 
				Optional.ofNullable(null), Optional.ofNullable(null), Optional.ofNullable(null));
	}
	
	public Boolean createAlert(AlertType type, String message, int duration, String color)
	{
		return createAlert(type, Optional.ofNullable(message), Optional.of(duration), 
				Optional.ofNullable(color), Optional.ofNullable(null), Optional.ofNullable(null));
	}
	
	
	/**
	 * Endpoint: Send Test Alert
	 * Trigger a test alert for the authenticated user.
	 * Requires Scope: alerts.write
	 * 
	 * @param type    AlertType Enum. This parameter determines which alert box this alert will show up in.
	 * @return        A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean createTestAlert(AlertType type)
	{
		// Endpoint
		String requestUrl = String.format("%s/alerts/show_video", getStreamlabsClient().getEndpointUrl());
		RestTemplate restTemplate = getStreamlabsClient().getRestClient().getRestTemplate();

		// Post Data
		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<String, Object>();
		postBody.add("access_token", getOAuthCredential().getToken());
		postBody.add("type", type.toString());
		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());

		// REST Request
		try 
		{
			AlertCreate responseObject = restTemplate.postForObject(requestUrl, postBody, AlertCreate.class);
			log.debug("Streamlabs: Created Test Alert for %s", getOAuthCredential().getDisplayName());

			return responseObject.getSuccess();
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
	
	
	/**
	 * A true copy pasta master knows how to copy paste efficiently.
	 */
	private Boolean AlertFactory(String endpointPath)
	{
		//Copy Paste Master at work.
		// Endpoint
		String requestUrl = String.format("%s/alerts/%s", endpointPath, getStreamlabsClient().getEndpointUrl());
		RestTemplate restTemplate = getStreamlabsClient().getRestClient().getRestTemplate();

		// Post Data
		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<String, Object>();
		postBody.add("access_token", getOAuthCredential().getToken());
		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());

		// REST Request
		try {
			AlertCreate responseObject = restTemplate.postForObject(requestUrl, postBody, AlertCreate.class);
			log.debug("Streamlabs: Alert Action %s for %s", endpointPath, getOAuthCredential().getDisplayName());

			return responseObject.getSuccess();
		} catch (RestException restException) {
			//Logger.error(this, "RestException: " + restException.getRestError().toString());
			log.error("RestException: " + restException.getRestError().toString());
			log.trace(ExceptionUtils.getStackTrace(restException));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * Endpoint: Skip Alert
	 * Skips current displayed alert.
	 * Requires Scope: alerts.write
	 * 
	 * @return    A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean skipAlert()
	{
		return AlertFactory("skip");
	}
	
	
	/**
	 * Endpoint: Mute Alert Volume
	 * Mute volume of current alert.
	 * Requires Scope: alerts.write
	 * 
	 * @return    A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean muteVolume()
	{
		return AlertFactory("mute_volume");
	}
	
	
	/**
	 * Endpoint: Unmute Alert Volume
	 * Unmute volume of alert.
	 * Requires Scope: alerts.write
	 * 
	 * @return    A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean unmuteVolume()
	{
		return AlertFactory("unmute_volume");
	}
	
	
	/**
	 * Endpoint: Pause Alerts
	 * Pause alerts from displaying on stream.
	 * Requires Scope: alerts.write
	 * 
	 * @return    A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean pauseQueue()
	{
		return AlertFactory("pause_queue");
	}
	
	
	/**
	 * Endpoint: Unpause Alert
	 * Resumes alerts displaying on stream.
	 * Requires Scope: alerts.write
	 * 
	 * @return    A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean unpauseQueue()
	{
		return AlertFactory("unpause_queue");
	}
	
	
	/**
	 * Endpoint: Show Video
	 * Not sure what this does. I think it redisplays media items such as gifs or webms on alerts.
	 * Requires Scope: alerts.write
	 * 
	 * @return    A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean showVideo()
	{
		return AlertFactory("show_video");
	}
	
	
	/**
	 * Endpoint: Hide Video
	 * Probably removes media such as gifs or webms from alerts.
	 * Requires Scope: alerts.write
	 * 
	 * @return    A boolean depending on the success of the request. Null if an error occurred.
	 */
	public Boolean hideVideo()
	{
		return AlertFactory("hide_video");
	}

}
