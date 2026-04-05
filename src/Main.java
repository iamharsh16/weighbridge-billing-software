import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.*;

/**
 * N-KARTECH Weighbridge Billing Software
 * Tech Stack : Java Swing + SQLite (JDBC)
 * Logo Integration : logo_banner.png (header) + logo_icon.png (window icon)
 * Place both image files in an "images/" folder next to the JAR / .class file.
 */
public class Main {

    // ═══════════════════════  DESIGN TOKENS  ════════════════════════════════
    static final Color C_NAVY        = new Color(10, 20, 50);
    static final Color C_NAVY2       = new Color(18, 32, 78);
    static final Color C_ACCENT      = new Color(37,  99, 235);
    static final Color C_ACCENT_SOFT = new Color(219, 234, 254);
    static final Color C_TEAL        = new Color(0,  188, 188);
    static final Color C_PANEL_BG    = new Color(241, 245, 249);
    static final Color C_CARD_BG     = Color.WHITE;
    static final Color C_LABEL_FG    = new Color( 51,  65,  85);
    static final Color C_SUBLABEL    = new Color(100, 116, 139);
    static final Color C_BORDER      = new Color(203, 213, 225);
    static final Color C_INPUT_BG    = new Color(250, 252, 255);
    static final Color C_NET_BG      = new Color(220, 252, 231);
    static final Color C_NET_FG      = new Color( 22, 101,  52);
    static final Color C_NET_BORDER  = new Color(134, 239, 172);

    static final Color BTN_SAVE      = new Color( 22, 163,  74);
    static final Color BTN_PRINT     = new Color( 37,  99, 235);
    static final Color BTN_CLEAR     = new Color(220,  38,  38);
    static final Color BTN_SEARCH    = new Color(109,  40, 217);
    static final Color BTN_UPDATE    = new Color(  2, 132, 199);
    static final Color BTN_SAVEPRINT = new Color(  5, 150, 105);
    static final Color BTN_ADDWB     = new Color( 99, 102, 241);
    static final Color BTN_CANCEL    = new Color(100, 116, 139);
    static final Color BTN_CSV       = new Color( 21, 128,  61);
    static final Color BTN_PDF       = new Color(185,  28,  28);

