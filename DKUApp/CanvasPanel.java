import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * FRONT-END. Draws the campus map and the current route on top of it.
 *
 * It reads the model and paints it, and forwards clicks to the model as
 * selections. It never computes a route itself.
 *
 * This class is also the only place that knows the map is stored one way up and
 * drawn the other. The data keeps the map's own coordinates -- origin at the
 * bottom-left, +y upwards -- while Swing counts down from the top-left corner.
 * Both directions of that conversion are the two methods sx() and sy() below,
 * and nothing else in the program does it.
 */
public class CanvasPanel extends JPanel {

    /** The campus map, cropped to the region the coordinates cover and scaled
     *  to twice the canvas size. */
    private static final String MAP_IMAGE = "campus-map.jpg";

    /** Washes the map back so the route on top of it stands out. */
    private static final Color MAP_VEIL = new Color(255, 255, 255, 78);

    private static final Color BACKGROUND = new Color(250, 250, 252);
    private static final Color GRAPH_EDGE = new Color(40, 55, 85, 45);
    private static final Color ROUTE      = new Color(214, 45, 45);
    private static final Color PLACE      = new Color(52, 92, 196);
    private static final Color WAYPOINT   = new Color(96, 108, 132);
    private static final Color LABEL      = new Color(28, 32, 42);
    private static final Color LABEL_HALO = new Color(255, 255, 255, 210);
    private static final Color START      = new Color(22, 148, 82);
    private static final Color END        = new Color(126, 12, 12);

    private final Model model;
    private final BufferedImage map = loadMap();

    /**
     * @param onChange run after a click has changed the model, so the window
     *                 can refresh whatever else it is showing.
     */
    public CanvasPanel(Model model, Runnable onChange) {
        this.model = model;

        // The view window decides how much room the canvas needs; it is sized
        // for the part of the map that has anything on it, not the whole page.
        setPreferredSize(new Dimension(CampusData.CANVAS_WIDTH, CampusData.CANVAS_HEIGHT));
        setBackground(BACKGROUND);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Back to map units before the model sees it -- the model has
                // never heard of screen pixels.
                model.clickAt(mapX(e.getX()), mapY(e.getY()));
                onChange.run();
            }
        });
    }

    // ---- the projection, and its inverse ------------------------------------

    // Not private: the projection is checked on its own, outside the drawing
    // code, and a test that has to re-derive the formula cannot catch a mistake
    // in it. See CanvasPanel's tx/ty pair in the test folder.
    static int sx(double x) {
        return (int) Math.round((x - CampusData.VIEW_LEFT) * CampusData.VIEW_SCALE);
    }

    /**
     * The y flip. The map's y counts up from the bottom of the page, the
     * screen's down from the top, so the distance to measure is the one from
     * the TOP edge of the view -- which is VIEW_TOP, a value of its own, and
     * not the top of the page, which the view stops short of.
     */
    static int sy(double y) {
        return (int) Math.round((CampusData.VIEW_TOP - y) * CampusData.VIEW_SCALE);
    }

    private static double mapX(int screenX) {
        return screenX / CampusData.VIEW_SCALE + CampusData.VIEW_LEFT;
    }

    private static double mapY(int screenY) {
        return CampusData.VIEW_TOP - screenY / CampusData.VIEW_SCALE;
    }

    /**
     * Read the map out of the project folder. Loaded from the classpath rather
     * than a file path so it is found however the program is launched.
     */
    private static BufferedImage loadMap() {
        try (InputStream in = CanvasPanel.class.getClassLoader().getResourceAsStream(MAP_IMAGE)) {
            if (in == null) {
                System.err.println("WARNING: " + MAP_IMAGE + " not found. "
                        + "Drawing the map without its background.");
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            System.err.println("WARNING: could not read " + MAP_IMAGE + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawMap(g2);
        drawEdges(g2);
        drawRoute(g2);
        drawNodes(g2);
    }

    private void drawMap(Graphics2D g2) {
        if (map == null) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        g2.drawImage(map, 0, 0, w, h, null);
        g2.setColor(MAP_VEIL);
        g2.fillRect(0, 0, w, h);
    }

    /** The road network we actually search, drawn faintly over the real roads. */
    private void drawEdges(Graphics2D g2) {
        g2.setColor(GRAPH_EDGE);
        g2.setStroke(new BasicStroke(1.5f));
        for (Node[] road : model.getCampus().getRoadSegments()) {
            g2.drawLine(sx(road[0].getX()), sy(road[0].getY()),
                        sx(road[1].getX()), sy(road[1].getY()));
        }
    }

    /** The current route, drawn on top of everything else. */
    private void drawRoute(Graphics2D g2) {
        List<Node> route = model.getRoute();
        if (route.size() < 2) {
            return;
        }
        g2.setColor(ROUTE);
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i + 1 < route.size(); i++) {
            Node a = route.get(i);
            Node b = route.get(i + 1);
            g2.drawLine(sx(a.getX()), sy(a.getY()), sx(b.getX()), sy(b.getY()));
        }
    }

    /**
     * Every place gets a marker and a name; the bends in between get a marker
     * only when it has been asked for.
     *
     * They are worth being able to see, because they are what the route is
     * actually made of -- the red line turns at them, and without them it looks
     * as though it were being drawn through open ground. They are left off by
     * default and drawn small and grey when on, so that turning them on shows
     * the machinery without burying the answer.
     */
    private void drawNodes(Graphics2D g2) {
        g2.setFont(getFont().deriveFont(Font.BOLD, 11f));

        if (model.isShowingWaypoints()) {
            for (Node n : model.getCampus().getNodes()) {
                if (!n.isSelectable()) {
                    int x = sx(n.getX());
                    int y = sy(n.getY());
                    // Ringed like the named places, only smaller. A dot without
                    // the ring disappears into the road lines it sits on.
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - 5, y - 5, 10, 10);
                    g2.setColor(WAYPOINT);
                    g2.fillOval(x - 3, y - 3, 6, 6);
                }
            }
        }

        for (Node n : model.getCampus().getNodes()) {
            if (!n.isSelectable()) {
                continue;
            }
            int x = sx(n.getX());
            int y = sy(n.getY());
            boolean terminal = n.equals(model.getStart()) || n.equals(model.getEnd());
            int r = terminal ? 6 : 4;

            g2.setColor(Color.WHITE);
            g2.fillOval(x - r - 2, y - r - 2, 2 * r + 4, 2 * r + 4);
            g2.setColor(colorOf(n));
            g2.fillOval(x - r, y - r, 2 * r, 2 * r);
        }

        // Labels last, so they sit above every marker.
        for (Node n : model.getCampus().getNodes()) {
            if (n.isSelectable()) {
                drawLabel(g2, n.getName(), sx(n.getX()), sy(n.getY()) - 8);
            }
        }
    }

    /**
     * Draw a name centred at x, with a pale outline. The outline is what keeps
     * the text readable where it crosses the dark parts of the map.
     */
    private void drawLabel(Graphics2D g2, String text, int cx, int y) {
        int x = cx - g2.getFontMetrics().stringWidth(text) / 2;
        g2.setColor(LABEL_HALO);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g2.drawString(text, x + dx, y + dy);
                }
            }
        }
        g2.setColor(LABEL);
        g2.drawString(text, x, y);
    }

    private Color colorOf(Node n) {
        if (n.equals(model.getStart())) {
            return START;
        }
        if (n.equals(model.getEnd())) {
            return END;
        }
        return PLACE;
    }
}
