# Enonic CMS Maven Plugin

This Maven plugin helps developers to package Enonic CMS plugins. It hides the details of the OSGi layer and
makes the development and packaging a little smoother.

## Repository

First thing is to set up a maven plugin repository to be able to access the plugin.

    <pluginRepositories>
        <pluginRepository>
            <id>enonic</id>
            <url>http://repo.enonic.com/maven</url>
        </pluginRepository>
    </pluginRepositories>

## Packaging

To be able to package the plugin, you need to set the packaging to 'cms-plugin' like this:

    <packaging>cms-plugin</packaging>

Also, you need to configure the actual maven plugin:

    <build>
        <plugins>
            <plugin>
                <groupId>com.enonic.cms.tools</groupId>
                <artifactId>maven-cms-plugin</artifactId>
                <version>1.0.1</version>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>

## Dependencies

All runtime dependencies are automatically packaged into your resulting jar file (inside META-INF/lib). If you do
not want to package certain dependencies into the jar, just set the scope of that dependency to 'provided'.

## Example

For a complete example of using this, please refer to the
[Example plugin](https://github.com/enonic/cms-example-plugin).

## License

This software is licensed under Apache 2.0 license.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
