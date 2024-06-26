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
package org.dependencytrack.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.dependencytrack.persistence.model.Repository;
import org.dependencytrack.persistence.model.RepositoryType;

import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class RepoEntityRepository implements PanacheRepository<Repository> {

    public List<Repository> findEnabledRepositoriesByType(final RepositoryType type) {
        return find("type = :type AND enabled = :enabled ORDER BY resolutionOrder ASC",
                Parameters.with("type", type).and("enabled", true))
                .withHint(HINT_READ_ONLY, true)
                .list();
    }

}
