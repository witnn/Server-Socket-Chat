package com.company;

// witnn

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class ClientPanel extends JFrame {
    private JPanel panel1;
    private JTextField msg_field;
    private JButton gönderButton;
    private JButton baglanButton;
    private JTextField port_field;
    private JTextField server_field;
    private JTextArea log_area;

    public boolean _isConnected = false;
    Socket client = null;

    // Okuyucular
    InputStreamReader isr = null;
    BufferedReader br = null;
    PrintWriter print = null;

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

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClientPanel cp = new ClientPanel();
                cp.setVisible(true);
            }
        });


    }

    // Panel constructor
    public ClientPanel(){
        add(panel1);
        setSize(400,400);
        setTitle("Client Panel");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Bağlantı butonu

        baglanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(_isConnected == false){
                    Thread connect = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connection(server_field.getText(), Integer.parseInt(port_field.getText()));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    connect.start();
                }
            }
        });

        // Gönder butonu

        gönderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(canCommunicate == true){
                    Thread t2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PrintWriter prints = null;
                            try {
                                prints = new PrintWriter(client.getOutputStream());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            String send = msg_field.getText();
                            log_area.setText(log_area.getText()+  "\n Client : " + send);

                            String enc_message = encrypt(send);

                            prints.println(enc_message);
                            prints.flush();

                            msg_field.setText("");
                        }
                    });
                    t2.start();
                }
            }
        });
    }

    // Socket oluşturma ve bağlantı sağlama

    public void connection(String server, int port) throws IOException {
        try{
            client = new Socket(server, port);
        }
        catch (ConnectException e){
            log_area.setText(log_area.getText()+  "\nServer'a bağlantı sağlanamadı");
        }

        if(client.isConnected() == true){
            _isConnected = true;
            mesajAlma();
            log_area.setText(log_area.getText()+  "\nServer'a bağlantı sağlandı");
        }
    }

    // Mesaj alma

    public void mesajAlma() throws IOException{
        InputStreamReader isr = new InputStreamReader(client.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        PrintWriter print = new PrintWriter(client.getOutputStream());

        canCommunicate = true;

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        String crypted_message = br.readLine();
                        String dec_message = decrypt(crypted_message);

                        log_area.setText(log_area.getText()  + " \n Server : " + dec_message);

                        System.out.println("Server : " + dec_message);
                    } catch (IOException e) {
                        log_area.setText(log_area.getText()  + " \n Server Kapandı :(");
                        System.out.println("Server kapandı :(");
                        canCommunicate = false;
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
