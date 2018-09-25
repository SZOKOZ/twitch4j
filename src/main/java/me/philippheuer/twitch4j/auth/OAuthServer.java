package me.philippheuer.twitch4j.auth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;

import lombok.extern.slf4j.Slf4j;
import me.philippheuer.twitch4j.events.EventDispatcher;
import me.philippheuer.twitch4j.events.event.system.ApplicationAuthorizationEvent;

/**
 * Internal server for retrieving OAuth tokens.
 * 
 * @author SZOKOZ
 *
 */
@Slf4j
public final class OAuthServer implements HttpHandler
{

	HttpServer httpserver;
	EventDispatcher dispatcher;
	public OAuthServer(int port, String context, EventDispatcher dispatcher)
	{
		try 
		{
			this.dispatcher = dispatcher;
			
			httpserver = HttpServer.create(new InetSocketAddress(port), 0);
			httpserver.createContext(String.format("/%s", context), this);
			httpserver.setExecutor(null);
			httpserver.start();
		} 
		catch (IOException e) 
		{
			log.error(e.getMessage());
			log.trace(e.getStackTrace().toString());
		}
	}

	@Override
	public void handle(HttpExchange request) throws IOException 
	{
		String authCode = null;
		ApplicationAuthorizationEvent appAuthEvent = null;
		String uriQuery = request.getRequestURI().getQuery();
		log.debug(uriQuery);
		if (!uriQuery.isEmpty())
		{
			if (!uriQuery.contains("error="))
			{
				authCode = uriQuery.split("&")[0].replace("code=", "");
				appAuthEvent = new ApplicationAuthorizationEvent(authCode);
			}
			else
			{
				appAuthEvent = new ApplicationAuthorizationEvent(authCode);
				appAuthEvent.setError(uriQuery.split("&")[0].replace("error=", ""));
				if (uriQuery.contains("error_description"))
				{
					appAuthEvent.setErrorDesc(uriQuery.split("&")[1]
							.replace("error_description=", ""));
				}
				
			}
			
		}
		
		dispatcher.dispatch(appAuthEvent);
		
		String response = "Initialisation continuing... You may now close this page.";
        request.sendResponseHeaders(200, response.getBytes("UTF-8").length);
        OutputStream outStream = request.getResponseBody();
        outStream.write(response.getBytes());
        outStream.close();
	}
}
