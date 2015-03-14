/*
 * Copyright 2014 Kaaprotech Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaaprotech.satu.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.kaaprotech.satu.SatuToJava;

@Mojo(name = "satu", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true)
public class SatuMojo extends AbstractMojo {

    @Parameter(property = "project.build.sourceEncoding")
    protected String encoding;

    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Parameter
    protected Set<String> includes = new HashSet<String>();

    @Parameter
    protected Set<String> excludes = new HashSet<String>();

    @Parameter
    protected boolean jsonCompatible = false;

    /**
     * Root directory where the Satu model definition files ({@code *.satu}) are located.
     */
    @Parameter(defaultValue = "${basedir}/src/main/satu")
    private File sourceDirectory;

    /**
     * Root output directory where the Java files are generated.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/satu")
    private File outputDirectory;

    @Component
    private BuildContext buildContext;

    public void execute() throws MojoExecutionException {
        final Log log = getLog();

        if (log.isDebugEnabled()) {
            for (String e : excludes) {
                log.debug("SATU: Exclude: " + e);
            }

            for (String e : includes) {
                log.debug("SATU: Include: " + e);
            }

            log.debug("SATU: Output: " + outputDirectory);
        }

        if (!sourceDirectory.isDirectory()) {
            log.info("SATU: Root source directory doesn't exist " + sourceDirectory.getAbsolutePath());
            return;
        }

        log.info("SATU: Processing root source directory " + sourceDirectory.getAbsolutePath());

        log.debug("Output root directory is " + outputDirectory.getAbsolutePath());

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        final List<File> satuModelFiles;
        try {
            satuModelFiles = getSatuModelDefinitionFiles();
        }
        catch (InclusionScanException e) {
            log.error(e);
            throw new MojoExecutionException("Error occured finding the Satu model definition files", e);
        }

        log.info("Compiling " + satuModelFiles.size() + " Satu model definition files to " + outputDirectory.getAbsolutePath());

        final SatuToJava generator = new SatuToJava(outputDirectory.getAbsolutePath());

        Exception exception = null;
        for (File file : satuModelFiles) {
            try {
                generator.generate(file.getPath(), encoding, jsonCompatible);
            }
            catch (Exception e) {
                if (exception == null) {
                    exception = e;
                }
                log.error("Failed to generate code for Satu model definition " + file.getPath(), e);
            }
        }

        if (exception != null) {
            throw new MojoExecutionException("Error occured generating code for Sata model definition files", exception);
        }

        if (project != null) {
            project.addCompileSourceRoot(outputDirectory.getPath());
        }
    }

    private List<File> getSatuModelDefinitionFiles() throws InclusionScanException {
        final Log log = getLog();
        final Set<String> inculdePatterns = includes == null || includes.isEmpty() ? Collections.singleton("**/*.satu") : includes;
        final SourceInclusionScanner scanner = new SimpleSourceInclusionScanner(inculdePatterns, excludes);
        scanner.addSourceMapping(new SuffixMapping("satu", Collections.<String> emptySet()));
        final Set<File> modelFiles = scanner.getIncludedSources(sourceDirectory, null);

        if (modelFiles.isEmpty()) {
            log.info("No Satu model files to process");
            return Collections.emptyList();
        }

        final List<File> retVal = new ArrayList<File>();
        for (File modelFile : modelFiles) {
            if (!buildContext.hasDelta(modelFile)) {
                continue;
            }
            buildContext.removeMessages(modelFile);
            log.debug("Satu model file detected: " + modelFile.getPath());
            retVal.add(modelFile);
        }

        Collections.sort(retVal);
        return retVal;
    }
}
