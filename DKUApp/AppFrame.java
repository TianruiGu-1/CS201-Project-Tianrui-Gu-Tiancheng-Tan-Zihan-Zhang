import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * FRONT-END. The application window.
 *
 * It owns the canvas and the controls. Every interaction it collects is turned
 * into a method call on the Model -- that is the only way the two layers talk.
 */
public class AppFrame extends JFrame {

    private final Model model;
    private final CanvasPanel canvas;

    private final JLabel fromLabel = new JLabel();
    private final JLabel toLabel = new JLabel();
    private final JLabel distanceLabel = new JLabel();
    private final JLabel stepsLabel = new JLabel();

    public AppFrame(Model model) {
        super("DKU Campus Navigator");
        this.model = model;
        this.canvas = new CanvasPanel(model, this::refresh);

        add(canvas, BorderLayout.CENTER);
        add(controls(), BorderLayout.EAST);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // The map art is scaled to the canvas, and the building positions are
        // fixed pixel values. Resizing would stretch one without the other.
        setResizable(false);
        pack();
        setLocationRelativeTo(null);   // centre on screen
        refresh();
    }

    /**
     * Put the current model state on screen: the side panel text, and the map.
     * Everything that changes the model ends up calling this, so this is the
     * one place that has to remember the canvas needs redrawing.
     */
    private void refresh() {
        canvas.repaint();

        fromLabel.setText("From:  " + nameOf(model.getStart()));
        toLabel.setText("To:    " + nameOf(model.getEnd()));

        if (model.getRoute().size() >= 2) {
            // The route is measured in raw map units all the way through the back
            // end; metres are only how it is shown. METRES_PER_UNIT is the whole
            // of the conversion, and it happens here and nowhere else.
            distanceLabel.setText(String.format("Distance: %.0f m",
                    model.getRouteLength() * CampusData.METRES_PER_UNIT));
            // Only the real destinations are listed. The bends in between are
            // corners the walker turns at, not places anyone would name.
            stepsLabel.setText("<html><body style='width:160px'>"
                    + model.getPublicStops().stream()
                            .map(Node::getName)
                            .collect(Collectors.joining("<br>"))
                    + "</body></html>");
        } else {
            distanceLabel.setText("Distance: -");
            stepsLabel.setText("<html><body style='width:160px'><i>"
                    + "Click a highlighted entrance to set the start, "
                    + "then another one for the destination."
                    + "</i></body></html>");
        }
    }

    private static String nameOf(Node n) {
        return n == null ? "-" : n.getName();
    }

    /**
     * Controls live here. Front-end -> back-end calls go through the Model;
     * nothing in this panel holds application state of its own.
     */
    private JPanel controls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Campus Navigator");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(18));

        fromLabel.setAlignmentX(LEFT_ALIGNMENT);
        toLabel.setAlignmentX(LEFT_ALIGNMENT);
        distanceLabel.setAlignmentX(LEFT_ALIGNMENT);
        distanceLabel.setFont(distanceLabel.getFont().deriveFont(Font.BOLD));
        stepsLabel.setAlignmentX(LEFT_ALIGNMENT);
        stepsLabel.setVerticalAlignment(JLabel.TOP);

        panel.add(fromLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(toLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(distanceLabel);
        panel.add(Box.createVerticalStrut(18));

        JLabel stepsHeading = new JLabel("Route");
        stepsHeading.setFont(stepsHeading.getFont().deriveFont(Font.BOLD));
        stepsHeading.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(stepsHeading);
        panel.add(Box.createVerticalStrut(6));
        panel.add(stepsLabel);

        panel.add(Box.createVerticalGlue());

        JCheckBox showWaypoints = new JCheckBox("Show waypoints");
        showWaypoints.setAlignmentX(LEFT_ALIGNMENT);
        showWaypoints.setBackground(Color.WHITE);
        showWaypoints.setToolTipText(
                "Draw the bends the route turns at, not just the named places.");
        showWaypoints.addActionListener(e -> {
            model.setShowingWaypoints(showWaypoints.isSelected());
            refresh();
        });
        panel.add(showWaypoints);
        panel.add(Box.createVerticalStrut(10));

        JButton reset = new JButton("Reset");
        reset.setAlignmentX(LEFT_ALIGNMENT);
        reset.addActionListener(e -> {
            model.reset();
            refresh();
        });
        panel.add(reset);

        return panel;
    }
}
