# Shared Editor — Editor de Código Colaborativo en Java

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Architecture](https://img.shields.io/badge/Arquitectura-Cliente--Servidor%20%7C%20MVP-1f6feb?style=flat-square)
![Sockets](https://img.shields.io/badge/Red-Sockets%20TCP%20%2B%20Hilos-2ea043?style=flat-square)
![GUI](https://img.shields.io/badge/Interfaz-Java%20Swing-6e7681?style=flat-square)

Aplicación cliente–servidor de edición colaborativa de archivos `.java` en tiempo real, construida en **Java 21** sin dependencias externas. Utiliza **Sockets TCP**, **concurrencia multihilo**, **estructuras de datos dinámicas propias** y una interfaz gráfica de escritorio en **Java Swing** bajo el patrón **MVP (Model–View–Presenter)** en tres capas.

---

## Características principales

### Conexión, usuarios y roles
* **Registro con identificador único:** el servidor valida que el nombre de usuario no esté vacío ni duplicado antes de admitirlo en la sesión (hasta 10 usuarios simultáneos).
* **Identificación visual automática:** el servidor asigna y recicla un identificador numérico de ranura (`colorId`) que la vista del cliente traduce a colores distintivos para identificar a cada colaborador.
* **Lista de conectados en tiempo real:** panel lateral actualizado automáticamente ante ingresos, desconexiones o cambios de rol.
* **Gestión automática del rol Administrador:** el primer usuario en conectarse recibe el rol `ADMIN`; si se desconecta, el rol se transfiere automáticamente al siguiente usuario más antiguo en la sesión.

### Gestión y persistencia de archivos `.java`
* **Creación e importación sincronizada (solo `ADMIN`):** el administrador puede crear archivos nuevos (con plantilla básica de clase Java) o importar archivos `.java` desde su equipo para compartirlos al instante con todos los clientes.
* **Exportación local (todos los usuarios):** cualquier colaborador puede guardar en su equipo una copia del archivo abierto en cualquier momento.
* **Persistencia en el servidor:** cada archivo creado o editado se respalda automáticamente en el directorio `server_workspace/` del servidor y se recarga al reiniciar el servicio.

### Edición colaborativa por líneas
* **Bloqueo exclusivo por línea:** al seleccionar una línea libre, queda reservada para el usuario y se resalta en todos los clientes con su color y etiqueta de nombre, evitando sobrescrituras simultáneas.
* **Sincronización en tiempo real:** los cambios de texto, inserciones (`Enter` o botón **+ Insertar línea**) y eliminaciones de líneas se difunden inmediatamente a los demás clientes.
* **Numeración automática de líneas:** cada fila mantiene su numeración sincronizada al insertar o eliminar líneas.
* **Liberación automática de bloqueos:** una línea se desbloquea automáticamente al presionar `Esc` o **Liberar linea**, al seleccionar otra línea, al cambiar de archivo, al desconectarse del servidor o tras **30 segundos de inactividad** (verificados por un hilo demonio cada 5 segundos).

---

## Arquitectura del sistema

El proyecto separa de forma estricta responsabilidades entre servidor y cliente siguiendo el modelo de tres capas (**MVP adaptado a cliente–servidor**):

* **Módulo Servidor (`shared-editor-server`):** concentra exclusivamente el **Modelo** (`model` y `structures`), la **Persistencia** (`persistence`), la capa de **Red TCP multihilo** (`network`) y el **Presentador del servidor** (`ServerPresenter`). No contiene clases de interfaz gráfica ni valores visuales de color.
* **Módulo Cliente (`shared-editor-client`):** concentra exclusivamente la **Vista Swing** (`view`), la **Persistencia local y serialización** (`persistence`), el **Socket cliente** (`network`) y el **Presentador del cliente** (`ClientPresenter` junto con las interfaces `IClientPresenter`, `IConnectionView` e `IEditorView`). No conoce ni importa clases del modelo ni de estructuras.

```text
+------------------------------------------+         TCP Socket (Puerto 1372)         +---------------------------------------------+
|            CLIENTE (Swing)               |    Mensajes serializados en Base64       |             SERVIDOR (Headless)             |
|                                          | <======================================> |                                             |
|  View (Swing + UserColorPalette)         |                                          |  Network (CollaborativeServer +             |
|    ↕ IConnectionView / IEditorView       |                                          |           ClientHandler + InactivityMonitor)|
|  Presenter (ClientPresenter)             |                                          |    ↕ ServerMessageListener                  |
|    ↕                                     |                                          |  Presenter (ServerPresenter)                |
|  Persistence + Network (ClientConnection)|                                          |    ↕                                        |
|                                          |                                          |  Model (Workspace, CodeFile, CodeLine, User)|
|                                          |                                          |  Structures (AVL, Simple/DoubleList, Stack) |
|                                          |                                          |  Persistence (WorkspaceFilePersistence)     |
+------------------------------------------+                                          +---------------------------------------------+

```

### Protocolo de comunicación TCP

Los mensajes viajan en UTF-8 como líneas delimitadas y codificadas en Base64 mediante `MessageSerializer` para preservar saltos de línea, indentación y caracteres especiales del código fuente:

```text
TIPO_MENSAJE|Base64(sender)|Base64(fileName)|lineNumber|Base64(payload)

```

Soporta los siguientes tipos de mensaje (`MessageType`):
`REGISTER_REQUEST`, `REGISTER_OK`, `REGISTER_ERROR`, `USER_LIST_UPDATE`, `ROLE_UPDATED`, `CREATE_FILE`, `FILE_LIST_UPDATE`, `SELECT_FILE`, `FILE_CONTENT`, `LOCK_LINE`, `LINE_LOCKED`, `LOCK_REJECTED`, `EDIT_LINE`, `INSERT_LINE`, `DELETE_LINE`, `UNLOCK_LINE`, `LINE_UNLOCKED`, `ERROR_MESSAGE` y `DISCONNECT`.

---

## Estructuras de datos dinámicas propias

El servidor implementa su lógica sobre las estructuras dinámicas del paquete `co.edu.uptc.structures`:

| Estructura | Ubicación de uso | Propósito |
| --- | --- | --- |
| `MyBinaryTreeAVL<User>` | `Workspace` | Mantener los usuarios activos ordenados por orden de llegada (`joinOrder`) para la lista de conectados y la reasignación del rol `ADMIN`. |
| `MySimpleList<String>` | `Workspace` | Verificar disponibilidad y unicidad de nombres de usuario activos. |
| `MyBinaryTreeAVL<CodeFile>` | `Workspace` | Indexar y consultar archivos `.java` en orden alfabético. |
| `MyDoubleList<String>` | `Workspace` | Registrar los nombres de archivos creados y evitar duplicados. |
| `MyStack<Integer>` | `Workspace` | Gestionar la pila de identificadores de color (`0` a `9`) disponibles y reciclarlos al desconectarse un usuario. |
| `MyBinaryTreeAVL<CodeLine>` | `CodeFile` | Almacenar, buscar y recorrer en orden las líneas de código de cada archivo por número de línea. |
| `MyBinaryTreeAVL<ClientHandler>` | `CollaborativeServer` | Administrar los hilos activos de clientes conectados para difusión (`broadcast`) y envío dirigido (`sendToClient`). |

---

## Estructura del repositorio

```text
project-shared-editor/
├── class-diagrams/                          # Diagramas de clases UML por paquete
├── shared-editor-server/
│   ├── shared-editor-server.jar             # Ejecutable del servidor (Java 21)
│   └── src/co/edu/uptc/
│       ├── structures/                      # Node, DoubleNode, NodeAVL, MySimpleList, MyDoubleList, MyStack, MyBinaryTreeAVL
│       ├── model/                           # Workspace, CodeFile, CodeLine, LockReference, User, UserRole
│       ├── persistence/                     # MessageSerializer, WorkspaceFilePersistence
│       ├── network/                         # CollaborativeServer, ClientHandler, InactivityMonitor, NetworkMessage, MessageType, ServerMessageListener
│       ├── presenter/                       # ServerPresenter
│       └── runner/                          # ServerRunner
└── shared-editor-client/
    ├── shared-editor-client.jar             # Ejecutable del cliente (Java 21)
    └── src/co/edu/uptc/
        ├── network/                         # ClientConnection, ClientMessageListener, NetworkMessage, MessageType
        ├── persistence/                     # MessageSerializer, LocalJavaFilePersistence
        ├── presenter/                       # ClientPresenter, IClientPresenter, IConnectionView, IEditorView
        ├── view/                            # ConnectionFrame, MainEditorFrame, FileExplorerPanel, CodeEditorPanel, CodeLineRow, LineInteractionHandler, ActiveUsersPanel, UserColorPalette
        └── runner/                          # ClientRunner

```

---

## Requisitos

* **Java Development Kit (JDK) 21** instalado y disponible en el `PATH`.

Verifica tu instalación con:

```bash
java -version
javac -version

```

---

## Ejecución rápida (con los `.jar`)

El servidor y el cliente vienen configurados por defecto para trabajar en local en el puerto **`1372`**.

### 1. Iniciar el servidor

```bash
cd shared-editor-server
java -jar shared-editor-server.jar

```

La consola del servidor notificará cada evento relevante de la sesión:

```text
[SERVIDOR] Encendido y escuchando en el puerto 1372
[CONEXION] Cliente conectado: usuario1 (IP: 127.0.0.1)
[DESCONEXION] Cliente desconectado: usuario1 (IP: 127.0.0.1)
[SERVIDOR] Servidor finalizado correctamente.

```

### 2. Iniciar uno o más clientes

En otra terminal (puedes abrir varias instancias para probar la edición simultánea):

```bash
cd shared-editor-client
java -jar shared-editor-client.jar

```

En la ventana de conexión aparecerán precargados `localhost` y el puerto `1372`. Ingresa un nombre de usuario único y presiona **Conectar**.

> **Uso en red local o servidor remoto:** si ejecutas `shared-editor-server.jar` en otro equipo o servidor Linux con el puerto `1372` habilitado, solo cambia `localhost` por la dirección IP o dominio del servidor en la ventana de conexión del cliente.

---

## Compilación desde el código fuente

Los comandos funcionan en **Bash**, **Zsh** y **Fish**:

### Compilar el servidor

```bash
cd shared-editor-server
rm -rf bin shared-editor-server.jar
mkdir -p bin
find src -name "*.java" > sources.txt
javac -encoding UTF-8 --release 21 -d bin @sources.txt
jar cfe shared-editor-server.jar co.edu.uptc.runner.ServerRunner -C bin .
rm sources.txt

```

### Compilar el cliente

```bash
cd shared-editor-client
rm -rf bin shared-editor-client.jar
mkdir -p bin
find src -name "*.java" > sources.txt
javac -encoding UTF-8 --release 21 -d bin @sources.txt
jar cfe shared-editor-client.jar co.edu.uptc.runner.ClientRunner -C bin .
rm sources.txt

```

---

## Diagramas de clases

En la carpeta `class-diagrams/` se encuentran los 6 diagramas UML actualizados y sincronizados con el código fuente, separados por paquete:

* `model` — Entidades de dominio y gestión del espacio de trabajo compartido.
* `structures` — Estructuras de datos dinámicas genéricas y sus nodos.
* `persistence` — Serialización del protocolo TCP y persistencia de archivos `.java` en servidor y cliente.
* `network` — Arquitectura de sockets TCP, hilos por cliente y monitor de inactividad.
* `presenter` — Coordinación servidor–cliente e interfaces del patrón MVP.
* `view` — Ventanas, paneles Swing, control de filas y paleta de colores del cliente.

---

## Autor

**Kilian David Gómez Joropa** ([@KilianGJ23](https://github.com/KilianGJ23))

Ingeniería de Sistemas y Computación — Universidad Pedagógica y Tecnológica de Colombia (UPTC)
