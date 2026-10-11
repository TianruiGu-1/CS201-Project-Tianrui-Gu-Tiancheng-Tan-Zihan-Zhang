import java.util.ArrayList;
import java.util.List;

/**
 * BACK-END. Every place on the DKU campus, and every path between them.
 *
 * GENERATED FILE -- do not hand-edit. It is written by
 * tools/make_campus_data.py from the exported campus graph JSON, so the
 * table below and the JSON can never disagree. Re-run the script to
 * refresh it.
 *
 * Coordinates are in raw map units: origin at the BOTTOM-LEFT of the map
 * page, +y pointing UP. Swing draws the other way up, so CanvasPanel
 * flips y when it paints; nothing on the back end does.
 *
 * A node is either a real destination (a building entrance, a gate --
 * what the JSON calls "public") or a bare bend in a path (a waypoint).
 * Only the real destinations can be clicked, and only they are named on
 * the map. A gate additionally charges a flat equivalent distance the
 * moment a route passes INTO it, which CampusGraph folds into the road
 * weight.
 */
public class CampusData {

    /** The raw coordinate space, as exported. */
    public static final int MAP_WIDTH  = 6229;
    public static final int MAP_HEIGHT = 3494;

    /**
     * Metres per raw map unit, so that a distance can be shown to a person.
     *
     * The map sheet is 8305 x 4658 and one of its pixels measures 0.163 m
     * on the ground; these coordinates are that sheet scaled by 0.75, so
     * one raw unit is 0.163 / 0.75 metres.
     *
     * The back end never uses this. Every distance it compares is in raw
     * units; this exists only for the one number the window prints.
     */
    public static final double METRES_PER_UNIT = 0.21733333333333335;

    /**
     * The window of the map the view shows, and how much it is shrunk.
     *
     * The nodes only cover part of the page -- the rest is margin and the
     * legend -- so the view is cropped to them first. CanvasPanel maps a
     * raw coordinate to a canvas pixel with
     *
     *     screenX = (x - VIEW_LEFT) * VIEW_SCALE
     *     screenY = (VIEW_TOP  - y) * VIEW_SCALE
     *
     * The second line is the whole of the y flip, and it lives only here.
     *
     * Note that VIEW_TOP is NOT MAP_HEIGHT: the crop stops short of the
     * top of the page, because nothing is drawn up there. Measuring the
     * view from the map's own edges instead of its own is an easy way to
     * shift every marker up the screen by the size of the margin.
     */
    public static final double VIEW_LEFT   = 1889.01;
    public static final double VIEW_BOTTOM = 693.19;
    public static final double VIEW_TOP    = 3343.85;
    public static final double VIEW_SCALE  = 0.294265;
    public static final int CANVAS_WIDTH  = 1241;
    public static final int CANVAS_HEIGHT = 780;

