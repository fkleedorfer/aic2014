package com.github.aic2014.onion.shell;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The Shell was strongly inspired by the Shell framework used in the Vienna UTs lecture 'Verteilte Systeme UE'
 */

public class Shell implements Runnable, Closeable {
    private static final PrintStream stdout = System.out;
    private static final InputStream stdin = System.in;
    private static final char[] EMPTY = new char[0];

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss.SSS");
        }
    };

    private String name;

    private ShellCommandInvocationHandler invocationHandler = new ShellCommandInvocationHandler();
    private Map<String, ShellCommandDefinition> commandMap = new ConcurrentHashMap<>();
    private ConversionService conversionService = new DefaultConversionService();

    private OutputStream out;
    private BufferedReader in;
    private Closeable readMonitor;

    public Shell(String name, InputStream in, OutputStream out) {
        this.name = name;
        this.out = out;
        this.readMonitor = in;
        this.in = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public void run() {
        try {
            for (String line; !Thread.currentThread().isInterrupted()
                    && (line = readLine()) != null;) {
                write(String.format("%s\t\t%s> %s%n",
                        DATE_FORMAT.get().format(new Date()), name, line)
                        .getBytes());
                Object result;
                try {
                    result = invoke(line);
                } catch (CommandNotRegisteredException e) {
                    result = e.getMessage();
                } catch (Throwable throwable) {
                    ByteArrayOutputStream str = new ByteArrayOutputStream(1024);
                    throwable.printStackTrace(new PrintStream(str, true));
                    result = str.toString();
                }
                if (result != null) {
                    print(result);
                }
            }
        } catch (IOException e) {
            try {
                writeLine("Shell closed");
            } catch (IOException ex) {
                System.out.println(ex.getClass().getName() + ": "
                        + ex.getMessage());
            }
        }
    }

    private void print(Object result) throws IOException {
        if (result instanceof Iterable) {
            for (Object e : ((Iterable) result)) {
                print(e);
            }
        } else if (result instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) result).entrySet()) {
                writeLine(entry.getKey() + "\t" + entry.getValue());
            }
        } else {
            writeLine(String.valueOf(result));
        }
    }

    public void writeLine(String line) throws IOException {
        String now = DATE_FORMAT.get().format(new Date());
        if (line.indexOf('\n') >= 0 && line.indexOf('\n') < line.length() - 1) {
            write((String.format("%s\t\t%s:\n", now, name)).getBytes());
            for (String l : line.split("[\\r\\n]+")) {
                write((String.format("%s\t\t%s\n", now, l)).getBytes());
            }
        } else {
            write((String.format("%s\t\t%s: %s%s", now, name, line,
                    line.endsWith("\n") ? "" : "\n")).getBytes());
        }
    }

    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public String readLine() throws IOException {
        synchronized (readMonitor) {
            return in.readLine();
        }
    }

    public char[] read(int len) throws IOException {
        synchronized (readMonitor) {
            len = len < 0 ? 4096 : len;
            char[] cbuf = new char[len];
            int read = in.read(cbuf, 0, len);
            return read <= 0 ? EMPTY : Arrays.copyOfRange(cbuf, 0, read);
        }
    }

    public char[] read() throws IOException {
        return read(-1);
    }

    @Override
    public void close() {
        Thread.currentThread().interrupt();
        if (readMonitor != stdin) {
            try {
                readMonitor.close();
            } catch (IOException e) {
                System.err.printf("Cannot close console input. %s: %s%n",
                        getClass(), e.getMessage());
            }
        }
        if (out != stdout) {
            try {
                out.close();
            } catch (IOException e) {
                System.err.printf("Cannot close console output. %s: %s%n",
                        getClass(), e.getMessage());
            }
        }
    }

    public void register(Object obj) {
        for (Method method : obj.getClass().getMethods()) {
            Command command = method.getAnnotation(Command.class);
            if (command != null) {
                String name = command.value().isEmpty() ? method.getName()
                        : command.value();
                name = name.startsWith("!") ? name : "!" + name;
                if (commandMap.containsKey(name)) {
                    throw new IllegalArgumentException(String.format(
                            "Command '%s' is already registered.", name));
                }
                method.setAccessible(true);
                commandMap.put(name, new ShellCommandDefinition(obj, method));
            }
        }
    }

    public Object invoke(String cmd) throws Throwable {
        if (cmd == null || (cmd = cmd.trim()).isEmpty()) {
            return null;
        }

        int pos = cmd.indexOf(' ');
        String cmdName = pos >= 0 ? cmd.substring(0, pos) : cmd;
        ShellCommandDefinition cmdDef = commandMap.get(cmdName);
        if (cmdDef == null) {
            throw new CommandNotRegisteredException(cmdName);
        }

        String[] parts = cmd.split("\\s+",
                cmdDef.targetMethod.getParameterTypes().length + 1);
        Object[] args = new Object[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            args[i - 1] = conversionService.convert(parts[i],
                    cmdDef.targetMethod.getParameterTypes()[i - 1]);
        }
        return invocationHandler.invoke(cmdDef.targetObject,
                cmdDef.targetMethod, args);
    }

    public BufferedReader getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    static class ShellCommandDefinition {
        protected Object targetObject;
        protected Method targetMethod;

        ShellCommandDefinition(Object targetObject, Method targetMethod) {
            this.targetObject = targetObject;
            this.targetMethod = targetMethod;
        }
    }

    static class ShellCommandInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object target, Method method, Object... args)
                throws Throwable {
            return method.invoke(target, args);
        }
    }
}
