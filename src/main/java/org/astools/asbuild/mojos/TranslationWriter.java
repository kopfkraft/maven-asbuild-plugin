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

/**
 * Created by IntelliJ IDEA.
 * User: hannes
 * Date: 28.02.12
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */
abstract class TranslationWriter {

    abstract void writeHeader();

    abstract void writeEntry(String key, String value);

    abstract void writeFooter();

    abstract void close();

    abstract void open();
}
