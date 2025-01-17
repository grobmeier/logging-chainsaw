/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.xml;

import org.apache.log4j.helpers.Constants;
import org.apache.log4j.rule.ExpressionRule;
import org.apache.log4j.rule.Rule;
import org.apache.log4j.spi.Decoder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.apache.log4j.chainsaw.ChainsawReceiverSkeleton;
import org.apache.log4j.chainsaw.logevents.ChainsawLoggingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LogFileXMLReceiver will read an xml-formated log file and make the events in the log file
 * available to the log4j framework.
 * <p>
 * This receiver supports log files created using log4j's XMLLayout, as well as java.util.logging
 * XMLFormatter (via the org.apache.log4j.spi.Decoder interface).
 * <p>
 * By default, log4j's XMLLayout is supported (no need to specify a decoder in that case).
 * <p>
 * To configure this receiver to support java.util.logging's XMLFormatter, specify a 'decoder' param
 * of org.apache.log4j.xml.UtilLoggingXMLDecoder.
 * <p>
 * Tailing -may- work, but not in all cases (try using a file:// URL). If a process has a log file
 * open, the receiver may be able to read and tail the file. If the process closes the file and
 * reopens the file, the receiver may not be able to continue tailing the file.
 * <p>
 * An expressionFilter may be specified. Only events passing the expression will be forwarded to the
 * log4j framework.
 * <p>
 * Once the event has been "posted", it will be handled by the appenders currently configured in the
 * LoggerRespository.
 *
 * @author Scott Deboy &lt;sdeboy@apache.org&gt;
 * @since 1.3
 */

public class LogFileXMLReceiver extends ChainsawReceiverSkeleton {
    private String fileURL;
    private Rule expressionRule;
    private String filterExpression;
    private String decoder = "org.apache.log4j.xml.XMLDecoder";
    private boolean tailing = false;

    private Decoder decoderInstance;
    private Reader reader;
    private static final String FILE_KEY = "file";
    private String host;
    private String path;
    private boolean useCurrentThread;

    private static final Logger logger = LogManager.getLogger();

    /**
     * Accessor
     *
     * @return file URL
     */
    public String getFileURL() {
        return fileURL;
    }

    /**
     * Specify the URL of the XML-formatted file to process.
     *
     * @param fileURL
     */
    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    /**
     * Accessor
     *
     * @return
     */
    public String getDecoder() {
        return decoder;
    }

    /**
     * Specify the class name implementing org.apache.log4j.spi.Decoder that can process the file.
     *
     * @param _decoder
     */
    public void setDecoder(String _decoder) {
        decoder = _decoder;
    }

    /**
     * Accessor
     *
     * @return filter expression
     */
    public String getFilterExpression() {
        return filterExpression;
    }

    /**
     * Accessor
     *
     * @return tailing flag
     */
    public boolean isTailing() {
        return tailing;
    }

    /**
     * Set the 'tailing' flag - may only work on file:// URLs and may stop tailing if the writing
     * process closes the file and reopens.
     *
     * @param tailing
     */
    public void setTailing(boolean tailing) {
        this.tailing = tailing;
    }

    /**
     * Set the filter expression that will cause only events which pass the filter to be forwarded
     * to the log4j framework.
     *
     * @param filterExpression
     */
    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    private boolean passesExpression(ChainsawLoggingEvent event) {
        if (event != null) {
            if (expressionRule != null) {
                return (expressionRule.evaluate(event, null));
            }
        }
        return true;
    }

    public static void main(String[] args) {
        /*
         * LogFileXMLReceiver test = new LogFileXMLReceiver();
         * test.setFileURL("file:///c:/samplelog.xml"); test.setFilterExpression("level >= TRACE");
         * test.activateOptions();
         */
    }

    /**
     * Close the receiver, release any resources that are accessing the file.
     */
    public void shutdown() {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void process(Reader unbufferedReader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(unbufferedReader);
        char[] content = new char[10000];
        logger.debug("processing starting: " + fileURL);
        int length;
        do {
            System.out.println("in do loop-about to process");
            while ((length = bufferedReader.read(content)) > -1) {
                processEvents(decoderInstance.decodeEvents(String.valueOf(content, 0, length)));
            }
            if (tailing) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } while (tailing);
        logger.debug("processing complete: " + fileURL);

        shutdown();
    }

    private void processEvents(Collection<ChainsawLoggingEvent> c) {
        if (c == null) {
            return;
        }

        for (ChainsawLoggingEvent evt : c) {
            if (passesExpression(evt)) {
                if (evt.getProperty(Constants.HOSTNAME_KEY) != null) {
                    evt.setProperty(Constants.HOSTNAME_KEY, host);
                }
                if (evt.getProperty(Constants.APPLICATION_KEY) != null) {
                    evt.setProperty(Constants.APPLICATION_KEY, path);
                }
                append(evt);
            }
        }
    }

    /**
     * When true, this property uses the current Thread to perform the import, otherwise when false
     * (the default), a new Thread is created and started to manage the import.
     *
     * @return
     */
    public final boolean isUseCurrentThread() {
        return useCurrentThread;
    }

    /**
     * Sets whether the current Thread or a new Thread is created to perform the import, the default
     * being false (new Thread created).
     *
     * @param useCurrentThread
     */
    public final void setUseCurrentThread(boolean useCurrentThread) {
        this.useCurrentThread = useCurrentThread;
    }

    @Override
    public void start() {
        Runnable runnable = () -> {
            try {
                URL url = new URL(fileURL);
                host = url.getHost();
                if (host != null && host.equals("")) {
                    host = FILE_KEY;
                }
                path = url.getPath();
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                if (filterExpression != null) {
                    expressionRule = ExpressionRule.getRule(filterExpression);
                }
            } catch (Exception e) {
                logger.warn("Invalid filter expression: " + filterExpression, e);
            }

            Class c;
            try {
                c = Class.forName(decoder);
                Object o = c.newInstance();
                if (o instanceof Decoder) {
                    decoderInstance = (Decoder) o;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                reader = new InputStreamReader(new URL(getFileURL()).openStream());
                process(reader);
            } catch (FileNotFoundException fnfe) {
                logger.info("file not available");
            } catch (IOException ioe) {
                logger.warn("unable to load file", ioe);
                return;
            }
        };
        if (useCurrentThread) {
            runnable.run();
        } else {
            Thread thread = new Thread(runnable, "LogFileXMLReceiver-" + getName());

            thread.start();

        }
    }

}
