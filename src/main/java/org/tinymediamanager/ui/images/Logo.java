/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.ui.images;

import static java.awt.MultipleGradientPaint.ColorSpaceType.SRGB;
import static java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * This class has been automatically generated using <a href="https://flamingo.dev.java.net">Flamingo SVG transcoder</a>.
 */
public class Logo implements javax.swing.Icon {
  // default values
  private static int    DEFAULT_WIDTH  = 256;
  private static int    DEFAULT_HEIGHT = 256;

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
      double coef = Math.min((double) width / (double) DEFAULT_WIDTH, (double) height / (double) DEFAULT_HEIGHT);

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

    float origAlpha = 1.0f;

    java.util.LinkedList<AffineTransform> transformations = new java.util.LinkedList<>();

    //

    // _0

    // _0_0

    // _0_0_0
    shape = new RoundRectangle2D.Double(0, 0, 256, 256, 40, 40);
    g.setPaint(new RadialGradientPaint(new Point2D.Double(0.5, 0.45517972111701965), 0.6055503f, new Point2D.Double(0.5, 0.45517972111701965),
        new float[] { 0, 1 }, new Color[] { new Color(0x494949), new Color(0x303030) }, NO_CYCLE, SRGB, new AffineTransform(256, 0, 0, 256, 0, 0)));
    g.fill(shape);
    transformations.offer(g.getTransform());
    g.transform(new AffineTransform(1, 0, 0, 1, 56, 26));

    // _0_0_1

