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

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.osgi.DefaultMaven2OsgiConverter;
import org.apache.maven.shared.osgi.Maven2OsgiConverter;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Package the plugin and generate metadata for the plugin.
 * 
 * @goal package-plugin
 * @phase package
 * @requiresProject
 * @requiresDependencyResolution runtime
 * @threadSafe
 */
public final class PackagePluginMojo
    extends AbstractPluginMojo
{
    private final static String LIB_DIR = "META-INF/lib/";

    /**
     * The Jar archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" role-hint="jar"
     */
    private JarArchiver jarArchiver = null;

    /**
     * The archive configuration to use.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
    
    /**
     * List of files to include.
     *
     * @parameter
     */
    private String[] includes = { "**/**" };

    /**
     * List of files to exclude.
     *
     * @parameter
     */
    private String[] excludes = { "**/package.html" };

    protected void doExecute()
        throws Exception
    {
        final File jarFile = createArchive();
        final String classifier = getClassifier();

        if (classifier != null) {
            getProjectHelper().attachArtifact(getProject(), "jar", classifier, jarFile);
        } else {
            getProject().getArtifact().setFile(jarFile);
        }
    }

    private File createArchive()
        throws Exception
    {
        final File jarFile = getJarFile(getOutputDirectory(), getFinalName(), getClassifier());

        final MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(this.jarArchiver);
        archiver.setOutputFile(jarFile);

        if (getClassesDirectory().isDirectory()) {
            archiver.getArchiver().addDirectory(getClassesDirectory(), this.includes, this.excludes);
        } else {
            getLog().warn("Classes directory not found - no content to add to jar");
        }

        final List<String> libs = copyDependencies();

        this.archive.setAddMavenDescriptor(true);
        this.archive.addManifestEntry("Bundle-ManifestVersion", "2");
        this.archive.addManifestEntry("Bundle-SymbolicName", getPluginId());
        this.archive.addManifestEntry("Bundle-Name", getPluginName());
        this.archive.addManifestEntry("Bundle-Version", getOsgiVersion());
        this.archive.addManifestEntry("Require-Bundle", "system.bundle");
        this.archive.addManifestEntry("Bundle-ClassPath", getBundleClassPath(libs));

        if (!libs.isEmpty()) {
            archiver.getArchiver().addDirectory(getAppDirectory(), this.includes, this.excludes);
        }

        archiver.createArchive(getProject(), this.archive);
        return jarFile;
    }

    private static File getJarFile(final File baseDir, final String finalName, final String classifier)
    {
        final StringBuilder str = new StringBuilder(finalName);
        if (classifier != null && classifier.trim().length() > 0 && !classifier.startsWith("-")) {
            str.append("-").append(classifier);
        }

        str.append(".jar");
        return new File(baseDir, str.toString());
    }

    private String getOsgiVersion()
    {
        final Maven2OsgiConverter converter = new DefaultMaven2OsgiConverter();
        return converter.getVersion(getProject().getVersion());
    }

    private String getDefaultFinalName(final Artifact artifact)
    {
        return artifact.getArtifactId() + "-" + artifact.getVersion() + "."
                + artifact.getArtifactHandler().getExtension();
    }

    private List<String> copyDependencies()
        throws Exception
    {
        final List<String> ids = new ArrayList<String>();
        final List<String> libs = new ArrayList<String>();
        final File libDirectory = new File(getAppDirectory(), LIB_DIR);
        final Set<Artifact> artifacts = getNotProvidedDependencies();

        for (final Artifact artifact : artifacts) {
            final String targetFileName = getDefaultFinalName(artifact);
            FileUtils.copyFileIfModified(artifact.getFile(), new File(libDirectory, targetFileName));
            libs.add(LIB_DIR + targetFileName);
            ids.add(artifact.getDependencyConflictId());
        }

        if (!ids.isEmpty()) {
            getLog().info(getMessage("Following dependencies are packaged inside the plugin:", ids));
        }

        return libs;
    }

    private Set<Artifact> getNotProvidedDependencies()
        throws Exception
    {
        final Set<Artifact> result = new HashSet<Artifact>();
        for (final Artifact artifact : getIncludedArtifacts()) {
            if (Artifact.SCOPE_PROVIDED.equals(artifact.getScope()) || Artifact.SCOPE_TEST.equals(artifact.getScope())) {
                continue;
            }

            result.add(artifact);
        }

        return result;
    }

    private String getBundleClassPath(final List<String> libs)
    {
        final StringBuilder str = new StringBuilder(".");

        for (final String lib : libs) {
            str.append(",/").append(lib);
        }

        return str.toString();
    }
}
