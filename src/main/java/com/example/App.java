package com.example;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.EscPos.CutMode;
import com.github.anastaciocintra.escpos.EscPosConst.Justification;
import com.github.anastaciocintra.escpos.Style.FontSize;
import com.github.anastaciocintra.escpos.barcode.BarCode;
import com.github.anastaciocintra.escpos.barcode.PDF417;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Spark.port(8080);
        Spark.after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        });
        // Respuesta para solicitudes OPTIONS
        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        // Endpoint para obtener impresoras
        Spark.get("/getprinters", (request, response) -> {
            Gson gson = new Gson();
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices((DocFlavor) null, null);
            List<String> printerList = new ArrayList<>();
            for (PrintService printer : printServices) {
                printerList.add(printer.getName());
            }
            return gson.toJson(printerList);
        });

        // Endpoint para realizar la impresión
        Spark.post("/imprimir", (request, response) -> {
            String json = request.body();
            Gson gson = new Gson();
            Informacion informacion = gson.fromJson(json, Informacion.class);
            Operaciones[] operaciones = gson.fromJson(gson.toJson(informacion.getOperaciones()), Operaciones[].class);

            try {
                PrintService printService = PrinterOutputStream.getPrintServiceByName(informacion.getNombre_impresora());
                PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
                EscPos escpos = new EscPos(printerOutputStream);
                QRCode qrcode = new QRCode();
                BarCode barcode = new BarCode();
                Style sty = new Style();

                for (Operaciones operacion : operaciones) {
                    switch (operacion.getAccion()) {
                        case "textaling":
                            switch (operacion.getDatos()) {
                                case "center":
                                    sty.setJustification(Justification.Center);
                                    break;
                                case "left":
                                    sty.setJustification(Justification.Left_Default);
                                    break;
                                case "right":
                                    sty.setJustification(Justification.Right);
                                    break;
                            }
                            break;
                        case "pdf417":
                            PDF417 pdf417 = new PDF417();
                            escpos.write(pdf417, operacion.getDatos());
                            break;
                        case "qr":
                            qrcode.setSize(3);
                            qrcode.setJustification(Justification.Center);
                            escpos.write(qrcode, operacion.getDatos());
                            break;
                        case "cut":
                            escpos.cut(CutMode.FULL);
                            break;
                        case "feed":
                            escpos.feed(Integer.parseInt(operacion.getDatos()));
                            break;
                        case "text":
                            escpos.writeLF(sty, operacion.getDatos());
                            break;
                        case "fontsize":
                            switch (operacion.getDatos()) {
                                case "1":
                                    sty.setFontSize(FontSize._1, FontSize._1);
                                    break;
                                case "2":
                                    sty.setFontSize(FontSize._2, FontSize._2);
                                    break;
                                case "3":
                                    sty.setFontSize(FontSize._3, FontSize._3);
                                    break;
                            }
                            break;
                        case "barcode_93":
                            barcode.setBarCodeSize(3, 100);
                            barcode.setSystem(BarCode.BarCodeSystem.CODE93_Default);
                            barcode.setJustification(Justification.Center);
                            escpos.write(barcode, operacion.getDatos());
                            break;
                        case "barcode_128":
                            MultiFormatWriter writer = new MultiFormatWriter();
                            BufferedImage img = MatrixToImageWriter.toBufferedImage(writer.encode(operacion.getDatos(), BarcodeFormat.CODE_128, 480, 100));
                            EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(img), new BitonalThreshold(150));
                            RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
                            imageWrapper.setJustification(Justification.Center);
                            escpos.write(imageWrapper, escposImage);
                            break;
                        case "barcode_ean13":
                            barcode.setBarCodeSize(3, 100);
                            barcode.setSystem(BarCode.BarCodeSystem.JAN13_A);
                            barcode.setJustification(Justification.Center);
                            escpos.write(barcode, operacion.getDatos());
                            break;
                        case "img_url":
                            URL url = new URL(operacion.getDatos());
                            BufferedImage image = ImageIO.read(url);
                            EscPosImage escposImage2 = new EscPosImage(new CoffeeImageImpl(image), new BitonalThreshold(150));
                            RasterBitImageWrapper imageWrapper2 = new RasterBitImageWrapper();
                            imageWrapper2.setJustification(Justification.Center);
                            escpos.write(imageWrapper2, escposImage2);
                            break;
                    }
                }

                escpos.close();
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
