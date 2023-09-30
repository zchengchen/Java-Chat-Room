import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class ServerForm extends JFrame {
    private JList<String> list, qlist, olist;
    private JTextArea area;
    private DefaultListModel<String> lm, qlm, olm;
    private JTextField tfdMsg;
    private static final int PORT = 8000;

    public ServerForm() {
        this.setTitle("Java简易聊天室 服务器端");
        JPanel p = new JPanel(new BorderLayout());
        olm = new DefaultListModel<String>();
        olist = new JList<String>(olm);

        // 在线用户列表
        lm = new DefaultListModel<String>();
        lm.addElement("全部");
        list = new JList<String>(lm);
        JScrollPane jsOnline = new JScrollPane(list);
        jsOnline.setBackground(new Color(0, 241, 221));
        Border border = new TitledBorder("在线");
        jsOnline.setBorder(border);
        Dimension d = new Dimension(150, p.getHeight());
        jsOnline.setPreferredSize(d);
        p.add(jsOnline, BorderLayout.WEST);

        // 群组列表
        qlm = new DefaultListModel<String>();
        qlist = new JList<String>(qlm);
        JScrollPane qjs = new JScrollPane(qlist);
        qjs.setBackground(new Color(0, 241, 221));
        Border qborder = new TitledBorder("群组");
        qjs.setBorder(qborder);
        Dimension qd = new Dimension(150, p.getHeight());
        qjs.setPreferredSize(qd);
        p.add(qjs, BorderLayout.EAST);

        // 聊天消息显示
        area = new JTextArea();
        area.setEditable(false);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        this.getContentPane().add(p);

        // 控制菜单
        JMenuBar bar = new JMenuBar();
        this.setJMenuBar(bar);
        JMenu jm = new JMenu("控制");
        bar.add(jm);
        final JMenuItem jmiRun = new JMenuItem("开启");
        jmiRun.setActionCommand("run");
        jm.add(jmiRun);
        JMenuItem jmiExit = new JMenuItem("退出");
        jmiExit.setActionCommand("exit");
        jm.add(jmiExit);
        JMenuItem jmiLeave = new JMenuItem("下线");
        jmiLeave.setActionCommand("leave");
        jm.add(jmiLeave);

        // 发送消息的Panel
        JPanel msgp = new JPanel();
        JLabel jlMsg = new JLabel("消息:");
        msgp.add(jlMsg);
        tfdMsg = new JTextField(50);
        msgp.add(tfdMsg);
        JButton jbSend = new JButton("发送");
        msgp.add(jbSend);
        this.getContentPane().add(msgp, BorderLayout.SOUTH);

        // Listener
        ActionListener a1 = new ActionListener() {
            @Override
            //事件驱动监听事件源
            public void actionPerformed(ActionEvent e) {
                    //运行服务器
                if (e.getActionCommand().equals("run")) {
                    startServer();
                    jmiRun.setEnabled(false);
                } else if (e.getActionCommand().equals("exit")) {
                    //退出
                    System.exit(0);
                } else if (e.getActionCommand().equals("leave")) {
                    //强制下线
                    final JDialog dlg = new JDialog(ServerForm.this);
                    JPanel p1 = new JPanel();
                    JPanel p2 = new JPanel();
                    dlg.setBounds(ServerForm.this.getX() + 20,
                            ServerForm.this.getY() + 30, 55, 250);
                    dlg.setLayout(new FlowLayout());
                    dlg.add(new JLabel("在线列表"));
                    JScrollPane jsOnlineList = new JScrollPane(olist);
                    p1.add(jsOnlineList);
                    dlg.add(p1);
                    JButton btnleave = new JButton("下线");
                    p2.add(btnleave);
                    dlg.add(p2);
                    btnleave.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String[] msg1 = {"msg", olist.getSelectedValue(),
                                    "您已被请出聊天室", "server"};
                            try {
                                sendMsgToSb(msg1);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            lm.remove(olist.getSelectedIndex() + 1);
                            olm.remove(olist.getSelectedIndex());
                            dlg.dispose();
                        }
                    });
                    dlg.setVisible(true);
                } else {
                    //发送消息
                    String[] msg = {"msg", list.getSelectedValue(),
                            tfdMsg.getText(), "server"};
                    tfdMsg.setText("");
                    try {
                        sendMsgToSb(msg);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        jmiRun.addActionListener(a1);
        jmiExit.addActionListener(a1);
        jmiLeave.addActionListener(a1);
        jbSend.addActionListener(a1);

        //JFrame 设置
        Toolkit tk = Toolkit.getDefaultToolkit();
        int width = (int) tk.getScreenSize().getWidth();
        int height = (int) tk.getScreenSize().getHeight();
        this.setBounds(width / 4, height / 4, width / 2, height / 2);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    protected void startServer() {
        try {
            ServerSocket server = new ServerSocket(PORT);
            area.append("启动服务：" + server);
            new ServerThread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 用户和群组映射信息
    private Map<String, Socket> usersMap = new HashMap<String, Socket>();
    private Map<String, String> qzMap = new HashMap<String, String>();
    private Map<String, Socket> qz1 = new HashMap<String, Socket>();
    private Map<String, Socket> qz2 = new HashMap<String, Socket>();
    private Map<String, Socket> qz3 = new HashMap<String, Socket>();
    private Map<String, Socket> qz4 = new HashMap<String, Socket>();
    private Map<String, Socket> qz5 = new HashMap<String, Socket>();

    // 服务器核心线程
    class ServerThread extends Thread {
        private ServerSocket server;
        public ServerThread(ServerSocket server) {
            this.server = server;
        }
        @Override
        public void run() {
            try {
                while (true) {
                    Socket socketClient = server.accept();
                    Scanner sc = new Scanner(socketClient.getInputStream());
                    if (sc.hasNext()) {
                        String userName = sc.nextLine();
                        area.append(
                                "\r\n用户[ " + userName + " ]登录 ");
                        lm.addElement(userName);
                        olm.addElement(userName);
                        new ClientThread(socketClient).start();
                        usersMap.put(userName, socketClient);
                        //群发登录消息
                        msgAll(userName);
                        //返回登录成功
                        msgLoginInfo(socketClient);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Client 消息中转站,每个client一个线程
    class ClientThread extends Thread {
        private Socket socketClient;

        public ClientThread(Socket socketClient) {
            this.socketClient = socketClient;
        }
        @Override
        public void run() {
            try {
                Scanner sc = new Scanner(socketClient.getInputStream());
                while (sc.hasNext()) {
                    String msg = sc.nextLine();
                    // 用@#分割消息
                    String msgs[] = msg.split("@#@#");
                    // 转发消息
                    if ("on".equals(msgs[0])) {
                        sendMsgToSb(msgs);
                    }
                    // 有人退出
                    if ("exit".equals(msgs[0])) {
                        area.append("\r\n用户[ " + msgs[3] + " ]已退出!");
                        usersMap.remove(msgs[3]);
                        lm.removeElement(msgs[3]);
                        olm.removeElement(msgs[3]);
                        sendExitMsgToAll(msgs);
                    }
                    // 创建群组
                    if ("ct".equals(msgs[0])) {
                        createGroup(msgs);
                    }
                    // 加入群组
                    if ("join".equals(msgs[0])) {
                        joinGroup(msgs);
                    }
                    // 群发消息
                    if ("qzmsg".equals(msgs[0])) {
                        sendMsgToGroup(msgs);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer count = 0;

    // 创建群组
    public void createGroup(String[] msgs) throws IOException {
        String qzName = msgs[2];
        qlm.addElement(qzName);
        ++count;
        String x = count.toString();
        x = "qz" + x;
        qzMap.put(qzName, x);
    }

    //加入群组
    public void joinGroup(String[] msgs) throws IOException {
        String qz = qzMap.get(msgs[2]);
        //根据群组名加入
        switch (qz) {
            case "qz1" :
                //给群组Map加入<昵称，Socket>
                qz1.put(msgs[3], usersMap.get(msgs[3]));
                break;
            case "qz2" :
                qz2.put(msgs[3], usersMap.get(msgs[3]));
                break;
            case "qz3" :
                qz3.put(msgs[3], usersMap.get(msgs[3]));
                break;
            case "qz4" :
                qz4.put(msgs[3], usersMap.get(msgs[3]));
                break;
            case "qz5" :
                qz5.put(msgs[3], usersMap.get(msgs[3]));
                break;
        }
        String msg = msgs[3] + "加入了群组" + msgs[2];
        String[] x = {"qzmsg", msgs[2], msg, "系统"};
        sendMsgToGroup(x);
        Socket s = usersMap.get(msgs[3]);
        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
        String str = "qzAdd@#@#server@#@#" + msgs[2];
        pw.println(str);
        pw.flush();
    }

    //群发消息
    public void sendMsgToGroup(String[] msgs) throws IOException {
        String qz = qzMap.get(msgs[1]);
        System.out.println(qz);
        Iterator<String> userNames = null;
        switch (qz) {
            case "qz1" :
                userNames = qz1.keySet().iterator();
                break;
            case "qz2" :
                userNames = qz2.keySet().iterator();
                break;
            case "qz3" :
                userNames = qz3.keySet().iterator();
                break;
            case "qz4" :
                userNames = qz4.keySet().iterator();
                break;
            case "qz5" :
                userNames = qz5.keySet().iterator();
                break;
        }
        while (userNames.hasNext()) {
            String userName = userNames.next();
            Socket s = null;
            switch (qz) {
                case "qz1" :
                    s = qz1.get(userName);
                    break;
                case "qz2" :
                    s = qz2.get(userName);
                    break;
                case "qz3" :
                    s = qz3.get(userName);
                    break;
                case "qz4" :
                    s = qz4.get(userName);
                    break;
                case "qz5" :
                    s = qz5.get(userName);
                    break;
            }
            String msg = "(" + msgs[1] + ")" + msgs[3];
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            String str = "qzmsg" + "@#@#" + msg + "@#@#" + msgs[2];
            pw.println(str);
            pw.flush();
        }
    }

    //通知其他人有人退出
    private void sendExitMsgToAll(String[] msgs) throws IOException {
        Iterator<String> userNames = usersMap.keySet().iterator();

        while (userNames.hasNext()) {
            String userName = userNames.next();
            Socket s = usersMap.get(userName);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            String str = "msg@#@#server@#@#用户[ " + msgs[3] + " ]已退出！";
            pw.println(str);
            pw.flush();
            str = "cmdRemove@#@#server@#@#" + msgs[3];
            pw.println(str);
            pw.flush();
        }
    }

    //广播发送/单对单发送
    public void sendMsgToSb(String[] msgs) throws IOException {
        if ("全部".equals(msgs[1])) {
            Iterator<String> userNames = usersMap.keySet().iterator();
            while (userNames.hasNext()) {
                String userName = userNames.next();
                Socket s = usersMap.get(userName);
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                String str = "msg@#@#" + msgs[3] + "@#@#" + msgs[2];
                pw.println(str);
                pw.flush();
            }
        }
        else {
            Socket s = usersMap.get(msgs[1]);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            String str = "msg@#@#" + msgs[3] + "对你@#@#" + msgs[2];
            pw.println(str);
            pw.flush();
        }
    }

    // 群发新用户登录控制类消息
    public void msgAll(String userName) {
        Iterator<Socket> it = usersMap.values().iterator();
        while (it.hasNext()) {
            Socket s = it.next();
            try {
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                String msg = "msg@#@#server@#@#用户[ " + userName + " ]已登录!";
                pw.println(msg);
                pw.flush();
                msg = "cmdAdd@#@#server@#@#" + userName;
                pw.println(msg);
                pw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 给新登录用户发送当前在线用户列表
    public void msgLoginInfo(Socket socketClient) {
        try {
            PrintWriter pw = new PrintWriter(socketClient.getOutputStream(),
                    true);
            Iterator<String> it = usersMap.keySet().iterator();
            while (it.hasNext()) {
                String msg = "cmdAdd@#@#server@#@#" + it.next();
                pw.println(msg);
                pw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new ServerForm();
    }
}