    // _0_0_1_0
    shape = new GeneralPath();
    ((GeneralPath) shape).moveTo(152.75726, 168.80508);
    ((GeneralPath) shape).curveTo(149.3169, 168.80508, 146.5185, 165.96284, 146.5185, 162.46817);
    ((GeneralPath) shape).curveTo(146.5185, 158.9735, 149.3169, 156.13026, 152.75726, 156.13026);
    ((GeneralPath) shape).curveTo(156.19862, 156.13026, 158.99702, 158.9735, 158.99702, 162.46817);
    ((GeneralPath) shape).curveTo(158.99702, 165.96284, 156.19862, 168.80508, 152.75726, 168.80508);
    ((GeneralPath) shape).lineTo(152.75726, 168.80508);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(154.05908, 157.69853);
    ((GeneralPath) shape).lineTo(153.13786, 162.09106);
    ((GeneralPath) shape).curveTo(153.0832, 162.32552, 153.06532, 162.50908, 153.06532, 162.67368);
    ((GeneralPath) shape).curveTo(153.06532, 163.18147, 153.29787, 163.34508, 153.7987, 163.34508);
    ((GeneralPath) shape).curveTo(154.49632, 163.34508, 155.14027, 162.65373, 155.40958, 161.74489);
    ((GeneralPath) shape).lineTo(156.16086, 161.74489);
    ((GeneralPath) shape).curveTo(155.14027, 164.70784, 153.36942, 165.10788, 152.40349, 165.10788);
    ((GeneralPath) shape).curveTo(151.32924, 165.10788, 150.48953, 164.45245, 150.48953, 162.92609);
    ((GeneralPath) shape).curveTo(150.48953, 162.5809, 150.54219, 162.18086, 150.63164, 161.74489);
    ((GeneralPath) shape).lineTo(151.48328, 157.69055);
    ((GeneralPath) shape).curveTo(149.41031, 158.26219, 147.87895, 160.18462, 147.87895, 162.46817);
    ((GeneralPath) shape).curveTo(147.87895, 165.19867, 150.06818, 167.42137, 152.75726, 167.42137);
    ((GeneralPath) shape).curveTo(155.44734, 167.42137, 157.63559, 165.19867, 157.63559, 162.46817);
    ((GeneralPath) shape).curveTo(157.63559, 160.19359, 156.11813, 158.27914, 154.05908, 157.69853);
    ((GeneralPath) shape).lineTo(154.05908, 157.69853);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(74.02643, 145.14742);
    ((GeneralPath) shape).curveTo(34.298286, 145.14742, 1.9785563, 112.70167, 1.9785563, 72.81763);
    ((GeneralPath) shape).curveTo(1.9785563, 32.933586, 34.298286, 0.4888361, 74.02643, 0.4888361);
    ((GeneralPath) shape).curveTo(113.755554, 0.4888361, 146.07529, 32.933586, 146.07529, 72.81763);
    ((GeneralPath) shape).curveTo(146.07529, 112.70167, 113.755554, 145.14742, 74.02643, 145.14742);
    ((GeneralPath) shape).lineTo(74.02643, 145.14742);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(89.0559, 18.38722);
    ((GeneralPath) shape).lineTo(78.412834, 68.51986);
    ((GeneralPath) shape).curveTo(77.78479, 71.19549, 77.58206, 73.284515, 77.58206, 75.16304);
    ((GeneralPath) shape).curveTo(77.58206, 80.963234, 80.269165, 82.82679, 86.04683, 82.82679);
    ((GeneralPath) shape).curveTo(94.10812, 82.82679, 101.541374, 74.93658, 104.64684, 64.56827);
    ((GeneralPath) shape).lineTo(113.32129, 64.56827);
    ((GeneralPath) shape).curveTo(101.541374, 98.38076, 81.09298, 102.94888, 69.93913, 102.94888);
    ((GeneralPath) shape).curveTo(57.530174, 102.94888, 47.83018, 95.46471, 47.83018, 78.04817);
    ((GeneralPath) shape).curveTo(47.83018, 74.11055, 48.443325, 69.54143, 49.476826, 64.56827);
    ((GeneralPath) shape).lineTo(59.31197, 18.297434);
    ((GeneralPath) shape).curveTo(35.37551, 24.819904, 17.695707, 46.760666, 17.695707, 72.81763);
    ((GeneralPath) shape).curveTo(17.695707, 103.98542, 42.964783, 129.35202, 74.02643, 129.35202);
    ((GeneralPath) shape).curveTo(105.08807, 129.35202, 130.35814, 103.98542, 130.35814, 72.81763);
    ((GeneralPath) shape).curveTo(130.35814, 46.865417, 112.83534, 25.014442, 89.0559, 18.38722);
    ((GeneralPath) shape).lineTo(89.0559, 18.38722);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(21.64785, 170.00124);
    ((GeneralPath) shape).curveTo(19.492407, 170.00124, 17.77918, 168.26038, 17.77918, 166.0706);
    ((GeneralPath) shape).curveTo(17.77918, 163.8818, 19.492407, 162.09106, 21.64785, 162.09106);
    ((GeneralPath) shape).curveTo(23.803293, 162.09106, 25.566206, 163.8818, 25.566206, 166.0706);
    ((GeneralPath) shape).curveTo(25.566206, 168.26038, 23.803293, 170.00124, 21.64785, 170.00124);
    ((GeneralPath) shape).lineTo(21.64785, 170.00124);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(2.1077437, 172.68684);
    ((GeneralPath) shape).lineTo(3.7235813, 172.68684);
    ((GeneralPath) shape).lineTo(5.1933374, 165.82318);
    ((GeneralPath) shape).lineTo(12.44175, 164.82756);
    ((GeneralPath) shape).lineTo(10.776225, 172.68684);
    ((GeneralPath) shape).lineTo(13.715737, 172.68684);
    ((GeneralPath) shape).lineTo(13.323206, 174.6781);
    ((GeneralPath) shape).lineTo(10.384687, 174.6781);
    ((GeneralPath) shape).lineTo(7.2504, 189.60257);
    ((GeneralPath) shape).curveTo(7.103325, 190.24902, 7.0536375, 190.74684, 7.0536375, 191.19478);
    ((GeneralPath) shape).curveTo(7.0536375, 192.58746, 7.691625, 193.0354, 9.063, 193.0354);
    ((GeneralPath) shape).curveTo(10.970006, 193.0354, 12.729938, 191.14989, 13.466307, 188.66779);
    ((GeneralPath) shape).curveTo(13.4673, 188.6638, 13.4673, 188.66081, 13.468294, 188.65681);
    ((GeneralPath) shape).lineTo(16.79835, 172.68684);
    ((GeneralPath) shape).lineTo(23.851988, 172.68684);
    ((GeneralPath) shape).lineTo(20.325169, 189.60257);
    ((GeneralPath) shape).curveTo(20.178093, 190.24902, 20.1294, 190.74684, 20.1294, 191.19478);
    ((GeneralPath) shape).curveTo(20.1294, 192.58746, 20.766394, 193.0354, 22.13777, 193.0354);
    ((GeneralPath) shape).curveTo(24.039806, 193.0354, 25.79477, 191.15886, 26.535112, 188.68675);
    ((GeneralPath) shape).lineTo(29.874113, 172.68684);
    ((GeneralPath) shape).lineTo(36.926758, 172.68684);
    ((GeneralPath) shape).lineTo(36.388145, 175.27467);
    ((GeneralPath) shape).curveTo(38.445206, 172.88637, 40.60065, 172.48831, 42.1191, 172.48831);
    ((GeneralPath) shape).curveTo(45.253387, 172.48831, 47.554913, 174.1304, 47.554913, 178.11093);
    ((GeneralPath) shape).curveTo(47.554913, 182.14034, 45.2037, 188.159, 45.2037, 190.94537);
    ((GeneralPath) shape).curveTo(45.2037, 192.1894, 45.694614, 193.0354, 47.163376, 193.0354);
    ((GeneralPath) shape).curveTo(49.31385, 193.0354, 50.1963, 191.25163, 51.270542, 188.67477);
    ((GeneralPath) shape).curveTo(51.271538, 188.66779, 51.271538, 188.66281, 51.273525, 188.65681);
    ((GeneralPath) shape).lineTo(54.60358, 172.68684);
    ((GeneralPath) shape).lineTo(61.656223, 172.68684);
    ((GeneralPath) shape).lineTo(58.1304, 189.60257);
    ((GeneralPath) shape).curveTo(58.03202, 189.99962, 57.983326, 190.44756, 57.983326, 190.8466);
    ((GeneralPath) shape).curveTo(57.983326, 191.98988, 58.326168, 193.0354, 59.453083, 193.0354);
    ((GeneralPath) shape).curveTo(61.363068, 193.0354, 62.636063, 191.14389, 63.370445, 188.65681);
    ((GeneralPath) shape).lineTo(66.70149, 172.68684);
    ((GeneralPath) shape).lineTo(73.803825, 172.68684);
    ((GeneralPath) shape).lineTo(67.827415, 201.54114);
    ((GeneralPath) shape).curveTo(66.35865, 208.6562, 62.146145, 209.99901, 58.91447, 209.99901);
    ((GeneralPath) shape).curveTo(55.92626, 209.99901, 53.133823, 207.8611, 53.133823, 204.6268);
    ((GeneralPath) shape).curveTo(53.133823, 199.99982, 56.954792, 198.55725, 61.607533, 197.16457);
    ((GeneralPath) shape).lineTo(62.09745, 194.97478);
    ((GeneralPath) shape).curveTo(59.991695, 197.41296, 57.787556, 197.8609, 56.122032, 197.8609);
    ((GeneralPath) shape).curveTo(53.634674, 197.8609, 51.620342, 196.559, 51.0549, 193.63098);
    ((GeneralPath) shape).curveTo(48.732506, 197.2773, 46.153725, 197.8609, 44.224857, 197.8609);
    ((GeneralPath) shape).curveTo(39.963657, 197.8609, 38.445206, 195.07454, 38.445206, 192.2383);
    ((GeneralPath) shape).curveTo(38.445206, 188.85535, 40.649345, 182.73691, 40.649345, 179.55249);
    ((GeneralPath) shape).curveTo(40.649345, 177.9104, 40.061043, 177.01553, 38.886433, 177.01553);
    ((GeneralPath) shape).curveTo(37.12352, 177.01553, 35.80084, 179.10556, 35.065464, 181.59264);
    ((GeneralPath) shape).lineTo(31.735407, 197.5626);
    ((GeneralPath) shape).lineTo(24.682762, 197.5626);
    ((GeneralPath) shape).lineTo(25.2969, 194.61763);
    ((GeneralPath) shape).curveTo(22.833393, 197.36009, 20.08468, 197.8609, 18.317795, 197.8609);
    ((GeneralPath) shape).curveTo(15.760875, 197.8609, 13.688907, 196.50114, 13.193025, 193.39653);
    ((GeneralPath) shape).curveTo(10.512881, 197.22641, 7.2454314, 197.8609, 5.242031, 197.8609);
    ((GeneralPath) shape).curveTo(2.3035126, 197.8609, 0.0019875, 196.07016, 0.0019875, 191.89111);
    ((GeneralPath) shape).curveTo(0.0019875, 190.94537, 0.14806876, 189.85097, 0.393525, 188.65681);
    ((GeneralPath) shape).lineTo(3.3320436, 174.6781);
    ((GeneralPath) shape).lineTo(1.7152125, 174.6781);
    ((GeneralPath) shape).lineTo(2.1077437, 172.68684);
    ((GeneralPath) shape).lineTo(2.1077437, 172.68684);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(55.82788, 203.93045);
    ((GeneralPath) shape).curveTo(55.82788, 204.6268, 56.70934, 205.57254, 57.787556, 205.57254);
    ((GeneralPath) shape).curveTo(58.7664, 205.57254, 60.040386, 204.6268, 60.67738, 201.69179);
    ((GeneralPath) shape).lineTo(61.264687, 198.75677);
    ((GeneralPath) shape).curveTo(58.179092, 199.8003, 55.82788, 201.24385, 55.82788, 203.93045);
    ((GeneralPath) shape).lineTo(55.82788, 203.93045);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(82.34511, 160.25046);
    ((GeneralPath) shape).lineTo(79.748436, 172.68684);
    ((GeneralPath) shape).lineTo(85.749695, 172.68684);
    ((GeneralPath) shape).lineTo(85.21108, 175.27467);
    ((GeneralPath) shape).curveTo(87.26814, 172.88637, 89.47228, 172.43843, 91.1865, 172.43843);
    ((GeneralPath) shape).curveTo(93.78217, 172.43843, 95.69216, 173.63258, 96.231766, 176.71825);
    ((GeneralPath) shape).curveTo(98.581985, 173.03601, 101.22735, 172.48831, 103.038956, 172.48831);
    ((GeneralPath) shape).curveTo(106.17424, 172.48831, 108.47576, 174.1304, 108.47576, 178.11093);
    ((GeneralPath) shape).curveTo(108.47576, 182.14034, 106.12455, 188.159, 106.12455, 190.94537);
    ((GeneralPath) shape).curveTo(106.12455, 192.1894, 106.61447, 193.0354, 108.08423, 193.0354);
    ((GeneralPath) shape).curveTo(110.22874, 193.0354, 111.11218, 191.26062, 112.18245, 188.69473);
    ((GeneralPath) shape).lineTo(115.522446, 172.68684);
    ((GeneralPath) shape).lineTo(122.57509, 172.68684);
    ((GeneralPath) shape).lineTo(122.03747, 175.27467);
    ((GeneralPath) shape).curveTo(124.09453, 172.88637, 126.297676, 172.43843, 128.0129, 172.43843);
    ((GeneralPath) shape).curveTo(130.60757, 172.43843, 132.51855, 173.63258, 133.05716, 176.71825);
    ((GeneralPath) shape).curveTo(135.40837, 173.03601, 138.05275, 172.48831, 139.86534, 172.48831);
    ((GeneralPath) shape).curveTo(142.99963, 172.48831, 145.30215, 174.1304, 145.30215, 178.11093);
    ((GeneralPath) shape).curveTo(145.30215, 182.14034, 142.95094, 188.159, 142.95094, 190.94537);
    ((GeneralPath) shape).curveTo(142.95094, 192.1894, 143.44086, 193.0354, 144.90962, 193.0354);
    ((GeneralPath) shape).curveTo(145.74239, 193.0354, 146.2323, 192.93463, 146.52644, 192.7361);
    ((GeneralPath) shape).curveTo(146.08522, 196.31758, 143.73401, 197.8609, 141.13834, 197.8609);
    ((GeneralPath) shape).curveTo(137.36705, 197.8609, 136.19244, 195.07454, 136.19244, 192.2383);
    ((GeneralPath) shape).curveTo(136.19244, 188.85535, 138.39558, 182.73691, 138.39558, 179.55249);
    ((GeneralPath) shape).curveTo(138.39558, 177.9104, 137.80829, 177.01553, 136.63268, 177.01553);
    ((GeneralPath) shape).curveTo(134.86877, 177.01553, 133.54709, 179.10556, 132.8127, 181.59264);
    ((GeneralPath) shape).lineTo(129.48164, 197.5626);
    ((GeneralPath) shape).lineTo(122.42901, 197.5626);
    ((GeneralPath) shape).lineTo(126.00452, 180.39848);
    ((GeneralPath) shape).curveTo(126.1029, 180.00043, 126.1506, 179.60237, 126.1506, 179.15544);
    ((GeneralPath) shape).curveTo(126.1506, 178.06105, 125.75906, 176.96666, 124.68184, 176.96666);
    ((GeneralPath) shape).curveTo(122.77086, 176.96666, 121.44917, 179.10556, 120.71479, 181.59264);
    ((GeneralPath) shape).lineTo(117.384735, 197.5626);
    ((GeneralPath) shape).lineTo(110.33109, 197.5626);
    ((GeneralPath) shape).lineTo(110.828964, 195.17929);
    ((GeneralPath) shape).curveTo(108.83351, 197.45387, 106.76154, 197.8609, 105.145706, 197.8609);
    ((GeneralPath) shape).curveTo(100.88451, 197.8609, 99.36606, 195.07454, 99.36606, 192.2383);
    ((GeneralPath) shape).curveTo(99.36606, 188.85535, 101.5702, 182.73691, 101.5702, 179.55249);
    ((GeneralPath) shape).curveTo(101.5702, 177.9104, 100.981895, 177.01553, 99.80728, 177.01553);
    ((GeneralPath) shape).curveTo(98.04337, 177.01553, 96.720695, 179.10556, 95.98631, 181.59264);
    ((GeneralPath) shape).lineTo(92.65626, 197.5626);
    ((GeneralPath) shape).lineTo(85.602615, 197.5626);
    ((GeneralPath) shape).lineTo(89.17813, 180.39848);
    ((GeneralPath) shape).curveTo(89.27651, 180.00043, 89.3252, 179.60237, 89.3252, 179.15544);
    ((GeneralPath) shape).curveTo(89.3252, 178.06105, 88.93367, 176.96666, 87.85545, 176.96666);
    ((GeneralPath) shape).curveTo(85.945465, 176.96666, 84.62278, 179.10556, 83.8884, 181.59264);
    ((GeneralPath) shape).lineTo(80.55834, 197.5626);
    ((GeneralPath) shape).lineTo(74.55709, 197.5626);
    ((GeneralPath) shape).lineTo(71.96141, 209.99901);
    ((GeneralPath) shape).lineTo(70.469795, 209.99901);
    ((GeneralPath) shape).lineTo(80.853485, 160.25046);
    ((GeneralPath) shape).lineTo(82.34511, 160.25046);
    ((GeneralPath) shape).lineTo(82.34511, 160.25046);
    ((GeneralPath) shape).closePath();

    g.setPaint(new Color(0xFF7D00));
    g.fill(shape);

    g.setTransform(transformations.poll()); // _0_0_1
  }
}
