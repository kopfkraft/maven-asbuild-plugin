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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.astools.asbuild.utils.ASUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The AbstractASMojo delivers the basic functionality like
 * running ANT tasks, copyFile, copyResource ... etc.
 * All MOJOs extend this class.
 *
 * @author kopfkraft@hotmail.com
 */
abstract class AbstractASMojo extends AbstractMojo {

    public static final String PATH_SEPARATOR = "/";

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject mavenProject;


    /**
     * @parameter
     */
    protected String main;

    public String buildPath(String name) {
        return formatAntPath(mavenProject.getBuild().getOutputDirectory() + File.separator + name);
    }

    public String buildPath() {
        return formatAntPath(mavenProject.getBuild().getOutputDirectory());
    }

    public File baseDir() {
        return mavenProject.getBasedir();
    }

    public String basePath(String name) {
        return formatAntPath(baseDir().getAbsolutePath() + File.separator + name);
    }

    public String basePath() {
        return formatAntPath(baseDir().getAbsolutePath());
    }

    public String workingPath(String name) {
        return formatAntPath(baseDir().getAbsolutePath() + File.separator + "target" + File.separator + "asbuild" + File.separator + name);
    }

    public String workingPath() {
        return formatAntPath(baseDir().getAbsolutePath() + File.separator + "target" + File.separator + "asbuild");
    }

    public String sourcePath() {
        return formatAntPath(baseDir().getAbsolutePath() + File.separator + "src/main/as");
    }

    public String resourcePath(String name) {
        return "/ant/" + name;
    }

    public String artifactName() {
        return mavenProject.getArtifact().getArtifactId() + "-" + mavenProject.getArtifact().getVersion();
    }

    public Project getAntProject() {

        Project project = (Project) getPluginContext().get("antProject");

        if (project == null) {
            project = initializeAntProject();
            getPluginContext().put("antProject", project);
        }

        return project;
    }

    protected Project initializeAntProject() {

        Project antProject = new Project();

        DefaultLogger logger = new DefaultLogger();
        logger.setMessageOutputLevel(Project.MSG_VERBOSE);
        logger.setErrorPrintStream(System.err);
        logger.setOutputPrintStream(System.out);
        antProject.addBuildListener(logger);

        File buildFile = new File(workingPath("build.xml"));
        getLog().info("using buildfile: " + buildFile.getAbsolutePath());
        antProject.setProperty("ant.file", buildFile.getAbsolutePath());

        //TODO check right order of calls
        antProject.init();
        ProjectHelper.configureProject(antProject, buildFile);

        return antProject;
    }

    public void resourceToFile(String resid, File file) {

        if (!file.exists()) {

            byte[] buffer = new byte[1024];

            try {
                BufferedInputStream inBuffer = new BufferedInputStream(getClass().getResourceAsStream(resid));

                FileOutputStream fileStream = new FileOutputStream(file);

                int bytesRead = 0;

                while ((bytesRead = inBuffer.read(buffer)) != -1) {
                    fileStream.write(buffer, 0, bytesRead);
                }

                inBuffer.close();
                fileStream.close();

            } catch (Exception e) {
                System.out.println(e);
            }

        }
    }

    public void copyFile(String from, String to) {
        try {

            FileInputStream input = new FileInputStream(from);
            FileOutputStream output = new FileOutputStream(to);
            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            while ((bytesRead = input.read(buffer)) != -1)
                output.write(buffer, 0, bytesRead);

            input.close();
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    
    public static void copyAndReplace(String input, String output, Properties prop)
    {
        ASUtils.copyAndReplace(input, output, prop);
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     * @author Greg Briggs
     */
    public String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
            * In case of a jar file, we can't actually find a directory.
            * Have to assume the same jar as clazz.
            */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            HashSet<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && !name.equals(path)) { //filter according to the path

                    result.add(name);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    public void runTask(String task) throws MojoExecutionException {

        getLog().info("ASBuild running task: " + task);
        Project project = getAntProject();

        try {

            getLog().info("executing task " + task + " " + project);
            project.executeTarget(task);
            getLog().info("finished task " + task);

        } catch (Exception e) {

            getLog().error("error while running: " + task);
            getLog().error(e.getMessage());

            throw new MojoExecutionException("ant task did not succeed - " + task);
        }

    }

    public static String formatAntPath(String path) {

        return path.replaceAll("\\\\", "/");
    }

}
