import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Main {
    static String selectedAddress = "";
    static String selectedPhone = "";

    public static String formatDate(String input){
        try{
            java.time.LocalDate d = java.time.LocalDate.parse(input);
            return d.getDayOfMonth()+"/"+d.getMonthValue()+"/"+d.getYear();
        }catch(Exception e){ return input; }
    }

    public static String formatTime(String input){
        try{
            java.time.LocalTime t = java.time.LocalTime.parse(input);
            return t.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
        }catch(Exception e){ return input; }
    }

    public static String numberToWords(String num){
        String[] map={"ZERO","ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN","EIGHT","NINE"};
        String out="";
        for(char c:num.toCharArray()){
            if(Character.isDigit(c)) out+=map[c-'0']+" ";
        }
        return out+"KG";
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Weighbridge Billing Software");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new java.awt.Dimension(900, 600));

        // Use BorderLayout so panel fills the whole frame
        frame.setLayout(new java.awt.BorderLayout());

        JPanel panel = new JPanel(null) {
            @Override
            public void doLayout() {
                super.doLayout();
                layoutComponents(this);
            }
        };
        panel.setBackground(new java.awt.Color(255, 255, 255));
        frame.add(panel, java.awt.BorderLayout.CENTER);

        // Create DB tables
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bills (id INTEGER PRIMARY KEY AUTOINCREMENT,weighbridge TEXT,rst TEXT,date TEXT,time TEXT,vehicle TEXT,material TEXT,gross TEXT,tare TEXT,net TEXT,charges TEXT,customer_name TEXT,place TEXT,source TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS weighbridges (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,address TEXT,phone TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS vehicles (id INTEGER PRIMARY KEY AUTOINCREMENT,vehicle_no TEXT)");
            // Migrate existing DB: add new columns if they don't exist yet
            try{ stmt.executeUpdate("ALTER TABLE bills ADD COLUMN customer_name TEXT DEFAULT ''"); }catch(Exception ignored){}
            try{ stmt.executeUpdate("ALTER TABLE bills ADD COLUMN place TEXT DEFAULT ''"); }catch(Exception ignored){}
            try{ stmt.executeUpdate("ALTER TABLE bills ADD COLUMN source TEXT DEFAULT ''"); }catch(Exception ignored){}
            conn.close();
        } catch (Exception ex) { ex.printStackTrace(); }

        // --- COMPONENTS ---
        JLabel title = new JLabel("WEIGHBRIDGE BILLING SYSTEM", SwingConstants.CENTER);
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        title.setForeground(new java.awt.Color(0, 0, 0));
        panel.add(title);

        JLabel wbLabel = new JLabel("Select Weighbridge:");
        panel.add(wbLabel);

        JComboBox<String> wbDropdown = new JComboBox<>();
        panel.add(wbDropdown);

        JButton addWBBtn = new JButton("Add Weighbridge");
        panel.add(addWBBtn);

        JLabel address = new JLabel("Address will appear here");
        panel.add(address);

        JLabel rstLabel = new JLabel("RST No:");
        panel.add(rstLabel);
        JTextField rstField = new JTextField();
        panel.add(rstField);

        JLabel dateLabel = new JLabel("Date:");
        panel.add(dateLabel);
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());
        panel.add(dateField);

        JLabel vehicleLabel = new JLabel("Vehicle No:");
        panel.add(vehicleLabel);
        JComboBox<String> vehicleField = new JComboBox<>();
        vehicleField.setEditable(true);
        panel.add(vehicleField);

        JLabel timeLabel = new JLabel("Time:");
        panel.add(timeLabel);
        JTextField timeField = new JTextField(java.time.LocalTime.now().withNano(0).toString());
        panel.add(timeField);

        JLabel materialLabel = new JLabel("Material:");
        panel.add(materialLabel);
        JTextField materialField = new JTextField();
        panel.add(materialField);

        JLabel grossLabel = new JLabel("Gross Weight:");
        panel.add(grossLabel);
        JTextField grossField = new JTextField();
        panel.add(grossField);

        JLabel tareLabel = new JLabel("Tare Weight:");
        panel.add(tareLabel);
        JTextField tareField = new JTextField();
        panel.add(tareField);

        JLabel netLabel = new JLabel("Net Weight:");
        panel.add(netLabel);
        JTextField netField = new JTextField();
        netField.setEditable(false);
        panel.add(netField);

        JLabel chargeLabel = new JLabel("Charges:");
        panel.add(chargeLabel);
        JTextField chargeField = new JTextField();
        panel.add(chargeField);

        JLabel customerLabel = new JLabel("Customer Name:");
        panel.add(customerLabel);
        JTextField customerField = new JTextField();
        panel.add(customerField);

        JLabel placeLabel = new JLabel("Place:");
        panel.add(placeLabel);
        JTextField placeField = new JTextField();
        panel.add(placeField);

        JLabel sourceLabel = new JLabel("Source:");
        panel.add(sourceLabel);
        JTextField sourceField = new JTextField();
        panel.add(sourceField);

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(new java.awt.Color(255, 255, 255));
        saveBtn.setForeground(Color.black);
        saveBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        panel.add(saveBtn);

        JButton printBtn = new JButton("Print");
        panel.add(printBtn);

        JButton clearBtn = new JButton("Clear");
        panel.add(clearBtn);

        JButton searchBtn = new JButton("Search Bills");
        panel.add(searchBtn);

        JButton updateBtn = new JButton("Update Bill");
        panel.add(updateBtn);

        JButton savePrintBtn = new JButton("Save & Print");
        panel.add(savePrintBtn);

        JLabel brand = new JLabel("Developed by N-kartech");
        brand.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        brand.setForeground(new java.awt.Color(0, 102, 204));
        panel.add(brand);

        // --- LAYOUT METHOD (called on resize) ---
        // Store refs so layoutComponents can access them
        panel.putClientProperty("title", title);
        panel.putClientProperty("wbLabel", wbLabel);
        panel.putClientProperty("wbDropdown", wbDropdown);
        panel.putClientProperty("addWBBtn", addWBBtn);
        panel.putClientProperty("address", address);
        panel.putClientProperty("rstLabel", rstLabel);
        panel.putClientProperty("rstField", rstField);
        panel.putClientProperty("dateLabel", dateLabel);
        panel.putClientProperty("dateField", dateField);
        panel.putClientProperty("vehicleLabel", vehicleLabel);
        panel.putClientProperty("vehicleField", vehicleField);
        panel.putClientProperty("timeLabel", timeLabel);
        panel.putClientProperty("timeField", timeField);
        panel.putClientProperty("materialLabel", materialLabel);
        panel.putClientProperty("materialField", materialField);
        panel.putClientProperty("grossLabel", grossLabel);
        panel.putClientProperty("grossField", grossField);
        panel.putClientProperty("tareLabel", tareLabel);
        panel.putClientProperty("tareField", tareField);
        panel.putClientProperty("netLabel", netLabel);
        panel.putClientProperty("netField", netField);
        panel.putClientProperty("chargeLabel", chargeLabel);
        panel.putClientProperty("chargeField", chargeField);
        panel.putClientProperty("saveBtn", saveBtn);
        panel.putClientProperty("printBtn", printBtn);
        panel.putClientProperty("clearBtn", clearBtn);
        panel.putClientProperty("searchBtn", searchBtn);
        panel.putClientProperty("updateBtn", updateBtn);
        panel.putClientProperty("savePrintBtn", savePrintBtn);
        panel.putClientProperty("brand", brand);
        panel.putClientProperty("customerLabel", customerLabel);
        panel.putClientProperty("customerField", customerField);
        panel.putClientProperty("placeLabel", placeLabel);
        panel.putClientProperty("placeField", placeField);
        panel.putClientProperty("sourceLabel", sourceLabel);
        panel.putClientProperty("sourceField", sourceField);

        // Load weighbridges
        try {
            Connection conn2 = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
            ResultSet rs = conn2.createStatement().executeQuery("SELECT * FROM weighbridges");
            while(rs.next()) wbDropdown.addItem(rs.getString("name"));
            conn2.close();
        } catch(Exception ex){ ex.printStackTrace(); }

        // Auto-load address for the first item on startup
        if(wbDropdown.getItemCount() > 0) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                PreparedStatement pst = conn.prepareStatement("SELECT * FROM weighbridges WHERE name=?");
                pst.setString(1, wbDropdown.getItemAt(0).toString());
                ResultSet rs = pst.executeQuery();
                if(rs.next()){
                    selectedAddress = rs.getString("address");
                    selectedPhone = rs.getString("phone");
                    address.setText(selectedAddress + " | Ph: " + selectedPhone);
                }
                conn.close();
            } catch(Exception ex){ ex.printStackTrace(); }
        }

        // Load vehicles
        try {
            Connection conn3 = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
            ResultSet rs3 = conn3.createStatement().executeQuery("SELECT * FROM vehicles");
            while(rs3.next()) vehicleField.addItem(rs3.getString("vehicle_no"));
            conn3.close();
        } catch(Exception ex){ ex.printStackTrace(); }

        // Weighbridge selection
        wbDropdown.addActionListener(e -> {
            if(wbDropdown.getSelectedItem()==null) return;
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                PreparedStatement pst = conn.prepareStatement("SELECT * FROM weighbridges WHERE name=?");
                pst.setString(1, wbDropdown.getSelectedItem().toString());
                ResultSet rs = pst.executeQuery();
                if(rs.next()){
                    selectedAddress = rs.getString("address");
                    selectedPhone = rs.getString("phone");
                    address.setText(selectedAddress + " | Ph: " + selectedPhone);
                }
                conn.close();
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Add Weighbridge
        addWBBtn.addActionListener(e -> {
            JFrame wbFrame = new JFrame("Add Weighbridge");
            wbFrame.setSize(400, 300);
            wbFrame.setLayout(null);
            JLabel nl=new JLabel("Name:"); nl.setBounds(30,30,100,25); wbFrame.add(nl);
            JTextField nf=new JTextField(); nf.setBounds(150,30,180,25); wbFrame.add(nf);
            JLabel al=new JLabel("Address:"); al.setBounds(30,70,100,25); wbFrame.add(al);
            JTextField af=new JTextField(); af.setBounds(150,70,180,25); wbFrame.add(af);
            JLabel pl=new JLabel("Phone:"); pl.setBounds(30,110,100,25); wbFrame.add(pl);
            JTextField pf=new JTextField(); pf.setBounds(150,110,180,25); wbFrame.add(pf);
            JButton sv=new JButton("Save"); sv.setBounds(150,160,100,30); wbFrame.add(sv);
            sv.addActionListener(ev -> {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                    PreparedStatement pst = conn.prepareStatement("INSERT INTO weighbridges (name,address,phone) VALUES (?,?,?)");
                    pst.setString(1,nf.getText()); pst.setString(2,af.getText()); pst.setString(3,pf.getText());
                    pst.executeUpdate(); conn.close();
                    wbDropdown.addItem(nf.getText());
                    JOptionPane.showMessageDialog(wbFrame,"Saved!"); wbFrame.dispose();
                } catch(Exception ex){ ex.printStackTrace(); }
            });
            wbFrame.setVisible(true);
        });

        // Auto net calc
        KeyAdapter calc = new KeyAdapter(){
            public void keyReleased(KeyEvent e){
                try{
                    double net = Double.parseDouble(grossField.getText()) - Double.parseDouble(tareField.getText());
                    netField.setText(String.valueOf(net));
                }catch(Exception ex){ netField.setText(""); }
            }
        };
        grossField.addKeyListener(calc);
        tareField.addKeyListener(calc);

        // Save
        saveBtn.addActionListener(e -> {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                PreparedStatement pst = conn.prepareStatement("INSERT INTO bills (weighbridge,rst,date,time,vehicle,material,gross,tare,net,charges,customer_name,place,source) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                pst.setString(1,(String)wbDropdown.getSelectedItem()); pst.setString(2,rstField.getText());
                pst.setString(3,dateField.getText()); pst.setString(4,timeField.getText());
                pst.setString(5,vehicleField.getSelectedItem().toString()); pst.setString(6,materialField.getText());
                pst.setString(7,grossField.getText()); pst.setString(8,tareField.getText());
                pst.setString(9,netField.getText()); pst.setString(10,chargeField.getText());
                pst.setString(11,customerField.getText()); pst.setString(12,placeField.getText());
                pst.setString(13,sourceField.getText());
                pst.executeUpdate(); conn.close();
                // Save vehicle if new
                Connection cv=DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                PreparedStatement cp=cv.prepareStatement("SELECT * FROM vehicles WHERE vehicle_no=?");
                cp.setString(1,vehicleField.getSelectedItem().toString());
                if(!cp.executeQuery().next()){
                    PreparedStatement ip=cv.prepareStatement("INSERT INTO vehicles(vehicle_no) VALUES(?)");
                    ip.setString(1,vehicleField.getSelectedItem().toString()); ip.executeUpdate();
                    vehicleField.addItem(vehicleField.getSelectedItem().toString());
                }
                cv.close();
                JOptionPane.showMessageDialog(frame,"Bill Saved Successfully ✅");
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Update
        updateBtn.addActionListener(e -> {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                PreparedStatement pst = conn.prepareStatement("UPDATE bills SET vehicle=?,material=?,gross=?,tare=?,net=?,charges=?,customer_name=?,place=?,source=? WHERE rst=?");
                pst.setString(1,vehicleField.getSelectedItem().toString()); pst.setString(2,materialField.getText());
                pst.setString(3,grossField.getText()); pst.setString(4,tareField.getText());
                pst.setString(5,netField.getText()); pst.setString(6,chargeField.getText());
                pst.setString(7,customerField.getText()); pst.setString(8,placeField.getText());
                pst.setString(9,sourceField.getText()); pst.setString(10,rstField.getText());
                int rows=pst.executeUpdate(); conn.close();
                JOptionPane.showMessageDialog(frame,rows>0?"Bill Updated ✅":"RST not found");
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Print
        printBtn.addActionListener(e -> {
            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setPrintable((graphics, pageFormat, pageIndex) -> {
                    if(pageIndex>0) return java.awt.print.Printable.NO_SUCH_PAGE;
                    java.awt.Graphics2D g = (java.awt.Graphics2D) graphics;
                    g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    // ── fonts ──────────────────────────────────────────────
                    java.awt.Font fName   = new java.awt.Font("Arial", java.awt.Font.BOLD,  20);
                    java.awt.Font fNormal = new java.awt.Font("Arial", java.awt.Font.PLAIN, 15);
                    java.awt.Font fWtLbl  = new java.awt.Font("Arial", java.awt.Font.BOLD,  16);
                    java.awt.Font fNetLbl = new java.awt.Font("Arial", java.awt.Font.BOLD,  18);
                    java.awt.Font fFooter = new java.awt.Font("Arial", java.awt.Font.PLAIN, 13);
                    java.awt.Font fBrand  = new java.awt.Font("Arial", java.awt.Font.BOLD,  11);

                    // ── data prep ──────────────────────────────────────────
                    String wbName   = wbDropdown.getSelectedItem()!=null ? wbDropdown.getSelectedItem().toString() : "";
                    String addrLine = "Address: " + selectedAddress;
                    String phoneLine= (selectedPhone!=null && !selectedPhone.isEmpty()) ? "Phone: "+selectedPhone : "";

                    String grossStr = grossField.getText().trim();
                    String tareStr  = tareField.getText().trim();
                    String netStr   = netField.getText().trim();
                    try{ grossStr = String.valueOf((int)Double.parseDouble(grossStr)); }catch(Exception ignored){}
                    try{ tareStr  = String.valueOf((int)Double.parseDouble(tareStr));  }catch(Exception ignored){}
                    try{ netStr   = String.valueOf((int)Double.parseDouble(netStr));   }catch(Exception ignored){}

                    String dateFmt  = formatDate(dateField.getText());
                    String timeFmt  = formatTime(timeField.getText());
                    String dateTime = "Date: "+dateFmt+"   Time: "+timeFmt;
                    String rstLine  = "RST No: " + rstField.getText();
                    String vehLine  = "Vehicle No: " + (vehicleField.getSelectedItem()!=null?vehicleField.getSelectedItem().toString():"");
                    String matLine  = "Material: "   + materialField.getText();
                    String custName  = customerField.getText().trim();
                    String placeVal  = placeField.getText().trim();
                    String sourceVal = sourceField.getText().trim();
                    String custLine = "Customer: " + (custName.isEmpty()  ? "____________" : custName);
                    String placeLine= "Place:    " + (placeVal.isEmpty()  ? "____________" : placeVal);
                    String srcLine  = "Source:   " + (sourceVal.isEmpty() ? "____________" : sourceVal);
                    String chgLine  = "Charges: " + chargeField.getText();
                    String opLine   = "Operator Sign: ________________";
                    String wordsStr = numberToWords(netStr);

                    int padL = 14; // left padding inside box
                    int padR = 14; // right padding inside box
                    int rowH  = 24;
                    int secGap= 8;

                    // ── MEASURE PASS: find the minimum box width needed ────
                    // We use a temporary Graphics2D context to measure strings.
                    // Weight section columns: label | :: | number | kg | datetime/words
                    // Left half  = padL + wtLabelW + gap + colonW + gap + numberW + gap + kgW + gap + COL_MID_PAD
                    // Right half = dateTimeW + padR  (or wordsW + padR for NET row)
                    // Full width = left half + right half, but we keep a minimum divider

                    g.setFont(fName);   java.awt.FontMetrics fmN = g.getFontMetrics();
                    g.setFont(fNormal); java.awt.FontMetrics fmP = g.getFontMetrics();
                    g.setFont(fWtLbl);  java.awt.FontMetrics fmW = g.getFontMetrics();
                    g.setFont(fNetLbl); java.awt.FontMetrics fmNW= g.getFontMetrics();
                    g.setFont(fBrand);  java.awt.FontMetrics fmB = g.getFontMetrics();

                    int GAP = 8; // spacing between sub-columns in weight section

                    // Weight label widths (use widest one as fixed col0 width)
                    int lblW = Math.max(fmW.stringWidth("GROSS  WT"),
                            Math.max(fmW.stringWidth("TARE    WT"),
                                    fmNW.stringWidth("NET      WT")));
                    int colonW  = fmW.stringWidth("::");
                    int numW    = Math.max(fmW.stringWidth(grossStr),
                            Math.max(fmW.stringWidth(tareStr), fmNW.stringWidth(netStr)));
                    int kgW     = fmW.stringWidth("kg");
                    int dtW     = fmP.stringWidth(dateTime);
                    int wordsW  = fmW.stringWidth(wordsStr);

                    // Left part of weight row (label + :: + number + kg)
                    int wtLeftW = padL + lblW + GAP + colonW + GAP + numW + GAP + kgW + GAP*2;
                    // Right part needs dateTime or words — use the larger
                    int wtRightW= Math.max(dtW, wordsW) + padR;
                    int wFromWT = wtLeftW + wtRightW;

                    // Info section: left content + right content
                    int infoLeftW = Math.max(fmP.stringWidth(rstLine),
                            Math.max(fmP.stringWidth(vehLine), fmP.stringWidth(matLine)));
                    int infoRightW= Math.max(fmP.stringWidth(custLine),
                            Math.max(fmP.stringWidth(placeLine), fmP.stringWidth(srcLine)));
                    int wFromInfo = padL + infoLeftW + GAP*3 + infoRightW + padR;

                    // Header section: name, address, phone must all fit
                    int wFromHeader = Math.max(fmN.stringWidth(wbName),
                            Math.max(fmP.stringWidth(addrLine),
                                    fmP.stringWidth(phoneLine))) + padL + padR;

                    // Charges row: charges + operator sign
                    int wFromCharges= padL + fmP.stringWidth(chgLine) + GAP*4 + fmP.stringWidth(opLine) + padR;

                    // Footer
                    int wFromFooter = fmP.stringWidth("Please check weight before leaving") + padL + padR;

                    // Final box width = max of all, minimum 480
                    int w = Math.max(480, Math.max(wFromWT, Math.max(wFromInfo,
                            Math.max(wFromHeader, Math.max(wFromCharges, wFromFooter)))));

                    // ── Scale to fit page if content is wider than printable area ──
                    // Available printable width and height
                    int pageW = (int) pageFormat.getImageableWidth();
                    int pageH = (int) pageFormat.getImageableHeight();

                    // Total width needed = left margin(10) + box(w) + right margin(10)
                    int totalNeeded = w + 20;
                    if(totalNeeded > pageW){
                        double scale = (double) pageW / totalNeeded;
                        g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                        g.scale(scale, scale);
                        // After scaling, origin is (0,0) within imageable area
                    } else {
                        // No scaling needed — just shift to imageable origin
                        g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    }

                    // ── Column positions — now relative to translated origin ───
                    int x   = 10;
                    int y   = 10;
                    int col0= x + padL;                          // weight label start
                    int col1= col0 + lblW + GAP;                 // "::" position
                    int col2= col1 + colonW + GAP + numW;        // right-edge of number field
                    int col3= col2 + GAP;                        // "kg" start
                    int col4= col3 + kgW + GAP*2;               // date/time or words start
                    // Info section right column — halfway across box, min gap from left content
                    int infoRight = Math.max(x + padL + infoLeftW + GAP*3,
                            x + w/2 + 10);

                    int cur = y + 22;
                    java.awt.FontMetrics fm;

                    // ── HEADER ─────────────────────────────────────────────
                    g.setFont(fName);
                    fm = g.getFontMetrics();
                    g.drawString(wbName, x + (w - fm.stringWidth(wbName)) / 2, cur);

                    cur += rowH + 2;
                    g.setFont(fNormal);
                    fm = g.getFontMetrics();
                    g.drawString(addrLine, x + (w - fm.stringWidth(addrLine)) / 2, cur);

                    if(!phoneLine.isEmpty()){
                        cur += rowH;
                        fm = g.getFontMetrics();
                        g.drawString(phoneLine, x + (w - fm.stringWidth(phoneLine)) / 2, cur);
                    }

                    // double-line separator
                    cur += secGap + 6;
                    g.drawLine(x, cur, x+w, cur);
                    cur += 4;
                    g.drawLine(x, cur, x+w, cur);

                    // ── INFO SECTION ───────────────────────────────────────
                    cur += rowH;
                    g.setFont(fNormal);
                    g.drawString(rstLine,  col0,      cur);
                    g.drawString(custLine, infoRight, cur);

                    cur += rowH;
                    g.drawString(vehLine,  col0,      cur);
                    g.drawString(placeLine,infoRight, cur);

                    cur += rowH;
                    g.drawString(matLine,  col0,      cur);
                    g.drawString(srcLine,  infoRight, cur);

                    // separator
                    cur += secGap + 8;
                    g.drawLine(x, cur, x+w, cur);

                    // ── WEIGHT SECTION ─────────────────────────────────────
                    cur += rowH + 4;

                    // GROSS WT
                    g.setFont(fWtLbl);
                    fm = g.getFontMetrics();
                    g.drawString("GROSS  WT", col0, cur);
                    g.drawString("::",        col1, cur);
                    g.drawString(grossStr,    col2 - fm.stringWidth(grossStr), cur);
                    g.drawString("kg",        col3, cur);
                    g.setFont(fNormal);
                    g.drawString(dateTime, col4, cur);

                    cur += rowH + 4;

                    // TARE WT
                    g.setFont(fWtLbl);
                    fm = g.getFontMetrics();
                    g.drawString("TARE    WT", col0, cur);
                    g.drawString("::",         col1, cur);
                    g.drawString(tareStr,      col2 - fm.stringWidth(tareStr), cur);
                    g.drawString("kg",         col3, cur);
                    g.setFont(fNormal);
                    g.drawString(dateTime, col4, cur);

                    cur += rowH + 6;

                    // NET WT
                    g.setFont(fNetLbl);
                    fm = g.getFontMetrics();
                    g.drawString("NET      WT", col0, cur);
                    g.drawString("::",          col1, cur);
                    g.drawString(netStr,        col2 - fm.stringWidth(netStr), cur);
                    g.drawString("kg",          col3, cur);
                    g.setFont(fWtLbl);
                    g.drawString(wordsStr, col4, cur);

                    // separator
                    cur += secGap + 8;
                    g.drawLine(x, cur, x+w, cur);

                    // ── CHARGES + OPERATOR SIGN ────────────────────────────
                    cur += rowH;
                    g.setFont(fNormal);
                    g.drawString(chgLine, col0, cur);
                    // Operator sign: right-aligned with padR from box edge
                    g.drawString(opLine, x + w - fmP.stringWidth(opLine) - padR, cur);

                    // separator
                    cur += secGap + 8;
                    g.drawLine(x, cur, x+w, cur);

                    // ── FOOTER ─────────────────────────────────────────────
                    cur += rowH - 2;
                    g.setFont(fFooter);
                    fm = g.getFontMetrics();
                    String f1 = "Please check weight before leaving";
                    g.drawString(f1, x + (w - fm.stringWidth(f1)) / 2, cur);

                    cur += rowH - 4;
                    String f2 = "** Thank you visit again **";
                    g.drawString(f2, x + (w - fm.stringWidth(f2)) / 2, cur);

                    // N-kartech brand bottom-right
                    cur += 12;
                    g.setFont(fBrand);
                    g.drawString("N-kartech", x + w - fmB.stringWidth("N-kartech") - padR, cur);

                    // ── draw outer box LAST — height AND width fit content ─
                    g.drawRect(x, y, w, cur + 10 - y);

                    return java.awt.print.Printable.PAGE_EXISTS;
                });
                // Warn if Tare weight is 0 or empty
                String tareCheck = tareField.getText().trim();
                boolean tareZero = tareCheck.isEmpty() || tareCheck.equals("0") || tareCheck.equals("0.0");
                if(tareZero){
                    int choice = JOptionPane.showConfirmDialog(frame,
                            "Warning: Tare Weight is ZERO or empty!\nAre you sure you want to print?",
                            "Tare Weight Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if(choice != JOptionPane.YES_OPTION) return;
                }
                if(job.printDialog()) job.print();
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Save & Print
        savePrintBtn.addActionListener(e -> {
            String tareCheck2 = tareField.getText().trim();
            boolean tareZero2 = tareCheck2.isEmpty() || tareCheck2.equals("0") || tareCheck2.equals("0.0");
            if(tareZero2){
                int choice = JOptionPane.showConfirmDialog(frame,
                        "Warning: Tare Weight is ZERO or empty!\nAre you sure you want to save & print?",
                        "Tare Weight Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if(choice != JOptionPane.YES_OPTION) return;
            }
            saveBtn.doClick(); printBtn.doClick();
        });

        // Clear
        clearBtn.addActionListener(e -> {
            rstField.setText(""); vehicleField.setSelectedIndex(-1);
            materialField.setText(""); grossField.setText(""); tareField.setText("");
            netField.setText(""); chargeField.setText("");
            customerField.setText(""); placeField.setText(""); sourceField.setText("");
        });

        // Search
        searchBtn.addActionListener(e -> {
            JFrame sf = new JFrame("Search Bills");
            sf.setSize(1000, 640);
            sf.setLayout(null);
            sf.getContentPane().setBackground(new java.awt.Color(245, 248, 250));

            // ── Search fields row ──────────────────────────────────────
            JLabel l1 = new JLabel("Vehicle / RST:");
            l1.setBounds(20, 20, 120, 25); sf.add(l1);
            JTextField keyField = new JTextField();
            keyField.setBounds(140, 20, 160, 28); sf.add(keyField);

            JLabel l2 = new JLabel("Date (yyyy-mm-dd):");
            l2.setBounds(315, 20, 155, 25); sf.add(l2);
            JTextField dateSearchField = new JTextField();
            dateSearchField.setBounds(470, 20, 130, 28); sf.add(dateSearchField);

            JButton fb = new JButton("Search");
            fb.setBounds(615, 20, 120, 28);
            fb.setBackground(new java.awt.Color(255, 255, 255));
            fb.setForeground(Color.black);
            fb.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            sf.add(fb);

            JLabel hint = new JLabel("* Leave any field blank to match all.  Date example: 2026-03-01  or  2026-03  for whole month");
            hint.setBounds(20, 52, 820, 18);
            hint.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
            hint.setForeground(new java.awt.Color(120, 120, 120));
            sf.add(hint);

            // ── Results table ──────────────────────────────────────────
            String[] cols = {"RST No","Date","Time","Vehicle","Material","Gross (kg)","Tare (kg)","Net (kg)","Charges","Customer","Place","Source"};
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0){
                public boolean isCellEditable(int r, int c){ return false; }
            };
            JTable table = new JTable(model);
            table.setFont(new java.awt.Font("Segoe UI", 0, 12));
            table.setRowHeight(22);
            table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            table.getTableHeader().setBackground(new java.awt.Color(255, 255, 255));
            table.getTableHeader().setForeground(Color.black);
            table.setGridColor(new java.awt.Color(200, 200, 200));
            table.setSelectionBackground(new java.awt.Color(180, 210, 255));
            int[] colW = {55,75,65,95,75,65,65,65,60,80,70,65};
            for(int i=0;i<colW.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(colW[i]);

            JScrollPane sp = new JScrollPane(table);
            sp.setBounds(20, 80, 950, 450);
            sf.add(sp);

            // ── Bottom bar ─────────────────────────────────────────────
            JLabel countLabel = new JLabel("Results: 0");
            countLabel.setBounds(20, 542, 200, 24);
            countLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            sf.add(countLabel);

            JButton excelBtn = new JButton("Export to Excel (CSV)");
            excelBtn.setBounds(580, 538, 180, 32);
            excelBtn.setBackground(new java.awt.Color(255, 255, 255));
            excelBtn.setForeground(java.awt.Color.black);
            excelBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            excelBtn.setEnabled(false);
            sf.add(excelBtn);

            JButton pdfBtn = new JButton("Export to PDF");
            pdfBtn.setBounds(775, 538, 160, 32);
            pdfBtn.setBackground(new java.awt.Color(255, 255, 255));
            pdfBtn.setForeground(Color.black);
            pdfBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            pdfBtn.setEnabled(false);
            sf.add(pdfBtn);

            // ── Search logic ───────────────────────────────────────────
            fb.addActionListener(ev -> {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                    String keyword = keyField.getText().trim();
                    String dateVal = dateSearchField.getText().trim();
                    StringBuilder sql = new StringBuilder("SELECT * FROM bills WHERE 1=1");
                    if(!keyword.isEmpty()) sql.append(" AND (vehicle LIKE ? OR rst LIKE ?)");
                    if(!dateVal.isEmpty()) sql.append(" AND date LIKE ?");
                    sql.append(" ORDER BY id DESC");
                    PreparedStatement pst = conn.prepareStatement(sql.toString());
                    int idx = 1;
                    if(!keyword.isEmpty()){ pst.setString(idx++,"%"+keyword+"%"); pst.setString(idx++,"%"+keyword+"%"); }
                    if(!dateVal.isEmpty())  pst.setString(idx++,"%"+dateVal+"%");
                    ResultSet rs = pst.executeQuery();
                    model.setRowCount(0);
                    int count = 0;
                    while(rs.next()){
                        model.addRow(new Object[]{
                                rs.getString("rst"), rs.getString("date"), rs.getString("time"),
                                rs.getString("vehicle"), rs.getString("material"),
                                rs.getString("gross"), rs.getString("tare"),
                                rs.getString("net"), rs.getString("charges"),
                                rs.getString("customer_name"), rs.getString("place"), rs.getString("source")
                        });
                        count++;
                    }
                    conn.close();
                    countLabel.setText("Results: " + count);
                    excelBtn.setEnabled(count > 0);
                    pdfBtn.setEnabled(count > 0);
                } catch(Exception ex){ ex.printStackTrace(); }
            });

            java.awt.event.ActionListener enterSearch = ev -> fb.doClick();
            keyField.addActionListener(enterSearch);
            dateSearchField.addActionListener(enterSearch);

            // ── Export to Excel (CSV) ──────────────────────────────────
            excelBtn.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new java.io.File("WeighbridgeBills.csv"));
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files (*.csv)","csv"));
                if(fc.showSaveDialog(sf) != JFileChooser.APPROVE_OPTION) return;
                java.io.File file = fc.getSelectedFile();
                if(!file.getName().endsWith(".csv")) file = new java.io.File(file.getPath()+".csv");
                try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))){
                    // BOM for Excel UTF-8 detection
                    pw.print("\uFEFF");
                    StringBuilder header = new StringBuilder();
                    for(int c=0;c<model.getColumnCount();c++){
                        if(c>0) header.append(",");
                        header.append("\"").append(model.getColumnName(c)).append("\"");
                    }
                    pw.println(header);
                    // col 1=Date, col 2=Time: prefix \t so Excel keeps as plain text
                    for(int r=0;r<model.getRowCount();r++){
                        StringBuilder row = new StringBuilder();
                        for(int c=0;c<model.getColumnCount();c++){
                            if(c>0) row.append(",");
                            Object val = model.getValueAt(r,c);
                            String cell = val==null?"":val.toString().replace("\"","\"\"");
                            if((c==1||c==2) && !cell.isEmpty()) cell = "\t" + cell;
                            row.append("\"").append(cell).append("\"");
                        }
                        pw.println(row);
                    }
                    JOptionPane.showMessageDialog(sf,
                            "Excel file saved!\n"+file.getAbsolutePath()+
                                    "\n\nOpen with Microsoft Excel or Google Sheets.",
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                    try{ java.awt.Desktop.getDesktop().open(file); }catch(Exception ignored){}
                } catch(Exception ex){
                    JOptionPane.showMessageDialog(sf,"Error saving file:\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                }
            });

            // ── Export to PDF (pure Java, no library needed) ───────────
            pdfBtn.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new java.io.File("WeighbridgeBills.pdf"));
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files (*.pdf)","pdf"));
                if(fc.showSaveDialog(sf) != JFileChooser.APPROVE_OPTION) return;
                java.io.File file = fc.getSelectedFile();
                if(!file.getName().endsWith(".pdf")) file = new java.io.File(file.getPath()+".pdf");
                try {
                    exportTableAsPDF(file, model,
                            keyField.getText().trim(), dateSearchField.getText().trim());
                    JOptionPane.showMessageDialog(sf,
                            "PDF saved!\n"+file.getAbsolutePath(),
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                    try{ java.awt.Desktop.getDesktop().open(file); }catch(Exception ignored){}
                } catch(Exception ex){
                    JOptionPane.showMessageDialog(sf,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });

            sf.setLocationRelativeTo(frame);
            sf.setVisible(true);
        });

        frame.setVisible(true);
    }

    // Dynamic layout: all positions are calculated relative to panel size
    static void layoutComponents(JPanel panel) {
        int W = panel.getWidth();
        int H = panel.getHeight();
        if(W==0||H==0) return;

        int cx = W/2; // horizontal center

        // Title
        JLabel title = (JLabel)panel.getClientProperty("title");
        title.setBounds(cx-350, 20, 700, 45);

        // Row 1: Select Weighbridge
        int row1y = 80;
        JLabel wbLabel=(JLabel)panel.getClientProperty("wbLabel");
        wbLabel.setBounds(cx-500,row1y,160,25);
        JComboBox wbDrop=(JComboBox)panel.getClientProperty("wbDropdown");
        wbDrop.setBounds(cx-340,row1y,220,27);
        JButton addWB=(JButton)panel.getClientProperty("addWBBtn");
        addWB.setBounds(cx-110,row1y,160,27);

        // Address
        JLabel addr=(JLabel)panel.getClientProperty("address");
        addr.setBounds(cx-340,row1y+32,700,22);

        // Form fields - two column layout
        int leftX   = cx - 500;
        int leftFX  = cx - 340;
        int rightX  = cx + 30;
        int rightFX = cx + 130;
        int fw      = 220; // field width
        int rowH    = 42;
        int startY  = 165;

        // RST | Date
        setRow(panel,"rstLabel","rstField",leftX,leftFX,fw,startY);
        setRow(panel,"dateLabel","dateField",rightX,rightFX,fw,startY);

        // Vehicle | Time
        int r2=startY+rowH;
        setRow(panel,"vehicleLabel","vehicleField",leftX,leftFX,fw,r2);
        setRow(panel,"timeLabel","timeField",rightX,rightFX,fw,r2);

        // Material
        int r3=r2+rowH;
        setRow(panel,"materialLabel","materialField",leftX,leftFX,fw+200,r3);

        // Gross | (blank right)
        int r4=r3+rowH;
        setRow(panel,"grossLabel","grossField",leftX,leftFX,fw,r4);

        // Tare
        int r5=r4+rowH;
        setRow(panel,"tareLabel","tareField",leftX,leftFX,fw,r5);

        // Net
        int r6=r5+rowH;
        setRow(panel,"netLabel","netField",leftX,leftFX,fw,r6);

        // Charges
        int r7=r6+rowH;
        setRow(panel,"chargeLabel","chargeField",leftX,leftFX,fw,r7);

        // Customer Name | Place | Source in right column, BELOW Date and Time
        // startY        = Date  (right col row 1)
        // startY+rowH   = Time  (right col row 2)
        // startY+rowH*2 = Customer Name (right col row 3 = same Y as Material)
        // startY+rowH*3 = Place         (right col row 4 = same Y as Gross)
        // startY+rowH*4 = Source        (right col row 5 = same Y as Tare)
// Move customer section slightly DOWN to avoid overlap
        int rCust = startY + rowH*3;   // was *2
        int rPlace= startY + rowH*4;   // was *3
        int rSrc  = startY + rowH*5;   // was *4
        setRow(panel,"customerLabel","customerField",rightX,rightFX,fw,rCust);
        setRow(panel,"placeLabel","placeField",rightX,rightFX,fw,rPlace);
        setRow(panel,"sourceLabel","sourceField",rightX,rightFX,fw,rSrc);

        // Buttons row 1
        int btnY = r7+55;
        int bw=130, bh=36, gap=18;
        int btnStartX = cx-300;
        JButton sv=(JButton)panel.getClientProperty("saveBtn"); sv.setBounds(btnStartX,btnY,bw,bh);
        JButton pr=(JButton)panel.getClientProperty("printBtn"); pr.setBounds(btnStartX+bw+gap,btnY,bw,bh);
        JButton cl=(JButton)panel.getClientProperty("clearBtn"); cl.setBounds(btnStartX+2*(bw+gap),btnY,bw,bh);
        JButton sr=(JButton)panel.getClientProperty("searchBtn"); sr.setBounds(btnStartX+3*(bw+gap),btnY,bw,bh);

        // Buttons row 2
        int btnY2=btnY+52;
        JButton up=(JButton)panel.getClientProperty("updateBtn"); up.setBounds(btnStartX+bw/2+gap/2,btnY2,bw,bh);
        JButton sp2=(JButton)panel.getClientProperty("savePrintBtn"); sp2.setBounds(btnStartX+bw/2+gap/2+bw+gap,btnY2,bw,bh);

        // Brand
        JLabel brand=(JLabel)panel.getClientProperty("brand");
        brand.setBounds(W-230,H-40,220,25);
    }

    static void setRow(JPanel panel, String labelKey, String fieldKey, int lx, int fx, int fw, int y){
        JComponent lbl=(JComponent)panel.getClientProperty(labelKey);
        JComponent fld=(JComponent)panel.getClientProperty(fieldKey);
        if(lbl!=null) lbl.setBounds(lx,y,130,25);
        if(fld!=null) fld.setBounds(fx,y,fw,27);
    }

    // Pure-Java PDF export — no external library needed
    static void exportTableAsPDF(java.io.File file,
                                 javax.swing.table.DefaultTableModel model,
                                 String keyFilter, String dateFilter) throws Exception {

        // Page size: A4 landscape in points (1pt = 1/72 inch)
        final int PW = 842, PH = 595; // A4 landscape
        final int ML = 30, MR = 30, MT = 40, MB = 30;
        final int contentW = PW - ML - MR;

        // Column widths proportional
        int[] cw = {40,55,50,80,65,55,55,60,50,65,55,50};
        int totalCW = 0; for(int v:cw) totalCW+=v;
        int[] fcw = new int[cw.length];
        for(int i=0;i<cw.length;i++) fcw[i]= (int)((double)cw[i]/totalCW*contentW);

        final int ROWS_PER_PAGE = 28;
        int rowCount  = model.getRowCount();
        int colCount  = model.getColumnCount();
        int totalPages= Math.max(1,(rowCount+ROWS_PER_PAGE-1)/ROWS_PER_PAGE);

        // We'll collect each page as a BufferedImage then write a multi-page PDF
        java.util.List<java.awt.image.BufferedImage> pages = new java.util.ArrayList<>();

        for(int pageIndex=0; pageIndex<totalPages; pageIndex++){
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(PW, PH,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);

            // White background
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0,0,PW,PH);
            g.setColor(java.awt.Color.BLACK);

            int cur = MT;

            // Title
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
            java.awt.FontMetrics fm = g.getFontMetrics();
            String titleStr = "WEIGHBRIDGE BILLING REPORT";
            g.drawString(titleStr, ML + (contentW - fm.stringWidth(titleStr))/2, cur);
            cur += 20;

            // Subtitle / filters
            g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 9));
            fm = g.getFontMetrics();
            String sub = "Generated: " + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            if(!keyFilter.isEmpty())  sub += "   Search: "+keyFilter;
            if(!dateFilter.isEmpty()) sub += "   Date: "+dateFilter;
            sub += "   |   Page "+(pageIndex+1)+" of "+totalPages
                    + "   |   Total Records: "+rowCount;
            g.drawString(sub, ML + (contentW - fm.stringWidth(sub))/2, cur);
            cur += 6;

            // Horizontal rule
            g.setColor(new java.awt.Color(255, 255, 255));
            g.fillRect(ML, cur, contentW, 2);
            g.setColor(java.awt.Color.BLACK);
            cur += 10;

            // Table header background
            g.setColor(new java.awt.Color(255, 255, 255));
            g.fillRect(ML, cur, contentW, 18);

            // Header text
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
            g.setColor(Color.black);
            int cx = ML + 3;
            for(int c=0;c<colCount;c++){
                g.drawString(model.getColumnName(c), cx, cur+13);
                cx += fcw[c];
            }
            cur += 18;
            g.setColor(java.awt.Color.BLACK);

            // Data rows
            int startRow = pageIndex * ROWS_PER_PAGE;
            int endRow   = Math.min(startRow + ROWS_PER_PAGE, rowCount);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 9));
            fm = g.getFontMetrics();

            for(int r=startRow; r<endRow; r++){
                // Alternating row background
                if(r%2==0){
                    g.setColor(new java.awt.Color(235,243,255));
                    g.fillRect(ML, cur, contentW, 16);
                }
                g.setColor(java.awt.Color.BLACK);
                cx = ML + 3;
                for(int c=0;c<colCount;c++){
                    Object val = model.getValueAt(r,c);
                    String cell = val==null?"":val.toString();
                    // Clip text to fit column
                    while(cell.length()>1 && fm.stringWidth(cell) > fcw[c]-5)
                        cell = cell.substring(0,cell.length()-1);
                    g.drawString(cell, cx, cur+12);
                    cx += fcw[c];
                }
                // Row separator
                g.setColor(new java.awt.Color(210,210,210));
                g.drawLine(ML, cur+16, ML+contentW, cur+16);
                g.setColor(java.awt.Color.BLACK);
                cur += 16;
            }

            // Bottom border
            g.setColor(new java.awt.Color(255, 255, 255));
            g.fillRect(ML, cur+4, contentW, 2);

            // Footer
            cur += 12;
            g.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 8));
            g.setColor(new java.awt.Color(120,120,120));
            g.drawString("Developed by N-kartech  |  Weighbridge Billing Software", ML, PH-MB);

            g.dispose();
            pages.add(img);
        }

        // Write as multi-page PDF using raw PDF syntax
        writePDF(file, pages, PW, PH);
    }

    // Write a list of BufferedImages as a multi-page PDF file (pure Java, no library)
    static void writePDF(java.io.File file,
                         java.util.List<java.awt.image.BufferedImage> pages,
                         int pw, int ph) throws Exception {

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.util.List<Integer> offsets = new java.util.ArrayList<>();
        java.util.List<Integer> pageObjIds = new java.util.ArrayList<>();
        java.util.List<Integer> imgObjIds  = new java.util.ArrayList<>();

        // Helper to write a line
        java.util.function.Consumer<String> w = s -> {
            try{ out.write((s+"\n").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)); }
            catch(Exception e){ throw new RuntimeException(e); }
        };

        w.accept("%PDF-1.4");
        w.accept("%\u00e2\u00e3\u00cf\u00d3"); // binary comment

        int objId = 1;
        // Catalog obj 1
        offsets.add(out.size()); w.accept(objId+" 0 obj"); w.accept("<<"); w.accept("/Type /Catalog");
        w.accept("/Pages 2 0 R"); w.accept(">>"); w.accept("endobj"); objId++;

        // Pages obj 2 (forward reference — we'll patch later, but just mark position)
        int pagesObjOffset = out.size();
        offsets.add(out.size());
        // placeholder — write after we know page obj ids
        // We'll write a temporary placeholder
        String pagesPlaceholder = objId+" 0 obj\n<<\n/Type /Pages\n/Kids [KIDS]\n/Count "+pages.size()+"\n>>\nendobj\n";
        out.write(pagesPlaceholder.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        objId++;

        // For each page: encode image as JPEG, write image stream obj, then page obj
        for(int i=0;i<pages.size();i++){
            java.awt.image.BufferedImage img = pages.get(i);
            // Encode as JPEG
            java.io.ByteArrayOutputStream jpegOut = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img,"jpeg",jpegOut);
            byte[] jpegBytes = jpegOut.toByteArray();

            // Image stream object
            imgObjIds.add(objId);
            offsets.add(out.size());
            String imgHdr = objId+" 0 obj\n<<\n/Type /XObject\n/Subtype /Image\n"
                    +"/Width "+img.getWidth()+"\n/Height "+img.getHeight()+"\n"
                    +"/ColorSpace /DeviceRGB\n/BitsPerComponent 8\n"
                    +"/Filter /DCTDecode\n/Length "+jpegBytes.length+"\n>>\nstream\n";
            out.write(imgHdr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            out.write(jpegBytes);
            out.write("\nendstream\nendobj\n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            objId++;

            // Page object
            pageObjIds.add(objId);
            offsets.add(out.size());
            String contentStr = "q "+pw+" 0 0 "+ph+" 0 0 cm /Im"+i+" Do Q";
            byte[] contentBytes = contentStr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            String pageObj = objId+" 0 obj\n<<\n/Type /Page\n/Parent 2 0 R\n"
                    +"/MediaBox [0 0 "+pw+" "+ph+"]\n"
                    +"/Resources <<\n  /XObject << /Im"+i+" "+(objId-1)+" 0 R >>\n>>\n"
                    +"/Contents "+(objId+1)+" 0 R\n>>\nendobj\n";
            out.write(pageObj.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            objId++;

            // Content stream
            offsets.add(out.size());
            String contentHdr = objId+" 0 obj\n<<\n/Length "+contentBytes.length+"\n>>\nstream\n";
            out.write(contentHdr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            out.write(contentBytes);
            out.write("\nendstream\nendobj\n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            objId++;
        }

        // Now patch the Pages object — rebuild the full PDF with correct Kids
        StringBuilder kids = new StringBuilder();
        for(int id : pageObjIds){ if(kids.length()>0) kids.append(" "); kids.append(id+" 0 R"); }
        String correctPages = "2 0 obj\n<<\n/Type /Pages\n/Kids ["+kids+"]\n/Count "+pages.size()+"\n>>\nendobj\n";

        // Rebuild: replace placeholder in out
        byte[] rawOut = out.toByteArray();
        // Find and replace the Pages placeholder
        String rawStr = new String(rawOut, java.nio.charset.StandardCharsets.ISO_8859_1);
        String corrected = rawStr.replace(pagesPlaceholder, correctPages);
        byte[] finalBytes = corrected.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

        // Recalculate offsets by scanning for "N 0 obj"
        java.util.List<Integer> xrefOffsets = new java.util.ArrayList<>();
        int totalObjs = objId - 1;
        for(int n=1;n<=totalObjs;n++){
            String marker = n+" 0 obj";
            int pos = corrected.indexOf(marker);
            xrefOffsets.add(pos < 0 ? 0 : pos);
        }

        // Write final PDF to file
        java.io.ByteArrayOutputStream finalOut = new java.io.ByteArrayOutputStream();
        finalOut.write(finalBytes);

        // xref table
        int xrefOffset = finalOut.size();
        finalOut.write(("xref\n0 "+objId+"\n").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        finalOut.write("0000000000 65535 f \n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        for(int off : xrefOffsets){
            finalOut.write((String.format("%010d 00000 n \n", off))
                    .getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        }
        finalOut.write(("trailer\n<<\n/Size "+objId+"\n/Root 1 0 R\n>>\nstartxref\n"+xrefOffset+"\n%%EOF\n")
                .getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

        try(java.io.FileOutputStream fos = new java.io.FileOutputStream(file)){
            finalOut.writeTo(fos);
        }
    }
}