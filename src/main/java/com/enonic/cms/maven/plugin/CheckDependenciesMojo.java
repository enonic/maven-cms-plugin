/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.enonic.cms.maven.plugin;

import org.apache.maven.artifact.Artifact;
import java.util.*;

/**
 * @goal check-dependencies
 * @requiresDependencyResolution runtime
 * @phase initialize
 * @threadSafe
 */
public final class CheckDependenciesMojo
    extends AbstractPluginMojo
{
    private final static List<String> SHOULD_BE_PROVIDED = Arrays.asList(
        "cms-api", "jdom", "servlet-api", "jaxen",
        "log4j", "commons-logging", "slf4j", "spring-beans", "spring-context", "spring-core"
    );

    protected void doExecute()
        throws Exception
    {
        checkDependencies(SHOULD_BE_PROVIDED, "The following dependencies should be excluded,  "
                    + "be declared with scope 'provided' or set with optional flag:");
    }

    private void checkDependencies(final List<String> set, final String message)
        throws Exception
    {
        final List<String> ids = new ArrayList<String>();
        for (final Artifact dep : getIncludedArtifacts()) {
            if (set.contains(dep.getArtifactId())) {
                ids.add(dep.getDependencyConflictId());
            }
        }

        if (ids.isEmpty()) {
            return;
        }

        final String str = getMessage(message, ids);
        getLog().warn(str);
    }
}
