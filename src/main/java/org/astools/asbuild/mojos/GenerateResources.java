/*
    maven-asbuild-plugin integrates the adobe flash based artifacts into maven.
    Copyright (C) 2011  kopfkraft(at)hotmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.astools.asbuild.mojos;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate-resources
 */
public class GenerateResources extends AbstractASMojo {

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List remoteRepositories;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * @parameter
     *   expression="${flexSDK}"
     */
    private String flexSDK;

    /**
     * @parameter
     *   expression="${buildNumber}"
     */
    private String buildNumber;
    
    /**
     * @parameter
     *   expression="${compileMode}"
     */
    private String compileMode;    
    
    public void execute() throws MojoExecutionException {

        // create antlibs folders
        File dir = new File(workingPath("antlib"));
        if (!dir.exists())
            dir.mkdirs();
        
        // check and create en_US default locale
        dir = new File(basePath("src/main/locale/en_US"));
        if (!dir.exists())
            dir.mkdirs();

        // create compiler-debug.xml and compiler-release.xml in case they do not exist
        File compilerConfig = new File(basePath() + "/src/conf/compiler-debug.xml");
        if(!compilerConfig.exists())
            resourceToFile(resourcePath("compiler-debug.xml"), compilerConfig);
        
        compilerConfig = new File(basePath() + "/src/conf/compiler-release.xml");
        if(!compilerConfig.exists())
            resourceToFile(resourcePath("compiler-release.xml"), compilerConfig);

        compilerConfig = new File(basePath() + "/src/conf/compiler-tosca.xml");
        if(!compilerConfig.exists())
            resourceToFile(resourcePath("compiler-tosca.xml"), compilerConfig);

        // copy build.xml to asbuild folder
        resourceToFile(resourcePath("build.xml"), new File(workingPath("build.xml")));

        // read default properties file
        Properties prop = new Properties();
        try {
            InputStream stream = getClass().getResourceAsStream(resourcePath("build.properties"));
            prop.load(stream);
            stream.close();
        } catch (IOException e) {
            throw new MojoExecutionException("could not retrieve build.properties from jar");
        }

        // read projects properties files
        Properties conf = new Properties();
        try {
            File conffile = new File(basePath() + "/src/conf/build.properties");

            if (conffile.exists()) {
                InputStream stream = new FileInputStream(conffile);
                conf.load(stream);
                stream.close();
            } else {
                getLog().info("project doesnt contain src/conf/build.properties");
            }

        } catch (IOException e) {
            getLog().error("error reading projects build.properties");
        }

        // copy jars
        copyJars();

        // set FLEX_HOME
        String flexHome = flexSDK != null ? (String)flexSDK : System.getenv("FLEX_HOME") ;
        if (flexHome == null || flexHome.equals("")) {
            flexHome = "/home/willo/buildTools/flex_sdk_4.5.2";
            getLog().error("NO FLEX_HOME DEFINED, using fallback: " + flexHome);
        }

        prop.setProperty("flex.home", formatAntPath(flexHome));
        getLog().info("using FLEX_HOME: " + flexHome);

        // set flex-config.xml (air-config.xml, airmobile-config.xml, ..)
        String confxml = conf.getProperty("flex.config");
        confxml = confxml == null || confxml.equals("") ? flexHome + "/frameworks/air-config.xml" : flexHome + "/frameworks/" + confxml;
        prop.setProperty("flex.config", formatAntPath(confxml));

        // set main class
        String mvnmain = conf.getProperty("maven.main");
        mvnmain = mvnmain == null || mvnmain.equals("") ? sourcePath() + "/Main.as" : sourcePath() + "/" + mvnmain;
        prop.setProperty("maven.main", formatAntPath(mvnmain));

        // set nickname (name in runtime)
        String nick = conf.getProperty("maven.nickname");
        nick = nick == null || nick.equals("") ? artifactName() : nick;
        prop.setProperty("maven.nickname", nick);

        // set other properties
        prop.setProperty("maven.name", artifactName());
        prop.setProperty("maven.project", basePath());
        prop.setProperty("maven.target", basePath("target"));
        prop.setProperty("maven.build", basePath("target/asbuild"));
        prop.setProperty("maven.source", sourcePath());
        prop.setProperty("maven.testSource", basePath("src/test/as"));
        prop.setProperty("maven.includeLibs", basePath("src/libs"));//);
        prop.setProperty("maven.dependencies", workingPath("aslib"));
        prop.setProperty("maven.testLibs", workingPath("testlib"));
        prop.setProperty("maven.antlib", workingPath("antlib"));
        prop.setProperty("maven.buildnumber", buildNumber == null ? "0" : (String)buildNumber);
        prop.setProperty("maven.locale", basePath("src/main/locale"));

        //FIXME proper name for tosca prop
        prop.setProperty("maven.tosca", workingPath("testlib"));
        prop.setProperty("maven.tosca.conf", workingPath("compiler-tosca.xml"));

        //TODO take proper compiler config.xml (maybe more than just debug and release)
        compileMode = compileMode != null && !compileMode.equals("") ? "compiler-" + compileMode + ".xml" : "compiler-release.xml";
        prop.setProperty("project.config", workingPath(compileMode));

        try {

            FileWriter writer = new FileWriter(workingPath("build.properties"));
            Enumeration<Object> keys = prop.keys();
            String key = "";
            String value = "";

            while (keys.hasMoreElements()) {
                key = (String) keys.nextElement();
                value = prop.getProperty(key);
                writer.write(key + "=" + value + "\r");
            }

            writer.close();

        } catch (IOException e) {
            throw new MojoExecutionException("cannot write build.properties in asbuild folder");
        }

        // copy compiler configurations to working directory
        copyAndReplace(basePath() + "/src/conf/compiler-release.xml", workingPath("compiler-release.xml"),prop);
        copyAndReplace(basePath() + "/src/conf/compiler-debug.xml", workingPath("compiler-debug.xml"),prop);
        copyAndReplace(basePath() + "/src/conf/compiler-tosca.xml", workingPath("compiler-tosca.xml"),prop);

        for (Object obj : remoteRepositories)
        {
            MavenArtifactRepository repo = (MavenArtifactRepository) obj;
            getLog().info("remoteRepository: " + repo.getUrl());
        }
        
        getLog().info("localRepository: " + localRepository.getUrl());
        
        Set<Artifact> dependencies = mavenProject.getDependencyArtifacts();
        for(Artifact artifact : dependencies)
        {
            getLog().info("dependency: " + artifact.toString());
            Artifact local = localRepository.find(artifact);
            getLog().info("url: " + local.getFile().getAbsolutePath());
            
        }


        
        runTask("copy-dep");
    }

    public void copyJars() throws MojoExecutionException {

        // do not distribute jars when workingPath exists
        File working = new File( workingPath("antlib"));
        if (working.list().length > 0) {
            getLog().info("jar files are not updated.");
            return;
        }

        String[] jars = null;
        try {
            jars = getResourceListing(getClass(), "ant/libs/");

            for (String resource : jars) {
                String rname = resource.replaceFirst("ant/libs/", "");
                resourceToFile("/" + resource, new File(workingPath("antlib" + File.separator + rname)));
            }
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("cannot find jar folder");
        } catch (IOException e) {
            throw new MojoExecutionException("cannot copy jar resources");
        }

        // display jar info
        for (String j : jars)
            getLog().info("JARS: " + j);

    }
}
