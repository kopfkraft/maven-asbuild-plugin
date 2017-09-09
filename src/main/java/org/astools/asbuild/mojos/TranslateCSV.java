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
import org.apache.maven.plugin.MojoFailureException;
import org.astools.asbuild.csv.CsvReader;
import org.astools.asbuild.utils.TranslationScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Goal which takes a comma seperated values (*.csv) file and
 * generates translation files of it. (bash framework for now)
 *
 * @goal translate-csv
 */
public class TranslateCSV extends AbstractASMojo 
{
    /**
     * The csv file to read
     * @parameter
     *   expression="${csvFile}"
     */
    private String csvFile;

    /**
     * The csv file to read
     * @parameter
     *   expression="${source}"
     */
    private String source;

    /**
     * The csv file to read
     * @parameter
     *   expression="${outputFolder}"
     */
    private String outputFolder;


    /**
     * The csv file to read
     * @parameter
     *   expression="${format}"
     */
    private String format;

    protected TranslationScanner scanner;

    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("TranslateCSV Mojo");
        getLog().info(csvFile);

        // get csv file or return
        File csv = new File(csvFile);
        if(!csv.exists() || !new File(source).exists())
            return;

        File destination = new File(outputFolder);
        if(!destination.exists())
            destination.mkdirs();

        getLog().info("trying to parse csv file: " + csv.getAbsolutePath());

        scanner = new TranslationScanner();
        scanner.initialize(new File(source));

        TranslationWriter[] writers = null;

        try {

            //TODO naming is not right for xml (takes EN,DE as path not as filename)
            CsvReader reader = new CsvReader(csvFile,',', Charset.forName("UTF-8"));
            reader.setTextQualifier('"');

            if(reader.readHeaders())
            {
                getLog().info("read csv headers: " + reader.getHeaders().toString());
                
                int numLang = reader.getHeaderCount() - 1;
                writers = format.equals("xml") ? new XMLWriter[numLang] : new PropWriter[numLang];

                for(int i=0;i < numLang;i++)
                {
                    File resouceFolder = new File(outputFolder + "/" + reader.getHeader(i + 1));
                    if(!resouceFolder.exists())
                        resouceFolder.mkdirs();
                    
                    String path = resouceFolder.getAbsolutePath() + "/" + csv.getName().toLowerCase().replace(".csv","");
                    writers[i] = format.equals("xml") ? new XMLWriter(path) : new PropWriter(path);
                    writers[i].open();
                    writers[i].writeHeader();

                    getLog().info("created translation file: " + path);
                }
            }

            while(reader.readRecord())
            {
                String key = reader.get(0);
                
                for(int i=0;i < reader.getHeaderCount() - 1; i++)
                    writers[i].writeEntry(key, reader.get(i + 1));

                // scan source folder
                int count = scanner.search(key);

                if(count < 1)
                    getLog().warn("UNUSED TRANSLATION WITH KEY: " + key);
                else
                    getLog().info(key + " found in " + count + " file(s)");
            }
            
            for(TranslationWriter writer : writers)
            {
                writer.writeFooter();
                writer.close();
            }
            
            reader.close();

        } catch (FileNotFoundException e) {
            throw new MojoFailureException("translation source file (csv) couldnt be found.");
        } catch (IOException e) {
            throw new MojoFailureException("translation could not be generated.");
        }
    }
}
