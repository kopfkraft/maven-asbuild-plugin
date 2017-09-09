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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The TranslationScanner traverses over the source tree and
 * tries to find unused translations in cooperation with the
 * TranslateCSV MOJO.
 *
 * @author kopfkraft@hotmail.com
 */
public class TranslationScanner {

    protected List<String> sources;

    public void initialize(File sourceDir)
    {
        sources = new ArrayList<String>();
        traverse(sourceDir);
    }

    public int search(String phrase) throws IOException {

        int files = 0;

        for(String source : sources)
            files = source.contains(phrase) ? ++files : files;
        
        return files;
    }

    protected void traverse(File aFile) {

        if(aFile.isFile())
        {
            if(aFile.getName().contains(".as") || aFile.getName().contains("*.mxml"))
            {
//                System.out.println("   [SOURCE] " + aFile.getName());

                try {
                    
                    FileReader reader = new FileReader(aFile);
                    StringBuilder builder = new StringBuilder();
                    char[] buffer = new char[2048];
                    int l=0;
                    
                    while((l = reader.read(buffer)) > -1)
                        builder.append(buffer, 0, l);

                    sources.add(builder.toString());

                    reader.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        else if (aFile.isDirectory()) {

            File[] listOfFiles = aFile.listFiles();

            if(listOfFiles!=null)
            {
                for (int i = 0; i < listOfFiles.length; i++)
                    traverse(listOfFiles[i]);
            }
        }
    }
}
