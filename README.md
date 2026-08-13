# 🍇 EcoViñedos

Aplicación multiplataforma para enoturismo y gestión del mantenimiento de viñedos.

## Integrantes

- Vargas Vargas Zayda Fernanda
- Banda López Mildred Mariana
- Juarez Cruz Juan Diego

## Grupo

`GIDS6092`

## Objetivo

Desarrollar una aplicación multiplataforma para enoturismo y gestión del mantenimiento de viñedos, optimizada para smartphones, Smart TV y SmartWatch, que permita a los usuarios monitorear y administrar sus viñedos de manera eficiente, así como ofrecer una experiencia enoturística integral.

---

## Descripción de las Funcionalidades

### Gestión de Parcelas
- Creación, edición y consulta de viñedos.
- Configuración de umbrales de humedad y temperatura.
- Visualización de datos en tiempo real.

### Bitácora
- Registro histórico de actividades y observaciones por parcela.
- Seguimiento de eventos y tareas realizadas en campo.

### Control de Riego
- Monitoreo de sistemas de riego.
- Gestión y programación de ciclos de riego.

### Alertas y Notificaciones
- Generación automática de alertas basadas en umbrales configurados.
- Notificaciones en tiempo real en SmartWatch y Smartphone.

### Autenticación de Usuarios
- Login seguro con diferentes roles (enólogo, administrador, trabajador).
- Gestión de perfiles y accesos.

### Enoturismo
- Funcionalidades para la experiencia del visitante.
- Información de bodegas y catas.

### Sincronización Multiplataforma
- Comunicación en tiempo real entre SmartWatch y Smartphone vía MQTT.
- Visualización de datos en Smart TV.

---

## 🛠️ Tecnologías Utilizadas

### Frontend Mobile
- Kotlin para aplicación Android.
- Android Studio como entorno de desarrollo.

### Frontend Web/TV
- Kotlin para la aplicación.
- Android Studio como entorno de desarrollo
- JavaScript para interfaces en Smart TV.

### Comunicación
- MQTT (Message Queuing Telemetry Transport) para mensajería ligera.
- HTTP (Hypertext Transfer Protocol) para comunicación cliente-servidor.

### Backend
- Node.js para lógica de servidor.
- MongoDB como base de datos.

---

## Instrucciones para Ejecutar el Proyecto

### Requisitos Previos
- Node.js instalado (versión 14 o superior).
- Android Studio instalado.
- Dispositivo Android o emulador configurado.

### 1. Clonar el Repositorio
```bash
git clone https://github.com/Diegue83/EcoVinedos.git
cd EcoVinedos
```

### 2. Configuración del Backend
```bash
cd backend
npm install
cp .env.example .env   # Configurar variables de entorno
npm start
```

### 3. Configuración del Mobile y Wear
1. Abrir el proyecto en Android Studio.
2. Sincronizar dependencias de Gradle.
3. Conectar dispositivo Android o iniciar emulador.
4. Ejecutar la aplicación correspondiente (mobile o wear).

### 4. Configuración del Broker MQTT
- Configurar conexión MQTT en las variables de entorno.
- Asegurar que el broker esté activo para la comunicación.

---

## Capturas de Pantalla

## Wearable
<table>
  <tr>
    <td><img width="160" alt="Wearable 1" src="https://github.com/user-attachments/assets/ef630b37-e105-4201-bf08-69f681cf64b9" /></td>
    <td><img width="160" alt="Wearable 2" src="https://github.com/user-attachments/assets/0155d77d-80e7-4bfc-8268-ff632a3334ff" /></td>
    <td><img width="160" alt="Wearable 3" src="https://github.com/user-attachments/assets/3dd12069-13b0-4c07-b87f-0323efa4cac4" /></td>
    <td><img width="160" alt="Wearable 4" src="https://github.com/user-attachments/assets/4766d9a9-8609-4f40-85b8-4cb39515f2f8" /></td>
    <td><img width="160" alt="Wearable 5" src="https://github.com/user-attachments/assets/0d936cab-0f65-4198-b2fd-5d8604a243ff" /></td>
  </tr>
</table>

---

## Móvil

### Autenticación
<img width="260" alt="Autenticación" src="https://github.com/user-attachments/assets/43cd5314-06ca-4cc2-bee1-00005aa04d6e" />

