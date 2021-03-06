package com.resource;

import static spark.Spark.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.bootstrap.Bootstrap;
import com.entity.User;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.service.AuthenticationService;
import static com.util.HTTPHelper.*;
import com.util.Utility;

public class AuthenticationResource {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);
	
	public AuthenticationService authService;
	
	public AuthenticationResource(AuthenticationService authenticationService){
		this.authService = authenticationService;
		setupEndpoints();
	}

	private void setupEndpoints() {
		
		post(Bootstrap.API_CONTEXT + "/register", "application/json", (request, response) -> {
			JsonObject  userObject = new JsonParser().parse(request.body()).getAsJsonObject();
			String email = userObject.get(EMAIL).getAsString();
			String username = userObject.get(USER_NAME).getAsString();
			String password = userObject.get(PASSWORD).getAsString();
			User newUser = new User(email, username, password);
			if (Utility.isNotEmptyValues(Lists.newArrayList(email, username, password)) && authService.registrer(newUser)) {
				LOGGER.info("New user was regiter "+newUser);
				JsonObject body = getAuthenticateResponseJson(newUser);
				response.status(OK_STATUS);
				return body.toString();
			} else {
				response.status(UNAUTHORIZED_STATUS);
			}
            return EMPTY_RESPONSE;
        });
		
		post(Bootstrap.API_CONTEXT + "/authenticate", "application/json",(request, response) -> {
			JsonObject  userObject = new JsonParser().parse(request.body()).getAsJsonObject();
			String email = userObject.get(EMAIL).getAsString();
			String password = userObject.get(PASSWORD).getAsString();
			User user = authService.getUser(email);
			if(user != null && password.equals(user.getPassword())){
				LOGGER.info(user.toString()+" was login. ");
				JsonObject body = getAuthenticateResponseJson(user);
				response.status(OK_STATUS);
				return body.toString();
			}else{
				response.status(UNAUTHORIZED_STATUS);
			}
			return EMPTY_RESPONSE;
		});
		
	}

	private JsonObject getAuthenticateResponseJson(User user) {
		String tocken = authService.generateJWT(user.getId());
		JsonObject body = new JsonObject();
		body.addProperty(USER_NAME, user.getUserName());
		body.addProperty(ACCESS_TOKEN, tocken);
		return body;
	} 
	
}
