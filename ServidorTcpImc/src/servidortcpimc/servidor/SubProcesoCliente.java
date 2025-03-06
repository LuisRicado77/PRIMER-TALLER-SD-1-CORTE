/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidortcpimc.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import servidortcpimc.modelo.CalculoImc;
import servidortcpimc.views.MainWindow;

/**
 *
 * @author ldric
 */
public class SubProcesoCliente extends Thread {

    private Socket cliente;
    private String ip;
    private MainWindow ventana;

    public SubProcesoCliente(Socket cliente, MainWindow v) {
        this.cliente = cliente;
        ip = cliente.getInetAddress().getHostAddress();
        ventana = v;
    }

   @Override
    public void run() {
        try {
            CalculoImc.Imc imc = calcularImc();
            enviarRespuesta(imc);
        } catch (Exception ex) {
            System.out.println(log()+ex.getMessage());
            ventana.getCajaLog().append(log()+ex.getMessage() + "\n");
            try {
                cliente.close();
            } catch (IOException ex1) {
                ServidorTcp.listaDeClientes.remove(ip);
            } finally {
                ServidorTcp.listaDeClientes.remove(ip);
            }
        }
    }

    public CalculoImc.Imc calcularImc() throws Exception {
        DataInputStream input = null;
        try {
            input = new DataInputStream(cliente.getInputStream());
            String msg = "Esperando el PESO: ";
            System.out.println(log() + msg);
            ventana.getCajaLog().append(log() + msg + "\n" + "\n");
            float peso = input.readFloat();
            msg = "PESO: " + peso;
            System.out.println(log() + msg);
            ventana.getCajaLog().append(log() + msg + "\n");
            msg = "Esperando La Altura: ";
            System.out.println(log() + msg);
            ventana.getCajaLog().append(log() + msg + "\n");
            float altura = input.readFloat();
            msg = "ALTURA: " + altura;
            System.out.println(log() + msg);
            ventana.getCajaLog().append(log() + msg + "\n");
            CalculoImc datosImc = new CalculoImc(peso, altura);
            System.out.println(log() + "IMC: " + datosImc.getImc().resultado);
            msg = "IMC: " + datosImc.getImc().resultado;
            System.out.println(log() + msg);
            ventana.getCajaLog().append(log() + msg + "\n");
            System.out.println(log() + "MENSAJE: " + datosImc.getImc().mensaje);
            msg = "MENSAJE: " + datosImc.getImc().mensaje;
            System.out.println(log() + msg);
            ventana.getCajaLog().append(log() + msg + "\n");
            return datosImc.getImc();
        } catch (IOException ex) {
            String msg = "Error al caputurar datos del cliente " + ip;
            System.out.println(log() + msg);
            ventana.getCajaLog().append(log() + msg + "\n");
            throw new Exception("Error al caputurar datos del cliente " + ip);

        }

    }

    public void enviarRespuesta(CalculoImc.Imc imc) {

        Thread hiloResponde = new Thread() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(cliente.getOutputStream());
                    output.writeFloat(imc.resultado);
                    output.writeUTF(imc.mensaje);
                    String msg = "IMC: " + imc.resultado;
                    System.out.println(log() + msg);
                    ventana.getCajaLog().append(log() + msg + "\n");
                    msg = "MENSAJE: " + imc.mensaje;
                    System.out.println(log() + msg);
                    ventana.getCajaLog().append(log() + msg + "\n");
                    output.flush();
                    enviarRespuesta(calcularImc());
                } catch (IOException ex) {
                    String msg = "Error al enviar datos al cliente " + ip;
                    System.out.println(log() + msg);
                    ventana.getCajaLog().append(log() + msg + "\n");
                    ServidorTcp.listaDeClientes.remove(ip);
                } catch (Exception ex) {
                    String msg = "Error al leer datos del cliente " + ip;
                    System.out.println(log() + msg);
                    ventana.getCajaLog().append(log() + msg + "\n");
                    try {
                        cliente.close();
                    } catch (IOException ex1) {
                        ServidorTcp.listaDeClientes.remove(ip);
                    } finally {
                        ServidorTcp.listaDeClientes.remove(ip);
                    }
                }
            }
        };
        hiloResponde.start();
    }

    public Socket getCliente() {
        return cliente;
    }

    public void setCliente(Socket cliente) {
        this.cliente = cliente;
    }

    public String log() {
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        return ip + " -> " + f.format(new Date()) + " - ";
    }

}
