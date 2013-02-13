package org.everit.tools.javataskkiller;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to kill a java process with all of it's subprocess. This class does not have any dependent class
 * therefore it can be used as a .class file in any distribution environment.
 * 
 */
public class Main {

    public static final String OS_WINDOWS = "windows";

    public static final String OS_LINUX_UNIX = "linux";

    public static final String OS_MACINTOSH = "mac";

    public static final String OS_SUNOS = "sunos";

    public static void main(String[] args) {
        
        String uniqueId = System.getProperty("processIdToKill");
        if (uniqueId == null) {
            System.out.println("[ERROR] 'processIdToKill' system property is not defined.");
        }

        try {
            Process jpsProcess = Runtime.getRuntime().exec("jps -mlvV");
            StreamDumper stdOutDumper = new StreamDumper(jpsProcess.getInputStream());
            stdOutDumper.start();
            StreamDumper stdErrorDumper = new StreamDumper(jpsProcess.getErrorStream());
            stdErrorDumper.start();

            int exitValue = jpsProcess.waitFor();
            if (exitValue != 0) {
                System.out.println("ERROR: JPS call returned with exit code " + exitValue);
                System.exit(exitValue);
            }
            stdOutDumper.stop();
            stdErrorDumper.stop();
            List<String> jpsResultLines = stdOutDumper.retrieveLines(null);

            for (String jpsResultLine : jpsResultLines) {
                if (jpsResultLine.contains(uniqueId) && !jpsResultLine.contains("processIdToKill")) {
                    killProcess(jpsResultLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void killProcess(String jpsLine) {
        String[] jpsTokens = jpsLine.split(" ");
        String processId = jpsTokens[0];
        System.out.println("Killing process with id " + processId);
        String os = getOS();
        if (OS_WINDOWS.equals(os)) {
            try {
                Runtime.getRuntime().exec("taskkill /F /T /PID " + processId);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                Runtime.getRuntime().exec("kill -2 " + processId);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return OS_WINDOWS;
        }
        if (os.indexOf("mac") >= 0) {
            return OS_MACINTOSH;
        }
        if ((os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)) {
            return OS_LINUX_UNIX;
        }
        if (os.indexOf("sunos") >= 0) {
            return OS_SUNOS;
        }
        return null;
    }
}
