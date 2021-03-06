/**
 * Copyright © 2016 Open Text.  All Rights Reserved.
 */
package com.appworks.service.example.services;

import com.opentext.otag.sdk.client.v3.TrustedProviderClient;
import com.opentext.otag.sdk.types.v3.TrustedProvider;
import com.opentext.otag.sdk.types.v3.TrustedProviders;
import com.opentext.otag.sdk.types.v3.api.error.APIException;
import com.opentext.otag.service.context.components.AWComponent;
import com.appworks.service.example.util.ServiceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.appworks.service.example.util.ServiceLogger.LOG_MARKER;

/**
 * Some AppWorks services interact with a EIM backend that is considered trusted.
 * The trusted providers as they are known can use a limited subset of the AppWorks
 * Gateway API's using their key.
 * <p>
 * The SDK offers a client that allows the creation and retrieval of such trusted providers.
 */
public class TrustedProviderService implements AWComponent {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedProviderService.class);

    public static final String TRUSTED_PROVIDER_NAME = "ImaginaryProvider";

    private TrustedProviderClient trustedProviderClient;

    public TrustedProviderService(TrustedProviderClient trustedProviderClient) {
        this.trustedProviderClient = trustedProviderClient;
        validateMyServiceTrustedProvider();
    }

    public Optional<TrustedProvider> getMyServiceTrustedProvider() {
        try {
            return Optional.of(trustedProviderClient.getOrCreate(TRUSTED_PROVIDER_NAME));
        } catch (APIException e) {
            ServiceLogger.error(LOG, "Failed to get our Trusted Provider");
            return Optional.empty();
        }
    }

    /**
     * Get or create a {@link TrustedProvider} via the AppWorks SDK client.
     */
    private void validateMyServiceTrustedProvider() {
        boolean providerExists = doesProviderExist();

        try {
            if (!providerExists) {
                TrustedProvider created = trustedProviderClient.getOrCreate(TRUSTED_PROVIDER_NAME);
                if (created != null) {
                    ServiceLogger.info(LOG, "The MyService related trusted provider was created");
                } else {
                    throw new RuntimeException(LOG_MARKER +
                            "Failed to create the MyService related trusted provider");
                }
            } else {
                ServiceLogger.info(LOG, "The MyService related trusted " +
                        "provider already exists, no further action required");
            }
        } catch (APIException e) {
            String errMsg = String.format(
                    "Trusted provider calls via SDK client failed - %s", e.getCallInfo());
            ServiceLogger.error(LOG, errMsg, e);
        }

    }

    /**
     * Use the SDK client to lookup the available trusted providers.
     *
     * @return true if the provider existed, false otherwise
     */
    private boolean doesProviderExist() {
        boolean providerExists = false;

        try {
            TrustedProviders allProviders = trustedProviderClient.getAllProviders();
            List<TrustedProvider> trustedProviders = allProviders.getTrustedProviders();

            if (trustedProviders != null)
                providerExists = trustedProviders.stream()
                        .anyMatch(trustedProvider ->
                                TRUSTED_PROVIDER_NAME.equals(trustedProvider.getName()));

        } catch (APIException e) {
            String errMsg = String.format(
                    "Failed to lookup trusted provider via SDK client - %s", e.getCallInfo());
            ServiceLogger.error(LOG, errMsg, e);
        }

        return providerExists;
    }

}
