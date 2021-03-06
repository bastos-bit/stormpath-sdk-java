/*
 * Copyright 2016 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.spring.security.provider;

import com.stormpath.sdk.idsite.AuthenticationResult;
import com.stormpath.sdk.idsite.LogoutResult;
import com.stormpath.sdk.saml.SamlResultListener;
import com.stormpath.spring.security.token.SamlAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @since 1.0.RC8
 */
public class SpringSecuritySamlResultListener extends AbstractSpringSecurityProviderResultListener implements SamlResultListener {

    private static final Logger logger = LoggerFactory.getLogger(SpringSecuritySamlResultListener.class);

    public SpringSecuritySamlResultListener(AuthenticationProvider authenticationProvider) {
        super(authenticationProvider);
    }

    @Override
    public void onAuthenticated(AuthenticationResult result) {
        super.doAuthenticate(new SamlAuthenticationToken(result.getAccount()));
    }

    @Override
    public void onLogout(LogoutResult result) {
        SecurityContextHolder.clearContext();
    }

}
