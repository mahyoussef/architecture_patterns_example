package com.charity_hub.api;

import com.charity_hub.api.common.DeferredResults;
import com.charity_hub.application.Authenticate;
import com.charity_hub.application.AuthenticateService;
import com.charity_hub.domain.contracts.ILogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class AuthController {
    private final AuthenticateService authenticateHandler;
    private final ILogger logger;

    public AuthController(AuthenticateService authenticateService, ILogger logger) {
        this.authenticateHandler = authenticateService;
        this.logger = logger;
    }

    @PostMapping("/v1/accounts/authenticate")
    public DeferredResult<ResponseEntity<?>> login(@RequestBody Authenticate authenticate) {
        logger.info("Processing authentication request");
        return DeferredResults.from(
                authenticateHandler.handle(authenticate)
                        .thenApply(ResponseEntity::ok)
        );
    }
}

