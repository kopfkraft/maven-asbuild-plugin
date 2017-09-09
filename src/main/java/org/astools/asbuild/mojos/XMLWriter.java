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
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class XMLWriter extends TranslationWriter {

    protected File outputFile;
    protected FileWriter fileWriter;
    protected String extension = ".xml";
    
    public XMLWriter(String path)
    {
        outputFile = new File(path + extension);
    }
    
    @Override
    void writeHeader() {
        try {
            fileWriter.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            fileWriter.write("<items>\n");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    void writeEntry(String key, String value) {
        try {
            fileWriter.write("<item key=\"" + key + "\" value=\"" + value + "\" />\n");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    void close() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    void open() {
        try {
            fileWriter = new FileWriter(outputFile);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    void writeFooter() {
        try {
            fileWriter.write("</items>\n");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
