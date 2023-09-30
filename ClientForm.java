import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Scanner;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientForm extends JFrame implements ActionListener {
    private JTextField tfdUserName;
    private JList<String> list, qlist;
    private DefaultListModel<String> lm, qlm;
    private JTextArea allMsg;
    private JTextField tfdMsg;
    private JButton jbLogin;
    private JButton jbExit;
    private JButton jbSend;
    private static String HOST = "127.0.0.1";
    private static int PORT = 8000;
    private Socket serverSocket;
    private PrintWriter pw;

    public ClientForm() {
        this.setTitle("Java简易聊天室 客户端");
        addJMenu();
        JPanel northPanel = new JPanel();
        JLabel jlb1 = new JLabel("用户昵称:");
        tfdUserName = new JTextField(10);
        jbLogin = new JButton("Login");
        jbLogin.setActionCommand("login");
        jbLogin.addActionListener(this);
        jbExit = new JButton("Exit");
        jbExit.setActionCommand("exit");
        jbExit.addActionListener(this);
        jbExit.setEnabled(false);
        //North Penal 昵称、登录、退出
        northPanel.add(jlb1);
        northPanel.add(tfdUserName);
        northPanel.add(jbLogin);
        northPanel.add(jbExit);
        getContentPane().add(northPanel, BorderLayout.NORTH);
        //Central Penal 用户群组列表、聊天记趣区
        JPanel centralPanel = new JPanel(new BorderLayout());
        this.getContentPane().add(centralPanel, BorderLayout.CENTER);

        // 在线用户列表
        lm = new DefaultListModel<String>();
        list = new JList<String>(lm);
        lm.addElement("全部");
        list.setSelectedIndex(0);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(2);
        JScrollPane js = new JScrollPane(list);
        Border border = new TitledBorder("在线");
        js.setBackground(new Color(0, 241, 221));
        js.setBorder(border);
        Dimension preferredSize = new Dimension(80, centralPanel.getHeight());
        js.setPreferredSize(preferredSize);
        centralPanel.add(js, BorderLayout.WEST);
        // 群组列表
        qlm = new DefaultListModel<String>();
        qlist = new JList<String>(qlm);
        qlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        qlist.setVisibleRowCount(2);
        JScrollPane qjs = new JScrollPane(qlist);
        Border qborder = new TitledBorder("群组");
        qjs.setBackground(new Color(0, 241, 221));
        qjs.setBorder(qborder);
        Dimension qpreferredSize = new Dimension(80, centralPanel.getHeight());
        qjs.setPreferredSize(qpreferredSize);
        centralPanel.add(qjs, BorderLayout.EAST);

        // 聊天内容显示区
        Border chatborder = new TitledBorder("聊天内容");
        allMsg = new JTextArea();
        allMsg.setEditable(false);
        centralPanel.add(new JScrollPane(allMsg), BorderLayout.CENTER);
        centralPanel.setBorder(chatborder);
        // 消息发送区
        JPanel jpSendMsg = new JPanel();
        JLabel jlbMessage = new JLabel("消息:");
        jpSendMsg.add(jlbMessage);
        tfdMsg = new JTextField(50);
        jpSendMsg.add(tfdMsg);
        jbSend = new JButton("发送");
        jbSend.setEnabled(false);
        jbSend.setActionCommand("send");
        jbSend.addActionListener(this);
        jpSendMsg.add(jbSend);
        this.getContentPane().add(jpSendMsg, BorderLayout.SOUTH);

        //窗口事件驱动
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (pw == null) {
                    System.exit(0);
                }
                String msg = "exit@#@#全部@#@#null@#@#" + tfdUserName.getText();
                pw.println(msg);
                pw.flush();
                System.exit(0);
            }
        });
        setBounds(300, 300, 650, 400);
        setVisible(true);
    }
    //选项下拉模块
    public void addJMenu() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        JMenu menu = new JMenu("选项");
        menuBar.add(menu);
        JMenuItem jmiSetting = new JMenuItem("设置");
        JMenuItem jmiCreateGroup = new JMenuItem("创建群组");
        JMenuItem jmiJoinGroup = new JMenuItem("加入群组");
        menu.add(jmiSetting);
        menu.add(jmiCreateGroup);
        menu.add(jmiJoinGroup);

        //设置按钮事件驱动和GUI
        jmiSetting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JDialog dlg = new JDialog(ClientForm.this);
                dlg.setBounds(ClientForm.this.getX() + 20,
                        ClientForm.this.getY() + 30, 350, 150);
                dlg.setLayout(new FlowLayout());
                dlg.add(new JLabel("服务器IP和端口:"));

                final JTextField tfdHost = new JTextField(10);
                tfdHost.setText(ClientForm.HOST);
                dlg.add(tfdHost);
                dlg.add(new JLabel(":"));
                final JTextField tfdPort = new JTextField(5);
                tfdPort.setText("" + ClientForm.PORT);
                dlg.add(tfdPort);
                JButton btnSet = new JButton("设置");
                dlg.add(btnSet);

                //设置服务器套接字事件驱动
                btnSet.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String ip = tfdHost.getText();
                        String strs[] = ip.split("\\.");
                        if (strs == null || strs.length != 4) {
                            JOptionPane.showMessageDialog(ClientForm.this,
                                    "IP类型有误！");
                            return;
                        }
                        try {
                            for (int i = 0; i < 4; i++) {
                                int num = Integer.parseInt(strs[i]);
                                if (num > 255 || num < 0) {
                                    JOptionPane.showMessageDialog(
                                            ClientForm.this, "IP类型有误！");
                                    return;
                                }
                            }
                        } catch (NumberFormatException e2) {
                            JOptionPane.showMessageDialog(ClientForm.this,
                                    "IP类型有误！");
                            return;
                        }
                        ClientForm.HOST = tfdHost.getText();
                        try {
                            int port = Integer.parseInt(tfdPort.getText());
                            if (port < 0 || port > 65535) {
                                JOptionPane.showMessageDialog(ClientForm.this,
                                        "端口范围有误！");
                                return;
                            }
                        } catch (NumberFormatException e1) {
                            JOptionPane.showMessageDialog(ClientForm.this,
                                    "端口类型有误！");
                            return;
                        }
                        ClientForm.PORT = Integer.parseInt(tfdPort.getText());
                        dlg.dispose();
                    }
                });
                dlg.setVisible(true);
            }
        });

        //创建群组事件驱动
        jmiCreateGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JDialog dlg = new JDialog(ClientForm.this);
                dlg.setBounds(ClientForm.this.getX() + 20,
                        ClientForm.this.getY() + 30, 350, 150);
                dlg.setLayout(new FlowLayout());
                dlg.add(new JLabel("群组名称:"));
                final JTextField tfdQz = new JTextField(10);
                dlg.add(tfdQz);
                JButton btnCt = new JButton("创建");
                dlg.add(btnCt);
                btnCt.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        askCreateGroup(tfdQz.getText());
                        dlg.dispose();
                    }
                });
                dlg.setVisible(true);
            }
        });

        //加入群组事件驱动
        jmiJoinGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JDialog dlg = new JDialog(ClientForm.this);
                dlg.setBounds(ClientForm.this.getX() + 20,
                        ClientForm.this.getY() + 30, 350, 150);
                dlg.setLayout(new FlowLayout());
                dlg.add(new JLabel("群组名称:"));
                final JTextField tfdQz = new JTextField(10);
                dlg.add(tfdQz);
                JButton btnJoin = new JButton("加入");
                dlg.add(btnJoin);
                btnJoin.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        joinGroup(tfdQz.getText());
                        dlg.dispose();
                    }
                });
                dlg.setVisible(true);
            }
        });
    }

    //JFrame的事件驱动
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("login")) {
            if (tfdUserName.getText() == null
                    || tfdUserName.getText().trim().length() == 0
                    || "@#@#".equals(tfdUserName.getText())
                    || "@#".equals(tfdUserName.getText())) {
                JOptionPane.showMessageDialog(this, "用户名输入有误，请重新输入！");
                return;
            }
            //连接到服务器
            linkStart();
            if (pw == null) {
                JOptionPane.showMessageDialog(this, "服务器未开启或网络未连接，无法连接！");
                return;
            }
            ((JButton) (e.getSource())).setEnabled(false);
            jbExit.setEnabled(true);
            jbSend.setEnabled(true);
            tfdUserName.setEditable(false);
        } else if (e.getActionCommand().equals("send")) {
            if (tfdMsg.getText() == null
                    || tfdMsg.getText().trim().length() == 0) {
                return;
            }
            String name = list.getSelectedValue();
            String msg = "on@#@#" + list.getSelectedValue() + "@#@#"
                    + tfdMsg.getText() + "@#@#" + tfdUserName.getText();
            //选中群发消息
            if (qlist.getSelectedValue() != null) {
                name = qlist.getSelectedValue();
                msg = "qzmsg@#@#" + qlist.getSelectedValue() + "@#@#"
                        + tfdMsg.getText() + "@#@#" + tfdUserName.getText();
                list.clearSelection();
                qlist.clearSelection();
            }
            //选中某个人发消息
            if (list.getSelectedValue() != null
                    && list.getSelectedValue() != "全部")
                allMsg.append("\r\n" + "你对" + name + "说:" + tfdMsg.getText());
                pw.println(msg);
                pw.flush();
                tfdMsg.setText("");
        } else if (e.getActionCommand().equals("exit")) {
            lm.removeAllElements();
            qlm.removeAllElements();
            sendExitMsg();
            jbLogin.setEnabled(true);
            jbExit.setEnabled(false);
            tfdUserName.setEditable(true);
        }
    }

    private void sendExitMsg() {
        String msg = "exit@#@#全部@#@#null@#@#" + tfdUserName.getText();
        pw.println(msg);
        pw.flush();
    }

    // Ask server to create Group.
    private void askCreateGroup(String qzName) {
        String msg = "ct@#@#全部@#@#" + qzName + "@#@#" + tfdUserName.getText();
        pw.println(msg);
        pw.flush();
        joinGroup(qzName);
    }

    // Request to join Group.
    private void joinGroup(String qzName) {
        String msg = "join@#@#全部@#@#" + qzName + "@#@#" + tfdUserName.getText();
        pw.println(msg);
        pw.flush();
    }

    // 连接到服务器
    public void linkStart() {
        try {
            String userName = tfdUserName.getText();
            if (userName == null || userName.trim().length() == 0) {
                JOptionPane.showMessageDialog(this, "连接服务器失败!\r\n用户名有误，请重新输入！");
                return;
            }
            serverSocket = new Socket(HOST, PORT);
            pw = new PrintWriter(serverSocket.getOutputStream(), true);
            pw.println(userName);
            this.setTitle("用户[ " + userName + " ]");
            new ClientThread().start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //客户端执行线程
    class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                Scanner sc = new Scanner(serverSocket.getInputStream());
                while (sc.hasNextLine()) {
                    String str = sc.nextLine();
                    String msgs[] = str.split("@#@#");
                    if ("msg".equals(msgs[0])) {
                        if ("server".equals(msgs[1])) {
                            str = "[ 通知 ]:" + msgs[2];
                        } else {
                            str = "[ " + msgs[1] + " ]说: " + msgs[2];
                        }
                        allMsg.append("\r\n" + str);
                    }
                    if ("您已被强制下线".equals(msgs[2])) {
                        lm.removeAllElements();
                        qlm.removeAllElements();
                        sendExitMsg();
                        jbLogin.setEnabled(true);
                        jbExit.setEnabled(false);
                        tfdUserName.setEditable(true);
                    }
                    if ("qzmsg".equals(msgs[0])) {
                        str = "[ " + msgs[1] + " ]说: " + msgs[2];
                        allMsg.append("\r\n" + str);
                    }
                    if ("cmdAdd".equals(msgs[0])) {
                        boolean eq = false;
                        for (int i = 0; i < lm.getSize(); i++) {
                            if (lm.getElementAt(i).equals(msgs[2])) {
                                eq = true;
                            }
                        }
                        if (!eq) {
                            lm.addElement(msgs[2]);
                        }
                    }
                    if ("cmdRemove".equals(msgs[0])) {
                        lm.removeElement(msgs[2]);
                    }
                    if ("qzAdd".equals(msgs[0])) {
                        boolean eq = false;
                        for (int i = 0; i < qlm.getSize(); i++) {
                            if (qlm.getElementAt(i).equals(msgs[2])) {
                                eq = true;
                            }
                        }
                        if (!eq) {
                            qlm.addElement(msgs[2]);
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        new ClientForm();
    }
}