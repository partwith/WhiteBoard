package teaching;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;

/**
 * A graphical drawing board to perform basic operations on geometrical forms.
 *
 * WhiteBoard offers methods to draw points, lines, rectangles, polygones,
 * ellipses and parabolas. All drawing methods return an object that can be
 * removed using removeShape from the board.
 * @author $Author Georg Beier$
 * @version $Revision: 1.7 $ vom $Date: 2003/04/29 14:36:20 $
 */
public class WhiteBoard {

    /** Das äußere Fenster */
    private JFrame frame;
    /** Die "Arbeitsfläche" im äußeren Fenster */
    private Container container;
    /** inneres Fenster mit Rollbalken */
    private JScrollPane scrollPane;
    /** Liste aller Zeichenelemente */
    private java.util.List shapes = Collections.synchronizedList(new ArrayList());
    /** Flag das angibt, ob Fenster geschlossen wurde */
    private boolean windowExited = false;
    /** Action Listener Objekt, dass auf das WindowClose
     * Ereignis des äußeren Frame reagiert.
     */
    private WindowListener closeListener;
    /** Koordinaten der Randpunkte */
    private double minX;
    /** Koordinaten der Randpunkte */
    private double maxX;
    /** Koordinaten der Randpunkte */
    private double minY;
    /** Koordinaten der Randpunkte */
    private double  maxY;

    /** Komponente, die die eigentliche Darstellung übernimmt. */
    private JComponent graphicalComponent = new DrawingArea();

