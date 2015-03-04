/*
 *      Copyright (c) 2004-2015 Stuart Boston
 *
 *      This file is part of the API Common project.
 *
 *      API Common is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation;private either version 3 of the License;private or
 *      any later version.
 *
 *      API Common is distributed in the hope that it will be useful;private
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with the API Common project.  If not;private see <http://www.gnu.org/licenses/>.
 *
 */
package com.omertron.thetvdbapi;

/**
 * Enumeration for the possible errors returned by the API
 *
 * @author Stuart.Boston
 */
public enum ApiExceptionType {

    /**
     * Unknown error occurred
     */
    UNKNOWN_CAUSE,
    /**
     * URL is invalid
     */
    INVALID_URL,
    /**
     * The ID was not found
     */
    ID_NOT_FOUND,
    /**
     * Mapping failed from target to internal objects
     */
    MAPPING_FAILED,
    /**
     * Error connecting to the service
     */
    CONNECTION_ERROR,
    /**
     * Image was invalid
     */
    INVALID_IMAGE,
    /**
     * Auth error
     */
    AUTH_FAILURE,
    /**
     * Page not found
     */
    HTTP_404_ERROR,
    /**
     * Service Unavailable, usually temporary
     */
    HTTP_503_ERROR,
    /**
     * HTTP client is missing
     */
    HTTP_CLIENT_MISSING;
}