    /**
     * Build the campus road network.
     *
     * Roads are added by naming their two ends only. How long each one is
     * is worked out from the coordinates, so the map can never disagree
     * with itself about how far apart two places are.
     */
    public static CampusGraph build() {
        CampusGraph g = new CampusGraph();

        // ---- 150 nodes ----
        // Node(name, mapX, mapY, selectable, gateCost)
        g.addNode(new Node("节点 8", 2585.62, 2442.85, false, 0));
        g.addNode(new Node("Residence Hall", 2977.36, 2435.38, true, 0));
        g.addNode(new Node("节点 10", 2981.19, 2139.40, false, 0));
        g.addNode(new Node("Building E - North Entrance", 4384.63, 2420.94, true, 100));
        g.addNode(new Node("节点 2", 4702.64, 2506.85, false, 0));
        g.addNode(new Node("节点 4", 4356.78, 2483.61, false, 0));
        g.addNode(new Node("节点 5", 4382.31, 2296.77, false, 0));
        g.addNode(new Node("节点 6", 4597.41, 2301.03, false, 0));
        g.addNode(new Node("节点 13", 4162.57, 2294.44, false, 0));
        g.addNode(new Node("节点 14", 4160.25, 2476.27, false, 0));
        g.addNode(new Node("节点 15", 5001.30, 2303.77, false, 0));
        g.addNode(new Node("Building A - North Entrance", 5015.23, 2447.28, true, 100));
        g.addNode(new Node("Building A - South Entrance", 5005.98, 2367.61, true, 100));
        g.addNode(new Node("Building B - South Entrance", 5051.60, 2581.53, true, 100));
        g.addNode(new Node("节点 19", 4990.47, 2151.35, false, 0));
        g.addNode(new Node("CCTE North Entrance", 4783.11, 2234.14, true, 100));
        g.addNode(new Node("CCTE South Entrance", 4785.18, 2082.12, true, 100));
        g.addNode(new Node("CCTW - North Entrance", 4287.92, 2202.04, true, 100));
        g.addNode(new Node("CCTW - South Entrance", 4288.69, 2087.90, true, 100));
        g.addNode(new Node("节点 24", 5260.51, 2714.67, false, 0));
        g.addNode(new Node("节点 25", 5263.60, 2908.87, false, 0));
        g.addNode(new Node("Building C - North Entrance", 5081, 2826.47, true, 100));
        g.addNode(new Node("节点 27", 5079.45, 2906.56, false, 0));
        g.addNode(new Node("节点 28", 4992.02, 2908.48, false, 0));
        g.addNode(new Node("Building D - South Entrance", 4986.60, 2976.19, true, 100));
        g.addNode(new Node("节点 30", 4706.51, 2911.21, false, 0));
        g.addNode(new Node("节点 31", 4704.96, 2859.76, false, 0));
        g.addNode(new Node("节点 32", 4704.19, 2712.36, false, 0));
        g.addNode(new Node("节点 33", 4704.19, 2672.93, false, 0));
        g.addNode(new Node("节点 34", 4779.16, 2299.07, false, 0));
        g.addNode(new Node("Administration Building - South Entrance", 5081.69, 1660.74, true, 100));
        g.addNode(new Node("Library - East Entrance", 4592.69, 1798.10, true, 100));
        g.addNode(new Node("Library - West Entrance", 4198.86, 1784.56, true, 100));
        g.addNode(new Node("节点 38", 4622.87, 1610.88, false, 0));
        g.addNode(new Node("WDR - North Entrance", 3961.32, 1635.23, true, 100));
        g.addNode(new Node("WDR - West Entrance", 3883.94, 1451.87, true, 100));
        g.addNode(new Node("WDR - South Entrance", 3960.54, 1334.63, true, 100));
        g.addNode(new Node("WDR - East Entrance", 4119.16, 1574.90, true, 100));
        g.addNode(new Node("节点 43", 4165.59, 1597.31, false, 0));
        g.addNode(new Node("节点 44", 4286.29, 1591.90, false, 0));
        g.addNode(new Node("节点 45", 4624.42, 1339.27, false, 0));
        g.addNode(new Node("节点 46", 4627.51, 1290.90, false, 0));
        g.addNode(new Node("节点 47", 3795.74, 2301.79, false, 0));
        g.addNode(new Node("节点 48", 3797.29, 2490.99, false, 0));
        g.addNode(new Node("节点 49", 4157.07, 2682.88, false, 0));
        g.addNode(new Node("节点 50", 4370.63, 2862.01, false, 0));
        g.addNode(new Node("节点 51", 4402.35, 2863.94, false, 0));
        g.addNode(new Node("Building H - South Entrance", 4406.22, 2937.44, true, 100));
        g.addNode(new Node("节点 53", 4153.21, 2865.87, false, 0));
        g.addNode(new Node("节点 54", 3791.10, 2773.79, false, 0));
        g.addNode(new Node("节点 55", 3804.25, 3014.04, false, 0));
        g.addNode(new Node("节点 56", 4160.94, 3049.24, false, 0));
        g.addNode(new Node("节点 57", 4702.56, 3098.39, false, 0));
        g.addNode(new Node("节点 58", 5266.62, 3120.43, false, 0));
        g.addNode(new Node("North Takeaway Locker", 4776.29, 3161.05, true, 0));
        g.addNode(new Node("节点 61", 4938.65, 1293.98, false, 0));
        g.addNode(new Node("Sports Complex - North Entrance", 5519.79, 2261.87, true, 100));
        g.addNode(new Node("Sports Complex - South Entrance", 5522.35, 2079.09, true, 100));
        g.addNode(new Node("Academic Building - East Entrance", 3646.24, 2112.24, true, 100));
        g.addNode(new Node("Academic Building - West Entrance", 3412.64, 2115.97, true, 100));
        g.addNode(new Node("Conference Center - East Entrance", 2822.95, 2132.41, true, 100));
        g.addNode(new Node("Conference Center - West Entrance", 2669.50, 2122.87, true, 100));
        g.addNode(new Node("West Gate", 2179.82, 2128.74, true, 0));
        g.addNode(new Node("Building D - North Entrance", 4989.05, 3041.42, true, 100));
        g.addNode(new Node("Building C - South Entrance", 5084.50, 2747.51, true, 100));
        g.addNode(new Node("节点 66", 5083.59, 2711.92, false, 0));
        g.addNode(new Node("Building G - North Entrance", 4377.21, 2771.69, true, 100));
        g.addNode(new Node("Building G - South Entrance", 4413.92, 2700.70, true, 100));
        g.addNode(new Node("节点 68", 4155.78, 2758.47, false, 0));
        g.addNode(new Node("节点 69", 4285.81, 2291.50, false, 0));
        g.addNode(new Node("节点 70", 4291.88, 2009.29, false, 0));
        g.addNode(new Node("节点 71", 4790.46, 1996.97, false, 0));
        g.addNode(new Node("节点 72", 4989.95, 2003.26, false, 0));
        g.addNode(new Node("节点 73", 5814.97, 2000.26, false, 0));
        g.addNode(new Node("节点 74", 5523.11, 2005.69, false, 0));
        g.addNode(new Node("节点 75", 4704.71, 2305.37, false, 0));
        g.addNode(new Node("CCTE - East Entrance", 4928.02, 2148.28, true, 100));
        g.addNode(new Node("节点 77", 5296.90, 2317.39, false, 0));
        g.addNode(new Node("节点 78", 5262.57, 2494.93, false, 0));
        g.addNode(new Node("节点 79", 4156.07, 1782.48, false, 0));
        g.addNode(new Node("节点 80", 3966.45, 1706.20, false, 0));
        g.addNode(new Node("节点 81", 4157.45, 1707.52, false, 0));
        g.addNode(new Node("节点 82", 3635.47, 2272.64, false, 0));
        g.addNode(new Node("节点 83", 3638.15, 1733.99, false, 0));
        g.addNode(new Node("节点 84", 3635.45, 2314.10, false, 0));
        g.addNode(new Node("Innovation Building - North Entrance", 3108.16, 1494.53, true, 100));
        g.addNode(new Node("Innovation Building - South Entrance", 3104.21, 1312.77, true, 100));
        g.addNode(new Node("节点 87", 3792.74, 1710.14, false, 0));
        g.addNode(new Node("节点 88", 3796.76, 2108.76, false, 0));
        g.addNode(new Node("节点 89", 3798.72, 1446.90, false, 0));
        g.addNode(new Node("节点 90", 3790.79, 1608.10, false, 0));
        g.addNode(new Node("节点 91", 3789.45, 1527.87, false, 0));
        g.addNode(new Node("节点 92", 3107.55, 1535.13, false, 0));
        g.addNode(new Node("节点 93", 3134.08, 1600.80, false, 0));
        g.addNode(new Node("节点 94", 3130.11, 1790.51, false, 0));
        g.addNode(new Node("节点 95", 2904.55, 1794.48, false, 0));
        g.addNode(new Node("节点 96", 2794.43, 2298.61, false, 0));
        g.addNode(new Node("节点 97", 4640.31, 1997.91, false, 0));
        g.addNode(new Node("节点 98", 4939.80, 1878.14, false, 0));
        g.addNode(new Node("节点 99", 3960.75, 1200.50, false, 0));
        g.addNode(new Node("节点 100", 4161.91, 1202.27, false, 0));
        g.addNode(new Node("节点 101", 3199.10, 2133.48, false, 0));
        g.addNode(new Node("节点 21", 5008.57, 2519.10, false, 0));
        g.addNode(new Node("节点 102", 5056.79, 2516.60, false, 0));
        g.addNode(new Node("Building H - North Entrance", 4405.69, 2999.44, true, 100));
        g.addNode(new Node("节点 104", 4410.65, 3074.97, false, 0));
        g.addNode(new Node("节点 105", 4784.56, 3103.42, false, 0));
        g.addNode(new Node("节点 106", 4991.16, 3110.22, false, 0));
        g.addNode(new Node("Visitor Center - South Entrance", 5080.35, 1353.83, true, 100));
        g.addNode(new Node("Visitor Center - North Entrance", 5075.49, 1483.40, true, 100));
        g.addNode(new Node("节点 109", 5081.44, 1290.14, false, 0));
        g.addNode(new Node("节点 110", 4935.80, 1496.41, false, 0));
        g.addNode(new Node("节点 111", 4631.13, 1878.42, false, 0));
        g.addNode(new Node("节点 112", 3435.71, 2440.29, false, 0));
        g.addNode(new Node("Building F - South Entrance", 4353.29, 2549.71, true, 100));
        g.addNode(new Node("Building F - North Entrance", 4341.17, 2621.65, true, 100));
        g.addNode(new Node("Red Cage", 4449.23, 2663.01, true, 0));
        g.addNode(new Node("节点 116", 4337.43, 2651.94, false, 0));
        g.addNode(new Node("Administration Building - West Entrance", 4970.63, 1759.06, true, 100));
        g.addNode(new Node("节点 118", 4936.48, 1758.29, false, 0));
        g.addNode(new Node("节点 119", 3427.13, 2765.17, false, 0));
        g.addNode(new Node("节点 120", 2386.05, 2445.73, false, 0));
        g.addNode(new Node("节点 121", 2387.41, 2255.12, false, 0));
        g.addNode(new Node("节点 122", 2376.14, 2125.87, false, 0));
        g.addNode(new Node("节点 123", 2762.65, 1608.71, false, 0));
        g.addNode(new Node("节点 124", 3448.31, 1530.70, false, 0));
        g.addNode(new Node("节点 125", 3406.38, 1606.11, false, 0));
        g.addNode(new Node("节点 126", 3296.73, 2073.62, false, 0));
        g.addNode(new Node("节点 127", 3274.85, 1612.07, false, 0));
        g.addNode(new Node("South East Gate", 3106.63, 875.99, true, 0));
        g.addNode(new Node("节点 129", 3105.48, 1169.19, false, 0));
        g.addNode(new Node("节点 130", 3801.26, 1199.94, false, 0));
        g.addNode(new Node("节点 131", 4168.04, 1995.92, false, 0));
        g.addNode(new Node("节点 132", 2387.69, 2298.20, false, 0));
        g.addNode(new Node("CCTW - East Entrance", 4574.31, 2120.13, true, 100));
        g.addNode(new Node("CCTE - West Entrance", 4635.18, 2120.63, true, 100));
        g.addNode(new Node("节点 134", 4371.89, 2130.64, false, 0));
        g.addNode(new Node("South Gate 1", 4626.85, 1000.02, true, 0));
        g.addNode(new Node("South Gate 2", 4953.46, 1014.67, true, 0));
        g.addNode(new Node("North Gate", 5713.68, 3118.06, true, 100));
        g.addNode(new Node("Water Pavilion", 3028.84, 2095.11, true, 0));
        g.addNode(new Node("Building B - North Entrance", 5069.70, 2658.02, true, 100));
        g.addNode(new Node("Building E - South Entrance", 4383.58, 2332.57, true, 100));
        g.addNode(new Node("Graduate Students Center", 4131.71, 2370.19, true, 100));
        g.addNode(new Node("Service Building 2", 4076.09, 2840.07, true, 100));
        g.addNode(new Node("Service Building 1", 3568.20, 2869.31, true, 0));
        g.addNode(new Node("节点 1", 2820.39, 1276.40, false, 0));
        g.addNode(new Node("节点 3", 3434.76, 2377.89, false, 0));
        g.addNode(new Node("节点 7", 3797.54, 2374.14, false, 0));
        g.addNode(new Node("节点 9", 3635.66, 2377.59, false, 0));

        // ---- 207 roads ----
        // connect(a, b) -- the weight is derived, not typed in.
        g.connect("节点 30", "节点 31");
        g.connect("节点 31", "节点 32");
        g.connect("节点 32", "节点 33");
        g.connect("节点 2", "节点 33");
        g.connect("节点 4", "节点 2");
        g.connect("节点 4", "Building E - North Entrance");
        g.connect("节点 5", "节点 6");
        g.connect("CCTE North Entrance", "CCTE South Entrance");
        g.connect("节点 28", "节点 27");
        g.connect("节点 27", "节点 25");
        g.connect("节点 25", "节点 24");
        g.connect("节点 24", "节点 58");
        g.connect("Building D - North Entrance", "Building D - South Entrance");
        g.connect("Building D - South Entrance", "节点 28");
        g.connect("节点 28", "节点 30");
        g.connect("节点 53", "节点 50");
        g.connect("节点 50", "节点 51");
        g.connect("节点 51", "节点 31");
        g.connect("Building H - South Entrance", "节点 51");
        g.connect("节点 50", "Building G - North Entrance");
        g.connect("Building G - North Entrance", "Building G - South Entrance");
        g.connect("Building G - South Entrance", "节点 33");
        g.connect("节点 53", "节点 56");
        g.connect("节点 56", "节点 55");
        g.connect("节点 55", "节点 54");
        g.connect("节点 53", "节点 68");
        g.connect("节点 68", "节点 49");
        g.connect("节点 68", "节点 54");
        g.connect("节点 49", "节点 14");
        g.connect("节点 14", "节点 4");
        g.connect("节点 13", "节点 14");
        g.connect("节点 14", "节点 48");
        g.connect("节点 48", "节点 54");
        g.connect("节点 13", "节点 69");
        g.connect("节点 69", "CCTW - North Entrance");
        g.connect("节点 69", "节点 5");
        g.connect("CCTW - North Entrance", "CCTW - South Entrance");
        g.connect("节点 70", "CCTW - South Entrance");
        g.connect("节点 71", "节点 72");
        g.connect("节点 72", "节点 19");
        g.connect("节点 19", "节点 15");
        g.connect("节点 15", "Building A - South Entrance");
        g.connect("Building A - South Entrance", "Building A - North Entrance");
        g.connect("节点 15", "节点 34");
        g.connect("节点 34", "CCTE North Entrance");
        g.connect("节点 34", "节点 75");
        g.connect("节点 75", "节点 2");
        g.connect("节点 75", "节点 6");
        g.connect("CCTE - East Entrance", "节点 19");
        g.connect("节点 72", "节点 74");
        g.connect("节点 74", "Sports Complex - South Entrance");
        g.connect("Sports Complex - South Entrance", "Sports Complex - North Entrance");
        g.connect("节点 27", "Building C - North Entrance");
        g.connect("Building C - North Entrance", "Building C - South Entrance");
        g.connect("Building C - South Entrance", "节点 66");
        g.connect("节点 66", "节点 24");
        g.connect("节点 24", "节点 78");
        g.connect("节点 78", "节点 77");
        g.connect("节点 77", "Sports Complex - North Entrance");
        g.connect("节点 77", "节点 15");
        g.connect("CCTE South Entrance", "节点 71");
        g.connect("Library - East Entrance", "Library - West Entrance");
        g.connect("节点 47", "节点 88");
        g.connect("节点 88", "Academic Building - East Entrance");
        g.connect("节点 88", "节点 87");
        g.connect("节点 87", "节点 80");
        g.connect("节点 80", "节点 81");
        g.connect("节点 81", "节点 79");
        g.connect("节点 79", "Library - West Entrance");
        g.connect("节点 81", "节点 43");
        g.connect("节点 43", "节点 44");
        g.connect("WDR - East Entrance", "节点 43");
        g.connect("Innovation Building - South Entrance", "Innovation Building - North Entrance");
        g.connect("Innovation Building - North Entrance", "节点 92");
        g.connect("节点 92", "节点 93");
        g.connect("节点 93", "节点 94");
        g.connect("节点 94", "节点 95");
        g.connect("节点 95", "Conference Center - East Entrance");
        g.connect("Conference Center - East Entrance", "节点 10");
        g.connect("节点 96", "Conference Center - East Entrance");
        g.connect("Conference Center - East Entrance", "Conference Center - West Entrance");
        g.connect("Academic Building - West Entrance", "Academic Building - East Entrance");
        g.connect("Academic Building - East Entrance", "节点 82");
        g.connect("节点 82", "节点 84");
        g.connect("节点 83", "节点 87");
        g.connect("节点 83", "Academic Building - East Entrance");
        g.connect("节点 70", "节点 97");
        g.connect("节点 71", "节点 97");
        g.connect("Library - East Entrance", "节点 38");
        g.connect("节点 38", "节点 44");
        g.connect("节点 44", "节点 45");
        g.connect("节点 45", "节点 38");
        g.connect("节点 45", "节点 46");
        g.connect("节点 46", "节点 61");
        g.connect("节点 98", "节点 72");
        g.connect("节点 87", "节点 90");
        g.connect("节点 90", "节点 91");
        g.connect("节点 91", "节点 89");
        g.connect("节点 89", "WDR - West Entrance");
        g.connect("WDR - West Entrance", "WDR - North Entrance");
        g.connect("WDR - North Entrance", "节点 80");
        g.connect("WDR - West Entrance", "WDR - South Entrance");
        g.connect("节点 99", "WDR - South Entrance");
        g.connect("节点 100", "节点 99");
        g.connect("节点 100", "节点 43");
        g.connect("节点 100", "节点 46");
        g.connect("节点 10", "节点 101");
        g.connect("节点 101", "Academic Building - West Entrance");
        g.connect("节点 2", "节点 21");
        g.connect("节点 21", "Building A - North Entrance");
        g.connect("节点 21", "节点 102");
        g.connect("节点 102", "Building B - South Entrance");
        g.connect("节点 56", "节点 104");
        g.connect("节点 104", "节点 57");
        g.connect("节点 104", "Building H - North Entrance");
        g.connect("Building H - North Entrance", "Building H - South Entrance");
        g.connect("节点 57", "节点 105");
        g.connect("节点 105", "节点 106");
        g.connect("节点 106", "Building D - North Entrance");
        g.connect("North Takeaway Locker", "节点 105");
        g.connect("节点 57", "节点 30");
        g.connect("节点 84", "节点 47");
        g.connect("节点 61", "节点 109");
        g.connect("节点 109", "Visitor Center - South Entrance");
        g.connect("Visitor Center - South Entrance", "Visitor Center - North Entrance");
        g.connect("Visitor Center - North Entrance", "Administration Building - South Entrance");
        g.connect("节点 61", "节点 110");
        g.connect("节点 110", "Visitor Center - North Entrance");
        g.connect("节点 97", "节点 111");
        g.connect("节点 111", "节点 98");
        g.connect("节点 111", "Library - East Entrance");
        g.connect("节点 10", "Residence Hall");
        g.connect("Residence Hall", "节点 8");
        g.connect("节点 93", "节点 90");
        g.connect("Residence Hall", "节点 112");
        g.connect("节点 66", "节点 32");
        g.connect("节点 4", "Building F - South Entrance");
        g.connect("Building F - South Entrance", "Building F - North Entrance");
        g.connect("Red Cage", "节点 33");
        g.connect("Red Cage", "Building G - South Entrance");
        g.connect("节点 116", "Red Cage");
        g.connect("节点 116", "Building F - North Entrance");
        g.connect("节点 116", "节点 49");
        g.connect("Administration Building - West Entrance", "节点 118");
        g.connect("节点 98", "节点 118");
        g.connect("节点 118", "节点 110");
        g.connect("节点 93", "节点 123");
        g.connect("节点 123", "Conference Center - West Entrance");
        g.connect("Conference Center - West Entrance", "节点 122");
        g.connect("节点 122", "节点 121");
        g.connect("节点 120", "节点 8");
        g.connect("节点 122", "West Gate");
        g.connect("节点 92", "节点 124");
        g.connect("节点 124", "节点 91");
        g.connect("节点 126", "节点 127");
        g.connect("节点 126", "节点 125");
        g.connect("Academic Building - West Entrance", "节点 126");
        g.connect("节点 126", "节点 101");
        g.connect("Innovation Building - South Entrance", "节点 129");
        g.connect("节点 129", "South East Gate");
        g.connect("节点 129", "节点 130");
        g.connect("节点 130", "节点 99");
        g.connect("节点 130", "节点 89");
        g.connect("节点 70", "节点 131");
        g.connect("节点 131", "节点 79");
        g.connect("节点 131", "节点 13");
        g.connect("节点 13", "节点 47");
        g.connect("节点 54", "节点 119");
        g.connect("节点 119", "节点 112");
        g.connect("节点 96", "节点 132");
        g.connect("节点 120", "节点 132");
        g.connect("节点 132", "节点 121");
        g.connect("CCTW - South Entrance", "节点 134");
        g.connect("节点 134", "CCTW - East Entrance");
        g.connect("CCTW - East Entrance", "CCTE - West Entrance");
        g.connect("CCTE - West Entrance", "CCTE South Entrance");
        g.connect("CCTE North Entrance", "CCTE - West Entrance");
        g.connect("CCTE North Entrance", "CCTE - East Entrance");
        g.connect("CCTE South Entrance", "CCTE - East Entrance");
        g.connect("CCTW - North Entrance", "节点 134");
        g.connect("节点 73", "节点 74");
        g.connect("South Gate 1", "节点 46");
        g.connect("South Gate 2", "节点 61");
        g.connect("节点 106", "节点 58");
        g.connect("节点 58", "North Gate");
        g.connect("Water Pavilion", "节点 10");
        g.connect("Building B - North Entrance", "Building B - South Entrance");
        g.connect("Building B - North Entrance", "节点 66");
        g.connect("Building G - South Entrance", "节点 116");
        g.connect("Building E - North Entrance", "Building E - South Entrance");
        g.connect("Building E - South Entrance", "节点 5");
        g.connect("WDR - North Entrance", "WDR - East Entrance");
        g.connect("WDR - East Entrance", "WDR - South Entrance");
        g.connect("Administration Building - West Entrance", "Administration Building - South Entrance");
        g.connect("节点 14", "Graduate Students Center");
        g.connect("Graduate Students Center", "节点 13");
        g.connect("节点 53", "Service Building 2");
        g.connect("Service Building 2", "节点 68");
        g.connect("节点 119", "Service Building 1");
        g.connect("Service Building 1", "节点 54");
        g.connect("节点 123", "节点 1");
        g.connect("节点 7", "节点 48");
        g.connect("节点 112", "节点 3");
        g.connect("节点 7", "节点 47");
        g.connect("节点 3", "节点 9");
        g.connect("节点 9", "节点 7");
        g.connect("节点 9", "节点 84");

        return g;
    }

    private CampusData() {}   // static-only holder
}
