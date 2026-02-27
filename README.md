# BizStock – Sistema de Gestión de Inventario

**Universidad Interamericana de Puerto Rico – Recinto de Fajardo**  
COMP5525 - Seminario I | Abel Caro 

---

## ¿Qué es BizStock?

BizStock es una aplicación de escritorio desarrollada en Java que permite a propietarios y empleados de pequeños negocios gestionar su inventario de productos de manera digital, organizada y eficiente. Fue diseñada inicialmente para un salón de belleza, reemplazando el uso de libretas, hojas de cálculo y anotaciones manuales por un sistema centralizado con interfaz gráfica.

---

## Funcionalidades

- ✅ Registro de productos con nombre, categoría, marca, precio y cantidad
- ✅ Registro de entradas y salidas de inventario con historial
- ✅ Alertas automáticas de productos en nivel bajo o crítico
- ✅ Exportación de reportes en PDF (inventario completo y productos a reordenar)
- ✅ Control de acceso por roles (Administrador y Empleado)
- ✅ Autenticación segura con hash SHA-256
- ✅ Interfaz de una sola ventana con pestañas (sin ventanas emergentes)

---

## Tecnologías Utilizadas

| Tecnología | Detalle |
|---|---|
| Lenguaje | Java 17 (OpenJDK Temurin) |
| Interfaz | Java Swing |
| Base de datos | MySQL |
| Acceso a datos | JDBC con PreparedStatement |
| Generación de PDF | Apache PDFBox 3.x |
| Seguridad | SHA-256 para contraseñas |
| IDE recomendado | jGRASP |

---

## Estructura del Proyecto

```
bizstock/
├── src/
│   ├── MainBizStock.java
│   └── bizstock/
│       ├── ui/         → Interfaz gráfica (MainFrame, Panels)
│       ├── dao/        → Acceso a base de datos
│       ├── model/      → Modelos de datos
│       ├── service/    → Lógica de negocio
│       └── util/       → Utilidades (DB, PDF, Seguridad)
├── lib/                → JARs de dependencias
└── sql/
    └── bizstock.sql    → Script de base de datos
```

---

## Requisitos

- Java 17
- MySQL Server
- jGRASP (u otro IDE Java)
- JARs en la carpeta `lib/`:
  - `mysql-connector-j-9.5.0.jar`
  - `pdfbox-3.0.6.jar`
  - `pdfbox-io-3.0.6.jar`
  - `fontbox-3.0.6.jar`
  - `commons-logging-1.3.5.jar`

---

## Cómo Ejecutar

1. Importar la base de datos en MySQL Workbench:
   - `Server > Data Import > importar sql/bizstock.sql`

2. Abrir el proyecto en jGRASP desde la carpeta `src/`

3. Agregar todos los `.jar` de la carpeta `lib/` al CLASSPATH de jGRASP

4. Ejecutar `MainBizStock.java`

**Credenciales de prueba:**

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | admin | admin123 |
| Empleado | empleado | empleado123 |

---

## Módulos del Sistema

| Módulo | Descripción |
|---|---|
| Login | Autenticación con usuario y contraseña |
| Productos | CRUD completo del catálogo de productos |
| Movimientos | Registro de entradas y salidas de inventario |
| Alertas | Productos en nivel bajo o crítico |
| Exportar PDF | Reportes de inventario y productos a reordenar |

---

## Solución de Problemas

| Problema | Solución |
|---|---|
| No conecta a MySQL | Verificar que MySQL esté encendido y las credenciales en `DatabaseConnection.java` |
| Login inválido | Usar `admin / admin123` o `empleado / empleado123` |
| Error al exportar PDF | Verificar que todos los JARs de PDFBox estén en el CLASSPATH |
| Producto no aparece | Presionar "Refrescar" en el módulo de Productos |

Para otros problemas: **abca6409@interfajardo.edu**

---

## Trabajo Futuro

- Migrar a aplicación web
- Reportes estadísticos y gráficas de movimientos
- Notificaciones automáticas al alcanzar nivel de reorden
- Soporte para múltiples sucursales
- App móvil con escaneo de código de barras

---

## Referencias

- Apache PDFBox: https://pdfbox.apache.org
- Java SE 17: https://docs.oracle.com/en/java/javase/17/
- MySQL 8.0: https://dev.mysql.com/doc/refman/8.0/en/
- JDBC: https://docs.oracle.com/javase/tutorial/jdbc/
- Apache Commons Logging: https://commons.apache.org/proper/commons-logging/
