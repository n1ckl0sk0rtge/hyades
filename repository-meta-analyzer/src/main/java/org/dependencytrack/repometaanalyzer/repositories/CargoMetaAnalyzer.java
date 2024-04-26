/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */

package org.dependencytrack.repometaanalyzer.repositories;

import com.github.packageurl.PackageURL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.dependencytrack.persistence.model.Component;
import org.dependencytrack.persistence.model.RepositoryType;
import org.dependencytrack.repometaanalyzer.model.MetaAnalyzerException;
import org.dependencytrack.repometaanalyzer.model.MetaModel;
import org.dependencytrack.commonutil.DateUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * An IMetaAnalyzer implementation that supports Cargo via crates.io compatible repos
 *
 * @author Steve Springett
 * @since 4.1.0
 */

public class CargoMetaAnalyzer extends AbstractMetaAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CargoMetaAnalyzer.class);
    private static final String DEFAULT_BASE_URL = "https://crates.io";
    private static final String API_URL = "/api/v1/crates/%s";

    CargoMetaAnalyzer() {
        this.baseUrl = DEFAULT_BASE_URL;
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryType supportedRepositoryType() {
        return RepositoryType.CARGO;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicable(final Component component) {
        return component.getPurl() != null && PackageURL.StandardTypes.CARGO.equals(component.getPurl().getType());
    }

    /**
     * {@inheritDoc}
     */
    public MetaModel analyze(final Component component) {
        final MetaModel meta = new MetaModel(component);
        if (component.getPurl() != null) {
            final String url = String.format(baseUrl + API_URL, component.getPurl().getName());
            try (final CloseableHttpResponse response = processHttpRequest(url)) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String responseString = EntityUtils.toString(entity);
                        var jsonObject = new JSONObject(responseString);
                        final JSONObject crate = jsonObject.optJSONObject("crate");
                        if (crate != null) {
                            final String latest = crate.getString("newest_version");
                            meta.setLatestVersion(latest);
                        }
                        final JSONArray versions = jsonObject.optJSONArray("versions");
                        if (versions != null) {
                            for (int i = 0; i < versions.length(); i++) {
                                final JSONObject version = versions.getJSONObject(i);
                                final String versionString = version.optString("num");
                                if (meta.getLatestVersion() != null && meta.getLatestVersion().equals(versionString)) {
                                    final String publishedTimestamp = version.optString("created_at");
                                    try {
                                        meta.setPublishedTimestamp(DateUtil.fromISO8601(publishedTimestamp));
                                    } catch (IllegalArgumentException e) {
                                        LOGGER.warn("An error occurred while parsing published time", e);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    handleUnexpectedHttpResponse(LOGGER, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), component);
                }
            } catch (IOException ex) {
                handleRequestException(LOGGER, ex);
            } catch (Exception ex) {
                throw new MetaAnalyzerException(ex);
            }
        }
        return meta;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