    static final Font F_SECTION = new Font("Segoe UI", Font.BOLD,   12);
    static final Font F_LABEL   = new Font("Segoe UI", Font.PLAIN,  13);
    static final Font F_FIELD   = new Font("Segoe UI", Font.PLAIN,  13);
    static final Font F_BTN     = new Font("Segoe UI", Font.BOLD,   12);
    static final Font F_STATUS  = new Font("Segoe UI", Font.PLAIN,  11);
    static final Font F_ADDR    = new Font("Segoe UI", Font.PLAIN,  12);
    static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN,  11);
    static final Font F_NET     = new Font("Segoe UI", Font.BOLD,   13);
    static final Font F_COUNT   = new Font("Segoe UI", Font.BOLD,   13);
    static final Font F_DLG_TTL = new Font("Segoe UI", Font.BOLD,   16);
    static final Font F_WB_LBL  = new Font("Segoe UI", Font.BOLD,   12);

    // ═══════════════════════  GLOBAL STATE  ══════════════════════════════════
    static String selectedAddress = "";
    static String selectedPhone   = "";
    static JLabel statusLabel;

    // ═══════════════════════  IMAGE LOADER  ═══════════════════════════════════
    static Image loadImg(String filename) {
        try {
            java.io.File f = new java.io.File("images" + java.io.File.separator + filename);
            if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}
        return null;
    }

    static Image scaleImg(Image src, int w, int h) {
        if (src == null) return null;
        return src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    static BufferedImage cropCenterSquare(Image src, int targetSize) {
        if (src == null) return null;
        int sw = src.getWidth(null), sh = src.getHeight(null);
        if (sw <= 0 || sh <= 0) { sw = 200; sh = 200; }
        int side = Math.min(sw, sh), ox = (sw - side) / 2, oy = (sh - side) / 2;
        BufferedImage cropped = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cropped.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, -ox, -oy, sw, sh, null); g.dispose();
        BufferedImage scaled = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(cropped, 0, 0, targetSize, targetSize, null); g2.dispose();
        return scaled;
    }

    // ═══════════════════════  BUSINESS HELPERS  ═══════════════════════════════
    public static String formatDate(String input) {
        try { java.time.LocalDate d = java.time.LocalDate.parse(input);
            return d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear();
        } catch (Exception e) { return input; }
    }

    public static String formatTime(String input) {
        try { java.time.LocalTime t = java.time.LocalTime.parse(input);
            return t.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
        } catch (Exception e) { return input; }
    }

    public static String numberToWords(String num) {
        String[] map = {"ZERO","ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN","EIGHT","NINE"};
        StringBuilder out = new StringBuilder();
        for (char c : num.toCharArray()) if (Character.isDigit(c)) out.append(map[c - '0']).append(" ");
        return out + "KG";
    }

    static void setStatus(String msg) { if (statusLabel != null) statusLabel.setText("  " + msg); }

    // ═══════════════════════  UI FACTORIES  ═══════════════════════════════════
    static JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text); b.setFont(F_BTN); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setPreferredSize(new Dimension(148, 38));
        Color hover = bg.darker();
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg);   }
        });
        return b;
    }

    static JTextField makeField() {
        JTextField f = new JTextField(); f.setFont(F_FIELD); f.setBackground(C_INPUT_BG);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BORDER,1),BorderFactory.createEmptyBorder(5,9,5,9)));
        return f;
    }

    static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(F_LABEL); l.setForeground(C_LABEL_FG); return l;
    }

    static JPanel makeCard(String title) {
        JPanel p = new JPanel(); p.setBackground(C_CARD_BG);
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(C_BORDER,1,true),
                "   " + title + "   ", TitledBorder.LEFT, TitledBorder.TOP, F_SECTION, C_ACCENT);
        p.setBorder(BorderFactory.createCompoundBorder(tb, BorderFactory.createEmptyBorder(8,14,14,14)));
        return p;
    }

    static void addFormRow(JPanel p, GridBagConstraints g, int row,
                           String l1, Component f1, String l2, Component f2) {
        g.gridy = row;
        g.gridx = 0; g.weightx = 0; g.gridwidth = 1; p.add(makeLabel(l1), g);
        g.gridx = 1; g.weightx = 1.0; g.gridwidth = (l2 == null) ? 3 : 1; p.add(f1, g);
        g.gridwidth = 1;
        if (l2 != null && f2 != null) {
            g.gridx = 2; g.weightx = 0; p.add(makeLabel(l2), g);
            g.gridx = 3; g.weightx = 1.0; p.add(f2, g);
        }
    }

    // ═══════════════════════  DATABASE  ═══════════════════════════════════════
    static void initDB() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bills (id INTEGER PRIMARY KEY AUTOINCREMENT,weighbridge TEXT,rst TEXT,date TEXT,time TEXT,vehicle TEXT,material TEXT,gross TEXT,tare TEXT,net TEXT,charges TEXT,customer_name TEXT,place TEXT,source TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS weighbridges (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,address TEXT,phone TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS vehicles (id INTEGER PRIMARY KEY AUTOINCREMENT,vehicle_no TEXT)");
            try { stmt.executeUpdate("ALTER TABLE bills ADD COLUMN customer_name TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE bills ADD COLUMN place TEXT DEFAULT ''");         } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE bills ADD COLUMN source TEXT DEFAULT ''");        } catch (Exception ignored) {}
            conn.close();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ═══════════════════════  SPLASH  ════════════════════════════════════════
    static void showSplash(Image bannerImg) throws InterruptedException {
        JWindow splash = new JWindow();
        int SW = 720, SH = 320;
        splash.setSize(SW, SH);
        splash.setLocationRelativeTo(null);

        JPanel sp = new JPanel(null) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
                // Background matches banner image background colour
                GradientPaint gp = new GradientPaint(0,0,new Color(8,15,48),SW,SH,new Color(15,30,80));
                g.setPaint(gp); g.fillRect(0,0,SW,SH);
                // Subtle grid
                g.setColor(new Color(255,255,255,12));
                for (int x=0;x<SW;x+=45) g.drawLine(x,0,x,SH);
                for (int y=0;y<SH;y+=45) g.drawLine(0,y,SW,y);
                // Top teal accent
                g.setColor(C_TEAL); g.fillRect(0,0,SW,4);
            }
        };
        sp.setBackground(new Color(8,15,48));

        if (bannerImg != null) {
            int bw = 560, bh = 200;
            JLabel bannerLbl = new JLabel(new ImageIcon(scaleImg(bannerImg,bw,bh)));
            bannerLbl.setBounds((SW-bw)/2, 28, bw, bh);
            sp.add(bannerLbl);
        } else {
            JLabel t1 = new JLabel("N-KARTECH", SwingConstants.CENTER);
            t1.setFont(new Font("Segoe UI",Font.BOLD,36)); t1.setForeground(Color.WHITE);
            t1.setBounds(0,80,SW,50); sp.add(t1);
            JLabel t2 = new JLabel("WEIGHBRIDGE MANAGEMENT SOFTWARE", SwingConstants.CENTER);
            t2.setFont(new Font("Segoe UI",Font.PLAIN,16)); t2.setForeground(C_TEAL);
            t2.setBounds(0,140,SW,30); sp.add(t2);
        }

        JProgressBar bar = new JProgressBar(0,100);
        bar.setBounds(160,258,400,6); bar.setBackground(new Color(255,255,255,30));
        bar.setForeground(C_TEAL); bar.setBorderPainted(false); bar.setStringPainted(false);
        sp.add(bar);
        JLabel loadTxt = new JLabel("Initialising...", SwingConstants.CENTER);
        loadTxt.setFont(F_SMALL); loadTxt.setForeground(new Color(150,190,255));
        loadTxt.setBounds(0,272,SW,18); sp.add(loadTxt);
        JLabel ver = new JLabel("v2.0  ·  N-kartech  ·  All rights reserved", SwingConstants.CENTER);
        ver.setFont(new Font("Segoe UI",Font.ITALIC,10)); ver.setForeground(new Color(80,110,170));
        ver.setBounds(0,296,SW,16); sp.add(ver);

        splash.add(sp); splash.setVisible(true);

        String[] msgs = {"Initialising database...","Loading vehicle registry...","Preparing billing engine...","Ready!"};
        for (int i = 0; i <= 100; i++) {
            final int val = i; final String msg = msgs[Math.min(i/26,3)];
            SwingUtilities.invokeLater(() -> { bar.setValue(val); loadTxt.setText(msg); });
            Thread.sleep(19);
        }
        Thread.sleep(300);
        splash.dispose();
    }

    // ═══════════════════════  MAIN  ═══════════════════════════════════════════
    public static void main(String[] args) throws InterruptedException {
        initDB();
        Image bannerImg    = loadImg("logo_banner.png");
        Image iconImg      = loadImg("logo_icon.png");
        BufferedImage coin = cropCenterSquare(iconImg, 128);

        showSplash(bannerImg);

        final Image bannerFinal = bannerImg;

        JFrame frame = new JFrame("N-KARTECH  \u00B7  Weighbridge Billing Software");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1050, 720));
        frame.setLayout(new BorderLayout(0,0));
        if (coin != null) frame.setIconImage(coin);

        // ══════════════════  HEADER: LOGO BANNER PANEL  ═══════════════════════
        JPanel headerImgPanel = new JPanel(null) {
            @Override public Dimension getPreferredSize() { return new Dimension(0, 110); }
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();
                // Deep navy — same as the banner image background so it blends seamlessly
                g.setColor(C_NAVY); g.fillRect(0,0,W,H);
                // Subtle grid overlay (tech feel matching the logo)
                g.setColor(new Color(255,255,255,10));
                for (int x=0;x<W;x+=50) g.drawLine(x,0,x,H);
                for (int y=0;y<H;y+=50) g.drawLine(0,y,W,y);
                // Draw the horizontal banner logo — left-aligned, height-fitted
                if (bannerFinal != null) {
                    int logoH = H - 6;
                    int logoW = (int)(logoH * 2.66); // banner aspect ratio ≈ 1408×530
                    if (logoW > (int)(W * 0.52)) { logoW = (int)(W * 0.52); logoH = (int)(logoW / 2.66); }
                    int ly = (H - logoH) / 2;
                    g.drawImage(bannerFinal, 2, ly, logoW, logoH, null);
                } else {
                    g.setFont(new Font("Segoe UI",Font.BOLD,22)); g.setColor(Color.WHITE);
                    g.drawString("N-KARTECH", 24, 56);
                    g.setFont(new Font("Segoe UI",Font.PLAIN,13)); g.setColor(C_TEAL);
                    g.drawString("WEIGHBRIDGE MANAGEMENT SOFTWARE", 24, 82);
                }
                // Teal accent bar at bottom
                g.setColor(C_TEAL); g.fillRect(0, H-3, W, 3);
                // Right-side subtle teal glow
                GradientPaint rGlow = new GradientPaint(W-240,0,new Color(0,188,188,18),W,0,new Color(0,188,188,0));
                g.setPaint(rGlow); g.fillRect(W-240,0,240,H);
            }
        };
        headerImgPanel.setBackground(C_NAVY);

        // Live clock (top-right of header)
        JLabel clockLbl = new JLabel(); clockLbl.setFont(new Font("Segoe UI",Font.BOLD,14)); clockLbl.setForeground(Color.WHITE); clockLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel dateLbl2 = new JLabel(); dateLbl2.setFont(F_SMALL); dateLbl2.setForeground(new Color(150,190,255)); dateLbl2.setHorizontalAlignment(SwingConstants.RIGHT);
        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> {
            clockLbl.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a")) + "   ");
            dateLbl2.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")) + "   ");
        });
        clockTimer.setInitialDelay(0); clockTimer.start();
        JPanel clockPanel = new JPanel(new GridBagLayout()); clockPanel.setOpaque(false);
        GridBagConstraints cc = new GridBagConstraints();
        cc.gridx=0; cc.gridy=0; cc.anchor=GridBagConstraints.EAST; clockPanel.add(clockLbl,cc);
        cc.gridy=1; clockPanel.add(dateLbl2,cc);
        headerImgPanel.setLayout(new BorderLayout());
        headerImgPanel.add(clockPanel, BorderLayout.EAST);

        // Weighbridge selector bar
        JPanel wbBar = new JPanel(new FlowLayout(FlowLayout.LEFT,12,8));
        wbBar.setBackground(C_NAVY2);
        wbBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0,new Color(0,188,188,70)),
                BorderFactory.createEmptyBorder(2,14,2,14)));
        JLabel wbBarLbl = new JLabel("Weighbridge:");
        wbBarLbl.setFont(F_WB_LBL); wbBarLbl.setForeground(new Color(186,210,255));
        JComboBox<String> wbDropdown = new JComboBox<>();
        wbDropdown.setFont(F_FIELD); wbDropdown.setPreferredSize(new Dimension(230,30));
        JButton addWBBtn = makeBtn("+ Add Weighbridge", BTN_ADDWB); addWBBtn.setPreferredSize(new Dimension(164,30));
        JLabel pipeLbl = new JLabel("  |  "); pipeLbl.setForeground(new Color(71,109,180));
        JLabel addressLbl = new JLabel("Select a weighbridge to begin");
        addressLbl.setFont(F_ADDR); addressLbl.setForeground(new Color(165,200,255));
        wbBar.add(wbBarLbl); wbBar.add(wbDropdown); wbBar.add(addWBBtn); wbBar.add(pipeLbl); wbBar.add(addressLbl);

        JPanel fullHeader = new JPanel(new BorderLayout(0,0));
        fullHeader.setBackground(C_NAVY);
        fullHeader.add(headerImgPanel, BorderLayout.NORTH);
        fullHeader.add(wbBar,          BorderLayout.SOUTH);
        frame.add(fullHeader, BorderLayout.NORTH);

        // ══════════════════  FORM FIELDS  ═════════════════════════════════════
        JTextField rstField      = makeField();
        JTextField dateField     = makeField(); dateField.setText(java.time.LocalDate.now().toString());
        JComboBox<String> vehicleField = new JComboBox<>(); vehicleField.setEditable(true); vehicleField.setFont(F_FIELD); vehicleField.setBackground(C_INPUT_BG);
        JTextField timeField     = makeField(); timeField.setText(java.time.LocalTime.now().withNano(0).toString());
        JTextField materialField = makeField();
        JTextField grossField    = makeField();
        JTextField tareField     = makeField();
        JTextField netField = makeField(); netField.setEditable(false);
        netField.setBackground(C_NET_BG); netField.setForeground(C_NET_FG); netField.setFont(F_NET);
        netField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_NET_BORDER,1),BorderFactory.createEmptyBorder(5,9,5,9)));
        JTextField chargeField   = makeField();
        JTextField customerField = makeField();
        JTextField placeField    = makeField();
        JTextField sourceField   = makeField();

        // ── Card: Transaction Details ─────────────────────────────────────────
        JPanel txCard = makeCard("Transaction Details"); txCard.setLayout(new GridBagLayout());
        GridBagConstraints gTx = new GridBagConstraints(); gTx.insets = new Insets(5,5,5,10); gTx.fill = GridBagConstraints.HORIZONTAL;
        addFormRow(txCard,gTx,0,"RST No:",rstField,"Date:",dateField);
        addFormRow(txCard,gTx,1,"Vehicle No:",vehicleField,"Time:",timeField);
        gTx.gridx=0;gTx.gridy=2;gTx.gridwidth=1;gTx.weightx=0;txCard.add(makeLabel("Material:"),gTx);
        gTx.gridx=1;gTx.gridy=2;gTx.gridwidth=3;gTx.weightx=1.0;txCard.add(materialField,gTx);gTx.gridwidth=1;

        // ── Card: Weight Measurements ─────────────────────────────────────────
        JPanel wtCard = makeCard("Weight Measurements"); wtCard.setLayout(new GridBagLayout());
        GridBagConstraints gWt = new GridBagConstraints(); gWt.insets = new Insets(5,5,5,10); gWt.fill = GridBagConstraints.HORIZONTAL;
        addFormRow(wtCard,gWt,0,"Gross Weight (kg):",grossField,null,null);
        addFormRow(wtCard,gWt,1,"Tare Weight (kg):",tareField,null,null);
        gWt.gridx=0;gWt.gridy=2;gWt.gridwidth=1;gWt.weightx=0;
        JLabel netLbl = makeLabel("Net Weight (kg):"); netLbl.setForeground(C_NET_FG); netLbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        wtCard.add(netLbl,gWt);
        gWt.gridx=1;gWt.gridy=2;gWt.gridwidth=3;gWt.weightx=1.0;wtCard.add(netField,gWt);gWt.gridwidth=1;
        addFormRow(wtCard,gWt,3,"Charges (\u20B9):",chargeField,null,null);

        // ── Card: Customer Details ────────────────────────────────────────────
        JPanel custCard = makeCard("Customer Details"); custCard.setLayout(new GridBagLayout());
        GridBagConstraints gCust = new GridBagConstraints(); gCust.insets = new Insets(5,5,5,10); gCust.fill = GridBagConstraints.HORIZONTAL;
        addFormRow(custCard,gCust,0,"Customer Name:",customerField,null,null);
        addFormRow(custCard,gCust,1,"Place:",placeField,null,null);
        addFormRow(custCard,gCust,2,"Source:",sourceField,null,null);

        // ── Card: Live Bill Preview (coin watermark inside) ────────────────────
        final BufferedImage coinFinal = coin;
        JPanel previewCard = makeCard("Live Bill Preview"); previewCard.setLayout(new BorderLayout());
        JPanel previewBody = new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D)g0;
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(new Color(248,250,255)); g.fillRect(0,0,getWidth(),getHeight());
                // Coin logo as a faint watermark
                if (coinFinal != null) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.055f));
                    int sz = Math.min(getWidth(),getHeight()) - 20;
                    g.drawImage(coinFinal,(getWidth()-sz)/2,(getHeight()-sz)/2,sz,sz,null);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
                }
                int y = 24;
                g.setFont(new Font("Segoe UI",Font.BOLD,12)); g.setColor(new Color(30,60,120));
                g.drawString("RST: "+rstField.getText()+"   Date: "+formatDate(dateField.getText()),14,y); y+=22;
                g.setFont(new Font("Segoe UI",Font.PLAIN,12)); g.setColor(C_LABEL_FG);
                g.drawString("Vehicle: "+vehicleField.getEditor().getItem(),14,y); y+=20;
                g.drawString("Material: "+materialField.getText(),14,y); y+=20;
                g.setColor(C_BORDER); g.fillRect(14,y,getWidth()-28,1); y+=12;
                g.setFont(new Font("Segoe UI",Font.PLAIN,11)); g.setColor(C_SUBLABEL);
                g.drawString("Gross:  "+grossField.getText()+" kg",14,y); y+=18;
                g.drawString("Tare:   "+tareField.getText()+" kg",14,y); y+=18;
                g.setFont(new Font("Segoe UI",Font.BOLD,13)); g.setColor(C_NET_FG);
                g.drawString("Net:    "+netField.getText()+" kg",14,y); y+=22;
                if (!netField.getText().isEmpty()) {
                    g.setFont(new Font("Segoe UI",Font.ITALIC,10)); g.setColor(C_SUBLABEL);
                    g.drawString("("+numberToWords(netField.getText().trim())+")",14,y); y+=20;
                }
                g.setFont(new Font("Segoe UI",Font.PLAIN,12)); g.setColor(C_LABEL_FG);
                g.drawString("Customer: "+customerField.getText(),14,y); y+=18;
                g.drawString("Charges:  \u20B9"+chargeField.getText(),14,y);
            }
        };
        previewBody.setPreferredSize(new Dimension(0,220));
        previewCard.add(previewBody, BorderLayout.CENTER);

        // Preview refresh on keystrokes
        KeyAdapter previewRefresh = new KeyAdapter() { public void keyReleased(KeyEvent e) { previewBody.repaint(); } };
        for (JTextField f : new JTextField[]{rstField,dateField,timeField,materialField,grossField,tareField,chargeField,customerField,placeField,sourceField})
            f.addKeyListener(previewRefresh);

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel leftCol = new JPanel(); leftCol.setLayout(new BoxLayout(leftCol,BoxLayout.Y_AXIS)); leftCol.setBackground(C_PANEL_BG);
        leftCol.add(txCard); leftCol.add(Box.createRigidArea(new Dimension(0,12))); leftCol.add(wtCard);
        JPanel rightCol = new JPanel(); rightCol.setLayout(new BoxLayout(rightCol,BoxLayout.Y_AXIS)); rightCol.setBackground(C_PANEL_BG);
        rightCol.add(custCard); rightCol.add(Box.createRigidArea(new Dimension(0,12))); rightCol.add(previewCard);

        JPanel formGrid = new JPanel(new GridBagLayout()); formGrid.setBackground(C_PANEL_BG);
        GridBagConstraints gForm = new GridBagConstraints(); gForm.fill=GridBagConstraints.BOTH; gForm.weighty=1.0;
        gForm.gridx=0;gForm.gridy=0;gForm.weightx=0.60;gForm.insets=new Insets(0,0,0,14);formGrid.add(leftCol,gForm);
        gForm.gridx=1;gForm.gridy=0;gForm.weightx=0.40;gForm.insets=new Insets(0,0,0,0);formGrid.add(rightCol,gForm);

        JPanel mainBg = new JPanel(new BorderLayout()); mainBg.setBackground(C_PANEL_BG);
        mainBg.setBorder(BorderFactory.createEmptyBorder(16,24,10,24)); mainBg.add(formGrid,BorderLayout.CENTER);
        frame.add(mainBg, BorderLayout.CENTER);

        // ══════════════════  SOUTH  ═══════════════════════════════════════════
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,C_BORDER),BorderFactory.createEmptyBorder(4,26,4,26)));
        JButton saveBtn      = makeBtn("\u2713  Save",         BTN_SAVE);
        JButton printBtn     = makeBtn("\u2399  Print",        BTN_PRINT);
        JButton clearBtn     = makeBtn("\u2715  Clear",        BTN_CLEAR);
        JButton searchBtn    = makeBtn("\u2315  Search Bills", BTN_SEARCH); searchBtn.setPreferredSize(new Dimension(160,38));
        JButton updateBtn    = makeBtn("\u270E  Update Bill",  BTN_UPDATE);
        JButton savePrintBtn = makeBtn("\u26A1  Save & Print", BTN_SAVEPRINT); savePrintBtn.setPreferredSize(new Dimension(160,38));
        btnPanel.add(saveBtn); btnPanel.add(printBtn); btnPanel.add(clearBtn);
        btnPanel.add(searchBtn); btnPanel.add(updateBtn); btnPanel.add(savePrintBtn);

        JPanel statusBar = new JPanel(new BorderLayout()); statusBar.setBackground(new Color(241,245,249));
        statusBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,C_BORDER),BorderFactory.createEmptyBorder(4,16,4,16)));
        statusLabel = new JLabel("  Ready \u2014 Welcome to N-KARTECH Weighbridge Software");
        statusLabel.setFont(F_STATUS); statusLabel.setForeground(C_SUBLABEL);
        JLabel verLbl = new JLabel("Weighbridge Billing Software  v2.0  |  \u00A9 N-kartech   ");
        verLbl.setFont(F_STATUS); verLbl.setForeground(new Color(100,130,170));
        statusBar.add(statusLabel,BorderLayout.WEST); statusBar.add(verLbl,BorderLayout.EAST);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(btnPanel,BorderLayout.CENTER); southPanel.add(statusBar,BorderLayout.SOUTH);
        frame.add(southPanel, BorderLayout.SOUTH);

        // ══════════════════  LOAD DB DATA  ════════════════════════════════════
        try { Connection c=DriverManager.getConnection("jdbc:sqlite:weighbridge.db"); ResultSet rs=c.createStatement().executeQuery("SELECT * FROM weighbridges"); while(rs.next()) wbDropdown.addItem(rs.getString("name")); c.close(); } catch(Exception ex){ ex.printStackTrace(); }
        if (wbDropdown.getItemCount()>0) {
            try { Connection c=DriverManager.getConnection("jdbc:sqlite:weighbridge.db"); PreparedStatement p=c.prepareStatement("SELECT * FROM weighbridges WHERE name=?"); p.setString(1,wbDropdown.getItemAt(0).toString()); ResultSet rs=p.executeQuery(); if(rs.next()){selectedAddress=rs.getString("address");selectedPhone=rs.getString("phone");addressLbl.setText(selectedAddress+"   \u260E  "+selectedPhone);} c.close(); } catch(Exception ex){ ex.printStackTrace(); }
        }
        try { Connection c=DriverManager.getConnection("jdbc:sqlite:weighbridge.db"); ResultSet rs=c.createStatement().executeQuery("SELECT * FROM vehicles"); while(rs.next()) vehicleField.addItem(rs.getString("vehicle_no")); c.close(); } catch(Exception ex){ ex.printStackTrace(); }

        // ══════════════════  EVENTS  ══════════════════════════════════════════
        wbDropdown.addActionListener(e -> {
            if(wbDropdown.getSelectedItem()==null) return;
            try { Connection c=DriverManager.getConnection("jdbc:sqlite:weighbridge.db"); PreparedStatement p=c.prepareStatement("SELECT * FROM weighbridges WHERE name=?"); p.setString(1,wbDropdown.getSelectedItem().toString()); ResultSet rs=p.executeQuery(); if(rs.next()){selectedAddress=rs.getString("address");selectedPhone=rs.getString("phone");addressLbl.setText(selectedAddress+"   \u260E  "+selectedPhone);} c.close(); } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Add Weighbridge dialog — coin logo in dialog header
        addWBBtn.addActionListener(e -> {
            JDialog d = new JDialog(frame,"Add New Weighbridge",true);
            d.setSize(460,320); d.setLocationRelativeTo(frame); d.setLayout(new BorderLayout(0,0));
            if (coin!=null) d.setIconImage(coin);
            JPanel dHead = new JPanel(new BorderLayout(12,0)); dHead.setBackground(C_NAVY); dHead.setBorder(BorderFactory.createEmptyBorder(14,18,14,18));
            if (coinFinal!=null) { Image dlgIcon=scaleImg(coinFinal,48,48); dHead.add(new JLabel(new ImageIcon(dlgIcon)),BorderLayout.WEST); }
            JPanel dHeadText = new JPanel(new GridBagLayout()); dHeadText.setOpaque(false);
            JLabel dTitleLbl=new JLabel("Add New Weighbridge"); dTitleLbl.setFont(F_DLG_TTL); dTitleLbl.setForeground(Color.WHITE);
            JLabel dSubLbl=new JLabel("Enter weighbridge details to register"); dSubLbl.setFont(F_SMALL); dSubLbl.setForeground(new Color(150,190,255));
            GridBagConstraints dHgc=new GridBagConstraints(); dHgc.gridx=0;dHgc.gridy=0;dHgc.anchor=GridBagConstraints.WEST; dHeadText.add(dTitleLbl,dHgc); dHgc.gridy=1; dHeadText.add(dSubLbl,dHgc);
            dHead.add(dHeadText,BorderLayout.CENTER);
            JPanel dAccent=new JPanel(); dAccent.setBackground(C_TEAL); dAccent.setPreferredSize(new Dimension(1,3));
            JPanel dHeaderWrap=new JPanel(new BorderLayout()); dHeaderWrap.add(dHead,BorderLayout.CENTER); dHeaderWrap.add(dAccent,BorderLayout.SOUTH);
            JPanel dForm=new JPanel(new GridBagLayout()); dForm.setBackground(C_PANEL_BG); dForm.setBorder(BorderFactory.createEmptyBorder(22,26,18,26));
            GridBagConstraints dg=new GridBagConstraints(); dg.insets=new Insets(7,7,7,7); dg.fill=GridBagConstraints.HORIZONTAL;
            JTextField dName=makeField(),dAddr=makeField(),dPhone=makeField();
            dg.gridx=0;dg.gridy=0;dg.weightx=0;dForm.add(makeLabel("Name:"),dg); dg.gridx=1;dg.gridy=0;dg.weightx=1.0;dForm.add(dName,dg);
            dg.gridx=0;dg.gridy=1;dg.weightx=0;dForm.add(makeLabel("Address:"),dg); dg.gridx=1;dg.gridy=1;dg.weightx=1.0;dForm.add(dAddr,dg);
            dg.gridx=0;dg.gridy=2;dg.weightx=0;dForm.add(makeLabel("Phone:"),dg); dg.gridx=1;dg.gridy=2;dg.weightx=1.0;dForm.add(dPhone,dg);
            JPanel dBtns=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,12)); dBtns.setBackground(Color.WHITE); dBtns.setBorder(BorderFactory.createMatteBorder(1,0,0,0,C_BORDER));
            JButton dCancel=makeBtn("Cancel",BTN_CANCEL); dCancel.setPreferredSize(new Dimension(100,34));
            JButton dSave=makeBtn("Save",BTN_SAVE);       dSave.setPreferredSize(new Dimension(100,34));
            dCancel.addActionListener(ev->d.dispose());
            dSave.addActionListener(ev->{ try{ Connection conn=DriverManager.getConnection("jdbc:sqlite:weighbridge.db"); PreparedStatement pst=conn.prepareStatement("INSERT INTO weighbridges (name,address,phone) VALUES (?,?,?)"); pst.setString(1,dName.getText());pst.setString(2,dAddr.getText());pst.setString(3,dPhone.getText());pst.executeUpdate();conn.close();wbDropdown.addItem(dName.getText());setStatus("\u2713 Weighbridge added: "+dName.getText());d.dispose();}catch(Exception ex){ex.printStackTrace();}});
            dBtns.add(dCancel); dBtns.add(dSave);
            d.add(dHeaderWrap,BorderLayout.NORTH); d.add(dForm,BorderLayout.CENTER); d.add(dBtns,BorderLayout.SOUTH);
            d.setVisible(true);
        });

        // Net weight auto-calc
        KeyAdapter calc = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try { double net=Double.parseDouble(grossField.getText())-Double.parseDouble(tareField.getText()); netField.setText(String.valueOf((int)net)); } catch(Exception ex){ netField.setText(""); }
                previewBody.repaint();
            }
        };
        grossField.addKeyListener(calc); tareField.addKeyListener(calc);

        // Save
        saveBtn.addActionListener(e -> {
            try {
                Connection conn=DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                PreparedStatement pst=conn.prepareStatement("INSERT INTO bills (weighbridge,rst,date,time,vehicle,material,gross,tare,net,charges,customer_name,place,source) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                pst.setString(1,(String)wbDropdown.getSelectedItem());pst.setString(2,rstField.getText());pst.setString(3,dateField.getText());pst.setString(4,timeField.getText());
                pst.setString(5,vehicleField.getSelectedItem().toString());pst.setString(6,materialField.getText());pst.setString(7,grossField.getText());pst.setString(8,tareField.getText());
                pst.setString(9,netField.getText());pst.setString(10,chargeField.getText());pst.setString(11,customerField.getText());pst.setString(12,placeField.getText());pst.setString(13,sourceField.getText());
                pst.executeUpdate();conn.close();
                Connection cv=DriverManager.getConnection("jdbc:sqlite:weighbridge.db");PreparedStatement cp=cv.prepareStatement("SELECT * FROM vehicles WHERE vehicle_no=?");cp.setString(1,vehicleField.getSelectedItem().toString());
                if(!cp.executeQuery().next()){PreparedStatement ip=cv.prepareStatement("INSERT INTO vehicles(vehicle_no) VALUES(?)");ip.setString(1,vehicleField.getSelectedItem().toString());ip.executeUpdate();vehicleField.addItem(vehicleField.getSelectedItem().toString());}cv.close();
                setStatus("\u2713 Bill saved \u2014 RST: "+rstField.getText());
                JOptionPane.showMessageDialog(frame,"Bill Saved Successfully \u2705","Saved",JOptionPane.INFORMATION_MESSAGE);
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Update
        updateBtn.addActionListener(e -> {
            try {
                Connection conn=DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                PreparedStatement pst=conn.prepareStatement("UPDATE bills SET vehicle=?,material=?,gross=?,tare=?,net=?,charges=?,customer_name=?,place=?,source=? WHERE rst=?");
                pst.setString(1,vehicleField.getSelectedItem().toString());pst.setString(2,materialField.getText());pst.setString(3,grossField.getText());pst.setString(4,tareField.getText());
                pst.setString(5,netField.getText());pst.setString(6,chargeField.getText());pst.setString(7,customerField.getText());pst.setString(8,placeField.getText());pst.setString(9,sourceField.getText());pst.setString(10,rstField.getText());
                int rows=pst.executeUpdate();conn.close();
                if(rows>0) setStatus("\u2713 Bill updated \u2014 RST: "+rstField.getText());
                JOptionPane.showMessageDialog(frame,rows>0?"Bill Updated \u2705":"RST not found");
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Print (100% original logic preserved)
        printBtn.addActionListener(e -> {
            try {
                java.awt.print.PrinterJob job=java.awt.print.PrinterJob.getPrinterJob();
                job.setPrintable((graphics,pageFormat,pageIndex)->{
                    if(pageIndex>0) return java.awt.print.Printable.NO_SUCH_PAGE;
                    Graphics2D g=(Graphics2D)graphics;
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    Font fName=new Font("Arial",Font.BOLD,20),fNormal=new Font("Arial",Font.PLAIN,15),fWtLbl=new Font("Arial",Font.BOLD,16),fNetLbl=new Font("Arial",Font.BOLD,18),fFooter=new Font("Arial",Font.PLAIN,13),fBrand=new Font("Arial",Font.BOLD,11);
                    String wbName=wbDropdown.getSelectedItem()!=null?wbDropdown.getSelectedItem().toString():"";
                    String addrLine="Address: "+selectedAddress,phoneLine=(selectedPhone!=null&&!selectedPhone.isEmpty())?"Phone: "+selectedPhone:"";
                    String grossStr=grossField.getText().trim(),tareStr=tareField.getText().trim(),netStr=netField.getText().trim();
                    try{grossStr=String.valueOf((int)Double.parseDouble(grossStr));}catch(Exception ig){}try{tareStr=String.valueOf((int)Double.parseDouble(tareStr));}catch(Exception ig){}try{netStr=String.valueOf((int)Double.parseDouble(netStr));}catch(Exception ig){}
                    String dateFmt=formatDate(dateField.getText()),timeFmt=formatTime(timeField.getText()),dateTime="Date: "+dateFmt+"   Time: "+timeFmt;
                    String rstLine="RST No: "+rstField.getText(),vehLine="Vehicle No: "+(vehicleField.getSelectedItem()!=null?vehicleField.getSelectedItem().toString():""),matLine="Material: "+materialField.getText();
                    String custName=customerField.getText().trim(),placeVal=placeField.getText().trim(),sourceVal=sourceField.getText().trim();
                    String custLine="Customer: "+(custName.isEmpty()?"____________":custName),placeLine="Place:    "+(placeVal.isEmpty()?"____________":placeVal),srcLine="Source:   "+(sourceVal.isEmpty()?"____________":sourceVal);
                    String chgLine="Charges: "+chargeField.getText(),opLine="Operator Sign: ________________",wordsStr=numberToWords(netStr);
                    int padL=14,padR=14,rowH=24,secGap=8;
                    g.setFont(fName);FontMetrics fmN=g.getFontMetrics();g.setFont(fNormal);FontMetrics fmP=g.getFontMetrics();g.setFont(fWtLbl);FontMetrics fmW=g.getFontMetrics();g.setFont(fNetLbl);FontMetrics fmNW=g.getFontMetrics();g.setFont(fBrand);FontMetrics fmB=g.getFontMetrics();
                    int GAP=8,lblW=Math.max(fmW.stringWidth("GROSS  WT"),Math.max(fmW.stringWidth("TARE    WT"),fmNW.stringWidth("NET      WT")));
                    int colonW=fmW.stringWidth("::"),numW=Math.max(fmW.stringWidth(grossStr),Math.max(fmW.stringWidth(tareStr),fmNW.stringWidth(netStr)));
                    int kgW=fmW.stringWidth("kg"),dtW=fmP.stringWidth(dateTime),wordsW=fmW.stringWidth(wordsStr);
                    int wtLeftW=padL+lblW+GAP+colonW+GAP+numW+GAP+kgW+GAP*2,wtRightW=Math.max(dtW,wordsW)+padR,wFromWT=wtLeftW+wtRightW;
                    int infoLeftW=Math.max(fmP.stringWidth(rstLine),Math.max(fmP.stringWidth(vehLine),fmP.stringWidth(matLine)));
                    int infoRightW=Math.max(fmP.stringWidth(custLine),Math.max(fmP.stringWidth(placeLine),fmP.stringWidth(srcLine)));
                    int wFromInfo=padL+infoLeftW+GAP*3+infoRightW+padR,wFromHeader=Math.max(fmN.stringWidth(wbName),Math.max(fmP.stringWidth(addrLine),fmP.stringWidth(phoneLine)))+padL+padR;
                    int wFromCharges=padL+fmP.stringWidth(chgLine)+GAP*4+fmP.stringWidth(opLine)+padR,wFromFooter=fmP.stringWidth("Please check weight before leaving")+padL+padR;
                    int w=Math.max(480,Math.max(wFromWT,Math.max(wFromInfo,Math.max(wFromHeader,Math.max(wFromCharges,wFromFooter)))));
                    int pageW=(int)pageFormat.getImageableWidth(),totalNeeded=w+20;
                    if(totalNeeded>pageW){double scale=(double)pageW/totalNeeded;g.translate(pageFormat.getImageableX(),pageFormat.getImageableY());g.scale(scale,scale);}
                    else g.translate(pageFormat.getImageableX(),pageFormat.getImageableY());
                    int x=10,y=10,col0=x+padL,col1=col0+lblW+GAP,col2=col1+colonW+GAP+numW,col3=col2+GAP,col4=col3+kgW+GAP*2;
                    int infoRight=Math.max(x+padL+infoLeftW+GAP*3,x+w/2+10);
                    int cur=y+22;FontMetrics fm;
                    g.setFont(fName);fm=g.getFontMetrics();g.drawString(wbName,x+(w-fm.stringWidth(wbName))/2,cur);cur+=rowH+2;
                    g.setFont(fNormal);fm=g.getFontMetrics();g.drawString(addrLine,x+(w-fm.stringWidth(addrLine))/2,cur);
                    if(!phoneLine.isEmpty()){cur+=rowH;fm=g.getFontMetrics();g.drawString(phoneLine,x+(w-fm.stringWidth(phoneLine))/2,cur);}
                    cur+=secGap+6;g.drawLine(x,cur,x+w,cur);cur+=4;g.drawLine(x,cur,x+w,cur);
                    cur+=rowH;g.setFont(fNormal);g.drawString(rstLine,col0,cur);g.drawString(custLine,infoRight,cur);
                    cur+=rowH;g.drawString(vehLine,col0,cur);g.drawString(placeLine,infoRight,cur);
                    cur+=rowH;g.drawString(matLine,col0,cur);g.drawString(srcLine,infoRight,cur);
                    cur+=secGap+8;g.drawLine(x,cur,x+w,cur);cur+=rowH+4;
                    g.setFont(fWtLbl);fm=g.getFontMetrics();g.drawString("GROSS  WT",col0,cur);g.drawString("::",col1,cur);g.drawString(grossStr,col2-fm.stringWidth(grossStr),cur);g.drawString("kg",col3,cur);g.setFont(fNormal);g.drawString(dateTime,col4,cur);cur+=rowH+4;
                    g.setFont(fWtLbl);fm=g.getFontMetrics();g.drawString("TARE    WT",col0,cur);g.drawString("::",col1,cur);g.drawString(tareStr,col2-fm.stringWidth(tareStr),cur);g.drawString("kg",col3,cur);g.setFont(fNormal);g.drawString(dateTime,col4,cur);cur+=rowH+6;
                    g.setFont(fNetLbl);fm=g.getFontMetrics();g.drawString("NET      WT",col0,cur);g.drawString("::",col1,cur);g.drawString(netStr,col2-fm.stringWidth(netStr),cur);g.drawString("kg",col3,cur);g.setFont(fWtLbl);g.drawString(wordsStr,col4,cur);
                    cur+=secGap+8;g.drawLine(x,cur,x+w,cur);cur+=rowH;g.setFont(fNormal);g.drawString(chgLine,col0,cur);g.drawString(opLine,x+w-fmP.stringWidth(opLine)-padR,cur);
                    cur+=secGap+8;g.drawLine(x,cur,x+w,cur);cur+=rowH-2;g.setFont(fFooter);fm=g.getFontMetrics();String f1="Please check weight before leaving";g.drawString(f1,x+(w-fm.stringWidth(f1))/2,cur);
                    cur+=rowH-4;String f2="** Thank you visit again **";g.drawString(f2,x+(w-fm.stringWidth(f2))/2,cur);
                    cur+=12;g.setFont(fBrand);g.drawString("N-kartech",x+w-fmB.stringWidth("N-kartech")-padR,cur);g.drawRect(x,y,w,cur+10-y);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });
                String tc=tareField.getText().trim();boolean tz=tc.isEmpty()||tc.equals("0")||tc.equals("0.0");
                if(tz){int c=JOptionPane.showConfirmDialog(frame,"Warning: Tare Weight is ZERO or empty!\nAre you sure you want to print?","Tare Weight Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(c!=JOptionPane.YES_OPTION)return;}
                if(job.printDialog()) job.print();
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        savePrintBtn.addActionListener(e->{ String tc=tareField.getText().trim();boolean tz=tc.isEmpty()||tc.equals("0")||tc.equals("0.0");if(tz){int c=JOptionPane.showConfirmDialog(frame,"Warning: Tare Weight is ZERO!\nSave & Print anyway?","Tare Weight Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(c!=JOptionPane.YES_OPTION)return;}saveBtn.doClick();printBtn.doClick();});
        clearBtn.addActionListener(e->{ rstField.setText("");vehicleField.setSelectedIndex(-1);materialField.setText("");grossField.setText("");tareField.setText("");netField.setText("");chargeField.setText("");customerField.setText("");placeField.setText("");sourceField.setText("");previewBody.repaint();setStatus("  Form cleared");});

        // ── Search dialog ──────────────────────────────────────────────────────
        searchBtn.addActionListener(e -> {
            JFrame sf = new JFrame("N-KARTECH  \u00B7  Bill Search & Export");
            sf.setSize(1140,720); sf.setLayout(new BorderLayout(0,0));
            if(coin!=null) sf.setIconImage(coin);

            // Dialog header — banner logo painted inside
            JPanel sHead = new JPanel(null) {
                @Override public Dimension getPreferredSize() { return new Dimension(0,80); }
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g=(Graphics2D)g0;g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
                    int W=getWidth(),H=getHeight();
                    g.setColor(C_NAVY);g.fillRect(0,0,W,H);
                    g.setColor(new Color(255,255,255,10));for(int x=0;x<W;x+=50)g.drawLine(x,0,x,H);
                    if(bannerFinal!=null){int bh=H-8,bw=(int)(bh*2.66);g.drawImage(bannerFinal,2,4,bw,bh,null);}
                    g.setColor(C_TEAL);g.fillRect(0,H-3,W,3);
                }
            };
            sHead.setBackground(C_NAVY);
            JLabel sTitleLbl=new JLabel("Bill Search & Export   "); sTitleLbl.setFont(new Font("Segoe UI",Font.BOLD,14));sTitleLbl.setForeground(Color.WHITE);sTitleLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            sTitleLbl.setBounds(0,0,1100,76); sHead.add(sTitleLbl);

            JPanel sControls=new JPanel(new FlowLayout(FlowLayout.LEFT,12,10)); sControls.setBackground(C_PANEL_BG);
            sControls.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,C_BORDER),BorderFactory.createEmptyBorder(6,16,6,16)));
            JTextField keyField=makeField();keyField.setPreferredSize(new Dimension(190,30));
            JTextField dateSearchField=makeField();dateSearchField.setPreferredSize(new Dimension(155,30));
            JButton fb=makeBtn("Search",BTN_SEARCH);fb.setPreferredSize(new Dimension(120,32));
            sControls.add(makeLabel("Vehicle / RST:")); sControls.add(keyField);
            sControls.add(makeLabel("  Date (yyyy-mm-dd):")); sControls.add(dateSearchField); sControls.add(fb);
            JLabel hint=new JLabel("    * Leave blank to match all.  e.g. 2026-03-01  or  2026-03");
            hint.setFont(F_SMALL);hint.setForeground(C_SUBLABEL);sControls.add(hint);

            String[] cols={"RST No","Date","Time","Vehicle","Material","Gross (kg)","Tare (kg)","Net (kg)","Charges","Customer","Place","Source"};
            javax.swing.table.DefaultTableModel model=new javax.swing.table.DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            JTable table=new JTable(model);table.setFont(F_FIELD);table.setRowHeight(26);table.setGridColor(new Color(226,232,240));table.setSelectionBackground(C_ACCENT_SOFT);table.setSelectionForeground(C_NAVY);
            table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));table.getTableHeader().setBackground(C_NAVY);table.getTableHeader().setForeground(Color.WHITE);table.getTableHeader().setPreferredSize(new Dimension(0,34));
            table.setDefaultRenderer(Object.class,new javax.swing.table.DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){super.getTableCellRendererComponent(t,v,sel,foc,r,c);if(!sel)setBackground(r%2==0?Color.WHITE:new Color(248,250,255));setForeground(sel?C_NAVY:C_LABEL_FG);setBorder(BorderFactory.createEmptyBorder(0,8,0,8));return this;}});
            int[] colW={55,75,65,95,75,65,65,65,60,80,70,65};for(int i=0;i<colW.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(colW[i]);
            JScrollPane sp=new JScrollPane(table);sp.setBorder(BorderFactory.createLineBorder(C_BORDER,1));sp.getViewport().setBackground(Color.WHITE);
            JPanel cp=new JPanel(new BorderLayout());cp.setBackground(C_PANEL_BG);cp.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));cp.add(sp,BorderLayout.CENTER);

            JPanel sBottom=new JPanel(new BorderLayout());sBottom.setBackground(Color.WHITE);sBottom.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,C_BORDER),BorderFactory.createEmptyBorder(10,18,10,18)));
            JLabel countLabel=new JLabel("Results: 0");countLabel.setFont(F_COUNT);countLabel.setForeground(C_LABEL_FG);
            JPanel exportBtns=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));exportBtns.setBackground(Color.WHITE);
            JButton excelBtn=makeBtn("Export to Excel (CSV)",BTN_CSV);excelBtn.setEnabled(false);excelBtn.setPreferredSize(new Dimension(200,34));
            JButton pdfBtn=makeBtn("Export to PDF",BTN_PDF);pdfBtn.setEnabled(false);pdfBtn.setPreferredSize(new Dimension(150,34));
            exportBtns.add(excelBtn);exportBtns.add(pdfBtn);sBottom.add(countLabel,BorderLayout.WEST);sBottom.add(exportBtns,BorderLayout.EAST);

            JPanel sTopPanel=new JPanel(new BorderLayout());sTopPanel.add(sHead,BorderLayout.NORTH);sTopPanel.add(sControls,BorderLayout.SOUTH);
            sf.add(sTopPanel,BorderLayout.NORTH);sf.add(cp,BorderLayout.CENTER);sf.add(sBottom,BorderLayout.SOUTH);

            fb.addActionListener(ev->{try{Connection conn=DriverManager.getConnection("jdbc:sqlite:weighbridge.db");String keyword=keyField.getText().trim(),dateVal=dateSearchField.getText().trim();StringBuilder sql=new StringBuilder("SELECT * FROM bills WHERE 1=1");if(!keyword.isEmpty())sql.append(" AND (vehicle LIKE ? OR rst LIKE ?)");if(!dateVal.isEmpty())sql.append(" AND date LIKE ?");sql.append(" ORDER BY id DESC");PreparedStatement pst=conn.prepareStatement(sql.toString());int idx=1;if(!keyword.isEmpty()){pst.setString(idx++,"%"+keyword+"%");pst.setString(idx++,"%"+keyword+"%");}if(!dateVal.isEmpty())pst.setString(idx++,"%"+dateVal+"%");ResultSet rs=pst.executeQuery();model.setRowCount(0);int count=0;while(rs.next()){model.addRow(new Object[]{rs.getString("rst"),rs.getString("date"),rs.getString("time"),rs.getString("vehicle"),rs.getString("material"),rs.getString("gross"),rs.getString("tare"),rs.getString("net"),rs.getString("charges"),rs.getString("customer_name"),rs.getString("place"),rs.getString("source")});count++;}conn.close();countLabel.setText("Results: "+count);excelBtn.setEnabled(count>0);pdfBtn.setEnabled(count>0);}catch(Exception ex){ex.printStackTrace();}});
            java.awt.event.ActionListener enterSearch=ev->fb.doClick();keyField.addActionListener(enterSearch);dateSearchField.addActionListener(enterSearch);

            excelBtn.addActionListener(ev->{JFileChooser fc=new JFileChooser();fc.setSelectedFile(new java.io.File("WeighbridgeBills.csv"));fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files (*.csv)","csv"));if(fc.showSaveDialog(sf)!=JFileChooser.APPROVE_OPTION)return;java.io.File file=fc.getSelectedFile();if(!file.getName().endsWith(".csv"))file=new java.io.File(file.getPath()+".csv");try(java.io.PrintWriter pw=new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(file),java.nio.charset.StandardCharsets.UTF_8))){pw.print("\uFEFF");StringBuilder header=new StringBuilder();for(int c=0;c<model.getColumnCount();c++){if(c>0)header.append(",");header.append("\"").append(model.getColumnName(c)).append("\"");}pw.println(header);for(int r=0;r<model.getRowCount();r++){StringBuilder row=new StringBuilder();for(int c=0;c<model.getColumnCount();c++){if(c>0)row.append(",");Object val=model.getValueAt(r,c);String cell=val==null?"":val.toString().replace("\"","\"\"");if((c==1||c==2)&&!cell.isEmpty())cell="\t"+cell;row.append("\"").append(cell).append("\"");}pw.println(row);}JOptionPane.showMessageDialog(sf,"Excel file saved!\n"+file.getAbsolutePath()+"\n\nOpen with Microsoft Excel or Google Sheets.","Export Successful",JOptionPane.INFORMATION_MESSAGE);try{java.awt.Desktop.getDesktop().open(file);}catch(Exception ig){}}catch(Exception ex){JOptionPane.showMessageDialog(sf,"Error saving file:\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}});
            pdfBtn.addActionListener(ev->{JFileChooser fc=new JFileChooser();fc.setSelectedFile(new java.io.File("WeighbridgeBills.pdf"));fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files (*.pdf)","pdf"));if(fc.showSaveDialog(sf)!=JFileChooser.APPROVE_OPTION)return;java.io.File file=fc.getSelectedFile();if(!file.getName().endsWith(".pdf"))file=new java.io.File(file.getPath()+".pdf");try{exportTableAsPDF(file,model,keyField.getText().trim(),dateSearchField.getText().trim());JOptionPane.showMessageDialog(sf,"PDF saved!\n"+file.getAbsolutePath(),"Export Successful",JOptionPane.INFORMATION_MESSAGE);try{java.awt.Desktop.getDesktop().open(file);}catch(Exception ig){}}catch(Exception ex){JOptionPane.showMessageDialog(sf,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);ex.printStackTrace();}});
            sf.setLocationRelativeTo(frame);sf.setVisible(true);
        });

        frame.setVisible(true);
    }

    // ═══════════════════════  PDF TABLE EXPORT (PRESERVED)  ═══════════════════
    static void exportTableAsPDF(java.io.File file,javax.swing.table.DefaultTableModel model,String keyFilter,String dateFilter) throws Exception {
        final int PW=842,PH=595,ML=30,MR=30,MT=40,MB=30,contentW=PW-ML-MR;
        int[] cw={40,55,50,80,65,55,55,60,50,65,55,50};int totalCW=0;for(int v:cw)totalCW+=v;int[] fcw=new int[cw.length];for(int i=0;i<cw.length;i++)fcw[i]=(int)((double)cw[i]/totalCW*contentW);
        final int ROWS_PER_PAGE=28;int rowCount=model.getRowCount(),colCount=model.getColumnCount(),totalPages=Math.max(1,(rowCount+ROWS_PER_PAGE-1)/ROWS_PER_PAGE);
        java.util.List<BufferedImage> pages=new java.util.ArrayList<>();
        for(int pageIndex=0;pageIndex<totalPages;pageIndex++){
            BufferedImage img=new BufferedImage(PW,PH,BufferedImage.TYPE_INT_RGB);Graphics2D g=img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.WHITE);g.fillRect(0,0,PW,PH);g.setColor(Color.BLACK);
            int cur=MT;g.setFont(new Font("Arial",Font.BOLD,18));FontMetrics fm=g.getFontMetrics();String titleStr="WEIGHBRIDGE BILLING REPORT";g.drawString(titleStr,ML+(contentW-fm.stringWidth(titleStr))/2,cur);cur+=20;
            g.setFont(new Font("Arial",Font.PLAIN,9));fm=g.getFontMetrics();String sub="Generated: "+java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));if(!keyFilter.isEmpty())sub+="   Search: "+keyFilter;if(!dateFilter.isEmpty())sub+="   Date: "+dateFilter;sub+="   |   Page "+(pageIndex+1)+" of "+totalPages+"   |   Total Records: "+rowCount;g.drawString(sub,ML+(contentW-fm.stringWidth(sub))/2,cur);cur+=6;
            g.setColor(new Color(255,255,255));g.fillRect(ML,cur,contentW,2);g.setColor(Color.BLACK);cur+=10;g.setColor(new Color(255,255,255));g.fillRect(ML,cur,contentW,18);
            g.setFont(new Font("Arial",Font.BOLD,9));g.setColor(Color.black);int cx=ML+3;for(int c=0;c<colCount;c++){g.drawString(model.getColumnName(c),cx,cur+13);cx+=fcw[c];}cur+=18;g.setColor(Color.BLACK);
            int startRow=pageIndex*ROWS_PER_PAGE,endRow=Math.min(startRow+ROWS_PER_PAGE,rowCount);g.setFont(new Font("Arial",Font.PLAIN,9));fm=g.getFontMetrics();
            for(int r=startRow;r<endRow;r++){if(r%2==0){g.setColor(new Color(235,243,255));g.fillRect(ML,cur,contentW,16);}g.setColor(Color.BLACK);cx=ML+3;for(int c=0;c<colCount;c++){Object val=model.getValueAt(r,c);String cell=val==null?"":val.toString();while(cell.length()>1&&fm.stringWidth(cell)>fcw[c]-5)cell=cell.substring(0,cell.length()-1);g.drawString(cell,cx,cur+12);cx+=fcw[c];}g.setColor(new Color(210,210,210));g.drawLine(ML,cur+16,ML+contentW,cur+16);g.setColor(Color.BLACK);cur+=16;}
            g.setColor(new Color(255,255,255));g.fillRect(ML,cur+4,contentW,2);cur+=12;g.setFont(new Font("Arial",Font.ITALIC,8));g.setColor(new Color(120,120,120));g.drawString("Developed by N-kartech  |  Weighbridge Billing Software",ML,PH-MB);g.dispose();pages.add(img);
        }
        writePDF(file,pages,PW,PH);
    }

    // ═══════════════════════  PDF WRITER (PRESERVED)  ═════════════════════════
    static void writePDF(java.io.File file,java.util.List<BufferedImage> pages,int pw,int ph) throws Exception {
        java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
        java.util.List<Integer> pageObjIds=new java.util.ArrayList<>(),imgObjIds=new java.util.ArrayList<>();
        java.util.function.Consumer<String> w=s->{try{out.write((s+"\n").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));}catch(Exception e){throw new RuntimeException(e);}};
        w.accept("%PDF-1.4");w.accept("%\u00e2\u00e3\u00cf\u00d3");int objId=1;
        w.accept(objId+" 0 obj");w.accept("<<");w.accept("/Type /Catalog");w.accept("/Pages 2 0 R");w.accept(">>");w.accept("endobj");objId++;
        String pagesPlaceholder=objId+" 0 obj\n<<\n/Type /Pages\n/Kids [KIDS]\n/Count "+pages.size()+"\n>>\nendobj\n";out.write(pagesPlaceholder.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));objId++;
        for(int i=0;i<pages.size();i++){BufferedImage img=pages.get(i);java.io.ByteArrayOutputStream jpegOut=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(img,"jpeg",jpegOut);byte[] jpegBytes=jpegOut.toByteArray();imgObjIds.add(objId);String imgHdr=objId+" 0 obj\n<<\n/Type /XObject\n/Subtype /Image\n/Width "+img.getWidth()+"\n/Height "+img.getHeight()+"\n/ColorSpace /DeviceRGB\n/BitsPerComponent 8\n/Filter /DCTDecode\n/Length "+jpegBytes.length+"\n>>\nstream\n";out.write(imgHdr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));out.write(jpegBytes);out.write("\nendstream\nendobj\n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));objId++;pageObjIds.add(objId);String contentStr="q "+pw+" 0 0 "+ph+" 0 0 cm /Im"+i+" Do Q";byte[] contentBytes=contentStr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);String pageObj=objId+" 0 obj\n<<\n/Type /Page\n/Parent 2 0 R\n/MediaBox [0 0 "+pw+" "+ph+"]\n/Resources <<\n  /XObject << /Im"+i+" "+(objId-1)+" 0 R >>\n>>\n/Contents "+(objId+1)+" 0 R\n>>\nendobj\n";out.write(pageObj.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));objId++;String contentHdr=objId+" 0 obj\n<<\n/Length "+contentBytes.length+"\n>>\nstream\n";out.write(contentHdr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));out.write(contentBytes);out.write("\nendstream\nendobj\n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));objId++;}
        StringBuilder kids=new StringBuilder();for(int id:pageObjIds){if(kids.length()>0)kids.append(" ");kids.append(id+" 0 R");}
        String correctPages="2 0 obj\n<<\n/Type /Pages\n/Kids ["+kids+"]\n/Count "+pages.size()+"\n>>\nendobj\n";
        byte[] rawOut=out.toByteArray();String rawStr=new String(rawOut,java.nio.charset.StandardCharsets.ISO_8859_1);String corrected=rawStr.replace(pagesPlaceholder,correctPages);byte[] finalBytes=corrected.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        java.util.List<Integer> xrefOffsets=new java.util.ArrayList<>();int totalObjs=objId-1;for(int n=1;n<=totalObjs;n++){String marker=n+" 0 obj";int pos=corrected.indexOf(marker);xrefOffsets.add(pos<0?0:pos);}
        java.io.ByteArrayOutputStream finalOut=new java.io.ByteArrayOutputStream();finalOut.write(finalBytes);int xrefOffset=finalOut.size();
        finalOut.write(("xref\n0 "+objId+"\n").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));finalOut.write("0000000000 65535 f \n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));for(int off:xrefOffsets)finalOut.write((String.format("%010d 00000 n \n",off)).getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        finalOut.write(("trailer\n<<\n/Size "+objId+"\n/Root 1 0 R\n>>\nstartxref\n"+xrefOffset+"\n%%EOF\n").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        try(java.io.FileOutputStream fos=new java.io.FileOutputStream(file)){finalOut.writeTo(fos);}
    }
}