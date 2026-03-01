import javax.swing.*;
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
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bills (id INTEGER PRIMARY KEY AUTOINCREMENT,weighbridge TEXT,rst TEXT,date TEXT,time TEXT,vehicle TEXT,material TEXT,gross TEXT,tare TEXT,net TEXT,charges TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS weighbridges (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,address TEXT,phone TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS vehicles (id INTEGER PRIMARY KEY AUTOINCREMENT,vehicle_no TEXT)");
            conn.close();
        } catch (Exception ex) { ex.printStackTrace(); }

        // --- COMPONENTS ---
        JLabel title = new JLabel("WEIGHBRIDGE BILLING SYSTEM", SwingConstants.CENTER);
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        title.setForeground(new java.awt.Color(0, 102, 204));
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

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(new java.awt.Color(0, 153, 51));
        saveBtn.setForeground(java.awt.Color.blue);
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
                PreparedStatement pst = conn.prepareStatement("INSERT INTO bills (weighbridge,rst,date,time,vehicle,material,gross,tare,net,charges) VALUES (?,?,?,?,?,?,?,?,?,?)");
                pst.setString(1,(String)wbDropdown.getSelectedItem()); pst.setString(2,rstField.getText());
                pst.setString(3,dateField.getText()); pst.setString(4,timeField.getText());
                pst.setString(5,vehicleField.getSelectedItem().toString()); pst.setString(6,materialField.getText());
                pst.setString(7,grossField.getText()); pst.setString(8,tareField.getText());
                pst.setString(9,netField.getText()); pst.setString(10,chargeField.getText());
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
                PreparedStatement pst = conn.prepareStatement("UPDATE bills SET vehicle=?,material=?,gross=?,tare=?,net=?,charges=? WHERE rst=?");
                pst.setString(1,vehicleField.getSelectedItem().toString()); pst.setString(2,materialField.getText());
                pst.setString(3,grossField.getText()); pst.setString(4,tareField.getText());
                pst.setString(5,netField.getText()); pst.setString(6,chargeField.getText());
                pst.setString(7,rstField.getText());
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
                    java.awt.Graphics2D g=(java.awt.Graphics2D)graphics;
                    int x=40,y=40,w=520,h=320; g.drawRect(x,y,w,h);
                    int line=y+20;
                    g.setFont(new java.awt.Font("Arial",java.awt.Font.BOLD,14));
                    g.drawString(wbDropdown.getSelectedItem()!=null?wbDropdown.getSelectedItem().toString():"",x+140,line);
                    line+=15; g.setFont(new java.awt.Font("Arial",0,11));
                    g.drawString("Address: "+selectedAddress,x+20,line);
                    line+=15; g.drawString("Phone: "+selectedPhone,x+20,line);
                    line+=10; g.drawLine(x,line,x+w,line); line+=15; g.drawLine(x,line,x+w,line);
                    line+=20; g.drawString("RST No: "+rstField.getText(),x+20,line);
                    g.drawString("Customer Name: ____________",x+300,line);
                    line+=18; g.drawString("Vehicle No: "+vehicleField.getSelectedItem(),x+20,line);
                    g.drawString("Place: ____________",x+300,line);
                    line+=18; g.drawString("Material: "+materialField.getText(),x+20,line);
                    g.drawString("Source: ____________",x+300,line);
                    line+=18; g.drawString("Date: "+formatDate(dateField.getText()),x+20,line);
                    g.drawString("Time: "+formatTime(timeField.getText()),x+200,line);
                    line+=15; g.drawLine(x,line,x+w,line);
                    line+=20; g.drawString("Gross Wt: "+grossField.getText()+" kg",x+20,line);
                    line+=18; g.drawString("Tare Wt: "+tareField.getText()+" kg",x+20,line);
                    line+=18; g.setFont(new java.awt.Font("Arial",java.awt.Font.BOLD,16));
                    g.drawString("Net Wt: "+netField.getText()+" kg",x+20,line);
                    g.setFont(new java.awt.Font("Arial",0,11));
                    g.drawString("In Words: "+numberToWords(netField.getText()),x+250,line);
                    line+=20; g.drawString("Charges: "+chargeField.getText(),x+20,line);
                    line+=25; g.drawString("Operator Sign: ________________",x+20,line);
                    line+=15; g.drawLine(x,line,x+w,line);
                    line+=18; g.setFont(new java.awt.Font("Arial",0,10));
                    g.drawString("Please check weight before leaving",x+150,line);
                    line+=15; g.drawString("** Thank you visit again **",x+170,line);
                    g.setFont(new java.awt.Font("Arial",java.awt.Font.BOLD,9));
                    g.drawString("N-kartech",x+w-80,y+h-10);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });
                if(job.printDialog()) job.print();
            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // Save & Print
        savePrintBtn.addActionListener(e -> { saveBtn.doClick(); printBtn.doClick(); });

        // Clear
        clearBtn.addActionListener(e -> {
            rstField.setText(""); vehicleField.setSelectedIndex(-1);
            materialField.setText(""); grossField.setText(""); tareField.setText("");
            netField.setText(""); chargeField.setText("");
        });

        // Search
        searchBtn.addActionListener(e -> {
            JFrame sf=new JFrame("Search Bills"); sf.setSize(700,500); sf.setLayout(null);
            JLabel sl=new JLabel("Enter Vehicle or RST:"); sl.setBounds(30,20,200,25); sf.add(sl);
            JTextField sfld=new JTextField(); sfld.setBounds(200,20,200,25); sf.add(sfld);
            JTextArea ra=new JTextArea(); JScrollPane sp=new JScrollPane(ra); sp.setBounds(30,70,620,350); sf.add(sp);
            JButton fb=new JButton("Search"); fb.setBounds(420,20,100,25); sf.add(fb);
            fb.addActionListener(ev -> {
                try {
                    Connection conn=DriverManager.getConnection("jdbc:sqlite:weighbridge.db");
                    PreparedStatement pst=conn.prepareStatement("SELECT * FROM bills WHERE vehicle LIKE ? OR rst LIKE ?");
                    pst.setString(1,"%"+sfld.getText()+"%"); pst.setString(2,"%"+sfld.getText()+"%");
                    ResultSet rs=pst.executeQuery(); ra.setText("");
                    while(rs.next()) ra.append("RST: "+rs.getString("rst")+" | Vehicle: "+rs.getString("vehicle")+" | Net: "+rs.getString("net")+" | Charges: "+rs.getString("charges")+"\n");
                    conn.close();
                } catch(Exception ex){ ex.printStackTrace(); }
            });
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
}