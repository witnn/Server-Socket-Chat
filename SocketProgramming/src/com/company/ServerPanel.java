package com.company;

// witnn

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerPanel extends JFrame{
    private JButton gonderButton;
    private JPanel panel1;
    private JTextField msg_field;
    private JButton serveriBaşlatButton;
    private JTextField port_field;
    private JTextArea log_area;

    ServerSocket s;
    Socket client = null;

    // Okuyucular
    InputStreamReader in = null;
    BufferedReader br = null;
    PrintWriter print = null;

    public boolean isServerStarted = false;
    public boolean canCommunicate = false;

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Paneli başlatma
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ServerPanel sp = new ServerPanel();
                sp.setVisible(true);
            }
        });


    }

    // Panel constructor
    public ServerPanel(){
        add(panel1);
        setSize(400,400);
        setTitle("Server Panel");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Serveri başlatma butonu
        serveriBaşlatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(isServerStarted == false){
                    Thread serverStart = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                createServer(Integer.parseInt(port_field.getText()));
                                isServerStarted = true;
                            }
                            catch (IOException exc){
                                System.out.println("e");
                            }
                        }
                    });
                    serverStart.start();
                }
            }
        });

        // Gönderim butonu
        gonderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mesaj Gönderme
                if(canCommunicate == true){
                    Thread t2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(client.isConnected()){

                                String send = msg_field.getText();
                                log_area.setText(log_area.getText() + "\n Server : "  + send);

                                String enc_message = encrypt(send);

                                print.println(enc_message);
                                print.flush();

                                msg_field.setText("");
                            }
                        }
                    });
                    t2.start();
                }
            }
        });
    }

    // Server oluşturma
    public void createServer(int port) throws IOException {
        s = new ServerSocket(Integer.parseInt(port_field.getText()));
        System.out.println(" Server " + port + " portunda oluşturuldu");

        log_area.setText(log_area.getText()  + " \n" + " Server " + port + " portunda oluşturuldu, Client bekleniyor...");

        findClient(s);
    }

    // Client bulma
    public void findClient(ServerSocket s) throws IOException {
        client = s.accept();
        System.out.println(" Client bağlandı ");
        log_area.setText(log_area.getText()  + " \n" + " Client Bağlandı");
        communication(client);
    }

    // Mesaj alma
    public void communication(Socket client) throws IOException {
        in = new InputStreamReader(client.getInputStream());
        br = new BufferedReader(in);
        print = new PrintWriter(client.getOutputStream());

        canCommunicate = true;


        // Mesaj alma
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true ){
                    try {
                        String crypted_message = br.readLine();
                        String dec_message = decrypt(crypted_message);

                        System.out.println(" Client : " + dec_message);

                        log_area.setText(log_area.getText() + "\n Client : " + dec_message);

                    } catch (IOException e) {
                        System.out.println("Client bağlantısı kayboldu");
                        canCommunicate = false;
                        log_area.setText(log_area.getText() + "\n" + " Client bağlantısı kayboldu");
                        try {
                            findClient(s);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });



        t1.start();


    }

    // şifreleme algoritması (mesaj uzunlugu * mesajdaki karakterin sırası kadar kaydırılır)

    public static String encrypt(String msg){
        String encrypted_msg = "";
        for(int i = 0; i < msg.length(); i++){
            encrypted_msg += (char)((int)msg.charAt(i)+((i+1)*msg.length()));
        }
        return encrypted_msg;
    }


    public static String decrypt(String msg){
        String decrypted_msg = "";
        for(int i = 0; i < msg.length(); i++){
            decrypted_msg += (char)((int)msg.charAt(i)-((i+1)*msg.length()));
        }
        return decrypted_msg;
    }
}
