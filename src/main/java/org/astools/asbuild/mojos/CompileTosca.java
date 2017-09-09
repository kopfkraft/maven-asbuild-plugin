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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which runs an ant task.
 *
 * @goal compile-tosca
 */

public class CompileTosca extends AbstractASMojo {

    /**
     * Any Object to print out.
     * @parameter
     *   expression="${skipTosca}"
     */
    private Object skipTosca;

    public void execute() throws MojoExecutionException {

        if(skipTosca.equals("true"))
        {
            getLog().warn("skipTest=true skipping test");
            return;
        }

        this.getLog().info("CompileTosca Mojo");
        runTask("compile-tosca");

        /*
        Artifact lib = mavenProject.getArtifact();
        Artifact lnk = factory.createArtifactWithClassifier(lib.getGroupId(), lib.getArtifactId(), lib.getVersion(), "xml", "lnk");
        mavenProject.addAttachedArtifact(lnk);
        */
    }
}
