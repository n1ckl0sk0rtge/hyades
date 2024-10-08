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
package org.dependencytrack.notification.publisher;

import io.pebbletemplates.pebble.PebbleEngine;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import org.dependencytrack.proto.notification.v1.Notification;

@ApplicationScoped
@Startup // Force bean creation even though no direct injection points exist
public class CsWebexPublisher extends AbstractWebhookPublisher implements Publisher {

    private final PebbleEngine pebbleEngine;

    @Inject
    public CsWebexPublisher(@Named("pebbleEngineJson") final PebbleEngine pebbleEngine) {
        this.pebbleEngine = pebbleEngine;
    }

    public void inform(final PublishContext ctx, final Notification notification, final JsonObject config) throws Exception {
        publish(ctx, getTemplate(config), notification, config);
    }

    @Override
    public PebbleEngine getTemplateEngine() {
        return pebbleEngine;
    }

}