### Pantalla de inicio / Panel de control
<table>
  <tr>
    <td><img width="260" alt="Pantalla de inicio 1" src="https://github.com/user-attachments/assets/fd2930f7-466d-4154-b844-36d962f61ecb" /></td>
    <td><img width="260" alt="Pantalla de inicio 2" src="https://github.com/user-attachments/assets/4fce638a-10c1-4cb6-bd8b-42ad3b882e40" /></td>
    <td><img width="260" alt="Panel de Control" src="https://github.com/user-attachments/assets/c32a0670-db0e-4844-a5b1-fa73b7488688" /></td>
  </tr>
</table>

### Índice de Maduración
<img width="260" alt="Índice de Maduración" src="https://github.com/user-attachments/assets/0fc45ae5-e2ab-44bf-aba3-920f6df8d8e2" />

### Riego
<img width="260" alt="Riego" src="https://github.com/user-attachments/assets/621421fd-2baf-427a-8137-569a238cd36f" />

### Historial
<table>
  <tr>
    <td><img width="260" alt="Historial 1" src="https://github.com/user-attachments/assets/c87732dc-4d9f-4582-9de5-a300745da068" /></td>
    <td><img width="260" alt="Historial 2" src="https://github.com/user-attachments/assets/8c05261f-77ea-47d7-8df5-57b03f158fae" /></td>
  </tr>
</table>

### Panel de Administrador
<table>
  <tr>
    <td><img width="260" alt="Panel de Administrador 1" src="https://github.com/user-attachments/assets/9b28bb12-12b5-4e36-a007-7650960cf6ca" /></td>
    <td><img width="260" alt="Panel de Administrador 2" src="https://github.com/user-attachments/assets/852fda5f-3605-49cf-8292-e7d55841eb93" /></td>
  </tr>
</table>

### Gestión de Parcelas (Admin)
<table>
  <tr>
    <td><img width="260" alt="Gestión de Parcelas Admin 1" src="https://github.com/user-attachments/assets/7a34fb83-867a-4d4c-8395-29997b70b8ad" /></td>
    <td><img width="260" alt="Gestión de Parcelas Admin 2" src="https://github.com/user-attachments/assets/b919dc7d-63ed-4ddd-bbe4-29ccb0fd8e35" /></td>
  </tr>
</table>

### Nueva Parcela (Admin)
<img width="260" alt="Nueva Parcela Admin" src="https://github.com/user-attachments/assets/6c1d1714-cea2-4cdb-875d-9a3e6308c07e" />

### Gestión de Usuarios (Admin)
<img width="260" alt="Gestión de Usuarios Admin" src="https://github.com/user-attachments/assets/ffd32a8a-4f5a-4fad-ad9b-9d4819f3a6b1" />

### Nuevo Usuario (Admin)
<img width="260" alt="Nuevo Usuario Admin" src="https://github.com/user-attachments/assets/5c1d5c5d-5c77-4d12-b0c8-b091d01d1ea2" />

### Configuración de IoT
<table>
  <tr>
    <td><img width="260" alt="Configuración de IoT 1" src="https://github.com/user-attachments/assets/2fc8ff9c-f5c9-42dd-88ad-2429eefb0c06" /></td>
    <td><img width="260" alt="Configuración de IoT 2" src="https://github.com/user-attachments/assets/7dd5f486-2f4d-4486-89f6-fb93f38ebb55" /></td>
    <td><img width="260" alt="Configuración de IoT 3" src="https://github.com/user-attachments/assets/e1721679-5646-4cc1-936e-6461e14eba5a" /></td>
  </tr>
</table>

### Información de la Parcela
<img width="260" alt="Información de la Parcela" src="https://github.com/user-attachments/assets/a54dec69-3510-4ede-ae61-0664e5cc5283" />

### Muestras de Laboratorio
<table>
  <tr>
    <td><img width="260" alt="Muestras de Laboratorio 1" src="https://github.com/user-attachments/assets/a3d1dd8e-f18e-4999-9ec8-451862128336" /></td>
    <td><img width="260" alt="Muestras de Laboratorio 2" src="https://github.com/user-attachments/assets/5fae705a-b082-41d1-9f2f-3eac72cd21ad" /></td>
  </tr>
</table>

### Registrar Muestra
<img width="260" alt="Registrar Muestra" src="https://github.com/user-attachments/assets/7ee85e33-2437-444d-939e-7c130596da63" />
