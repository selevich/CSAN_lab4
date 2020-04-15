package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.text.ParseException;

public class Main {
    public static JFrame frm;
    public static JPanel pan;
    public static JTextArea textAreaServer,textAreaClient;
    public static JButton sendButton;
    public static JButton openFile;

    public static void main(String[] args) throws IOException {
        setGUI();

        openFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    setTextFromFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String header=textAreaClient.getText();
                try {
                    String answer = sendRequest(header);
                    textAreaServer.setText(answer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


    }

    public static String sendRequest(String httpHeader) throws Exception {
        String host = null;
        int port = 0;
        Socket socket = null;
        String res = null;

        try {
            host = getHost(httpHeader);
            port = getPort(host);
            host = getHostWithoutPort(host);
        } catch (Exception e) {
            throw new Exception("Не удалось получить адрес сервера.", e);
        }

        try {
            socket = new Socket(host, port);
            socket.getOutputStream().write(httpHeader.getBytes());

            byte[] serverResponse=new byte[2048];
            int sizeofResponse=socket.getInputStream().read(serverResponse,0,serverResponse.length);
            res = new String(serverResponse, 0, sizeofResponse);

        } catch (Exception e) {
            throw new Exception("Ошибка при чтении ответа от сервера.", e);
        }finally {
            socket.close();
        }

        return res;
    }

    private static String getHost(String header) throws ParseException {
        final String host = "Host: ";
        final String normalEnd = "\n";
        final String msEnd = "\r\n";

        int hostStrPosition = header.indexOf(host, 0);
        if (hostStrPosition < 0) {
            return "localhost";
        }
        hostStrPosition += host.length();

        int hostStrEndPosition = header.indexOf(normalEnd, hostStrPosition);
        hostStrEndPosition = (hostStrEndPosition > 0) ? hostStrEndPosition : header.indexOf(msEnd, hostStrPosition);
        if (hostStrEndPosition < 0) {
            throw new ParseException("В заголовке запроса не найдено " + "закрывающих символов после пункта Host.", 0);
        }
        String res = header.substring(hostStrPosition, hostStrEndPosition).trim();
        return res;
    }


    private static int getPort(String hostWithPort) {
        int port = hostWithPort.indexOf(":", 0);
        port = (port < 0) ? 80 : Integer.parseInt(hostWithPort.substring(port + 1));
        return port;
    }

    private static String getHostWithoutPort(String hostWithPort) {
        int portPosition = hostWithPort.indexOf(":", 0);
        if (portPosition < 0) {
            return hostWithPort;
        } else {
            return hostWithPort.substring(0, portPosition);
        }
    }

    private static void setTextFromFile() throws IOException {
        JFileChooser fileOpen = new JFileChooser();
        int ret = fileOpen.showDialog(null, "Open file");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileOpen.getSelectedFile();
            FileInputStream fileInputStream= null;
            BufferedInputStream bufferedInputStream=null;

            try {
                fileInputStream = new FileInputStream(file);
                bufferedInputStream=new BufferedInputStream(fileInputStream);
                byte[] arrInfo=new byte[1024];
                int size=bufferedInputStream.read(arrInfo,0,arrInfo.length);
                textAreaClient.setText(new String(arrInfo, 0, size));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }finally {
                fileInputStream.close();
                bufferedInputStream.close();
            }

        }
    }

    private static void setGUI(){
        frm = new JFrame();
        pan = new JPanel();
        sendButton=new JButton("send");
        sendButton.setLocation(10,400);
        openFile = new JButton("Choose file..");
        openFile.setLocation(10,30);

        textAreaServer = new JTextArea(22, 37);
        textAreaServer.setLineWrap(true);
        textAreaServer.setWrapStyleWord(true);
        textAreaServer.setEditable(false);
        JScrollPane scrServer = new JScrollPane(textAreaServer);
        scrServer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrServer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        textAreaClient = new JTextArea(22, 37);
        textAreaClient.setLineWrap(true);
        textAreaClient.setWrapStyleWord(true);
        JScrollPane srcClient = new JScrollPane(textAreaClient);
        srcClient.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        srcClient.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pan.add(srcClient);
        pan.add(scrServer);
        pan.add(openFile);
        pan.add(sendButton);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setContentPane(pan);
        frm.setBounds(10, 10, 10, 10);
        frm.setSize(900, 450);
        frm.setVisible(true);
    }
}
