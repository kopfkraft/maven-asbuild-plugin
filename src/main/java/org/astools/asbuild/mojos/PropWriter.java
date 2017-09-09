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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: hannes
 * Date: 28.02.12
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */
public class PropWriter extends TranslationWriter {
    
    protected File outputFile;
    protected FileWriter writer;
    protected String extension = ".properties";

    public PropWriter(String path)
    {
        outputFile = new File(path + extension);
    }

    @Override
    void writeHeader() {
        try {
            writer.write("# TRANSLATION GENERATED WITH ASBUILD MOJO\n");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    void writeEntry(String key, String value) {
        try {
            writer.write(key + "=" + value + "\n");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    void writeFooter() {
        try {
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    void open() {
        try {
            writer = new FileWriter(outputFile);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
