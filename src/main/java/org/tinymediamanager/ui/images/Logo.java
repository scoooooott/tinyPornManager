package org.tinymediamanager.ui.images;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.MultipleGradientPaint.ColorSpaceType.SRGB;
import static java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE;

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

/**
 * This class has been automatically generated using <a href="https://flamingo.dev.java.net">Flamingo SVG transcoder</a>.
 */
public class Logo implements javax.swing.Icon {
  // default values
  private static int    DEFAULT_WIDTH  = 877;
  private static int    DEFAULT_HEIGHT = 958;

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
  public Logo(int size) {
    this.width = size;
    this.height = size;
  }

  @Override
  public int getIconHeight() {
    return height;
  }

  @Override
  public int getIconWidth() {
    return width;
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

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.drawImage(getImage(), x, y, null);
  }

  /**
   * Paints the transcoded SVG image on the specified graphics context.
   * 
   * @param g
   *          Graphics context.
   */
  private static void paint(Graphics2D g) {
    Shape shape = null;

    java.util.LinkedList<AffineTransform> transformations = new java.util.LinkedList<>();

    //
    transformations.offer(g.getTransform());
    g.transform(new AffineTransform(1.0666667f, 0, 0, 1.0666667f, 0, 0));

    // _0
    transformations.offer(g.getTransform());
    g.transform(new AffineTransform(1, 0, 0, 1, 866.58136f, 295.00067f));

    // _0_0

    // _0_0_0
    shape = new Rectangle2D.Double(-866.2532958984375, -293.96893310546875, 821.3438720703125, 897.3438720703125);
    g.setPaint(new RadialGradientPaint(new Point2D.Double(-448.5714111328125, 384.4939880371094), 514.7857f,
        new Point2D.Double(-448.5714111328125, 384.4939880371094), new float[] { 0, 1 }, new Color[] { new Color(0x040404), new Color(0x333333) },
        NO_CYCLE, SRGB, new AffineTransform(0.7985288f, 0, 0, 0.872094f, -97.38416f, -180.61191f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.6561659f, 0, 0, 4));
    g.draw(shape);

    // _0_0_1
    shape = new RoundRectangle2D.Double(-706.5104370117188, -237.91339111328125, 498.2328186035156, 245.23281860351562, 63.69683837890625,
        63.69683837890625);
    g.setPaint(new LinearGradientPaint(new Point2D.Double(-788.5, -183.63778686523438), new Point2D.Double(-113.5, -183.63778686523438),
        new float[] { 0, 1 }, new Color[] { new Color(0x167AB1), new Color(0x91D9EC) }, NO_CYCLE, SRGB,
        new AffineTransform(0.73921794f, 0, 0, 0.79621047f, -124.00672f, 30.917376f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.76718515f, 0, 0, 4));
    g.draw(shape);

    // _0_0_2
    shape = new RoundRectangle2D.Double(-706.5103759765625, 28.08660888671875, 498.2328186035156, 245.23281860351562, 63.69683837890625,
        63.69683837890625);
    g.setPaint(new LinearGradientPaint(new Point2D.Double(-788.5, 148.36221313476562), new Point2D.Double(-113.5, 148.36221313476562),
        new float[] { 0, 1 }, new Color[] { new Color(0x167AB1), new Color(0x91D9EC) }, NO_CYCLE, SRGB,
        new AffineTransform(0.7392179f, 0, 0, 0.7962104f, -124.00676f, 32.57547f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.7671851f, 0, 0, 4));
    g.draw(shape);

    // _0_0_3
    shape = new RoundRectangle2D.Double(-706.5103759765625, 294.08660888671875, 498.2328186035156, 245.23281860351562, 63.69683837890625,
        63.69683837890625);
    g.setPaint(new LinearGradientPaint(new Point2D.Double(-788.5, 482.3622131347656), new Point2D.Double(-113.5, 482.3622131347656),
        new float[] { 0, 1 }, new Color[] { new Color(0x167AB1), new Color(0x91D9EC) }, NO_CYCLE, SRGB,
        new AffineTransform(0.7392179f, 0, 0, 0.7962104f, -124.00676f, 32.64117f)));
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_4
    shape = new Rectangle2D.Double(-820.3513793945312, -237.98419189453125, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.setStroke(new BasicStroke(0.79120874f, 0, 0, 4));
    g.draw(shape);

    // _0_0_5
    shape = new Rectangle2D.Double(-820.3513793945312, 119.90274047851562, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_6
    shape = new Rectangle2D.Double(-820.3513793945312, -59.040191650390625, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_7
    shape = new Rectangle2D.Double(-820.3513793945312, 294.0548095703125, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_8
    shape = new Rectangle2D.Double(-820.3513793945312, 468.2068176269531, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_9
    shape = new Rectangle2D.Double(-161.31739807128906, -237.98419189453125, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_10
    shape = new Rectangle2D.Double(-161.31739807128906, 119.90274047851562, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_11
    shape = new Rectangle2D.Double(-161.31739807128906, -59.040191650390625, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_12
    shape = new Rectangle2D.Double(-161.31739807128906, 294.0548095703125, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    // _0_0_13
    shape = new Rectangle2D.Double(-161.31739807128906, 468.2068176269531, 71.20879364013672, 71.20879364013672);
    g.setPaint(WHITE);
    g.fill(shape);
    g.setPaint(BLACK);
    g.draw(shape);

    g.setTransform(transformations.poll()); // _0_0

    g.setTransform(transformations.poll()); // _0

  }
}
