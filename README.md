# EcoVinedos

## Integrantes:
- Vargas Vargas Zayda Fernanda
- Banda López Mildred Mariana
- Juarez Cruz Juan Diego

## Grupo
[GIDS6092]

## Objetivo
Desarrollar una aplicacion multiplataforma para enoturismo y gestion del mantenimiento de viñedos, optimizada para smartphones, Smart TV y SmartWatch, que permita a los usuarios monitorear y administrar sus viñedos de manera eficiente, asi como ofrecer una experiencia enoturistica integral.

## Descripcion de las Funcionalidades

### Gestion de Parcelas
- Creacion, edicion y consulta de viñedos.
- Configuracion de umbrales de humedad y temperatura.
- Visualizacion de datos en tiempo real.

### Bitacora
- Registro historico de actividades y observaciones por parcela.
- Seguimiento de eventos y tareas realizadas en campo.

### Control de Riego
- Monitoreo de sistemas de riego.
- Gestion y programacion de ciclos de riego.

### Alertas y Notificaciones
- Generacion automatica de alertas basadas en umbrales configurados.
- Notificaciones en tiempo real en SmartWatch y Smartphone.

### Autenticacion de Usuarios
- Login seguro con diferentes roles (enologo, administrador, trabajador).
- Gestion de perfiles y accesos.

### Enoturismo
- Funcionalidades para la experiencia del visitante.
- Informacion de bodegas y catas.

### Sincronizacion Multiplataforma
- Comunicacion en tiempo real entre SmartWatch y Smartphone via MQTT.
- Visualizacion de datos en Smart TV.

## Tecnologias Utilizadas

### Frontend Mobile
- Kotlin para aplicacion Android.
- Android Studio como entorno de desarrollo.

### Frontend Web/TV
- JavaScript para interfaces en Smart TV.

### Comunicacion
- MQTT (Message Queuing Telemetry Transport) para mensajeria ligera.

### Backend
- Node.js para logica de servidor.
- [Especificar base de datos utilizada: MongoDB/PostgreSQL]

## Instrucciones para Ejecutar el Proyecto

### Requisitos Previos
- Node.js instalado (version 14 o superior).
- Android Studio instalado.
- Dispositivo Android o emulador configurado.

### Clonar el Repositorio
git clone https://github.com/Diegue83/EcoVinedos.git
cd EcoVinedos

### Configuracion del Backend
cd backend
npm install
cp .env.example .env # Configurar variables de entorno
npm start


### Configuracion del Mobile y Wear
1. Abrir el proyecto en Android Studio.
2. Sincronizar dependencias de Gradle.
3. Conectar dispositivo Android o iniciar emulador.
4. Ejecutar la aplicacion correspondiente (mobile o wear).

### Configuracion del Broker MQTT
- Configurar conexion MQTT en las variables de entorno.
- Asegurar que el broker este activo para la comunicacion.

## Capturas de Pantalla

### Gestion de Parcelas
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/3300e3ab-6a1e-493c-8cee-234521fa0a20" />

### Autenticacion
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/43cd5314-06ca-4cc2-bee1-00005aa04d6e" />

### Muestras de Laboratorio
<img width="720" height="1600" alt="image" src="https://github.com/<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/a3d1dd8e-f18e-4999-9ec8-451862128336" />user-attachments/assets/5fae705a-b082-41d1-9f2f-3eac72cd21ad" />


### Panel de Control
<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/c32a0670-db0e-4844-a5b1-fa73b7488688" />


### Wearable
<img width="384" height="384" alt="image" src="https://github.com/user-attachments/assets/ef630b37-e105-4201-bf08-69f681cf64b9" />
<img width="384" height="384" alt="image" src="https://github.com/user-attachments/assets/0155d77d-80e7-4bfc-8268-ff632a3334ff" />
<img width="384" height="384" alt="image" src="https://github.com/user-attachments/assets/3dd12069-13b0-4c07-b87f-0323efa4cac4" />
<img width="384" height="384" alt="image" src="https://github.com/user-attachments/assets/4766d9a9-8609-4f40-85b8-4cb39515f2f8" />
<img width="384" height="384" alt="image" src="https://github.com/user-attachments/assets/0d936cab-0f65-4198-b2fd-5d8604a243ff" />




