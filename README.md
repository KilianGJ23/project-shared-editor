# Project Shared Editor

Editor de código colaborativo en tiempo real basado en una arquitectura Cliente-Servidor en Java, con soporte para sincronización de archivos mediante Sockets, control de concurrencia y bloqueo de líneas de código, estructuras de datos avanzadas (Árboles AVL, Listas enlazadas, Pilas) e interfaz gráfica con Java Swing.

## 🚀 Características

- **Edición colaborativa en tiempo real**: Varios clientes pueden conectarse al servidor y visualizar el código simultáneamente.
- **Bloqueo por líneas**: Cada usuario puede bloquear y editar líneas específicas evitando colisiones de edición.
- **Estructuras de datos personalizadas**:
  - `MyBinaryTreeAVL`: Gestión balanceada y eficiente de líneas de código y ordenamiento.
  - `MySimpleList`, `MyDoubleList`: Manejo de colecciones dinámicas.
  - `MyStack`: Soporte de operaciones tipo pila.
- **Monitoreo de inactividad**: Detección automática de desconexiones o inactividad para liberar bloqueos.
- **Gestor de archivos y persistencia**: Explorador de archivos del proyecto y guardado local y en servidor.
- **Interfaz Gráfica (Swing)**: Identificación de usuarios por colores, resaltado de líneas bloqueadas y panel de usuarios activos.

---

## 📁 Estructura del Proyecto

```text
PROYECTO-PROGRAMACION/
├── class-diagrams/            # Diagramas de clases de la solución
├── shared-editor-server/      # Componente del Servidor
│   ├── src/co/edu/uptc/       # Código fuente del servidor (Model, Presenter, Network, Structures, Persistence)
│   ├── shared-editor-server.jar
│   └── README.md
├── shared-editor-client/      # Componente del Cliente
│   ├── src/co/edu/uptc/       # Código fuente del cliente (View, Presenter, Network, Persistence)
│   ├── shared-editor-client.jar
│   └── README.md
├── propuesta-proyecto.pdf     # Documento de propuesta del proyecto
└── lineamientos_proyecto_final_20262.pdf
```

---

## 🛠️ Requisitos

- Java JDK 17 o superior.
- Visual Studio Code o cualquier IDE compatible con Java.

---

## ▶️ Ejecución

### 1. Iniciar el Servidor

Desde la carpeta `shared-editor-server`:
```bash
java -jar shared-editor-server.jar
```
O ejecutando la clase principal `co.edu.uptc.runner.ServerRunner`.

### 2. Iniciar el Cliente

Desde la carpeta `shared-editor-client`:
```bash
java -jar shared-editor-client.jar
```
O ejecutando la clase principal `co.edu.uptc.runner.ClientRunner`.
