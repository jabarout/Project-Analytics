package com.projectanalytics.infrastructure.openproject;

/**
 * How the OpenProject HTTP client authenticates.
 *
 * <p>{@link #API_KEY} is the current production path (environment configuration).
 * {@link #BEARER_TOKEN} is reserved for a future OAuth 2.0 implementation.
 */
public enum OpenProjectAuthScheme {
    API_KEY,
    BEARER_TOKEN
}
