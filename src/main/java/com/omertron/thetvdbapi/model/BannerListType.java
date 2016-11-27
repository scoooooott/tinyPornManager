/*
 *      Copyright (c) 2004-2015 Matthew Altman & Stuart Boston
 *
 *      This file is part of TheTVDB API.
 *
 *      TheTVDB API is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      TheTVDB API is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with TheTVDB API.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.omertron.thetvdbapi.model;

import java.util.Locale;

/**
 * Describes the list of possible banner types stored in the "BannerType" field returned from TheTVDB
 *
 * @author Stuart.Boston
 *
 */
public enum BannerListType {

    SERIES,
    SEASON,
    POSTER,
    FANART;

    public static BannerListType fromString(String type) {
        if (type != null) {
            try {
        return BannerListType.valueOf(type.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("BannerListType " + type + " does not exist", ex);
            }
        }
        throw new IllegalArgumentException("BannerListType is null");
    }

}
