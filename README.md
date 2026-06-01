# YSH (Yousuf Shell)

YSH (Yousuf Shell) is a custom-built Java-based shell system that combines a command-line interface with a graphical user interface. It is designed as a hybrid environment for file management, system-style commands, and network-based chat communication within a single lightweight application.

Built for learning, experimentation, and extensibility, YSH simulates operating system shell behavior while adding custom enhancements, themes, and interactive features.

[Download](https://github.com/YousufProjs-exe/YSH/release)
---

## Features

### Dual Interface System
- Fully functional CLI (Command Line Interface)
- Graphical User Interface (GUI) built using Java Swing
- Consistent functionality across both CLI and GUI modes

---

### Custom File System Engine
- Create files and folders dynamically
- Read and write file contents
- Navigate through virtual directory paths
- Simulated file hierarchy structure similar to a shell environment

---

### Core Shell Commands
- File creation, reading, and deletion
- Directory navigation system (cd-style commands)
- Path-based file handling
- Custom command parser engine
- Lightweight and fast execution

---

### GUI System
- Multiple themes (light, dark, and custom variants)
- Minimal and structured interface design
- Command input panel with real-time output display
- Interactive console-based GUI layout

---

### Network Chat System
- Multi-user chat functionality
- Host-client architecture
- Username-based communication system
- Kick and user management features controlled by host
- Global message broadcasting support

---

### System Design Features
- Modular architecture for easy expansion
- Event-driven GUI logic
- Separation of CLI and GUI layers
- Designed for future plugin support

---

### Control Features
- Basic session handling
- Host-controlled chat environment
- Simple access control mechanisms

---

### Extras
- Hidden Easter egg command system
- Interactive internal responses
- Experimental command hooks

Note: Easter egg features exist but are intentionally undocumented.

---

## Tech Stack
- Java (Core)
- Java Swing (GUI)
- Java I/O Streams
- Socket Programming (Networking)
- JAR packaging compatible with Launch4j and jpackage

---

## Build and Run

### Run JAR File
```bash
java -jar YSH.jar
