// Source code is decompiled from a .class file using FernFlower decompiler.
package com.example;

import java.util.Arrays;

public class Informacion {
   private String nombre_impresora;
   private String api_key;
   private Operaciones[] operaciones;

   public Informacion() {
   }

   public String getNombre_impresora() {
      return this.nombre_impresora;
   }

   public String getApi_key() {
      return this.api_key;
   }

   public void setNombre_impresora(String nombre_impresora) {
      this.nombre_impresora = nombre_impresora;
   }

   public void setApi_key(String api_key) {
      this.api_key = api_key;
   }

   public Operaciones[] getOperaciones() {
      return this.operaciones;
   }

   public void setOperaciones(Operaciones[] operaciones) {
      this.operaciones = operaciones;
   }

   public String toString() {
      return "Informacion [nombre_impresora=" + this.nombre_impresora + "api_key" + this.api_key + Arrays.toString(this.operaciones) + "]";
   }
}
