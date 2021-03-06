package com.gorlah.kappabot.meme;

import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public abstract class MemeTemplate {

    private BufferedImage template;

    public abstract String getName();

    public abstract String getFilename();

    protected MemeTemplate() {
        try {
            InputStream inputStream = new ClassPathResource(getFilename()).getInputStream();
            this.template = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read: " + getFilename());
        }
    }

    protected void writeText(String text, int x, int y, int width, int height) {
        writeText(text, Color.BLACK, x, y, width, height);
    }

    protected void writeText(String text, Color color, int x, int y, int width, int height) {
        writeText(text, new Font("Arial", Font.PLAIN, 24), color, x, y, width, height);
    }

    protected void writeText(String text, Font font, Color color, int x, int y, int width, int height) {
        Graphics2D g = getGraphics2D();
        g.setColor(color);
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        while (!textFits(g, text, x, y, width, height)) {
            g.setFont(g.getFont().deriveFont((float) g.getFont().getSize() - 1));
        }
    }

    protected void drawImage(BufferedImage image, int x, int y, int width, int height) {
        Graphics2D g = getGraphics2D();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int widthToDraw = image.getWidth();
        int heightToDraw = image.getHeight();

        if (image.getWidth() > width || image.getHeight() > height) {
            double areaAspectRatio = height / (double) width;
            double imageAspectRatio = image.getHeight() / (double) image.getWidth();

            double scale;

            if (areaAspectRatio < imageAspectRatio) {
                scale = height / (double) image.getHeight();
            } else {
                scale = width / (double) image.getWidth();
            }

            widthToDraw = (int) Math.round(image.getWidth() * scale);
            heightToDraw = (int) Math.round(image.getHeight() * scale);
        }

        int xToDraw = x + ((width - widthToDraw) / 2);
        int yToDraw = y + ((height - heightToDraw) / 2);

        g.drawImage(image, xToDraw, yToDraw, widthToDraw, heightToDraw, null);
    }

    public BufferedImage getImage() {
        BufferedImage copiedImage = new BufferedImage(template.getWidth(), template.getHeight(), template.getType());
        Graphics2D copiedImageGraphics = copiedImage.createGraphics();

        copiedImageGraphics.drawImage(template, 0, 0, null);
        copiedImageGraphics.dispose();

        return copiedImage;
    }

    private Graphics2D getGraphics2D() {
        return (Graphics2D) template.getGraphics();
    }

    private boolean textFits(Graphics2D g, String text, int x, int y, int width, int height) {
        Font font = g.getFont();
        FontRenderContext frc = g.getFontRenderContext();
        FontMetrics fontMetrics = g.getFontMetrics();

        java.util.List<String> textList;

        try {
            textList = wrap(text, fontMetrics, width);
        } catch (Exception e) {
            return false;
        }

        double maxWidth = 0;
        double maxHeight = 0;

        for (String line : textList) {
            Rectangle2D rect = font.getStringBounds(line, frc);

            if (rect.getWidth() > maxWidth) {
                maxWidth = rect.getWidth();
            }

            maxHeight += rect.getHeight();
        }

        if (maxWidth <= width && maxHeight <= height) {
            y = (int) (y + ((height - maxHeight) / 2));

            for (String line : textList) {
                Rectangle2D rect = font.getStringBounds(line, frc);

                double stringWidth = rect.getWidth();

                Color fillColor = g.getColor();
                Color outlineColor = Color.WHITE.equals(fillColor) ? Color.BLACK : Color.WHITE;
                BasicStroke outlineStroke = new BasicStroke(font.getSize() * .125f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND);

                Color originalColor = g.getColor();
                Stroke originalStroke = g.getStroke();
                RenderingHints originalHints = g.getRenderingHints();

                GlyphVector glyphVector = g.getFont().createGlyphVector(g.getFontRenderContext(), line);

                AffineTransform transform = new AffineTransform();
                transform.translate(x + ((width - stringWidth) / 2), y);

                y += g.getFontMetrics().getHeight();

                Shape textShape = transform.createTransformedShape(glyphVector.getOutline());

                g.setColor(outlineColor);
                g.setStroke(outlineStroke);
                g.draw(textShape); // draw outline

                g.setColor(fillColor);
                g.fill(textShape); // fill the shape

                // reset to original settings after painting
                g.setColor(originalColor);
                g.setStroke(originalStroke);
                g.setRenderingHints(originalHints);
            }

            return true;
        } else {
            return false;
        }
    }

    private List<String> wrap(String text, FontMetrics fontMetrics, int maxWidth) throws Exception {
        ArrayList<String> returnList = new ArrayList<>();
        List<String> textSplit = Arrays.asList(text.split("\\s"));
        ListIterator iterator = textSplit.listIterator();
        StringBuilder stringToAdd = new StringBuilder();

        while (iterator.hasNext()) {
            String word = (String) iterator.next();

            int lengthOfWord = fontMetrics.stringWidth(word);

            if (lengthOfWord > maxWidth) {
                throw new Exception("Font size is too large to fit in specified width.");
            } else {
                if (stringToAdd.length() == 0) {
                    stringToAdd.append(word);

                    if (!iterator.hasNext()) {
                        returnList.add(stringToAdd.toString());
                    }
                } else {
                    int lengthOfSpace = fontMetrics.stringWidth(" ");
                    int lengthOfStringToAdd = fontMetrics.stringWidth(stringToAdd.toString());

                    if (lengthOfStringToAdd + lengthOfSpace + lengthOfWord < maxWidth) {
                        stringToAdd.append(" ").append(word);

                        if (!iterator.hasNext()) {
                            returnList.add(stringToAdd.toString());
                        }
                    } else {
                        returnList.add(stringToAdd.toString());
                        stringToAdd = new StringBuilder(word);
                    }
                }
            }
        }

        return returnList;
    }
}
