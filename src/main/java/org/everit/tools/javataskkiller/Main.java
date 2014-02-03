/**
 * This file is part of Everit - Java Task Killer.
 *
 * Everit - Java Task Killer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Java Task Killer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Java Task Killer.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.tools.javataskkiller;

import java.io.IOException;
import java.util.List;

/**
 * Helper class to kill a java process with all of it's subprocess. This class does not have any dependent class
 * therefore it can be used as a .class file in any distribution environment.
 * 
 */
public class Main {

    public static final String ARG_START_COMMAND_PART = "startCommandPart";

    public static final String OS_LINUX_UNIX = "linux";

    public static final String OS_MACINTOSH = "mac";

    public static final String OS_SUNOS = "sunos";

    public static final String OS_WINDOWS = "windows";

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

    private static void killProcess(final String jpsLine) {
        String[] jpsTokens = jpsLine.split(" ");
        String processId = jpsTokens[0];
        System.out.println("Killing process with id " + processId);
        String os = getOS();
        if (OS_WINDOWS.equals(os)) {
            try {
                Runtime.getRuntime().exec("taskkill /F /T /PID " + processId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Runtime.getRuntime().exec("kill -2 " + processId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("[ERROR] There should be one argument, the --" + ARG_START_COMMAND_PART + "=xxx");
        }

        String startCommandPartArg = args[0];
        if (!startCommandPartArg.startsWith("--" + ARG_START_COMMAND_PART + "=")) {
            System.err.println("[ERROR] There should be one argument: --" + ARG_START_COMMAND_PART + "=xxx");
        }

        String startCommandPart = startCommandPartArg.substring(("--" + ARG_START_COMMAND_PART + "=").length());

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
                if (jpsResultLine.contains(startCommandPart) && !jpsResultLine.contains(ARG_START_COMMAND_PART)) {
                    killProcess(jpsResultLine);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
