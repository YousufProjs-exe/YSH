
import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class YSH {

    // FILE SYSTEM
    static class FileNode {
        String name;
        String content = "";

        FileNode(String name) {
            this.name = name;
        }
    }

    // FOLDER SYSTEM
    static class Folder {
        String name;
        Folder parent;
        ArrayList<Folder> children = new ArrayList<>();
        ArrayList<FileNode> files = new ArrayList<>();

        Folder(String name, Folder parent) {
            this.name = name;
            this.parent = parent;
        }
    }

    static Folder root = new Folder("/", null);
    static Folder current = root;
    static FileNode copiedFile = null;
    static Folder copiedFolder = null;

    // GUI
    static JTextArea terminal;
    static JTextField input;
    static ArrayList<String> history = new ArrayList<>();
    static int historyIndex = -1;

    static Color bgColor = Color.WHITE;
    static Color textColor = Color.BLACK;
    static Color inputColor = Color.BLACK;

    // CHAT SYSTEM
    static ServerSocket serverSocket;
    static Socket socket;
    static PrintWriter out;
    static BufferedReader in;

    static boolean isChatRunning = false;
    static boolean isHost = false;

    // USERNAME
    static String username = "Guest";

    // MULTI CLIENT SUPPORT
    static ArrayList<ClientHandler> clients = new ArrayList<>();

    // CLIENT HANDLER
    static class ClientHandler extends Thread {
    
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        String username = "Guest";
    
        ClientHandler(Socket socket) {
            this.socket = socket;
        
            try {
                in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );
            
                out = new PrintWriter(
                    socket.getOutputStream(),
                    true
                );
            
            } catch (Exception e) {
                terminal.append("client error\n");
            }
        }
    
        @Override
        public void run() {
        
            try {
            
                // first message = username
                username = in.readLine();
            
                terminal.append(username + " joined chat\n");
            
                broadcast("[SYSTEM] " + username + " joined");
            
                String msg;
            
                while ((msg = in.readLine()) != null) {
                
                    terminal.append(
                        "[" + username + "]: " + msg + "\n"
                    );
                
                    broadcast(
                        "[" + username + "]: " + msg
                    );
                }
            
            } catch (Exception e) {
            
                terminal.append(username + " disconnected\n");
            
            } finally {
            
                clients.remove(this);
            
                broadcast(
                    "[SYSTEM] " + username + " left"
                );
            
                try {
                    socket.close();
                } catch (Exception ignored) {}
            }
        }
    
        void send(String msg) {
            out.println(msg);
        }
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("YSH - Yousuf Shell [Version v5.GUI]");
        frame.setSize(900, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        terminal = new JTextArea();
        terminal.setFont(new Font("Consolas", Font.PLAIN, 14));
        terminal.setEditable(false);

        JScrollPane scroll = new JScrollPane(terminal);
        DefaultCaret caret = (DefaultCaret) terminal.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        input = new JTextField();

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(input, BorderLayout.SOUTH);

        applyTheme();
        printBanner();

        input.addActionListener(e -> {

            String cmd = input.getText().trim();

            if (!cmd.isEmpty()) {
                history.add(cmd);
                historyIndex = history.size();
            }
        
            input.setText("");
        
            terminal.append("\nYSH " + getPath(current) + "> " + cmd + "\n");
        
            execute(cmd);
        });

        input.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
            
                // UP ARROW
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                
                    if (historyIndex > 0) {
                        historyIndex--;
                        input.setText(history.get(historyIndex));
                    }
                }
            
                // DOWN ARROW
                else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                
                    if (historyIndex < history.size() - 1) {
                        historyIndex++;
                        input.setText(history.get(historyIndex));
                    }
                    else {
                        input.setText("");
                    }
                }
            }
        });

        frame.setVisible(true);
    }

    // EXECUTOR 
    static void execute(String inputText) {

        if (inputText == null || inputText.trim().isEmpty()) return;

        String command;
        String args;

        inputText = inputText.trim();

        if (inputText.contains(" ")) {
            command = inputText.substring(0, inputText.indexOf(" "));
            args = inputText.substring(inputText.indexOf(" ") + 1);
        } else {
            command = inputText;
            args = "";
        }

        //terminal.append("DEBUG: [" + command + "]\n");

        switch (command) {

            // FEATURES / COMMANDS 
case "help":
    terminal.append("""
================= YSH HELP =================

FILE COMMANDS--
mkdir "name"               -> create a folder
touch "name"               -> create a file
rm "name"                  -> delete a file or folder
rename "oldname" "newname" -> rename a file or folder
copy "name"                -> copy a file or folder
paste                      -> paste copied item
move item folder           -> move file/folder
ls                         -> list contents
search "name"              -> search files/folders

FILE CONTENT--
cat "file"                 -> read file contents
write "file" "text"        -> write text to file

NAVIGATION--
cd folder                  -> enter folder
cd ..                      -> go back
pwd                        -> show current path
home                       -> go to root

SYSTEM--
echo "text"                -> print text
clear                      -> clear terminal
theme "name"               -> change theme
open app                   -> launch app
help                       -> show commands
exit                       -> close YSH

NETWORK / CHAT--
chat host                  -> start chat server
chat join ip               -> join LAN chat
msg "text"                 -> send message

THEMES--
matrix | blue | purple | red | dark

================================================
""");
    break;
            // ECHO TO PRINT
            case "echo":
                terminal.append(args + "\n");
                break;

            // PRINT WORKING DIRECTORY
            case "pwd":
                terminal.append(getPath(current) + "\n");
                break;

            // LIST CONTENTS
            case "ls":
                for (Folder f : current.children)
                    terminal.append("[DIR] " + f.name + "\n");

                for (FileNode f : current.files)
                    terminal.append("[FILE] " + f.name + "\n");
                break;

            case "7743":
                terminal.append("""
            Found an Easter Egg there are more.
             Learn Java.
             github.com/YousufProjs-exe
            """);
                break;    

            // CREATE A FOLDER
            case "mkdir":
                mkdir(args);
                break;

            // CREATE A FILE
            case "touch":
                touch(args);
                break;

            // CHANGE DIRECTORY
            case "cd":
                cd(args);
                break;

            // READ A FILE
            case "cat":
                cat(args);
                break;

            // WRITE A FILE
            case "write":
                write(args);
                break;

            // HOME    
            case "home":
                current = root;
                terminal.append("root\n");
                break;

            // CLEAR    
            case "clear":
                terminal.setText("");
                printBanner();
                break;

            // EXIT
            case "exit":
                System.exit(0);
                break;

            // THEME
            case "theme":
                setTheme(args);
                break;

            // DELETE 
            case "rm":
                delete(args);
                break;

            // RENAME
            case "rename":
                rename(args);
                break;    

            // COPY
            case "copy":
                copy(args);
                break;

            // PASTE
            case "paste":
                paste();
                break;

            case "move":
                move(args);
                break;

            case "search":
                search(args);
                break;

            // APP LAUNCH 
            case "open":
                openApp(args);
                break;

            // CHAT 
            case "chat":
                chatCommand(args);
                break;

            case "msg":
                sendMsg(args);
                break;

            case "username":
                setUsername(args);
                break;

            case "listuser":
                listUsers();
                break;

            case "kick":
                kickUser(args);
                break;

            case "announce":
                announce(args);
                break;

            default:
                terminal.append("command not found\n");
        }
    }

    // FILE OPS 
    static void delete(String name) {
        if (name.isEmpty()) return;

        for (Iterator<FileNode> it = current.files.iterator(); it.hasNext();) {
            FileNode f = it.next();
            if (f.name.equals(name)) {
                it.remove();
                terminal.append("file deleted\n");
                return;
            }
        }

        for (Iterator<Folder> it = current.children.iterator(); it.hasNext();) {
            Folder f = it.next();
            if (f.name.equals(name)) {
                it.remove();
                terminal.append("folder deleted\n");
                return;
            }
        }

        terminal.append("not found\n");
    }

    static void mkdir(String name) {
        if (name.isEmpty()) return;

        current.children.add(new Folder(name, current));
        terminal.append("folder created\n");
    }

    static void touch(String name) {
        if (name.isEmpty()) return;

        current.files.add(new FileNode(name));
        terminal.append("file created\n");
    }

    static void cd(String name) {
        if (name.isEmpty()) return;

        if (name.equals("..")) {
            if (current.parent != null) current = current.parent;
            return;
        }

        for (Folder f : current.children) {
            if (f.name.equals(name)) {
                current = f;
                return;
            }
        }

        terminal.append("not found\n");
    }

    static void cat(String name) {
        for (FileNode f : current.files) {
            if (f.name.equals(name)) {
                terminal.append(f.content + "\n");
                return;
            }
        }
        terminal.append("not found\n");
    }

    // WRITE TO A FILE SYSTEM
    static void write(String args) {
        String[] p = args.split(" ", 2);
        if (p.length < 2) return;

        for (FileNode f : current.files) {
            if (f.name.equals(p[0])) {
                f.content = p[1];
                terminal.append("written\n");
                return;
            }
        }
    }

    // RENAME FILE OR FOLDER
    static void rename(String args) {

        String[] p = args.split(" ", 2);

        if (p.length < 2) {
            terminal.append("usage: rename oldname newname\n");
            return;
        }

        String oldName = p[0];
        String newName = p[1];

        // rename file
        for (FileNode f : current.files) {
            if (f.name.equals(oldName)) {
                f.name = newName;
                terminal.append("file renamed\n");
                return;
            }
        }

        // rename folder
        for (Folder f : current.children) {
            if (f.name.equals(oldName)) {
                f.name = newName;
                terminal.append("folder renamed\n");
                return;
            }
        }

        terminal.append("not found\n");
    }

    // COPY FILE OR FOLDER
    static void copy(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: copy name\n");
            return;
        }

        // copy file
        for (FileNode f : current.files) {
            if (f.name.equals(name)) {
                copiedFile = f;
                copiedFolder = null;
                terminal.append("file copied\n");
                return;
            }
        }

        // copy folder
        for (Folder f : current.children) {
            if (f.name.equals(name)) {
                copiedFolder = f;
                copiedFile = null;
                terminal.append("folder copied\n");
                return;
            }
        }

        terminal.append("not found\n");
    }

    // PASTE FILE OR FOLDER
    static void paste() {

        // paste file
        if (copiedFile != null) {

            FileNode newFile = new FileNode(copiedFile.name + "_copy");
            newFile.content = copiedFile.content;

            current.files.add(newFile);

            terminal.append("file pasted\n");
            return;
        }

        // paste folder
        if (copiedFolder != null) {

            Folder newFolder = new Folder(
                copiedFolder.name + "_copy",
                current
            );

            current.children.add(newFolder);

            terminal.append("folder pasted\n");
            return;
        }

        terminal.append("nothing copied\n");
    }

    // MOVE FILE OR FOLDER
    static void move(String args) {

        String[] p = args.split(" ", 2);

        if (p.length < 2) {
            terminal.append("usage: move item folder\n");
            return;
        }

        String itemName = p[0];
        String targetFolder = p[1];

        Folder destination = null;

        // find target folder
        for (Folder f : current.children) {
            if (f.name.equals(targetFolder)) {
                destination = f;
                break;
            }
        }

        if (destination == null) {
            terminal.append("destination folder not found\n");
            return;
        }

        // move file
        Iterator<FileNode> fileIterator = current.files.iterator();

        while (fileIterator.hasNext()) {
            FileNode file = fileIterator.next();

            if (file.name.equals(itemName)) {
                destination.files.add(file);
                fileIterator.remove();

                terminal.append("file moved\n");
                return;
            }
        }

        // move folder
        Iterator<Folder> folderIterator = current.children.iterator();

        while (folderIterator.hasNext()) {
            Folder folder = folderIterator.next();

            if (folder.name.equals(itemName)) {
                destination.children.add(folder);
                folder.parent = destination;
                folderIterator.remove();

                terminal.append("folder moved\n");
                return;
            }
        }

        terminal.append("not found\n");
    }

    // SEARCH FILES AND FOLDERS
    static void search(String keyword) {

        if (keyword.isEmpty()) {
            terminal.append("usage: search name\n");
            return;
        }

        boolean found = false;

        // search folders
        for (Folder f : current.children) {
            if (f.name.toLowerCase().contains(keyword.toLowerCase())) {
                terminal.append("[DIR] " + f.name + "\n");
                found = true;
            }
        }

        // search files
        for (FileNode f : current.files) {
            if (f.name.toLowerCase().contains(keyword.toLowerCase())) {
                terminal.append("[FILE] " + f.name + "\n");
                found = true;
            }
        }

        if (!found) {
            terminal.append("nothing found\n");
        }
    }

    // APP LAUNCHER 
    static void openApp(String app) {
        try {
            switch (app.toLowerCase()) {
                case "notepad":
                    Runtime.getRuntime().exec("cmd /c start notepad");
                    break;

                case "calc":
                    Runtime.getRuntime().exec("cmd /c start calc");
                    break;

                case "chrome":
                    Runtime.getRuntime().exec("cmd /c start chrome");
                    break;

                default:
                    Runtime.getRuntime().exec("cmd /c start " + app);
            }

            terminal.append("app launched\n");
        } catch (Exception e) {
            terminal.append("failed to launch\n");
        }
    }

    // CHAT SYSTEM
    static void chatCommand(String arg) {
        try {
            // HOST
            if (arg.equals("host")) {
                serverSocket = new ServerSocket(6000);
                isChatRunning = true;
                isHost = true;
                terminal.append(
                    "chat server started on port 6000\n"
                );

                new Thread(() -> {
                    while (true) {
                        try {
                            Socket clientSocket =
                                serverSocket.accept();
                            ClientHandler client =
                                new ClientHandler(clientSocket);
                            clients.add(client);
                            client.start();
                        } catch (Exception e) {
                            break;
                        }
                    }

                }).start();
            }

            // JOIN
            else if (arg.startsWith("join")) {
                String[] p = arg.split(" ");
                if (p.length < 2) {
                    terminal.append(
                        "usage: chat join ip\n"
                    );
                    return;
                }

                String ip = p[1];

                socket = new Socket(ip, 6000);

                in = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()
                    )
                );

                out = new PrintWriter(
                    socket.getOutputStream(),
                    true
                );

                isChatRunning = true;

                // send username
                out.println(username);

                // receive thread
                new Thread(() -> {

                    try {

                        String msg;

                        while ((msg = in.readLine())
                                != null) {

                            terminal.append(
                                msg + "\n"
                            );
                        }

                    } catch (Exception e) {

                        terminal.append(
                            "Disconnected\n"
                        );
                    }

                }).start();

                terminal.append(
                    "connected to " + ip + "\n"
                );
            }

        } catch (Exception e) {

            terminal.append(
                "Connection Failed\n"
            );
        }
    }

    static void sendMsg(String msg) {

        if (!isChatRunning) {

            terminal.append(
                "chat not running\n"
            );

            return;
        }

        // client
        if (!isHost && out != null) {

            out.println(msg);
        }

        // host
        else if (isHost) {

            terminal.append(
                "[YOU]: " + msg + "\n"
            );

            broadcast(
                "[" + username + "]: " + msg
            );
        }
    }

    static void broadcast(String msg) {

        for (ClientHandler client : clients) {
            client.send(msg);
        }
    }

    // static void setUsername(String name) {

    //     if (name.isEmpty()) {

    //         terminal.append(
    //             "usage: username name\n"
    //         );

    //         return;
    //     }

    //     username = name;

    //     terminal.append(
    //         "username set to "
    //         + username + "\n"
    //     );
    // }

    static void setUsername(String name) {

        if (name.isEmpty()) {
            terminal.append("usage: username name\n");
            return;
        }

        username = name;

        terminal.append("username set to " + username + "\n");

        if (isChatRunning && out != null) {
            out.println(username);
        }
    }

    static void listUsers() {

        if (!isHost) {

            terminal.append(
                "host only command\n"
            );

            return;
        }

        terminal.append(
            "Connected Users:\n"
        );

        for (ClientHandler c : clients) {

            terminal.append(
                c.username + "\n"
            );
        }
    }

    static void kickUser(String name) {

        if (!isHost) {

            terminal.append(
                "host only command\n"
            );

            return;
        }

        for (ClientHandler c : clients) {

            if (c.username.equals(name)) {

                c.send(
                    "[SYSTEM] You were kicked"
                );

                try {
                    c.socket.close();
                } catch (Exception ignored) {}

                clients.remove(c);

                terminal.append(
                    name + " kicked\n"
                );

                return;
            }
        }

        terminal.append(
            "user not found\n"
        );
    }

    static void announce(String msg) {

        if (!isHost) {

            terminal.append(
                "host only command\n"
            );

            return;
        }

        broadcast(
            "[HOST]: " + msg
        );

        terminal.append(
            "announcement sent\n"
        );
    }

    // UTIL METHODS 
    static String getPath(Folder f) {
        if (f.parent == null) return "/";

        String path = "";
        while (f.parent != null) {
            path = "/" + f.name + path;
            f = f.parent;
        }
        return path;
    }

    static void printBanner() {
        terminal.append("YSH - Yousuf Shell [Version v5.GUI]\n");
        terminal.append("(c) K.Yousuf 25061-CS-010\n\n");
        terminal.append("Type 'help' to see available commands\n");
    }

    static void applyTheme() {
        terminal.setBackground(bgColor);
        terminal.setForeground(textColor);

        input.setBackground(bgColor);
        input.setForeground(inputColor);
    }

    // THEMES SYSTEM
    static void setTheme(String t) {
        switch (t.toLowerCase()) {
            case "matrix":
                bgColor = Color.BLACK;
                textColor = Color.GREEN;
                inputColor = Color.GREEN;
                break;

            case "blue":
                bgColor = Color.DARK_GRAY;
                textColor = Color.CYAN;
                inputColor = Color.CYAN;
                break;


            case "purple":
                bgColor = new Color(30, 0, 50);
                textColor = new Color(200, 120, 255);
                inputColor = new Color(200, 120, 255);
                break;

            case "red":
                bgColor = new Color(50, 0, 0);
                textColor = Color.RED;
                inputColor = Color.RED;
                break;

            case "dark":
                bgColor = Color.BLACK;
                textColor = Color.WHITE;
                inputColor = Color.WHITE;
                break;
            default:
                terminal.append("themes: matrix blue purple red dark\n");
                return;
        }
        applyTheme();
    }
}