    /** Legt ein neues WhiteBoard-Objekt an. Dieses wird automatisch
     * in einem eigenen Fenster dargestellt.
     */
    public WhiteBoard() {
        minX = maxX = minY = maxY = 0;
        closeListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                windowExited = true;
                System.exit(0);
            }
        };
        initFrame();
    }

    /** Legt alle zum Zeichnen benötigten Objekte an */
    private void initFrame() {
        frame = new JFrame("WhiteBoard");
        container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        scrollPane = new JScrollPane(graphicalComponent);
        scrollPane.getViewport().setBackground(Color.WHITE);
        graphicalComponent.setForeground(Color.GREEN);
        container.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocation(100, 100);
        frame.addWindowListener(closeListener);
        frame.setVisible(true);
        graphicalComponent.repaint();
    }

    /** Neuberechnung der Größe der Zeichenfläche. Dazu werden alle Shapes
     * durchgearbeitet.
     * @return gibt an, ob sich die Größe geändert hat
     */
    private boolean recomputeBounds() {
        AttributedShape as;
        Rectangle r;
        boolean boundsChanged = (shapes.size() == 0);
        double minX = 0, maxX = 0, minY = 0, maxY = 0;
        double dx, dy;
        int diag;
        Point p;
        synchronized(shapes) {
            for(Iterator it = shapes.iterator(); it.hasNext(); ) {
                as = (AttributedShape) it.next();
                r = as.shape.getBounds();
                if(as.rotation != 0.0) {
                    p = r.getLocation();
                    dx = r.getWidth();
                    dy = r.getHeight();
                    diag = (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
                    r = new Rectangle(p, new Dimension(diag, diag));
                }
                minX = Math.min(minX, r.getMinX());
                maxX = Math.max(maxX, r.getMaxX());
                minY = Math.min(minY, r.getMinY());
                maxY = Math.max(maxY, r.getMaxY());
            }
        }
        if(this.minX < minX) {
            boundsChanged = true;
            this.minX = minX;
        }
        if(this.minY < minY) {
            boundsChanged = true;
            this.minY = minY;
        }
        if(this.maxX > maxX) {
            boundsChanged = true;
            this.maxX = maxX;
        }
        if(this.maxY > maxY) {
            boundsChanged = true;
            this.maxY = maxY;
        }
        if(boundsChanged) graphicalComponent.revalidate();
        return boundsChanged;
    }

    /** Neuberechnung der Größe der Zeichenfläche, wenn ein Shape hinzugefügt wurde
     * @return gibt an, ob sich die Größe geändert hat
     * @param s hinzugefügtes Zeichnungselement
     */
    private boolean recomputeBounds(Shape s) {
        return recomputeBounds(s, 0.);
    }

    /** Neuberechnung der Größe der Zeichenfläche, wenn ein gedrehtes Zeichnungselement
     * hinzugefügt wird.
     * @return gibt an, ob sich die Größe geändert hat
     * @param s hinzugefügtes Zeichnungselement
     * @param rotation Drehung des Zeichenelements
     */
    private boolean recomputeBounds(Shape s, double rotation) {
        Rectangle r = s.getBounds();
        if(rotation != 0.0) {
            Point p = r.getLocation();
            double dx = r.getWidth();
            double dy = r.getHeight();
            int diag = (int) Math.ceil( Math.sqrt(dx * dx + dy * dy));
            p.translate(-diag / 2, -diag / 2);
            r = new Rectangle(p, new Dimension(10 * diag / 7, 10 * diag / 7));
        }
        double x1 = r.getMinX();
        double x2 = r.getMaxX();
        double y1 = r.getMinY();
        double y2 = r.getMaxY();
        boolean boundsChanged = false;
        if (minX > x1) {
            boundsChanged = true;
            minX = x1;
        }
        if (minY > y1) {
            boundsChanged = true;
            minY = y1;
        }
        if (maxX < x2) {
            boundsChanged = true;
            maxX = x2;
        }
        if (maxY < y2) {
            boundsChanged = true;
            maxY = y2;
        }
        if(boundsChanged) graphicalComponent.revalidate();
        return boundsChanged;
    }

    /** Zeichnet eine Linie in default-Farbe.
     * @param xfrom x Startkoordinate
     * @param yfrom y Startkoordinate
     * @param xto x Zielkoordinate
     * @param yto y Zielkoordinate
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public Object drawLine(double xfrom, double yfrom, double xto, double yto) {
        return drawLine(xfrom, yfrom, xto, yto, Color.BLACK);
    }

    /** Zeichnet eine farbige Linie.
     * @param xfrom x Startkoordinate
     * @param yfrom y Startkoordinate
     * @param xto x Zielkoordinate
     * @param yto y Zielkoordinate
     * @param color Linienfarbe. {@link java.awt.Color}
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public  Object drawLine(double xfrom, double yfrom, double xto, double yto, Color color) {
        Shape s = new Line2D.Double(xfrom, yfrom,  xto, yto);
        rebuild();
        AttributedShape as = new WhiteBoard.AttributedShape(s, color, 0., false);
        shapes.add(as);
        recomputeBounds(s);
        container.repaint();
        return as;
    }

    /** Zeichnet einen parabolischen Bogen in default-Farbe.
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     * @param xfrom x Startkoordinate
     * @param yfrom y Startkoordinate
     * @param xto x Zielkoordinate
     * @param yto y Zielkoordinate
     * @param excentricity maximale Auslenkung des Bogens aus der Verbindungsgerade
     */
    public Object drawArc(double xfrom, double yfrom, double xto, double yto, double excentricity) {
        return drawArc(xfrom, yfrom,  xto, yto, excentricity, Color.BLACK, false);
    }

    /** Zeichnet einen farbigen, evtl. gefüllten parabolischen Bogen.
     * @param xfrom x Startkoordinate
     * @param yfrom y Startkoordinate
     * @param xto x Zielkoordinate
     * @param yto y Zielkoordinate
     * @param excentricity maximale Auslenkung des Bogens aus der Verbindungsgerade
     * @param color Linienfarbe. {@link java.awt.Color}
     * @param solid true, wenn Zeichnungsobjekt gefüllt werden soll.
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public  Object drawArc(double xfrom, double yfrom, double xto, double yto, double excentricity, Color color, boolean solid) {
        double xs, ys, d, dx, dy, r, exx, exy;
        rebuild();
        dx = xto - xfrom;
        dy = yto - yfrom;
        d = Math.sqrt(dx * dx + dy * dy);
        r = excentricity;
        exx =  dy * r / d;
        exy = dx * r / d;
        xs = ((xfrom + xto) / 2) - 2 * exx;
        ys = ((yfrom + yto) / 2) + 2 * exy;
        Shape s = new QuadCurve2D.Double(xfrom, yfrom, xs, ys, xto, yto);
        AttributedShape as = new WhiteBoard.AttributedShape(s, color, 0., solid);
        shapes.add(as);
        recomputeBounds(s);
        container.repaint();
        return as;
    }

    /** Zeichnet einen schwarzen Punkt
     * @param x Koordinate
     * @param y Koordinate
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public Object drawPoint(double x, double y) {
        return drawPoint(x, y,  Color.BLACK);
    }

    /** Zeichnet einen farbigen Punkt
     * @param x Koordinate
     * @param y Koordinate
     * @param color Linienfarbe. {@link java.awt.Color}
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public  Object drawPoint(double x, double y, Color color) {
        rebuild();
        Shape s = new Ellipse2D.Double(x-1, y-1, 2, 2);
        AttributedShape as = new WhiteBoard.AttributedShape(s, color, 0., true);
        shapes.add(as);
        recomputeBounds(s);
        container.repaint();
        return as;
    }

    /** Zeichnet eine schwarze Ellipse
     * @param x Mittelpunkts-Koordinate
     * @param y Mittelpunkts-Koordinate
     * @param hx x Achse
     * @param hy y Achse
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public Object drawEllipse(double x, double y, double hx, double hy) {
        return drawEllipse(x, y, hx, hy, Color.BLACK, false, 0.0);
    }

    /** Zeichnet eine farbige Ellipse
     * @param x Mittelpunkts-Koordinate
     * @param y Mittelpunkts-Koordinate
     * @param hx x Achse
     * @param hy y Achse
     * @param color Linienfarbe. {@link java.awt.Color}
     * @param solid true, wenn Zeichnungsobjekt gefüllt werden soll.
     * @param rotation Drehung des Objekts, Winkel im Bogenmaß!
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public  Object drawEllipse(double x, double y, double hx, double hy, Color color, boolean solid, double rotation) {
        rebuild();
        Shape s = new Ellipse2D.Double(x-hx, y-hy, 2*hx, 2*hy);
        AttributedShape as = new WhiteBoard.AttributedShape(s, color, rotation, solid);
        shapes.add(as);
        recomputeBounds(s, rotation);
        container.repaint();
        return as;
    }

    /** Zeichnet ein schwarzes Polygon
     * Achtung: Obwohl die Eckpunkte als <code>double</code> angegeben werden,
     * wird intern mit <code>int</code> gerechnet und entsprechend gerundet!
     * @param x Eckpunkt-Koordinaten x
     * @param y Eckpunkt-Koordinaten y
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public Object drawPolygon(double[] x, double[] y) {
        return drawPolygon(x, y, Color.BLACK, false, 0.0);
    }

    /** Zeichnet ein farbiges Polygon
     * Achtung: Obwohl die Eckpunkte als <code>double</code> angegeben werden,
     * wird intern mit <code>int</code> gerechnet und entsprechend gerundet!
     * @param x Eckpunkt-Koordinaten x
     * @param y Eckpunkt-Koordinaten y
     * @param color Linienfarbe. {@link java.awt.Color}
     * @param solid true, wenn Zeichnungsobjekt gefüllt werden soll.
     * @param rotation Drehung des Objekts, Winkel im Bogenmaß!
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public  Object drawPolygon(double[] x, double y[], Color color, boolean solid, double rotation) {
        rebuild();
        int[] ix = new int[x.length], iy = new int[y.length];
        int pl = Math.min(x.length, y.length);
        for(int i = 0; i < pl; i++) {
            ix[i] = (int) x[i];
            iy[i] = (int) y[i];
        }
        Shape s = new java.awt.Polygon(ix, iy, pl);
        AttributedShape as = new WhiteBoard.AttributedShape(s, color, rotation, solid);
        shapes.add(as);
        recomputeBounds(s, rotation);
        container.repaint();
        return as;
    }

    /** Zeichnet ein schwarzes Rechteck
     * @param x Mittelpunkts-Koordinate
     * @param y Mittelpunkts-Koordinate
     * @param hx x Seitenlänge
     * @param hy y Seitenlönge
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public Object drawRectangle(double x, double y, double hx, double hy) {
        return drawRectangle(x, y, hx, hy, Color.BLACK, false, 0.0);
    }

    /** Zeichnet ein farbiges Rechteck
     * @param x Mittelpunkts-Koordinate
     * @param y Mittelpunkts-Koordinate
     * @param hx x Seitenlänge
     * @param hy y Seitenlänge
     * @param color Linienfarbe. {@link java.awt.Color}
     * @param solid true, wenn Zeichnungsobjekt gefüllt werden soll.
     * @param rotation Drehung des Objekts, Winkel im Bogenmaß!
     * @return Referenz auf das intern verwendete Zeichnungsobjekt. Wird benötigt, um das
     * Objekt wieder zu löschen.
     */
    public  Object drawRectangle(double x, double y, double hx, double hy, Color color, boolean solid, double rotation) {
        rebuild();
        Shape s = new Rectangle2D.Double(x-hx, y-hy, 2*hx, 2*hy);
        AttributedShape as = new WhiteBoard.AttributedShape(s, color, rotation, solid);
        shapes.add(as);
        recomputeBounds(s, rotation);
        container.repaint();
        return as;
    }

    /** Löscht ein Objekt nur aus der internen Speicherstruktur.
     * gObjekt wird nicht von der Zeichenfläche gelöscht, dies geschieht
     * erst beim nächsten redraw. Dadurch wird ein Flickern beim Bewegen von
     * Objekten vermieden.
     * @param o Referenz auf das interne Zeichenobjekt
     */
    public  void removeShape(Object o) {
        shapes.remove(o);
    }

    /** Löscht ein Objekt aus der internen Speicherstruktur und von
     * der Zeichenfläche. Führt zu Bildschirmflickern, wenn diese Methode
     * zum Bewegen von Objekten verwendet wird.
     * @param o Referenz auf das interne Zeichenobjekt
     */
    public  void wipeShape(Object o) {
        if(shapes.remove(o)) {
            recomputeBounds();
            graphicalComponent.repaint();
        }
    }

    /**
     * Methode zum @Demonstrieren und Testen der Zeichenmöglichkeiten von WhiteBoard.<br>
     * In der Methode actionPerformed des JButton sieht man, wie ein Objekt von der
     * Zeichenfläche wieder gelöscht wird.<br>
     * Die übrigen Statements zeigen, wie unterschiedliche Objekte gezeichnet werden.
     */
    public void demo() {
        JButton b = new JButton("weg mit der Form");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                    if(shapes.size() > 0)
                        wipeShape(shapes.get(shapes.size() - 1));

            }
        });
        this.container.add(b, BorderLayout.SOUTH);
        this.frame.validate();
        ArrayList mainShapes = new ArrayList();
        double[] xp = {0., 100., 150., 70.};
        double yp[] = {160., 150., 300., -100.};
        this.drawPolygon(xp, yp);
        this.drawPolygon(yp, xp, Color.BLUE, true, Math.PI/2);
        this.drawPolygon(yp, xp, Color.RED, true, Math.PI);
        this.drawPolygon(yp, xp, Color.GREEN, true, Math.PI*3/2);
        this.drawPolygon(yp, xp, Color.ORANGE, true, 0.);
        this.drawLine(-400,0, 400, 0);
        this.drawLine(0,-400, 0, 400);
        this.drawLine(-400,-0, 0, 400);
        this.drawLine(0,-400, 400, 0);
        this.drawLine(-400,0, 0, -400);
        this.drawLine(0, 400, 400, 0);
        this.drawLine(0,0, 100,100);
        this.drawLine(0,-400, 200,200);
        this.drawLine(0,-400, 200,-380);
        this.drawEllipse(0, 0, 400, 400, Color.RED, false, 0);
        this.drawLine(0,0, 100,-100);
        mainShapes.add(this.drawEllipse(-100, 0, 100, 100, Color.GREEN, true, 0));
        this.drawLine(0,0, -100, 0);
        mainShapes.add(this.drawRectangle(100, 100, 100, 100, Color.ORANGE, false, Math.PI/4));
        mainShapes.add(this.drawArc(100, 100, 100, -100, -100, Color.BLUE, false));
        this.drawPoint(150, 300);
        mainShapes.add(this.drawRectangle(450, 450, 500, 500, Color.ORANGE, false, Math.PI/4));
        mainShapes.add(this.drawRectangle(450, 450, 496, 496, Color.BLUE, true, Math.PI/4));
        mainShapes.add(this.drawEllipse(700, 500, 500, 200, Color.GREEN, true, -Math.PI/10));
        this.drawLine(100,0, 200,100);
        this.drawArc(100, 100, 400, 500, 100);
        mainShapes.add(this.drawArc(110, 100, 110, 500, 80));
        mainShapes.add(this.drawArc(90, 500, 90, 100, 80, Color.RED, true));
        mainShapes.add(this.drawArc(120, 500, 120, 100, -80));

        mainShapes.add(this.drawArc(600, 90, 200, 90, 80));
        mainShapes.add(this.drawArc(200, 100, 600, 100, -80));
        mainShapes.add(this.drawArc(200, 110, 600, 110, 80));
        mainShapes.add(this.drawArc(600, 120, 200, 120, -80));

        mainShapes.add(this.drawArc(500, 200, 600, 600, -80));
        mainShapes.add(this.drawArc(510, 210, 590, 590, 80));
        mainShapes.add(this.drawArc(580, 580, 520, 220, -70));
        mainShapes.add(this.drawArc(570, 570, 530, 230, 70));
    }

    /** Test- Mainmethode für Klasse WhiteBoard
     * @param args Kommandozeilenparameter (ignoriert)
     */
    public static void main(String[] args) {
        final WhiteBoard wb = new WhiteBoard();
        wb.demo();
    }

    /** Baut Windows wieder auf, falls der Frame geschlossen wurde. */
    private void rebuild() {
        if(windowExited) {
            windowExited = false;
            initFrame();
        }
    }

    /** Interne Klasse zum Abspeichern der Zeichenobjekte mit verschiedenen Eigenschaften
     */
    public class AttributedShape {
        Color color;
        double rotation;
        Shape shape;
        boolean isFilled;

        AttributedShape(Shape s) {
            this(s, Color.BLACK, 0.0, false);
        }

        AttributedShape(Shape s, Color c, double rot, boolean solid) {
            shape = s;
            color = c;
            rotation = rot;
            isFilled = solid;
        }
    }

    /** Innere Klasse stellt die aktuelle Zeichenfläche zur Verfügung. */
    private class DrawingArea extends JComponent {
    /** stellt die Zeichenfläche mit allen enthaltenen grafischen
     * Objekten neu dar. Wird automatisch aufgerufen, wenn das
     * Window neu gezeichnet werden muß. Wird auch indirekt über repaint() oder
     * revalidate aufgerufen.
     * @param g neues, vom Windowsystem bereitgestelltes Grafics-Objekt, auf dem das Whiteboard
     * neu aufgebaut wird.
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AttributedShape as;
        double x = 0.0, y = 0.0;
        Rectangle r;
        Rectangle clip = g.getClipBounds();
        double ysize = clip.getHeight();
        double xt = Math.min(minX-1, 0);
        double yt = Math.max(maxY+1, ysize + Math.min(0., minY));
        g2.scale(1.0, -1.0);
        g2.translate(-xt, -yt);
        synchronized(shapes) {
            for(Iterator it = shapes.iterator(); it.hasNext(); ) {
                as = (AttributedShape) it.next();
                g2.setPaint(as.color);
                if(as.rotation != 0.0) {
                    r = as.shape.getBounds();
                    x = r.getCenterX();
                    y = r.getCenterY();
                    g2.rotate(as.rotation, x, y);
                }
                if(as.isFilled) g2.fill(as.shape);
                else g2.draw(as.shape);
                if(as.rotation != 0.0) {
                    g2.rotate(-as.rotation, x, y);
                }
            }
        }
    }

    /** gibt gewünschte Größe in Bildschirmeinheiten an. Wird von ScrollPane benutzt, um
     * Rollbalken zu berechnen.
     * @return Größe der Zeichenfläche.
     */
    public Dimension getPreferredSize() {
        return new Dimension((int) (maxX-minX+3), (int) (maxY-minY+3));
    }


    }
}
