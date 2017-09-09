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

package org.astools.asbuild.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: hannes
 * Date: 10.05.12
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public class ASUtils {

    public static void copyAndReplace(String input, String output, Properties prop)
    {
        // read input file
        try {

            StringBuilder content = new StringBuilder();
            FileReader reader = new FileReader(input);
            char[] buffer = new char[1024];
            int length = 0;

            while((length = reader.read(buffer)) != -1)
                content.append(buffer,0,length);

            reader.close();

            FileWriter writer = new FileWriter(output);
            writer.write(StringPropertyReplacer.replaceProperties(content.toString(), prop));
            writer.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
