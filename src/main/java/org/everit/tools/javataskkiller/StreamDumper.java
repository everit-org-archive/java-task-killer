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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StreamDumper {

    private class PollerRunnable implements Runnable {

        @Override
        public void run() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {
                while (!stopped && (line = bufferedReader.readLine()) != null) {
                    synchronized (lines) {
                        lines.addLast(line);
                    }
                }
                stopped = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private InputStream inputStream;

    private LinkedList<String> lines = new LinkedList<String>();

    private boolean stopped;

    public StreamDumper(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void start() throws IOException {
        new Thread(new PollerRunnable()).start();
    }

    public void stop() throws IOException {
        if (!stopped) {
            stopped = true;
        }

    }

    public List<String> retrieveLines(Integer maxLineNum) {
        List<String> result = new ArrayList<String>();
        int readLine = 0;
        synchronized (lines) {
            
            int allLineNum = lines.size();
            
            while (readLine < allLineNum && (maxLineNum == null || readLine < maxLineNum.intValue())) {
                result.add(lines.removeFirst());
                readLine++;
            }
        }
        return result;
    }
}
