package org.sibyl.addons.reportbook;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Java 21 isolated Unix-domain-socket worker; no DB or network access. */
public final class WorkerMain {
    private static final int MAX_REQUEST=65536;
    private static final Pattern METHOD=Pattern.compile("\"method\"\\s*:\\s*\"([a-zA-Z0-9_-]+)\"");
    private static final Pattern MINUTES=Pattern.compile("\"minutes\"\\s*:\\s*\\[([^]]*)\\]");
    private WorkerMain() {}

    public static String respond(String request) {
        Matcher method=METHOD.matcher(request);
        if(!method.find())return "{\"ok\":false,\"error\":\"missing method\"}";
        if(method.group(1).equals("ping"))
            return "{\"ok\":true,\"service\":\"Sibyl.report-book\",\"isolated\":true}";
        if(!method.group(1).equals("totalMinutes"))
            return "{\"ok\":false,\"error\":\"unknown method\"}";
        Matcher values=MINUTES.matcher(request);
        if(!values.find())return "{\"ok\":false,\"error\":\"minutes array required\"}";
        String str=values.group(1).trim();
        if(!str.matches("[0-9,\\s]*"))
            return "{\"ok\":false,\"error\":\"invalid minutes\"}";
        long sum=0;
        if(!str.isEmpty()) {
            String[] chunks=str.split(",",-1);
            if(chunks.length>1000)return "{\"ok\":false,\"error\":\"too many entries\"}";
            for(String chunk:chunks) {
                try {
                    int n=Integer.parseInt(chunk.trim());
                    if(n<15||n>720||n%15!=0)
                        return "{\"ok\":false,\"error\":\"invalid time interval\"}";
                    sum+=n;
                } catch(NumberFormatException ex) {
                    return "{\"ok\":false,\"error\":\"invalid minutes\"}";
                }
            }
        }
        return "{\"ok\":true,\"totalMinutes\":"+sum+",\"entries\":"+
            (str.isEmpty()?0:str.split(",",-1).length)+"}";
    }

    public static void main(String[] args)throws Exception {
        if(args.length!=1||!args[0].equals("/run/sibyl-worker/worker.sock"))
            throw new IllegalArgumentException("Private socket required");
        Path socket=Path.of(args[0]);
        Files.deleteIfExists(socket);
        try(ServerSocketChannel server=ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            server.bind(UnixDomainSocketAddress.of(socket));
            while(true) {
                try(SocketChannel channel=server.accept()) {
                    DataInputStream in=new DataInputStream(Channels.newInputStream(channel));
                    DataOutputStream out=new DataOutputStream(Channels.newOutputStream(channel));
                    int size=in.readInt();
                    if(size<1||size>MAX_REQUEST)continue;
                    byte[] data=in.readNBytes(size);
                    if(data.length!=size)continue;
                    byte[] result=respond(new String(data,StandardCharsets.UTF_8))
                        .getBytes(StandardCharsets.UTF_8);
                    out.writeInt(result.length);out.write(result);out.flush();
                }catch(Exception ignored) {
                    // A malformed client request must not shut down the service.
                }
            }
        }finally{Files.deleteIfExists(socket);}
    }
}
