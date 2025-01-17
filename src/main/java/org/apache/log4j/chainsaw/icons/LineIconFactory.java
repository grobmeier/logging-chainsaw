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

package org.apache.log4j.chainsaw.icons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * A simple factory/facade for creating some of the standard Icons that are based
 * on line drawings
 *
 * @author Paul Smith &lt;psmith@apache.org&gt;
 * @author Scott Deboy &lt;sdeboy@apache.org&gt;
 */
public final class LineIconFactory {
    private static final Logger logger = LogManager.getLogger(LineIconFactory.class);

    /**
     *
     */
    private LineIconFactory() {
    }

    public static Icon createExpandIcon() {
        int size = 8;
        int xOffSet = 0;
        int yOffSet = 0;
        try {
            GraphicsEnvironment environment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2D =
                environment.createGraphics(
                    image);
            g2D.setBackground(new Color(0, 0, 0, 0));
            g2D.clearRect(0, 0, size, size);
            g2D.setStroke(new BasicStroke(1.5f));
            g2D.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2D.setColor(Color.black);
            g2D.drawLine(
                xOffSet, (size / 2) + yOffSet, size - xOffSet,
                (size / 2) + yOffSet);

            g2D.drawLine(
                xOffSet + (size / 2), yOffSet, xOffSet + (size / 2),
                (size) + yOffSet);

            return new ImageIcon(image);
        } catch (Exception e) {
            logger.error("failed to create a Expand icon", e);
        }

        return null;
    }

    public static Icon createCollapseIcon() {
        int size = 8;
        int xOffSet = 0;
        int yOffSet = 0;
        try {
            GraphicsEnvironment environment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2D =
                environment.createGraphics(
                    image);
            g2D.setBackground(new Color(0, 0, 0, 0));
            g2D.clearRect(0, 0, size, size);
            g2D.setStroke(new BasicStroke(1.5f));
            g2D.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2D.setColor(Color.black);
            g2D.drawLine(
                xOffSet, (size / 2) + yOffSet, size - xOffSet,
                (size / 2) + yOffSet);

            return new ImageIcon(image);
        } catch (Exception e) {
            logger.error("failed to create a Collapse icon", e);
        }

        return null;
    }

    public static Icon createCloseIcon() {
        return new CloseIcon(8, 0, 0);
    }

    public static Icon createBlankIcon() {
        return new BlankIcon(16);
    }

    /**
     * A nice and simple 'X' style icon that is used to indicate a 'close' operation.
     *
     * @author Scott Deboy &lt;sdeboy@apache.org&gt;
     */
    private static class BlankIcon implements Icon {
        int size;

        public BlankIcon(int size) {
            this.size = size;
        }

        public int getIconHeight() {
            return size;
        }

        public int getIconWidth() {
            return size;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
        }
    }

    /**
     * A nice and simple 'X' style icon that is used to indicate a 'close' operation.
     *
     * @author Scott Deboy &lt;sdeboy@apache.org&gt;
     */
    private static class CloseIcon implements Icon {
        int size;
        int xOffSet;
        int yOffSet;

        public CloseIcon(int size, int xOffSet, int yOffSet) {
            this.size = size;
            this.xOffSet = xOffSet;
            this.yOffSet = yOffSet;
        }

        public int getIconHeight() {
            return size;
        }

        public int getIconWidth() {
            return size;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2D = (Graphics2D) g;
            g2D.setStroke(new BasicStroke(1.5f));
            g2D.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2D.setColor(Color.black);
            g2D.drawLine(
                x + xOffSet, y + yOffSet, x + size + xOffSet, y + size + yOffSet);
            g2D.drawLine(
                x + xOffSet, y + size + yOffSet, x + size + xOffSet, y + yOffSet);
        }
    }
}
