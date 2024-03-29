/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @summary Redefine a class with an UnresolvedClass reference in the constant pool.
 * @bug 8035150
 * @library /testlibrary
 * @build UnresolvedClassAgent com.oracle.java.testlibrary.ProcessTools com.oracle.java.testlibrary.OutputAnalyzer
 * @run main TestRedefineWithUnresolvedClass
 */

import java.io.File;
import java.util.Arrays;

import com.oracle.java.testlibrary.OutputAnalyzer;
import com.oracle.java.testlibrary.ProcessTools;

public class TestRedefineWithUnresolvedClass {

    final static String slash = File.separator;
    final static String testClasses = System.getProperty("test.classes") + slash;

    public static void main(String... args) throws Throwable {
        // delete this class to cause a NoClassDefFoundError
        File unresolved = new File(testClasses, "MyUnresolvedClass.class");
        if (unresolved.exists() && !unresolved.delete()) {
            throw new Exception("Could not delete: " + unresolved);
        }

        // build the javaagent
        buildJar("UnresolvedClassAgent");

        // launch a VM with the javaagent
        launchTest();
    }

    private static void buildJar(String jarName) throws Throwable {
        String testSrc = System.getProperty("test.src", "?") + slash;

        String jarPath = String.format("%s%s.jar", testClasses, jarName);
        String manifestPath = String.format("%s%s.mf", testSrc, jarName);
        String className = String.format("%s.class", jarName);

        String[] args = new String[] {"-cfm", jarPath, manifestPath, "-C", testClasses, className};

        System.out.println("Running jar " + Arrays.toString(args));
        sun.tools.jar.Main jarTool = new sun.tools.jar.Main(System.out, System.err, "jar");
        if (!jarTool.run(args)) {
            throw new Exception("jar failed: args=" + Arrays.toString(args));
        }
    }

    private static void launchTest() throws Throwable {
        String[] args = {
            "-javaagent:" + testClasses + "UnresolvedClassAgent.jar",
            "-Dtest.classes=" + testClasses,
            "UnresolvedClassAgent" };
        OutputAnalyzer output = ProcessTools.executeTestJvm(args);
        output.shouldHaveExitValue(0);
    }
}
