package org.tinymediamanager.ui.images;

import static java.awt.Color.*;
import static java.awt.MultipleGradientPaint.ColorSpaceType.*;
import static java.awt.MultipleGradientPaint.CycleMethod.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.Icon;

/**
 * This class has been automatically generated using <a href="https://flamingo.dev.java.net">Flamingo SVG transcoder</a>.
 */
public class Logo implements Icon {
  // default values
  private static int    DEFAULT_WIDTH  = 878;
  private static int    DEFAULT_HEIGHT = 959;

  /** The width of this icon. */
  private int           width;

  /** The height of this icon. */
  private int           height;

  /** The rendered image. */
  private BufferedImage image;

  /**
   * Creates a new transcoded SVG image.
   */
  public Logo() {
    width = DEFAULT_WIDTH;
    height = DEFAULT_HEIGHT;
  }

  /**
   * Creates a new transcoded SVG image.
   */
  public Logo(int width) {
    this.width = width;
    this.height = DEFAULT_HEIGHT * width / DEFAULT_WIDTH;
  }

  @Override
  public int getIconHeight() {
    return height;
  }

  @Override
  public int getIconWidth() {
    return width;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.drawImage(getImage(), x, y, null);
  }

  public Image getImage() {
    if (image == null) {
      image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      double coef = Math.min((double) width / (double) 878, (double) height / (double) 959);

      Graphics2D g2d = image.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.scale(coef, coef);
      paint(g2d);
      g2d.dispose();
    }
    return image;
  }

  /**
   * Paints the transcoded SVG image on the specified graphics context.
   * 
   * @param g
   *          Graphics context.
   */
  private static void paint(Graphics2D g) {
    Shape shape = null;

    LinkedList<AffineTransform> transformations = new LinkedList<AffineTransform>();

    //
    transformations.offer(g.getTransform());
    g.transform(new AffineTransform(1.0666667f, 0, 0, 1.0666667f, 0, 0));

    // _0
    transformations.offer(g.getTransform());
    g.transform(new AffineTransform(1, 0, 0, 1, 866.58136f, 295.00067f));

    // _0_0

    // _0_0_0
    shape = new Rectangle2D.Double(-866.2530517578125, -294.67236328125, 821.8303833007812, 898.0470581054688);
    g.setPaint(new RadialGradientPaint(new Point2D.Double(-448.5714111328125, 384.4939880371094), 514.7857f,
        new Point2D.Double(-448.5714111328125, 384.4939880371094), new float[] { 0, 1 }, new Color[] { new Color(0x040404), new Color(0x333333) },
        NO_CYCLE, SRGB, new AffineTransform(0.7990018f, 0, 0, 0.8727774f, -96.928505f, -181.22652f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.6566173f, 0, 0, 4));
    g.draw(shape);

    // _0_0_1
    shape = new RoundRectangle2D.Double(-706.50927734375, -238.6955108642578, 499.4635314941406, 246.079345703125, 63.916717529296875,
        63.916717529296875);
    g.setPaint(new LinearGradientPaint(new Point2D.Double(-788.5, -183.63778686523438), new Point2D.Double(-113.5, -183.63778686523438),
        new float[] { 0, 1 }, new Color[] { new Color(0x167AB1), new Color(0x91D9EC) }, NO_CYCLE, SRGB,
        new AffineTransform(0.74104387f, 0, 0, 0.7989589f, -122.56673f, 31.063196f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.7694567f, 0, 0, 4));
    g.draw(shape);

    // _0_0_2
    shape = new RoundRectangle2D.Double(-706.5096435546875, 26.526756286621094, 498.4643249511719, 246.0801239013672, 63.91691589355469,
        63.91691589355469);
    g.setPaint(new LinearGradientPaint(new Point2D.Double(-788.5, 148.36221313476562), new Point2D.Double(-113.5, 148.36221313476562),
        new float[] { 0, 1 }, new Color[] { new Color(0x167AB1), new Color(0x91D9EC) }, NO_CYCLE, SRGB,
        new AffineTransform(0.7395613f, 0, 0, 0.7989614f, -123.73537f, 31.031128f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.7686878f, 0, 0, 4));
    g.draw(shape);

    // _0_0_3
    shape = new RoundRectangle2D.Double(-706.5096435546875, 293.34716796875, 498.4643249511719, 246.0801239013672, 63.91691589355469,
        63.91691589355469);
    g.setPaint(new LinearGradientPaint(new Point2D.Double(-788.5, 482.3622131347656), new Point2D.Double(-113.5, 482.3622131347656),
        new float[] { 0, 1 }, new Color[] { new Color(0x167AB1), new Color(0x91D9EC) }, NO_CYCLE, SRGB,
        new AffineTransform(0.7395613f, 0, 0, 0.7989614f, -123.73537f, 30.998405f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_4
    shape = new Rectangle2D.Double(-820.3475952148438, -238.67694091796875, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.7988634f, 0, 0, 4));
    g.draw(shape);

    // _0_0_5
    shape = new Rectangle2D.Double(-820.3475952148438, 119.20999908447266, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_6
    shape = new Rectangle2D.Double(-820.3475952148438, -59.732940673828125, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_7
    shape = new Rectangle2D.Double(-820.3475952148438, 293.362060546875, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_8
    shape = new Rectangle2D.Double(-820.3475952148438, 467.5140686035156, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_9
    shape = new Rectangle2D.Double(-161.31356811523438, -238.67694091796875, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_10
    shape = new Rectangle2D.Double(-161.31356811523438, 119.20999908447266, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_11
    shape = new Rectangle2D.Double(-161.31356811523438, -59.732940673828125, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_12
    shape = new Rectangle2D.Double(-161.31356811523438, 293.362060546875, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_13
    shape = new Rectangle2D.Double(-161.31356811523438, 467.5140686035156, 71.89771270751953, 71.89771270751953);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    g.setTransform(transformations.poll()); // _0_0

    g.setTransform(transformations.poll()); // _0

  }
}
